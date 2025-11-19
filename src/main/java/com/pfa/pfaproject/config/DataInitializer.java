package com.pfa.pfaproject.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.pfaproject.model.Admin;
import com.pfa.pfaproject.model.Country;
import com.pfa.pfaproject.model.enumeration.Role;
import com.pfa.pfaproject.repository.AdminRepository;
import com.pfa.pfaproject.repository.CountryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

//@Component
//public class DataInitializer implements CommandLineRunner {
//
//    private final AdminRepository adminRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    public DataInitializer(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
//        this.adminRepository = adminRepository;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        // Check if a SUPER_ADMIN already exists
//        if (adminRepository.findByRole(Role.SUPER_ADMIN).isEmpty()) {
//            Admin superAdmin = new Admin();
//            superAdmin.setUsername("superadmin");
//            superAdmin.setEmail("superadmin@example.com");
//            superAdmin.setPassword(passwordEncoder.encode("SuperSecret123"));
//            superAdmin.setRole(Role.SUPER_ADMIN);
//
//            adminRepository.save(superAdmin);
//            System.out.println("✅ Default SUPER_ADMIN account created!");
//        } else {
//            System.out.println("ℹ️ SUPER_ADMIN already exists — skipping creation.");
//        }
//    }
//}

@Component
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final CountryRepository countryRepository;

    @Value("${app.superadmin.username}")
    private String username;

    @Value("${app.superadmin.email}")
    private String email;

    @Value("${app.superadmin.password}")
    private String password;

    @Value("${app.superadmin.firstname}")
    private String firstname;

    @Value("${app.superadmin.lastname}")
    private String lastname;

    public DataInitializer(AdminRepository adminRepository,
                           PasswordEncoder passwordEncoder,
                           CountryRepository countryRepository) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.countryRepository = countryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1️⃣ Create SUPER_ADMIN if not exists
        if (adminRepository.findByRole(Role.SUPER_ADMIN).isEmpty()) {
            Admin superAdmin = new Admin();
            superAdmin.setFirstName(firstname);
            superAdmin.setLastName(lastname);
            superAdmin.setUsername(username);
            superAdmin.setEmail(email);
            superAdmin.setPassword(passwordEncoder.encode(password));
            superAdmin.setRole(Role.SUPER_ADMIN);
            adminRepository.save(superAdmin);
            System.out.println("✅ Default SUPER_ADMIN created!");
        }

//        // 2️⃣ Initialize African countries if not exists
//        if (countryRepository.count() == 0) {
//            ObjectMapper mapper = new ObjectMapper();
//
//            // Read JSON directly into List<Country>
//            InputStream inputStream = DataInitializer.class.getResourceAsStream("/data/african_countries.json");
//            List<Country> countries = mapper.readValue(inputStream, new TypeReference<List<Country>>() {});
//
//            // If you’re using Lombok builder
//            List<Country> countryEntities = countries.stream()
//                    .map(c -> Country.builder()
//                            .name(c.getName())
//                            .code(c.getCode())
//                            .region(c.getRegion())
//                            .build())
//                    .collect(Collectors.toList());
//
//            countryRepository.saveAll(countryEntities);
//            System.out.println("✅ African countries initialized!");
//        }

        ObjectMapper mapper = new ObjectMapper();

// Read JSON directly into List<Country>
        InputStream inputStream = DataInitializer.class.getResourceAsStream("/data/african_countries.json");
        List<Country> countries = mapper.readValue(inputStream, new TypeReference<List<Country>>() {});

// Filter out countries that already exist (assuming 'code' is unique)
        List<Country> countryEntities = countries.stream()
                .filter(c -> countryRepository.findByCode(c.getCode()).isEmpty()) // <- only add missing countries
                .map(c -> Country.builder()
                        .name(c.getName())
                        .code(c.getCode())
                        .region(c.getRegion())
                        .build())
                .collect(Collectors.toList());

        if (!countryEntities.isEmpty()) {
            countryRepository.saveAll(countryEntities);
            System.out.println("✅ African countries initialized!");
        } else {
            System.out.println("ℹ️ African countries already exist — skipping initialization.");
        }
    }
}


