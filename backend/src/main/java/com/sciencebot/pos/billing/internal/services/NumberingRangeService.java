package com.sciencebot.pos.billing.internal.services;

import com.sciencebot.pos.billing.CreateNumberingRangeRequest;
import com.sciencebot.pos.billing.NumberingRangeDto;

import java.util.List;

/**
 * Servicio interno para la gestión y sincronización de Rangos de Numeración.
 */
public interface NumberingRangeService {

    List<NumberingRangeDto> queryDianNumberingRanges();

    List<NumberingRangeDto> listNumberingRanges();

    NumberingRangeDto getNumberingRange(Long numberingRangeId);

    NumberingRangeDto createNumberingRange(CreateNumberingRangeRequest request);

    boolean deleteNumberingRange(Long numberingRangeId);

    boolean toggleNumberingRangeStatus(Long numberingRangeId);
}
