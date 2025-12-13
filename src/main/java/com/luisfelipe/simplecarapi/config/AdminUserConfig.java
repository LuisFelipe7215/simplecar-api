package com.luisfelipe.simplecarapi.config;

import com.luisfelipe.simplecarapi.domain.User;
import com.luisfelipe.simplecarapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class AdminUserConfig implements CommandLineRunner {
    private final UserRepository repository;
    private final PasswordEncoder encoder;
    @Value("${admin.username}")
    private String adminUsername;
    @Value("${admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (repository.findByUsername(adminUsername).isEmpty()) {
            User adminToSave = newAdminToSave();
            repository.save(adminToSave);
            log.info("-------------------------");
            log.info("ADMIN SUCCESSFUL CREATED!");
            log.info("User: '{}'", adminUsername);
            log.info("-------------------------");
        } else {
            log.info("Admin already exists, so creation is skipped.");
        }
    }

    public User newAdminToSave() {
        return User.builder()
                .username(adminUsername)
                .password(encoder.encode(adminPassword))
                .roles("ADMIN")
                .build();
    }


}
