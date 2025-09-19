package com.example;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BhavDownloadService {

    @Retryable(
            value = {Exception.class},
            maxAttempts = 7, // 1 initial attempt + 6 retries
            backoff =
                    @Backoff(
                            delay = 300_000, // 5 minutes (in ms)
                            multiplier = 2, // Double each time
                            maxDelay = 9_600_000 // 160 min cap (in ms)
                            ))
    /*
    @Retryable(
            value = {Exception.class},
            maxAttempts = 11, // 1 + 16 retries
            backoff =
                    @Backoff(
                            delay = 18_00_000, // 30 minutes in ms
                            multiplier = 1 // no exponential, just fixed delay
                            ))*/
    public byte[] downloadFile(String fileUrl) throws IOException {

        log.info("Downloading file from: {}", fileUrl);
        HttpURLConnection connection = (HttpURLConnection) new URL(fileUrl).openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
        connection.setRequestProperty("Referer", "https://www.nseindia.com/");

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10_000);
        connection.setReadTimeout(10_000);

        int responseCode = connection.getResponseCode();

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to download file, HTTP response code: " + responseCode);
        }

        try (InputStream inputStream = connection.getInputStream();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        }
    }

    /** This method is called when all retries are exhausted. */
    @Recover
    public byte[] recoverDownloadFile(IOException ex, String fileUrl) {
        log.error("Failed to download file after multiple retries: {}", fileUrl, ex);
        return new byte[0]; // Return empty array or handle failure accordingly
    }
}
