package com.sciencebot.pos.users.internal.services;

import com.sciencebot.pos.users.CreateUserCommand;
import com.sciencebot.pos.users.UserFacade;
import com.sciencebot.pos.users.internal.repositories.UserRepository;
import com.sciencebot.pos.customers.CustomerFacade;
import com.sciencebot.pos.customers.CreateCustomerCommand;
import com.sciencebot.pos.settings.SettingsFacade;
import com.sciencebot.pos.settings.SettingsDto;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserFacade userFacade;
    private final CustomerFacade customerFacade;
    private final SettingsFacade settingsFacade;

    public DatabaseSeeder(
            UserRepository userRepository,
            UserFacade userFacade,
            CustomerFacade customerFacade,
            SettingsFacade settingsFacade) {
        this.userRepository = userRepository;
        this.userFacade = userFacade;
        this.customerFacade = customerFacade;
        this.settingsFacade = settingsFacade;
    }

    @Override
    public void run(String... args) {
        // Seed default administrator
        if (userRepository.count() == 0) {
            CreateUserCommand adminCommand = new CreateUserCommand(
                    "Administrator",
                    "admin",
                    "admin@tienda.com",
                    "Password123",
                    "ADMINISTRATOR"
            );
            userFacade.createUser(adminCommand);
            System.out.println("==================================================");
            System.out.println("Default administrator seeded: admin / Password123");
            System.out.println("==================================================");
        }

        // Seed General Customer (RN-CUST-001)
        if (customerFacade.getByIdentification("9999999999").isEmpty()) {
            customerFacade.createCustomer(new CreateCustomerCommand(
                    "Cliente General",
                    "9999999999",
                    null, null, null
            ));
            System.out.println("==================================================");
            System.out.println("General Customer seeded: 9999999999");
            System.out.println("==================================================");
        }

        // Seed default business settings (RN-SET-001)
        try {
            settingsFacade.getSettings();
        } catch (Exception e) {
            settingsFacade.seedSettings(new SettingsDto(
                    "Mi Tienda",
                    "Calle Principal 123",
                    "555-0001",
                    "900111222-3",
                    "info@mitienda.com",
                    null
            ));
            System.out.println("==================================================");
            System.out.println("Default settings seeded");
            System.out.println("==================================================");
        }
    }
}

