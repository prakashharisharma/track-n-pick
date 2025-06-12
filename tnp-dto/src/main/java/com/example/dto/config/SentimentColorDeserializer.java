package com.example.dto.config;

import com.example.dto.type.SentimentColor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class SentimentColorDeserializer extends JsonDeserializer<SentimentColor> {

    @Override
    public SentimentColor deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        String value = jsonParser.getText();

        return SentimentColor.fromString(value);
    }
}

