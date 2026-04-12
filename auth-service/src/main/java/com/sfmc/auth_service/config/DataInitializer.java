package com.sfmc.auth_service.config;

import com.sfmc.auth_service.entity.Role;
import com.sfmc.auth_service.repository.RoleRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        // Créer les rôles s'ils n'existent pas encore
        for (String roleName : new String[]{"ROLE_USER", "ROLE_ADMIN", "ROLE_OPERATOR"}) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(new Role(roleName));
                System.out.println("Rôle créé : " + roleName);
            }
        }
    }
}