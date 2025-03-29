package com.example.worker.scheduler;


import com.example.transactional.model.StockPriceIN;
import com.example.transactional.processor.BhavProcessor;
import com.example.transactional.service.BhavcopyService;
import com.example.service.CalendarService;
import com.example.util.MiscUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProcessBhavScheduler {

    private final CalendarService calendarService;

    private final BhavcopyService bhavcopyService;

    private final BhavProcessor bhavProcessor;

    private final MiscUtil miscUtil;

    /**
     * Scheduled task to start downloading Bhavcopy at 4:45 AM IST
     */
    @Scheduled(cron = "0 45 16 * * *", zone = "Asia/Kolkata")// Runs every day at 4:45 AM IST
    public void scheduleBhavCopyDownload() {

        LocalDate sessionDate = miscUtil.currentDate(); // Adjust if needed

        try {
            log.info("Starting scheduled bhav copy download for session date: {}", sessionDate);

            if(calendarService.isWorkingDay(sessionDate)) {

                List<StockPriceIN> nseBhavIOList = bhavcopyService.downloadAndProcessBhavcopy(sessionDate);

                if (nseBhavIOList!= null && !nseBhavIOList.isEmpty()){
                    bhavProcessor.process(nseBhavIOList);
                }
            }
        } catch (IOException e) {
            log.error("bhav copy download failed for {}: {}", sessionDate, e.getMessage());
        }
    }
}
