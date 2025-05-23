package com.example.service;

import com.example.data.transactional.entities.FundsLedger;
import com.example.data.transactional.entities.type.FundTransactionType;
import com.example.data.transactional.repo.FundsLedgerRepository;
import com.example.data.transactional.view.FundsLedgerResult;
import com.example.dto.request.FundsLedgerRequest;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class FundsLedgerService {

    private final FundsLedgerRepository fundsLedgerRepository;

    public BigDecimal getTotalFundsValue(Long userId) {
        return fundsLedgerRepository.findTotalValueByUserId(userId);
    }

    public void addFund(Long userId, FundsLedgerRequest request) {

        FundsLedger ledger = new FundsLedger();
        ledger.setUserId(userId);

        ledger.setAmount(request.getAmount());
        ledger.setPaymentMode(request.getPaymentMode());
        ledger.setType(FundTransactionType.ADD);

        ledger.setTransactionDate(request.getTransactionDate());
        fundsLedgerRepository.save(ledger);
    }

    public void withdrawFund(Long userId, FundsLedgerRequest request) {
        FundsLedger ledger = new FundsLedger();
        ledger.setUserId(userId);
        ledger.setAmount(request.getAmount().negate());
        ledger.setPaymentMode(request.getPaymentMode());
        ledger.setType(FundTransactionType.WITHDRAW);
        ledger.setTransactionDate(request.getTransactionDate());
        fundsLedgerRepository.save(ledger);
    }

    public Page<FundsLedgerResult> listFunds(
            Long userId, int page, int size, String sortBy, String direction) {
        Sort sort =
                direction.equalsIgnoreCase("asc")
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<FundsLedger> rawPage = fundsLedgerRepository.findByUserId(userId, pageable);
        return rawPage.map(this::mapToResult);
    }

    private FundsLedgerResult mapToResult(FundsLedger ledger) {
        return FundsLedgerResult.builder()
                .amount(ledger.getAmount().abs()) // ✅ make it always positive
                .paymentMode(ledger.getPaymentMode())
                .transactionDate(ledger.getTransactionDate())
                .transactionType(ledger.getType())
                .createdAt(ledger.getTimestamp())
                .build();
    }
}
