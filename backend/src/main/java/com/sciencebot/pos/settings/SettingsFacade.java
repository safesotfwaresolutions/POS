package com.sciencebot.pos.settings;

public interface SettingsFacade {
    SettingsDto getSettings();
    SettingsDto updateSettings(UpdateSettingsCommand command);
    void seedSettings(SettingsDto defaultSettings);
}
