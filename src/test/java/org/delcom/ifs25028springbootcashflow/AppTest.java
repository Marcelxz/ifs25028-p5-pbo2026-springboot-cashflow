package org.delcom.ifs25028springbootcashflow;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AppTest {

    @Test
    void contextLoads() {
    }

    @Test
    void mainDapatDijalankan() throws Exception {
        assertNotNull(new App());
        // tanpa web server dan tanpa auto-config Vaadin (butuh WebApplicationContext),
        // supaya tidak ada frontend build / port yang terbuka saat tes
        App.main(new String[]{
                "--spring.profiles.active=test",
                "--spring.main.web-application-type=none",
                "--spring.autoconfigure.exclude=com.vaadin.flow.spring.SpringBootAutoConfiguration",
                "--spring.devtools.restart.enabled=false",
                "--spring.devtools.livereload.enabled=false"
        });
    }
}