package com.example;

import com.example.data.transactional.entities.BankingFinancials;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

public class BankingFinancialsParser {

    public static BankingFinancials parseFromXml(String xmlContent) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        InputSource inputSource = new InputSource(new StringReader(xmlContent));
        Document doc = dBuilder.parse(inputSource);
        doc.getDocumentElement().normalize();

        BankingFinancials financials = new BankingFinancials();

        // === StockFinancials Fields ===
        financials.setQuarterEnded(LocalDate.now());

        financials.setNetProfit(toBigDecimal(getValue(doc, "IndASNetProfitLoss")));
        financials.setTotalIncome(toBigDecimal(getValue(doc, "TotalIncome")));
        financials.setEarningsPerShare(
                toBigDecimal(getValue(doc, "EarningsPerShareBasicAndDiluted")));
        financials.setTotalAssets(toBigDecimal(getValue(doc, "TotalAssets")));
        financials.setTotalLiabilities(toBigDecimal(getValue(doc, "TotalLiabilities")));
        financials.setReserves(toBigDecimal(getValue(doc, "ReservesAndSurplus")));
        financials.setDividend(toBigDecimal(getValue(doc, "ProposedDividend")));
        financials.setCurrentAssets(toBigDecimal(getValue(doc, "CurrentAssets")));
        financials.setCurrentLiabilities(toBigDecimal(getValue(doc, "CurrentLiabilities")));
        financials.setTotalDebt(toBigDecimal(getValue(doc, "DebtSecurities")));
        financials.setCashEquivalents(toBigDecimal(getValue(doc, "CashAndCashEquivalents")));
        financials.setInventory(BigDecimal.ZERO); // Banks generally don't report inventories
        financials.setInterestExpense(toBigDecimal(getValue(doc, "FinanceCost")));
        financials.setFaceValue(toBigDecimal(getValue(doc, "FaceValuePerShare")));

        BigDecimal paidUpCapital = toBigDecimal(getValue(doc, "EquityShareCapital"));
        if (financials.getFaceValue().compareTo(BigDecimal.ZERO) > 0) {
            long totalShares =
                    paidUpCapital
                            .divide(financials.getFaceValue(), RoundingMode.HALF_UP)
                            .longValue();
            financials.setTotalShares(totalShares);
        }

        financials.setLastModified(LocalDate.now());

        // === Banking-Specific Fields ===
        financials.setLoansAndAdvances(toBigDecimal(getValue(doc, "LoansAdvances")));
        financials.setInvestments(toBigDecimal(getValue(doc, "Investments")));
        financials.setDeposits(toBigDecimal(getValue(doc, "Deposits")));
        financials.setBorrowings(toBigDecimal(getValue(doc, "Borrowings")));
        financials.setGrossNpa(toBigDecimal(getValue(doc, "GrossNPA")));
        financials.setNetNpa(toBigDecimal(getValue(doc, "NetNPA")));
        financials.setProvisions(toBigDecimal(getValue(doc, "Provisions")));
        financials.setTier1Capital(toBigDecimal(getValue(doc, "Tier1Capital")));
        financials.setTier2Capital(toBigDecimal(getValue(doc, "Tier2Capital")));
        financials.setRiskWeightedAssets(toBigDecimal(getValue(doc, "RiskWeightedAssets")));
        financials.setNetInterestIncome(toBigDecimal(getValue(doc, "NetInterestIncome")));
        financials.setOperatingExpenses(toBigDecimal(getValue(doc, "OperatingExpenses")));
        financials.setOtherIncome(toBigDecimal(getValue(doc, "OtherIncome")));
        financials.setNetWorth(toBigDecimal(getValue(doc, "NetWorth")));

        return financials;
    }

    private static String getValue(Document doc, String tag) {
        NodeList nList = doc.getElementsByTagName(tag);
        if (nList.getLength() > 0 && nList.item(0).getTextContent() != null) {
            return nList.item(0).getTextContent().trim();
        }
        return null;
    }

    private static BigDecimal toBigDecimal(String val) {
        try {
            return val == null ? BigDecimal.ZERO : new BigDecimal(val.replace(",", ""));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
