package com.dnd.moyeolak.global.converter;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Converter
public class LocalDateListConverter implements AttributeConverter<List<LocalDate>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<LocalDate> dates) {
        if (dates == null || dates.isEmpty()) {
            return "[]";
        }

        try {
            List<String> dateStrings = dates.stream()
                    .map(LocalDate::toString)
                    .toList();
            return objectMapper.writeValueAsString(dateStrings);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Failed to convert dates to JSON", e);
        }
    }

    @Override
    public List<LocalDate> convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }

        try {
            List<String> dateStrings = objectMapper.readValue(json, new TypeReference<>() {});
            return dateStrings.stream()
                    .map(LocalDate::parse)
                    .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Failed to convert JSON to dates", e);
        }
    }
}
