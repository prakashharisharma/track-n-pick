package com.example;

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

public class MainRediff {

    static Map<String, String> dateMap = new HashMap<>();

    static {
        dateMap.put("MAR", "31");
        dateMap.put("JUN", "30");
        dateMap.put("SEP", "30");
        dateMap.put("DEC", "31");
    }

    public static void main(String[] args) throws IOException {

        Document doc = Jsoup.connect("https://money.rediff.com/companies/atul-ltd").get();

        Element body = doc.body();

        Element allElement = body.getElementsByClass("zoom-container").first();

        String url = allElement.select("a").first().absUrl("href");

        System.out.println(url);
        System.out.println("*********************");
        String newURL = url.replace("bse/day/chart", "ratio");
        System.out.println(newURL);

        Document doc1 = Jsoup.connect(newURL).get();
        Element body1 = doc1.body();
        Elements allElements = body1.getAllElements();

        Elements sections = allElements.first().getElementsByTag("table");

        System.out.println("*******************************");

        int i = 0;

        for (Element element : sections) {
            i++;

            if (i == 2) {

                System.out.println("**********CHILDS*********************" + i);

                Elements chilrd = element.getElementsByTag("tr");

                int trCount = 0;

                for (Element childElement : chilrd) {
                    trCount++;

                    if (trCount == 1) {
                        Elements chilTheadTh = element.getElementsByTag("th");

                        int j = 0;

                        for (Element th : chilTheadTh) {

                            j++;
                            if (j == 2) {
                                String quarterStr = th.text();

                                String quarterStrArr[] = quarterStr.split("'");

                                String Month = quarterStrArr[0].trim().toUpperCase();

                                String date = dateMap.get(Month);

                                String dateStr =
                                        date + "-" + Month + "-" + "20" + quarterStrArr[1].trim();

                                DateTimeFormatter formatter =
                                        new DateTimeFormatterBuilder()
                                                .parseCaseInsensitive()
                                                .appendPattern("dd-MMM-yyyy")
                                                .toFormatter(Locale.ENGLISH);

                                LocalDate localDate = LocalDate.parse(dateStr, formatter);

                                System.out.println(localDate.toString());
                            }
                        }
                    }

                    System.out.println(childElement.text() + " " + trCount);
                }

            } else {
                continue;
            }
            System.out.println("*******************************");
        }
    }
}
