package com.library.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LibraryManagementApplication {
    public static void main(String[] args) {
//        default:
//        SpringApplication.run(LibraryManagementApplication.class, args);
        SpringApplication app = new SpringApplication(LibraryManagementApplication.class);
        app.setAdditionalProfiles("h2"); // or "postgres"
        app.run(args);
    }
}

