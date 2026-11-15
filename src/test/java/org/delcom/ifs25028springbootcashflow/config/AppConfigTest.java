package org.delcom.ifs25028springbootcashflow.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import javax.sql.DataSource;
import org.delcom.ifs25028springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25028springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25028springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25028springbootcashflow.support.TestDb;
import org.delcom.ifs25028springbootcashflow.usecase.AuthUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

class AppConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void semuaBeanDapatDibuat() throws Exception {
        AppConfig config = new AppConfig();
        DataSource ds = TestDb.newDataSource();

        IUserRepository users = config.userRepository(ds);
        ITransactionRepository transactions = config.transactionRepository(ds);
        AuthUseCase auth = config.authUseCase(users);

        assertNotNull(config.financeUseCase(transactions));
        assertNotNull(config.userUseCase(users));
        assertNotNull(config.financePresenter());

        // seed admin dijalankan dua kali: kedua kali tidak boleh error
        config.seedRootAdmin(auth, "root", "rahasia123").run(null);
        config.seedRootAdmin(auth, "root", "rahasia123").run(null);
        assertTrue(users.findByUsername("root").isPresent());
    }

    @Test
    void resourceHandlerUploadTerdaftar() {
        AppConfig config = new AppConfig();
        FileStorage storage = config.fileStorage(tempDir.resolve("uploads").toString());
        WebMvcConfigurer configurer = config.uploadResourceConfigurer(storage);

        ResourceHandlerRegistry registry = new ResourceHandlerRegistry(
                new GenericWebApplicationContext(new MockServletContext()), new MockServletContext());
        configurer.addResourceHandlers(registry);

        assertTrue(registry.hasMappingForPattern("/uploads/**"));
    }
}