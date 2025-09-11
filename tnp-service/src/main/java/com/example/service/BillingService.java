package com.example.service;

import com.example.data.transactional.entities.BillingHistory;
import com.example.data.transactional.entities.User;
import com.example.data.transactional.repo.BillingHistoryRepository;
import com.example.service.dhan.DhanTradeService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {

    private static final double MINIMUM_FIXED_CHARGES = 499.0;
    private static final double NET_WORTH_CHARGE_PERCENTAGE = 0.001; // 0.1%
    private static final double CHARGE_MULTIPLE = 500.0;
    private static final BigDecimal GST_RATE = new BigDecimal("0.18"); // 18% GST

    private final PortfolioService portfolioService;
    private final DhanTradeService dhanTradeService;
    private final BillingHistoryRepository billingHistoryRepository;

    /**
     * Calculate and record charges for a user's DhanTrade records from the previous month.
     *
     * @param user User to calculate charges for
     * @return Total charges in INR for the previous month's trades
     * @throws IllegalStateException if a bill already exists for the month
     */
    @Transactional
    public BigDecimal calculateAndRecordCharges(User user) {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        return calculateAndRecordChargesForMonth(user, lastMonth);
    }

    /**
     * Calculate and record charges for a user's DhanTrade records for a specific month.
     *
     * @param user User to calculate charges for
     * @param yearMonth The year and month to calculate charges for
     * @return Total charges in INR for the specified month's trades, or null if bill already exists
     */
    @Transactional
    public BigDecimal calculateAndRecordChargesForMonth(User user, YearMonth yearMonth) {
        // Check for existing bill using billMonth
        Optional<BillingHistory> existingBill =
                billingHistoryRepository.findFirstByUserIdAndBillMonth(
                        user.getId(), yearMonth.toString());

        if (existingBill.isPresent()) {
            log.info(
                    "Bill already exists for user {} for month {}, Bill No: {}, skipping",
                    user.getUsername(),
                    yearMonth,
                    existingBill.get().getBillNo());
            return existingBill.get().getTotal();
        }

        // Calculate charges for the month
        LocalDate fromDate = yearMonth.atDay(1);
        LocalDate toDate = yearMonth.atEndOfMonth();

        log.info(
                "Calculating DhanTrade charges for user {} for month {}",
                user.getUsername(),
                yearMonth);

        double totalCharges = dhanTradeService.calculateChargesForPeriod(user, fromDate, toDate);
        double fixedCharges = this.calculateFixedCharges(user);
        BigDecimal amount =
                BigDecimal.valueOf(Math.max(fixedCharges, totalCharges))
                        .setScale(2, RoundingMode.HALF_UP);

        // Calculate GST and total
        BigDecimal gst = amount.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = amount.add(gst).setScale(2, RoundingMode.HALF_UP);

        // Record billing history
        BillingHistory billingHistory =
                BillingHistory.builder()
                        .billNo(generateBillNumber())
                        .amount(amount)
                        .gst(gst)
                        .total(total)
                        .billDate(LocalDate.now())
                        .billMonth(yearMonth.toString())
                        .user(user)
                        .build();

        billingHistoryRepository.save(billingHistory);

        log.info(
                "Recorded bill for user {} for month {}: amount={}, gst={}, total={}, billNo={}",
                user.getUsername(),
                yearMonth,
                amount,
                gst,
                total,
                billingHistory.getBillNo());
        return total;
    }

    /**
     * Calculates fixed charges based on user's portfolio net worth. Charges are 0.1% of net worth,
     * floored to nearest 500 (499, 999, 1499, etc.) Minimum charges are 499.0
     *
     * @param user User to calculate charges for
     * @return Fixed charges amount
     */
    private double calculateFixedCharges(User user) {
        double netWorth = portfolioService.calculateNetWorth(user);

        // Calculate 0.1% of net worth
        double baseCharge = netWorth * NET_WORTH_CHARGE_PERCENTAGE;

        // If base charge is less than minimum, return minimum
        if (baseCharge <= MINIMUM_FIXED_CHARGES) {
            return MINIMUM_FIXED_CHARGES;
        }

        // Calculate number of 500 multiples and subtract 1 to get to 499, 999, etc.
        long multiplier = (long) Math.floor(baseCharge / CHARGE_MULTIPLE);
        return (multiplier * CHARGE_MULTIPLE) - 1;
    }

    /**
     * Mark a bill as paid
     *
     * @param billNo Bill number to mark as paid
     * @param paymentRef Payment reference number
     * @param paymentMode Mode of payment (e.g., "CASH", "ONLINE")
     * @throws IllegalStateException if bill is not found or already paid
     */
    @Transactional
    public void markBillAsPaid(String billNo, String paymentRef, String paymentMode) {
        BillingHistory bill = billingHistoryRepository.findByBillNo(billNo);
        if (bill == null) {
            throw new IllegalStateException("Bill not found: " + billNo);
        }
        if (bill.isPaid()) {
            throw new IllegalStateException("Bill already paid: " + billNo);
        }

        bill.markAsPaid(paymentRef, paymentMode);
        billingHistoryRepository.save(bill);

        log.info(
                "Marked bill {} as paid with reference {} and mode {}",
                billNo,
                paymentRef,
                paymentMode);
    }

    /**
     * Get all unpaid bills for a user
     *
     * @param user User to get unpaid bills for
     * @return List of unpaid bills ordered by date descending
     */
    @Transactional(readOnly = true)
    public List<BillingHistory> getUnpaidBills(User user) {
        return billingHistoryRepository.findByUserIdAndIsPaidOrderByBillDateDesc(
                user.getId(), false);
    }

    /**
     * Get all paid bills between two dates
     *
     * @param fromDate Start date inclusive
     * @param toDate End date inclusive
     * @return List of paid bills ordered by payment date descending
     */
    @Transactional(readOnly = true)
    public List<BillingHistory> getPaidBillsBetweenDates(
            LocalDateTime fromDate, LocalDateTime toDate) {
        return billingHistoryRepository.findByIsPaidTrueAndPaymentDateBetweenOrderByPaymentDateDesc(
                fromDate, toDate);
    }

    /**
     * Get bill by bill number
     *
     * @param billNo Bill number to find
     * @return Bill if found, null otherwise
     */
    @Transactional(readOnly = true)
    public BillingHistory getBill(String billNo) {
        return billingHistoryRepository.findByBillNo(billNo);
    }

    /**
     * Get all bills for a user in a specific month
     *
     * @param user User to get bills for
     * @param yearMonth Year and month to get bills for
     * @return List of bills ordered by date descending
     */
    @Transactional(readOnly = true)
    public BillingHistory getBillsForMonth(User user, YearMonth yearMonth) {
        return billingHistoryRepository
                .findByUserIdAndBillMonthOrderByBillDateDesc(user.getId(), yearMonth.toString())
                .orElseThrow();
    }

    /**
     * Records payment for a bill
     *
     * @param user The user making the payment
     * @param billNo The bill number being paid
     * @param paymentMode Mode of payment (e.g. UPI, NEFT, etc.)
     * @param paymentReference Payment reference number
     * @param paymentDate Date of payment
     * @return Updated BillingHistory
     * @throws IllegalStateException if bill is not found or already paid
     */
    @Transactional
    public BillingHistory makePayment(
            User user,
            String billNo,
            String paymentMode,
            String paymentReference,
            LocalDate paymentDate) {
        BillingHistory bill =
                billingHistoryRepository
                        .findByBillNoAndUserId(billNo, user.getId())
                        .orElseThrow(() -> new IllegalStateException("Bill not found: " + billNo));

        if (bill.isPaid()) {
            throw new IllegalStateException("Bill " + billNo + " is already paid");
        }

        bill.setPaid(true);
        bill.setPaymentMode(paymentMode);
        bill.setPaymentReference(paymentReference);
        bill.setPaymentDate(paymentDate);
        bill.setUpdatedAt(LocalDateTime.now());

        BillingHistory updatedBill = billingHistoryRepository.save(bill);

        log.info(
                "Recorded payment for bill {}: mode={}, reference={}, date={}",
                billNo,
                paymentMode,
                paymentReference,
                paymentDate);

        return updatedBill;
    }

    /**
     * Generate a unique bill number in the format ES-YYYYMMDD-XXXX where XXXX is a sequential
     * number for the day
     */
    private String generateBillNumber() {
        LocalDate today = LocalDate.now();
        String dateStr = today.toString().replace("-", "");

        // Get count of bills for today to generate sequence
        long sequence = billingHistoryRepository.countByBillDateBetween(today, today) + 1;

        return String.format("ES-%s-%04d", dateStr, sequence);
    }
}
