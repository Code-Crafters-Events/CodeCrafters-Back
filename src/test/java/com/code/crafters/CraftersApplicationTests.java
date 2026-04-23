package com.code.crafters;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("CraftersApplication Context Tests")
class CraftersApplicationTest {

    @Test
    void contextLoads() {
    }
}
