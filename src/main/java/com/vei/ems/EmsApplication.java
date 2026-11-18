package com.vei.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * Entry point of the Employee Management System web application.
 * The default user service is excluded because we do our own login with JWT tokens.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class EmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmsApplication.class, args);
    }
}
