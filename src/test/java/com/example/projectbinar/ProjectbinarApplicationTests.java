package com.example.projectbinar;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Disabled because it requires full infrastructure (DB, Redis). Use unit tests instead.")
class ProjectbinarApplicationTests {

  @Test
  void contextLoads() {}
}
