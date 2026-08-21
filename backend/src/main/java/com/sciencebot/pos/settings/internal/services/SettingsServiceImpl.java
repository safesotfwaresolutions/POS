package com.sciencebot.pos.settings.internal.services;

import com.sciencebot.pos.settings.*;
import com.sciencebot.pos.settings.internal.entities.Setting;
import com.sciencebot.pos.settings.internal.repositories.SettingsRepository;
import com.sciencebot.pos.settings.internal.mappers.SettingsMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SettingsServiceImpl implements SettingsFacade {

    private final SettingsRepository settingsRepository;
    private final SettingsMapper settingsMapper;
    private final ParameterService parameterService;

    public SettingsServiceImpl(SettingsRepository settingsRepository, SettingsMapper settingsMapper,
                               ParameterService parameterService) {
        this.settingsRepository = settingsRepository;
        this.settingsMapper = settingsMapper;
        this.parameterService = parameterService;
    }

    @Override
    public SettingsDto getSettings() {
        Setting setting = settingsRepository.findById(1)
                .orElseThrow(() -> new EntityNotFoundException("La configuración global no está inicializada"));
        return settingsMapper.toDto(setting);
    }

    @Override
    @Transactional
    public SettingsDto updateSettings(UpdateSettingsCommand command) {
        Setting setting = settingsRepository.findById(1)
                .orElseThrow(() -> new EntityNotFoundException("La configuración global no está inicializada"));

        if (command.businessName() == null || command.businessName().isBlank()) {
            throw new IllegalArgumentException("El nombre del negocio es obligatorio");
        }
        if (command.address() == null || command.address().isBlank()) {
            throw new IllegalArgumentException("La dirección del negocio es obligatoria");
        }
        if (command.phone() == null || command.phone().isBlank()) {
            throw new IllegalArgumentException("El teléfono del negocio es obligatorio");
        }

        setting.setBusinessName(command.businessName().trim());
        setting.setAddress(command.address().trim());
        setting.setPhone(command.phone().trim());
        setting.setTaxId(command.taxId() != null ? command.taxId().trim() : null);
        setting.setEmail(command.email() != null ? command.email().trim() : null);
        setting.setLogoUrl(command.logoUrl() != null ? command.logoUrl().trim() : null);

        Setting saved = settingsRepository.save(setting);
        return settingsMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void seedSettings(SettingsDto defaultSettings) {
        if (!settingsRepository.existsById(1)) {
            Setting setting = new Setting();
            setting.setBusinessName(defaultSettings.businessName());
            setting.setAddress(defaultSettings.address());
            setting.setPhone(defaultSettings.phone());
            setting.setTaxId(defaultSettings.taxId());
            setting.setEmail(defaultSettings.email());
            setting.setLogoUrl(defaultSettings.logoUrl());
            settingsRepository.save(setting);
        }
    }

    // --- Catálogos dinámicos: delegación al servicio de parámetros del módulo ---

    @Override
    public List<ParameterValueDto> getActiveValuesByTopic(String topicCode) {
        return parameterService.getActiveValuesByTopic(topicCode);
    }

    @Override
    public Optional<ParameterValueDto> getValueByTopicAndCode(String topicCode, String valueCode) {
        return parameterService.getValueByTopicAndCode(topicCode, valueCode);
    }
}
