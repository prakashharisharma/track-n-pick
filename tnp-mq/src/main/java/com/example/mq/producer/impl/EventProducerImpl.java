package com.example.mq.producer.impl;

import com.example.mq.producer.EventProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
@Slf4j
public class EventProducerImpl implements EventProducerService {

    @Value("${kafka.topic.prefix:}")
    private String topicPrefix;

    @Autowired
    @Qualifier("kafkaTemplate") private KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void create(String topicName, Object message) {
        this.sendMessage(topicPrefix + topicName, message);
    }

    public ListenableFuture<SendResult<String, Object>> sendMessage(
            String topicName, Object message) {

        log.debug("sending message asynchronously, message : {} to topic : {} ", message, topicName);

        ListenableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topicName, message);

        this.addCallback(message, future);

        return future;
    }

    private void addCallback(Object message, ListenableFuture<SendResult<String, Object>> future) {

        future.addCallback(
                new ListenableFutureCallback<SendResult<String, Object>>() {

                    @Override
                    public void onSuccess(SendResult<String, Object> result) {
                        log.debug(
                                "message sent to broker, message : {}, offset : {}",
                                message,
                                result.getRecordMetadata().offset());
                    }

                    @Override
                    public void onFailure(Throwable ex) {
                        log.error("Sending message to broker failed, message :  {} ", message, ex);
                    }
                });
    }
}

