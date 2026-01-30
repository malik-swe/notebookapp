package com.example.notebookapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NotebookappApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotebookappApplication.class, args);
    }

}
