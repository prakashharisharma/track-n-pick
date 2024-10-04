package com.example.mq.producer;

/**
 * Interface to produce kafka events
 *
 * @author prakash
 * @param <T>
 */
public interface EventProducerService {

    public void create(String topicName, Object message);
}
