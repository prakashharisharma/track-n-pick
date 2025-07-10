package com.example.external;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

@Service
public class NSEXmlService {

    public String fetchXmlContent(String url) {
        try {
            Connection.Response response =
                    Jsoup.connect(url)
                            .ignoreContentType(true)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                            .referrer("https://www.nseindia.com")
                            .timeout(10000)
                            .method(Connection.Method.GET)
                            .execute();

            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
