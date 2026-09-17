package com.sciencebot.pos.billing.internal.adapters.factus;

import com.sciencebot.pos.billing.CreateNumberingRangeRequest;
import com.sciencebot.pos.billing.NumberingRangeDto;
import com.sciencebot.pos.billing.internal.adapters.NumberingRangeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Cliente especializado para la gestión y sincronización de rangos de numeración ante Factus y la DIAN.
 */
@Component
public class FactusNumberingRangeClient implements NumberingRangeProvider {

    private static final Logger log = LoggerFactory.getLogger(FactusNumberingRangeClient.class);

    private final FactusClientHelper httpHelper;
    private final FactusNumberingRangeMapper rangeMapper;

    @org.springframework.beans.factory.annotation.Autowired
    public FactusNumberingRangeClient(FactusClientHelper httpHelper, FactusNumberingRangeMapper rangeMapper) {
        this.httpHelper = httpHelper;
        this.rangeMapper = rangeMapper;
    }

    @Override
    public String getProviderName() {
        return "factus";
    }

    @Override
    public List<NumberingRangeDto> queryDianNumberingRanges() {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = httpHelper.authorizedGet("/v2/numbering-ranges/dian")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);

            return rangeMapper.extractRangesList(response);
        } catch (Exception ex) {
            log.error("Error al consultar rangos DIAN en Factus", ex);
            return List.of();
        }
    }

    @Override
    public List<NumberingRangeDto> listNumberingRanges() {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = httpHelper.authorizedGet("/v2/numbering-ranges")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);

            return rangeMapper.extractRangesList(response);
        } catch (Exception ex) {
            log.error("Error al listar rangos de numeración en Factus", ex);
            return List.of();
        }
    }

    @Override
    public NumberingRangeDto getNumberingRange(Long numberingRangeId) {
        Objects.requireNonNull(numberingRangeId, "El ID del rango de numeración es obligatorio");
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = httpHelper.authorizedGet("/v2/numbering-ranges/" + numberingRangeId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.get("data") instanceof Map<?, ?> dataMap) {
                return rangeMapper.mapToDto(dataMap);
            }
            return null;
        } catch (Exception ex) {
            log.error("Error al obtener rango de numeración ID {}: {}", numberingRangeId, ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public NumberingRangeDto createNumberingRange(CreateNumberingRangeRequest request) {
        Objects.requireNonNull(request, "La solicitud de rango de numeración no puede ser nula");
        try {
            Map<String, Object> payload = rangeMapper.toCreatePayload(request);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = httpHelper.authorizedPost("/v2/numbering-ranges")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.get("data") instanceof Map<?, ?> dataMap) {
                return rangeMapper.mapToDto(dataMap);
            }
            return null;
        } catch (Exception ex) {
            log.error("Error al crear rango de numeración en Factus: {}", ex.getMessage(), ex);
            throw new RuntimeException("Error al registrar rango de numeración en Factus: " + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean deleteNumberingRange(Long numberingRangeId) {
        Objects.requireNonNull(numberingRangeId, "El ID del rango de numeración es obligatorio");
        try {
            httpHelper.authorizedDelete("/v2/numbering-ranges/" + numberingRangeId)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception ex) {
            log.error("Error al eliminar rango de numeración ID {}: {}", numberingRangeId, ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public boolean toggleNumberingRangeStatus(Long numberingRangeId) {
        Objects.requireNonNull(numberingRangeId, "El ID del rango de numeración es obligatorio");
        try {
            httpHelper.authorizedPatch("/v2/numbering-ranges/" + numberingRangeId + "/toggle-status")
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception ex) {
            log.error("Error al cambiar estado del rango de numeración ID {}: {}", numberingRangeId, ex.getMessage(), ex);
            return false;
        }
    }
}
