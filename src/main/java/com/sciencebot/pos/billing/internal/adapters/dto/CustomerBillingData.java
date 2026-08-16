package com.sciencebot.pos.billing.internal.adapters.dto;

public record CustomerBillingData(
        String identification,
        String dv,
        String legalName,
        String email,
        Integer legalOrganizationId,
        Integer tributeId,
        Integer municipalityId
) {}
