package com.example;

import com.example.dto.io.StockPriceIN;
import com.example.service.BhavcopyService;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class BhavcopyServiceImpl implements BhavcopyService {

    private static final String NSE_URL_TEMPLATE =
            "https://nsearchives.nseindia.com/content/cm/BhavCopy_NSE_CM_0_0_0_%s_F_0000.csv.zip";

    private final BhavDownloadService bhavDownloadService;

    @Override
    public List<StockPriceIN> downloadAndProcessBhavcopy(LocalDate sessionDate) throws IOException {
        // Generate URL dynamically
        String formattedDate = sessionDate.format(DateTimeFormatter.BASIC_ISO_DATE); // YYYYMMDD
        String fileUrl = String.format(NSE_URL_TEMPLATE, formattedDate);

        log.info("Downloading NSE Bhavcopy from: {}", fileUrl);

        // 1. Download ZIP from NSE
        byte[] zipData = bhavDownloadService.downloadFile(fileUrl);

        // 2. Extract CSV content from ZIP
        String csvContent = extractCsvFromZip(zipData);

        // 3. Parse CSV using OpenCSV
        return parseCsv(csvContent);
    }

    /** Retry strategy: 10 min → 20 min → 30 min → 40 min → 50 min → 60 min */
    private String extractCsvFromZip(byte[] zipData) throws IOException {
        log.info("Extracting CSV from ZIP...");
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipData);
                ZipInputStream zipInputStream = new ZipInputStream(byteArrayInputStream);
                StringWriter writer = new StringWriter()) {

            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.getName().endsWith(".csv")) {
                    try (BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            zipInputStream, StandardCharsets.UTF_8))) {
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
            CsvToBean<StockPriceIN> csvToBean =
                    new CsvToBeanBuilder<StockPriceIN>(reader)
                            .withType(StockPriceIN.class)
                            .withIgnoreLeadingWhiteSpace(true)
                            .withSeparator(',')
                            .build();
            return csvToBean.parse();
        }
    }
}
