package com.neocamp.api_futebol;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

class ApiDePartidasDeFutebolApplicationTests {

    @Test
    void mainRuns() {
        System.setProperty("spring.profiles.active", "test");
        ApiDePartidasDeFutebolApplication.main(new String[] {});
    }

}
