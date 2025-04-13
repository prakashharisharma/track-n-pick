package com.example;

import com.example.data.transactional.entities.StockFinancials;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

public class StockFinancialsParser {

    private static final String CONTEXT_REF = "OneD";

    public static StockFinancials parseFromXml(String xmlContent, LocalDate quarterEnded)
            throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        InputSource inputSource = new InputSource(new StringReader(xmlContent));
        Document doc = dBuilder.parse(inputSource);
        doc.getDocumentElement().normalize();

        StockFinancials financials = new StockFinancials();
        financials.setQuarterEnded(quarterEnded != null ? quarterEnded : LocalDate.now());

        // === Fields with contextRef="OneD" ===
        financials.setFaceValue(
                toBigDecimal(
                        findValueBySuffixWithContext(
                                doc, CONTEXT_REF, "FaceValueOfEquityShareCapital")));
        financials.setPaidUpCapital(
                toBigDecimal(
                        findValueBySuffixWithContext(
                                doc, CONTEXT_REF, "PaidUpValueOfEquityShareCapital")));
        financials.setTotalIncome(
                toBigDecimal(findValueBySuffixWithContext(doc, CONTEXT_REF, "Income")));
        financials.setEbitda(
                toBigDecimal(
                        findValueBySuffixWithContext(
                                doc, CONTEXT_REF, "ProfitBeforeExceptionalItemsAndTax")));
        financials.setPbt(
                toBigDecimal(findValueBySuffixWithContext(doc, CONTEXT_REF, "ProfitBeforeTax")));
        financials.setNetProfit(
                toBigDecimal(
                        findValueBySuffixWithContext(doc, CONTEXT_REF, "ProfitLossForPeriod")));
        financials.setDividend(BigDecimal.ZERO);

        // === Total Shares Calculation ===
        if (financials.getFaceValue().compareTo(BigDecimal.ZERO) > 0) {
            long totalShares =
                    financials
                            .getPaidUpCapital()
                            .divide(financials.getFaceValue(), RoundingMode.HALF_UP)
                            .longValue();
            financials.setTotalShares(totalShares);

            // EPS = Net Profit / Total Shares
            if (totalShares > 0) {
                BigDecimal eps =
                        financials
                                .getNetProfit()
                                .divide(BigDecimal.valueOf(totalShares), 2, RoundingMode.HALF_UP);
                financials.setEarningsPerShare(eps);
            } else {
                financials.setEarningsPerShare(BigDecimal.ZERO);
            }
        } else {
            financials.setTotalShares(0L);
            financials.setEarningsPerShare(BigDecimal.ZERO);
        }

        // === Meta ===
        financials.setLastModified(LocalDate.now());

        return financials;
    }

    // Find element where local name matches one of the suffixes and contextRef="OneD"
    private static String findValueBySuffixWithContext(
            Document doc, String contextRef, String... suffixes) {
        NodeList nodeList = doc.getElementsByTagNameNS("*", "*");
        for (String suffix : suffixes) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                String localName = node.getLocalName();
                NamedNodeMap attributes = node.getAttributes();

                if (localName != null && localName.equalsIgnoreCase(suffix) && attributes != null) {
                    Node contextAttr = attributes.getNamedItem("contextRef");
                    if (contextAttr != null
                            && contextRef.equalsIgnoreCase(contextAttr.getTextContent())) {
                        return node.getTextContent().trim();
                    }
                }
            }
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
