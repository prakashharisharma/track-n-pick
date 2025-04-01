package com.example.external.factor;

import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockFactor;
import com.example.external.common.FactorProvider;
import com.example.util.MiscUtil;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FactorRediff implements FactorBaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FactorRediff.class);

    private static String BASE_URL_REDIFF = "https://money.rediff.com/companies/";

    static Map<String, String> dateMap = new HashMap<>();

    static Map<String, String> resultQuarterMap = new HashMap<>();

    @Autowired private MiscUtil miscUtil;

    static {
        dateMap.put("JAN", "31");
        dateMap.put("FEB", "28");
        dateMap.put("MAR", "31");
        dateMap.put("APR", "30");
        dateMap.put("MAY", "31");
        dateMap.put("JUN", "30");
        dateMap.put("JUL", "31");
        dateMap.put("AUG", "31");
        dateMap.put("SEP", "30");
        dateMap.put("OCT", "31");
        dateMap.put("NOV", "30");
        dateMap.put("DEC", "31");
    }

    static {
        resultQuarterMap.put("JAN", "DEC");
        resultQuarterMap.put("FEB", "DEC");
        resultQuarterMap.put("MAR", "MAR");
        resultQuarterMap.put("APR", "MAR");
        resultQuarterMap.put("MAY", "MAR");
        resultQuarterMap.put("JUN", "JUN");
        resultQuarterMap.put("JUL", "JUN");
        resultQuarterMap.put("AUG", "JUN");
        resultQuarterMap.put("SEP", "SEP");
        resultQuarterMap.put("OCT", "SEP");
        resultQuarterMap.put("NOV", "SEP");
        resultQuarterMap.put("DEC", "DEC");
    }

    private String ratioURL;

    private StockFactor getMcapFaceValue(Stock stock, StockFactor stockFactor) throws IOException {

        String rediffURL = this.buildURL(stock);

        System.out.println("REDIFFURL " + rediffURL);

        if (!rediffURL.contains("---")) {
            Document doc = Jsoup.connect(rediffURL).get();

            Element body = doc.body();

            // Element allElement = body.getElementsByClass("zoom-container").first();

            stockFactor.setMarketCap(this.parseMarketCap(body, stock));
            stockFactor.setFaceValue(this.parseFaceValue(body, stock));

            Element zoomDiv = body.getElementsByClass("zoom-container").get(0);

            String zoomUrl = zoomDiv.select("a").first().absUrl("href");

            if (zoomUrl == null || zoomUrl.isEmpty()) {
                return stockFactor;
            }

            String ratioUrlPre = zoomUrl.replace("/bse/day/chart", "");

            ratioUrlPre = ratioUrlPre.replace("/nse/day/chart", "");

            LOGGER.info(ratioUrlPre);

            this.setRatioURL(ratioUrlPre + "/ratio");

            LOGGER.info("Ratio Url {}", ratioURL);
        }

        return stockFactor;
    }

    private Double parseMarketCap(Element body, Stock stock) {

        try {

            // select div
            Element forBse = body.getElementById("for_BSE");

            LOGGER.info("result 1 {}", forBse.text());

            if (forBse.text().contains("not listed")) {
                forBse = body.getElementById("for_NSE");
            }

            // select first table by class
            Element table = forBse.getElementsByClass("company-graph-wrap").get(0);
            // Select second row
            Element row = table.select("tr").get(1);

            // select 5th column
            Element column = row.select("td").get(4);

            return this.parseDouble(column.text().trim(), ",", "");

        } catch (Exception e) {
            LOGGER.error("An Error ocured while parsing MArketCap {} ", stock.getNseSymbol());
        }

        return 0.0;
    }

    private Double parseFaceValue(Element body, Stock stock) {

        try {
            // select div
            Element allElement = body.getElementById("div_rcard_more");

            int i = 0;
            for (Element element : allElement.getAllElements()) {

                if (i == 11) {
                    return this.parseDouble(element.text().trim(), ",", "");
                }
                i++;
            }

        } catch (Exception e) {
            LOGGER.error("An Error ocured while parsing Face Value {} ", stock.getNseSymbol());
        }

        return 0.0;
    }

    private double parseDouble(String text, CharSequence toBeReplaced, CharSequence replaceWith) {
        try {
            return Double.parseDouble(text.replace(toBeReplaced, replaceWith));
        } catch (Exception e) {
            LOGGER.error("An error occurred while parsing : " + text + " to double.", e);
        }
        return 0.0;
    }

    private StockFactor getRatios(Stock stock, StockFactor stockFactor) throws IOException {

        String ratioUrl = this.getRatioURL();

        if (ratioUrl == null || ratioUrl.isEmpty()) {
            return stockFactor;
        }

        Document doc = Jsoup.connect(ratioUrl).get();

        Elements allElement = doc.getElementsByClass("dataTable");

        if (allElement == null) {
            return stockFactor;
        }

        int i = 0;

        for (Element element : allElement) {

            i++;

            Elements childTr = element.getElementsByTag("tr");

            int trCount = 0;

            for (Element childElement : childTr) {
                trCount++;

                if (trCount == 1) { // Period
                    Elements chilTheadTh = element.getElementsByTag("th");

                    int j = 0;

                    for (Element th : chilTheadTh) {

                        j++;
                        if (j == 2) {
                            String quarterStr = th.text();

                            String quarterStrArr[] = quarterStr.split("'");

                            String Month = quarterStrArr[0].trim().toUpperCase();

                            String date = dateMap.get(Month);

                            String resultMonth = resultQuarterMap.get(Month);

                            String dateStr =
                                    date + "-" + resultMonth + "-" + "20" + quarterStrArr[1].trim();

                            DateTimeFormatter formatter =
                                    new DateTimeFormatterBuilder()
                                            .parseCaseInsensitive()
                                            .appendPattern("dd-MMM-yyyy")
                                            .toFormatter(Locale.ENGLISH);

                            LocalDate quarterEnded = LocalDate.parse(dateStr, formatter);

                            stockFactor.setQuarterEnded(quarterEnded);
                            // Store Quarter
                        }
                    }
                }

                if (childElement.text().startsWith("Adjusted EPS (Rs)")) {

                    Elements childTd = childElement.getElementsByTag("td");

                    int j = 0;

                    for (Element td : childTd) {

                        j++;
                        if (j == 2) {

                            double eps = 0.00;
                            if (td.text().trim().equalsIgnoreCase("-")) {
                                eps = 0.00;
                            } else {

                                eps = Double.parseDouble(td.text().replace(",", "").trim());
                            }
                            stockFactor.setEps(eps);
                        }
                    }

                } else if (childElement.text().startsWith("Dividend per share")) {

                    Elements childTd = childElement.getElementsByTag("td");
                    int j = 0;
                    for (Element td : childTd) {
                        j++;
                        if (j == 2) {

                            double dividend = 0.00;

                            if (td.text().trim().equalsIgnoreCase("-")) {
                                dividend = 0.00;
                            } else {

                                dividend = Double.parseDouble(td.text().replace(",", "").trim());
                            }
                            stockFactor.setDividend(dividend);
                        }
                    }

                } else if (childElement
                        .text()
                        .startsWith("Book value (incl rev res) per share EPS (Rs)")) {

                    Elements childTd = childElement.getElementsByTag("td");
                    int j = 0;
                    for (Element td : childTd) {
                        j++;
                        if (j == 2) {
                            double bookValue = 0.00;
                            if (td.text().trim().equalsIgnoreCase("-")) {
                                bookValue = 0.00;
                            } else {
                                bookValue = Double.parseDouble(td.text().replace(",", "").trim());
                            }
                            stockFactor.setBookValue(bookValue);
                        }
                    }

                } else if (childElement.text().startsWith("Adjusted return on net worth")) {

                    Elements childTd = childElement.getElementsByTag("td");
                    int j = 0;
                    for (Element td : childTd) {
                        j++;
                        if (j == 2) {

                            double roe = 0.00;
                            if (td.text().trim().equalsIgnoreCase("-")) {
                                roe = 0.00;
                            } else {

                                roe = Double.parseDouble(td.text().replace(",", "").trim());
                            }
                            stockFactor.setReturnOnEquity(roe);
                        }
                    }

                } else if (childElement.text().startsWith("Return on long term funds")) {

                    Elements childTd = childElement.getElementsByTag("td");
                    int j = 0;
                    for (Element td : childTd) {
                        j++;
                        if (j == 2) {

                            double roce = 0.00;
                            if (td.text().trim().equalsIgnoreCase("-")) {
                                roce = 0.00;
                            } else {
                                roce = Double.parseDouble(td.text().replace(",", "").trim());
                            }
                            stockFactor.setReturnOnCapital(roce);
                        }
                    }

                } else if (childElement.text().startsWith("Total debt/equity")) {

                    Elements childTd = childElement.getElementsByTag("td");
                    int j = 0;
                    for (Element td : childTd) {
                        j++;
                        if (j == 2) {
                            double debtEquity = 0.00;
                            if (td.text().trim().equalsIgnoreCase("-")) {
                                debtEquity = 0.00;
                            } else {

                                debtEquity = Double.parseDouble(td.text().replace(",", "").trim());
                            }

                            stockFactor.setDebtEquity(debtEquity);
                        }
                    }

                } else if (childElement.text().startsWith("Current ratio")) {

                    Elements childTd = childElement.getElementsByTag("td");
                    int j = 0;
                    for (Element td : childTd) {
                        j++;
                        if (j == 2) {
                            double currentRatio = 0.00;
                            if (td.text().trim().equalsIgnoreCase("-")) {
                                currentRatio = 0.00;
                            } else {

                                currentRatio =
                                        Double.parseDouble(td.text().replace(",", "").trim());
                            }

                            stockFactor.setCurrentRatio(currentRatio);
                        }
                    }

                } else if (childElement.text().startsWith("Quick ratio")) {

                    Elements childTd = childElement.getElementsByTag("td");
                    int j = 0;
                    for (Element td : childTd) {
                        j++;
                        if (j == 2) {
                            double quickRatio = 0.00;
                            if (td.text().trim().equalsIgnoreCase("-")) {
                                quickRatio = 0.00;
                            } else {

                                quickRatio = Double.parseDouble(td.text().replace(",", "").trim());
                            }

                            stockFactor.setQuickRatio(quickRatio);
                        }
                    }

                } else {
                    continue;
                }
            }
        }
        return stockFactor;
    }

    private String buildURL(Stock stock) {

        String companyName = stock.getCompanyName().replace(".", "");

        String companyNameurlStr = companyName.replaceAll(" ", "-").toLowerCase();

        companyNameurlStr = companyNameurlStr.replace("&-", "");

        companyNameurlStr = companyNameurlStr.replace("'s", "-s");

        companyNameurlStr = companyNameurlStr.replace("(", "");

        companyNameurlStr = companyNameurlStr.replace(")", "");

        return BASE_URL_REDIFF + companyNameurlStr;
    }

    public String getRatioURL() {
        return ratioURL;
    }

    public void setRatioURL(String ratioURL) {
        this.ratioURL = ratioURL;
    }

    @Override
    public FactorProvider getServiceProvider() {
        return FactorProvider.REDIFF;
    }

    @Override
    public StockFactor getFactor(Stock stock) {

        StockFactor stockFactor = null;

        if (stock.getFactor() == null) {

            stockFactor = new StockFactor();

        } else {

            stockFactor = stock.getFactor();
        }

        // if (stock.getStockFactor() == null || remoteCallCounter <= 20) {

        try {
            stockFactor.setLastModified(LocalDate.now());
            stockFactor = this.getMcapFaceValue(stock, stockFactor);
            stockFactor = this.getRatios(stock, stockFactor);

            LOGGER.info("Quarter {}", stockFactor.getQuarterEnded());
            LOGGER.info("EPS {}", stockFactor.getEps());
            LOGGER.info("Dividend {}", stockFactor.getDividend());
            LOGGER.info("BookValue {}", stockFactor.getBookValue());
            LOGGER.info("ROE {}", stockFactor.getReturnOnEquity());
            LOGGER.info("ROCE {}", stockFactor.getReturnOnCapital());
            LOGGER.info("Debt Equity {}", stockFactor.getDebtEquity());
            LOGGER.info("Current Ratio {}", stockFactor.getCurrentRatio());
            LOGGER.info("Quick  Ratio {}", stockFactor.getQuickRatio());

            // increment thye remoteCallCounter
            // remoteCallCounter++;

            // System.out.println("Remote Call counter : " + remoteCallCounter);

        } catch (IOException e) {

            e.printStackTrace();
        }

        stockFactor.setStock(stock);
        // }

        return stockFactor;
    }
}
