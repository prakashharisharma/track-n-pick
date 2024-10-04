package com.example.dto.kafka;

import java.io.Serializable;
import lombok.Getter;

/**
 * Contains generic message with or without jwt
 *
 * @author Prakash Hari Sharma
 * @param <T>
 */
@Getter
public class Message<T> implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;

    //private JwtToken jwtToken;
    private T payload;
    private boolean token;
    private String traceId;

    public Message(String traceId,  T payload) {
        super();
        this.traceId = traceId;
       // this.jwtToken = jwtToken;
        this.payload = payload;
       // this.token = true;
    }

    public Message(T payload) {
        super();
        this.payload = payload;
        this.token = false;
    }

    public Message() {
        super();
    }
}

