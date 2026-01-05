package com.example.projectbinar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ProjectbinarApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProjectbinarApplication.class, args);
  }
}
