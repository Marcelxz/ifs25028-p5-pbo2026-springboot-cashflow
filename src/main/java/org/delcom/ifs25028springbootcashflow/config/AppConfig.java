package org.delcom.ifs25028springbootcashflow.config;

import javax.sql.DataSource;
import org.delcom.ifs25028springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25028springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom.ifs25028springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom.ifs25028springbootcashflow.adapter.repository.SchemaInitializer;
import org.delcom.ifs25028springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25028springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25028springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25028springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25028springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25028springbootcashflow.usecase.UserUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Composition root: seluruh wiring bean ada di sini. */
@Configuration
public class AppConfig {

    @Bean
    public IUserRepository userRepository(DataSource dataSource) {
        SchemaInitializer.initialize(dataSource);
        return new JdbcUserRepository(dataSource);
    }

    @Bean
    public ITransactionRepository transactionRepository(DataSource dataSource) {
        return new JdbcTransactionRepository(dataSource);
    }

    @Bean
    public AuthUseCase authUseCase(IUserRepository userRepository) {
        return new AuthUseCase(userRepository);
    }

    @Bean
    public FinanceUseCase financeUseCase(ITransactionRepository transactionRepository) {
        return new FinanceUseCase(transactionRepository);
    }

    @Bean
    public UserUseCase userUseCase(IUserRepository userRepository) {
        return new UserUseCase(userRepository);
    }

    @Bean
    public FinancePresenter financePresenter() {
        return new FinancePresenter();
    }

    @Bean
    public FileStorage fileStorage(@Value("${app.upload-dir}") String uploadDir) {
        return new FileStorage(uploadDir);
    }

    @Bean
    public ApplicationRunner seedRootAdmin(
            AuthUseCase authUseCase,
            @Value("${app.root-username}") String rootUsername,
            @Value("${app.root-password}") String rootPassword) {
        return args -> authUseCase.ensureRootAdmin(rootUsername, rootPassword);
    }

    @Bean
    public WebMvcConfigurer uploadResourceConfigurer(FileStorage fileStorage) {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // pastikan lokasi berakhir dengan tepat satu "/"
                String location = fileStorage.getRoot().toUri().toString().replaceFirst("/*$", "/");
                registry.addResourceHandler("/uploads/**").addResourceLocations(location);
            }
        };
    }
}