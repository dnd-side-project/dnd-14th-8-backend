package com.dnd.moyeolak.global.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class LocalDateTimeListConverter implements AttributeConverter<List<LocalDateTime>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<LocalDateTime> dateTimes) {
        if (dateTimes == null || dateTimes.isEmpty()) {
            return "[]";
        }

        try {
            List<String> dateStrings = dateTimes.stream()
                    .map(LocalDateTime::toString)
                    .toList();
            return objectMapper.writeValueAsString(dateStrings);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Failed to convert dates to JSON", e);
        }
    }

    @Override
    public List<LocalDateTime> convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }

        try {
            List<String> dateStrings = objectMapper.readValue(json, new TypeReference<>() {
            });
            return dateStrings.stream()
                    .map(LocalDateTime::parse)
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Failed to convert JSON to dates", e);
        }
    }
}
