package com.example.mt.service.async;


import com.example.dto.kafka.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Abstract Class for KAFKA Consumer
 *
 * <p>This provides JWT token to security context
 *
 * <p>and common logging
 *
 * @author prakash
 * @param <T>
 */
@Slf4j
@Component
public abstract class AbstractEventConsumer<T> {

    public static final String TRACE_ID = "traceId";

    public static final String SPAN_ID = "spanId";

    protected void process(
            ConsumerRecord<String, Message<T>> consumerRecord, @Payload Message<T> messageWrapper) {

        log.debug(
                "Processing event topic:{}, partition:{}",
                consumerRecord.topic(),
                consumerRecord.partition());

        String currentTraceId = getCurrentTraceId();

        try {

          //  JwtToken jwtToken = (JwtToken) messageWrapper.getJwtToken();

            //this.setSecurityContext(jwtToken);

            this.setTraceId(messageWrapper.getTraceId());

            this.process(messageWrapper);

        } catch (Exception e) {
            log.error(
                    "Error processing event topic:{}, partition:{}",
                    consumerRecord.topic(),
                    consumerRecord.partition(),
                    e);
        } finally {
            this.resetTraceId(currentTraceId);
        }

        log.debug(
                "Processing completed topic:{}, partition:{}",
                consumerRecord.topic(),
                consumerRecord.partition());
    }

    public abstract void consume(
            ConsumerRecord<String, Message<T>> consumerRecord, @Payload Message<T> messageWrapper)
            throws Exception;

    public abstract void process(Message<T> messageWrapper) throws Exception;

    private String getCurrentTraceId() {
        return MDC.get(TRACE_ID);
    }

    private void setTraceId(String traceId) {
        MDC.put(TRACE_ID, traceId);
    }

    private void resetTraceId(String oldTraceId) {
        MDC.put(TRACE_ID, oldTraceId);
    }

    /*
    private void setSecurityContext(JwtToken jwtToken) {

        if (jwtToken != null) {

            Jwt jwt =
                    new Jwt(
                            jwtToken.getTokenValue(),
                            jwtToken.getIssuedAt(),
                            jwtToken.getExpiresAt(),
                            jwtToken.getHeaders(),
                            jwtToken.getClaims());

            JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(jwt);

            SecurityContextHolder.getContext().setAuthentication(jwtAuthenticationToken);
        }
    }
     */
}
