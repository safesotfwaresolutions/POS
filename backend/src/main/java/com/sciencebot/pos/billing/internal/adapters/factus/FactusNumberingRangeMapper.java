package com.sciencebot.pos.billing.internal.adapters.factus;

import com.sciencebot.pos.billing.CreateNumberingRangeRequest;
import com.sciencebot.pos.billing.NumberingRangeDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapper responsable de la transformación bidireccional entre los DTOs de rangos
 * de numeración de la aplicación y la estructura JSON de la API Factus v2.
 */
@Component
public class FactusNumberingRangeMapper {

    /**
     * Construye el payload esperado por Factus v2 para registrar un nuevo rango de numeración.
     */
    public Map<String, Object> toCreatePayload(CreateNumberingRangeRequest request) {
        if (request == null) {
            return Map.of();
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("document", request.document());
        if (request.prefix() != null) payload.put("prefix", request.prefix());
        payload.put("resolution_number", request.resolutionNumber());
        payload.put("current", request.current());
        if (request.startDate() != null) payload.put("start_date", request.startDate());
        if (request.endDate() != null) payload.put("end_date", request.endDate());
        if (request.from() != null) payload.put("from", request.from());
        if (request.to() != null) payload.put("to", request.to());
        if (request.technicalKey() != null) payload.put("technical_key", request.technicalKey());
        return payload;
    }

    /**
     * Extrae una lista de NumberingRangeDto desde una respuesta JSON de Factus API v2.
     * Soporta tanto listas directas en 'data' como listas anidadas en 'data.data'.
     */
    public List<NumberingRangeDto> extractRangesList(Map<String, Object> response) {
        List<NumberingRangeDto> list = new ArrayList<>();
        if (response == null || !response.containsKey("data") || response.get("data") == null) {
            return list;
        }
        Object dataObj = response.get("data");
        if (dataObj instanceof List<?> dataList) {
            for (Object item : dataList) {
                if (item instanceof Map<?, ?> itemMap) {
                    list.add(mapToDto(itemMap));
                }
            }
        } else if (dataObj instanceof Map<?, ?> dataMap && dataMap.get("data") instanceof List<?> nestedList) {
            for (Object item : nestedList) {
                if (item instanceof Map<?, ?> itemMap) {
                    list.add(mapToDto(itemMap));
                }
            }
        }
        return list;
    }

    /**
     * Transforma un mapa individual de respuesta de Factus a NumberingRangeDto.
     */
    public NumberingRangeDto mapToDto(Map<?, ?> map) {
        if (map == null) {
            return null;
        }
        Long id = parseLongValue(map.get("id"));
        String document = map.get("document") != null ? map.get("document").toString() : null;
        String prefix = map.get("prefix") != null ? map.get("prefix").toString() : null;
        String resolutionNumber = map.get("resolution_number") != null ? map.get("resolution_number").toString() : null;
        Long from = parseLongValue(map.get("from"));
        Long to = parseLongValue(map.get("to"));
        Long current = parseLongValue(map.get("current"));
        String startDate = map.get("start_date") != null ? map.get("start_date").toString() : null;
        String endDate = map.get("end_date") != null ? map.get("end_date").toString() : null;
        String technicalKey = map.get("technical_key") != null ? map.get("technical_key").toString() : null;
        Boolean isActive = map.get("is_active") instanceof Boolean b
                ? b
                : (map.get("is_active") instanceof Number n ? n.intValue() == 1 : null);

        return new NumberingRangeDto(id, document, prefix, resolutionNumber, from, to, current, startDate, endDate, technicalKey, isActive);
    }

    private Long parseLongValue(Object value) {
        if (value instanceof Number n) {
            return n.longValue();
        }
        if (value instanceof String s && !s.isBlank()) {
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
