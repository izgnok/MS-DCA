package dca.backend.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "dca.backend.moviedb.repository",   // ✅ moviedb repo
        entityManagerFactoryRef = "movieEntityManagerFactory",
        transactionManagerRef = "movieTransactionManager"
)
public class MovieDbConfig {

    @Bean(name = "movieDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.moviedb")
    public HikariDataSource movieDataSource() {
        return new HikariDataSource();
    }

    @Bean(name = "movieEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean movieEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("movieDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("dca.backend.moviedb.entity") // ✅ moviedb entity 패키지
                .persistenceUnit("moviedb")
                .properties(Map.of("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect"))
                .build();
    }

    @Bean(name = "movieTransactionManager")
    public PlatformTransactionManager movieTransactionManager(
            @Qualifier("movieEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}