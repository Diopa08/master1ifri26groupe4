package com.sfmc.auth_service.config;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sfmc.auth_service.entity.Role;
import com.sfmc.auth_service.entity.User;
import com.sfmc.auth_service.repository.RoleRepository;
import com.sfmc.auth_service.repository.UserRepository;

@Configuration
public class DataInitializer {

	@Bean
	CommandLineRunner initData(
	        RoleRepository roleRepository,
	        UserRepository userRepository,
	        PasswordEncoder passwordEncoder
	) {

	    return args -> {

	        // Création des rôles
	        if (roleRepository.findByName("ADMIN").isEmpty()) {

	            Role adminRole = roleRepository.save(new Role("ADMIN"));
	            Role clientRole = roleRepository.save(new Role("CLIENT"));
	            Role operatorRole = roleRepository.save(new Role("OPERATOR"));

	            System.out.println("Roles created");
	        }

	        // Création admin
	        if (userRepository.findByUsername("admin").isEmpty()) {

	            Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();

	            User admin = new User();
	            admin.setUsername("admin");
	            admin.setPassword(passwordEncoder.encode("admin123"));
	            admin.setEmail("admin@test.com");
	            admin.setRoles(Set.of(adminRole));

	            userRepository.save(admin);

	            System.out.println("Admin created");
	        }
	    };
}
}