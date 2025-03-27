package com.example;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NSEFileDownloader {
    public static void main(String[] args) {
        String fileURL = "https://nsearchives.nseindia.com/content/cm/BhavCopy_NSE_CM_0_0_0_20250318_F_0000.csv.zip";
        String saveFilePath = "BhavCopy_NSE_CM_20250318.zip";

        try {
            URL url = new URL(fileURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set headers to mimic the browser
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36");
            connection.setRequestProperty("Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br, zstd");
            connection.setRequestProperty("Accept-Language", "en-IN,en-GB;q=0.9,en-US;q=0.8,en;q=0.7,hi;q=0.6");
            connection.setRequestProperty("Referer", "https://www.nseindia.com/");
            connection.setRequestProperty("Cookie", "<PASTE-YOUR-COOKIES-HERE>"); // Use actual cookies

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(10_000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(saveFilePath);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();
                System.out.println("File downloaded: " + saveFilePath);
            } else {
                System.out.println("Failed to download file, HTTP response code: " + responseCode);
            }

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
