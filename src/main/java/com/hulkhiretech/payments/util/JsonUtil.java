package com.hulkhiretech.payments.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class JsonUtil {

    private final ObjectMapper objectMapper;

    public String toJson(Object obj){
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Error converting to JSON", e);
            throw new RuntimeException("Error converting to JSON", e);
        }
    }

    public <T> T fromJson(String json, Class<T> clazz){
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Error converting from JSON", e);
            throw new RuntimeException("Error converting from JSON", e);
        }
    }
}
