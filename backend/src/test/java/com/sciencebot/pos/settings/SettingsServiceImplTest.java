package com.sciencebot.pos.settings;

import com.sciencebot.pos.settings.internal.entities.Setting;
import com.sciencebot.pos.settings.internal.repositories.SettingsRepository;
import com.sciencebot.pos.settings.internal.services.SettingsServiceImpl;
import com.sciencebot.pos.settings.internal.mappers.SettingsMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SettingsServiceImplTest {

    @Mock
    private SettingsRepository settingsRepository;

    @Mock
    private SettingsMapper settingsMapper;

    @InjectMocks
    private SettingsServiceImpl settingsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getSettings_Success() {
        Setting setting = new Setting();
        setting.setId(1);
        setting.setBusinessName("Mi Tienda");
        setting.setAddress("Calle Principal 123");
        setting.setPhone("555-0001");
        when(settingsRepository.findById(1)).thenReturn(Optional.of(setting));

        SettingsDto expectedDto = new SettingsDto("Mi Tienda", "Calle Principal 123", "555-0001", "900111222-3", "info@mitienda.com", null);
        when(settingsMapper.toDto(any(Setting.class))).thenReturn(expectedDto);

        SettingsDto result = settingsService.getSettings();

        assertNotNull(result);
        assertEquals("Mi Tienda", result.businessName());
        verify(settingsRepository, times(1)).findById(1);
    }

    @Test
    void updateSettings_Success() {
        Setting setting = new Setting();
        setting.setId(1);
        setting.setBusinessName("Mi Tienda");
        setting.setAddress("Calle Principal 123");
        setting.setPhone("555-0001");
        when(settingsRepository.findById(1)).thenReturn(Optional.of(setting));

        UpdateSettingsCommand command = new UpdateSettingsCommand("Nuevo Nombre", "Nueva Calle", "555-0002", "1-2", "a@a.com", null);
        
        Setting saved = new Setting();
        saved.setId(1);
        saved.setBusinessName("Nuevo Nombre");
        when(settingsRepository.save(any(Setting.class))).thenReturn(saved);

        SettingsDto expectedDto = new SettingsDto("Nuevo Nombre", "Nueva Calle", "555-0002", "1-2", "a@a.com", null);
        when(settingsMapper.toDto(any(Setting.class))).thenReturn(expectedDto);

        SettingsDto result = settingsService.updateSettings(command);

        assertNotNull(result);
        assertEquals("Nuevo Nombre", result.businessName());
        verify(settingsRepository, times(1)).save(any(Setting.class));
    }
}
