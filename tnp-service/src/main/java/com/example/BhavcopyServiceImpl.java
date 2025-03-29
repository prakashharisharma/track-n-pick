package com.example;

import com.example.transactional.model.StockPriceIN;
import com.example.transactional.service.BhavcopyService;
import com.example.service.FileNameService;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@RequiredArgsConstructor
@Service
public class BhavcopyServiceImpl implements BhavcopyService {
    private final FileNameService fileNameService;

    private static final String NSE_URL_TEMPLATE = "https://nsearchives.nseindia.com/content/cm/BhavCopy_NSE_CM_0_0_0_%s_F_0000.csv.zip";

    @Override
    public List<StockPriceIN> downloadAndProcessBhavcopy(LocalDate sessionDate) throws IOException {
        // Generate URL dynamically
        String formattedDate = sessionDate.format(DateTimeFormatter.BASIC_ISO_DATE); // YYYYMMDD
        String fileUrl = String.format(NSE_URL_TEMPLATE, formattedDate);


        log.info("Downloading NSE Bhavcopy from: {}", fileUrl);

        // 1. Download ZIP from NSE
        byte[] zipData = downloadFile(fileUrl);

        // 2. Extract CSV content from ZIP
        String csvContent = extractCsvFromZip(zipData);

        // 3. Parse CSV using OpenCSV
        return parseCsv(csvContent);
    }

    /**
     * Retry strategy: 10 min → 20 min → 30 min → 40 min → 50 min → 60 min
     */
    @Retryable(
            value = {IOException.class},
            maxAttempts = 5,  // Initial attempt + 4 retries
            backoff = @Backoff(delay = 900_000, multiplier = 2, maxDelay = 7_200_000)  // 15 min, 30 min, 60 min...
    )
    private byte[] downloadFile(String fileUrl) throws IOException {

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

    /**
     * This method is called when all retries are exhausted.
     */
    @Recover
    public byte[] recoverDownloadFile(IOException ex, String fileUrl) {
        log.error("Failed to download file after multiple retries: {}", fileUrl, ex);
        return new byte[0];  // Return empty array or handle failure accordingly
    }

    private String extractCsvFromZip(byte[] zipData) throws IOException {
        log.info("Extracting CSV from ZIP...");
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipData);
             ZipInputStream zipInputStream = new ZipInputStream(byteArrayInputStream);
             StringWriter writer = new StringWriter()) {

            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.getName().endsWith(".csv")) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipInputStream, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            writer.write(line + "\n");
                        }
                    }
                    return writer.toString();
                }
            }
        }
        throw new IOException("CSV file not found in ZIP");
    }

    private List<StockPriceIN> parseCsv(String csvContent) throws IOException {
        log.info("Parsing CSV content...");
        try (Reader reader = new StringReader(csvContent)) {
            CsvToBean<StockPriceIN> csvToBean = new CsvToBeanBuilder<StockPriceIN>(reader)
                    .withType(StockPriceIN.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSeparator(',')
                    .build();
            return csvToBean.parse();
        }
    }
}