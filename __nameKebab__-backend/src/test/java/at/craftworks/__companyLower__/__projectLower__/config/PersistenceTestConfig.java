package at.craftworks.__companyLower__.__projectLower__.config;

import at.craftworks.__companyLower__.__projectLower__.model.AbstractDomainEntity;
import org.flywaydb.core.Flyway;
import org.hibernate.cfg.AvailableSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.persistence.ValidationMode;
import javax.sql.DataSource;
import java.util.Properties;


@Configuration
@EnableJpaRepositories("at.craftworks.__companyLower__.__projectLower__.repository")
@EnableTransactionManagement
@EnableJpaAuditing
@Profile("test")
@Primary
public class PersistenceTestConfig {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Bean
    PlatformTransactionManager transactionManager() {
        return new JpaTransactionManager(entityManagerFactory().getObject());
    }

    @Bean
    LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setGenerateDdl(false);
        // debugging jpa queries
        // jpaVendorAdapter.setShowSql(true);
        jpaVendorAdapter.setDatabasePlatform(org.hibernate.dialect.PostgreSQL10Dialect.class.getName());


        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();

        factoryBean.setDataSource(dataSource());
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        factoryBean.setPackagesToScan(AbstractDomainEntity.class.getPackage().getName());
        factoryBean.setValidationMode(ValidationMode.AUTO);
        factoryBean.setJpaProperties(additionalProperties());

        return factoryBean;
    }


    private Properties additionalProperties() {
        Properties properties = new Properties();
        properties.setProperty("spring.jpa.hibernate.ddl-auto", "validate");
        properties.setProperty(AvailableSettings.HBM2DDL_AUTO, "validate");

        // setting non_contextual_creation to true to avoid logged error message:
        // "Method org.postgresql.jdbc.PgConnection.createClob() is not yet implemented."
        properties.setProperty(AvailableSettings.NON_CONTEXTUAL_LOB_CREATION, "true");

        return properties;
    }

    @Bean
    @FlywayDataSource
    public DataSource dataSource() {
        PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:13")
                .withDatabaseName("__nameLower__")
                .withPassword("__nameLower__")
                .withUsername("__nameLower__");

        postgres.start();

        logger.info("Connecting to test database");

        DriverManagerDataSource source = new DriverManagerDataSource();

        source.setDriverClassName(org.postgresql.Driver.class.getName());
        source.setUrl(postgres.getJdbcUrl());
        source.setUsername(postgres.getUsername());
        source.setPassword(postgres.getPassword());

        return source;
    }


    @Bean(initMethod = "migrate")
    protected Flyway flyway() {
        return Flyway
                .configure()
                .dataSource(dataSource())
                .load();
    }

    /*
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareService();
    }
    */
}
