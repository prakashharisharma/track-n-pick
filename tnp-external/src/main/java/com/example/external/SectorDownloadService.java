package com.example.external;

import com.example.dto.io.BseSectorListResponse;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SectorDownloadService {

    private static final String BSE_URL_TEMPLATE =
            "https://www.bseindia.com/markets/Equity/EQReports/industrywatch.aspx?expandable=2&page=%s&scripname=All";

    public List<BseSectorListResponse> downloadAndProcessSectors(String pageId) throws IOException {
        String url = String.format(BSE_URL_TEMPLATE, pageId);
        log.info("sector page:{}", url);

        // Initialize WebDriver
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu");
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            driver.get(url);

            try {
                // Try to find and click "View all Scrip"
                WebElement viewAllScrip = driver.findElement(By.linkText("View all Scrip"));
                viewAllScrip.click();
            } catch (NoSuchElementException e) {
                log.warn("Element 'View all Scrip' not found. Returning empty list.");
                return Collections
                        .emptyList(); // ✅ Return empty list instead of throwing an exception
            }

            // Try to find and click the download button
            try {
                WebElement downloadButton =
                        driver.findElement(By.cssSelector("i.fa.fa-download.iconfont"));
                downloadButton.click();
            } catch (NoSuchElementException e) {
                log.warn("Download button not found. Returning empty list.");
                return Collections.emptyList();
            }

            // Wait for the download and process the CSV
            Path downloadedFilePath = waitForCsvDownload(pageId);
            return parseCsv(downloadedFilePath);
        } catch (Exception e) {
            log.error("Error processing sectors: {}", e.getMessage(), e);
            return Collections.emptyList(); // ✅ Handle any other unexpected errors gracefully
        } finally {
            driver.quit();
        }
    }

    /** Waits for the CSV file to be downloaded and returns the correct file path. */
    private Path waitForCsvDownload(String pageId) throws IOException {
        Path downloadDir = Path.of(System.getProperty("user.home"), "Downloads");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Path latestFile =
                Files.list(downloadDir)
                        .filter(file -> file.toString().endsWith(".csv"))
                        .max(
                                Comparator.comparingLong(
                                        f -> f.toFile().lastModified())) // Get latest file
                        .orElseThrow(() -> new IOException("CSV file not found in Downloads"));

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Rename the downloaded file to include the pageId
        Path newFilePath = downloadDir.resolve("sector_data_" + pageId + ".csv");
        Files.move(latestFile, newFilePath, StandardCopyOption.REPLACE_EXISTING);

        return newFilePath;
    }

    /** Parses the downloaded CSV file and converts it to a list of BseSectorListResponse. */
    private List<BseSectorListResponse> parseCsv(Path filePath) throws IOException {
        if (Files.isDirectory(filePath)) {
            throw new IOException("Expected a file, but found a directory: " + filePath);
        }

        List<BseSectorListResponse> sectorList = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { // Skip header
                    firstLine = false;
                    continue;
                }
                BseSectorListResponse response = parseCsvLine(line.trim());
                if (response != null) {
                    sectorList.add(response);
                }
            }
        }
        return sectorList;
    }

    private BseSectorListResponse parseCsvLine(String line) {
        String[] values = line.split(",");
        if (values.length < 5) return null;

        String securityCode = values[0].trim();
        String securityName = values[1].trim();
        String ltp = values[2].trim();
        String percentChange = values[3].trim();
        String timestamp = values[4].trim();

        return new BseSectorListResponse(securityCode, securityName, ltp, percentChange, timestamp);
    }
}
