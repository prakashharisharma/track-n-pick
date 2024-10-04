package com.example.mq.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.ErrorHandler;
import org.springframework.lang.Nullable;

/**
 * Implements custom error handler for kafka consumer
 *
 * @author prakash
 */
@Slf4j
public class LoggingErrorHandler implements ErrorHandler {

    @Override
    public void handle(Exception thrownException, @Nullable ConsumerRecord<?, ?> record) {
        log.error("Error while processing: {}", record, thrownException);
    }
}
