package ru.marattim.todolist.tests_infrastructure;

import java.util.Properties;
import javax.sql.DataSource;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@Profile("test")
@TestConfiguration
public class EmbeddedPostgresConfig {

    @Bean
    public DataSource dataSource() {
        try {
            EmbeddedPostgres.Builder builder = EmbeddedPostgres.builder();
            EmbeddedPostgres embeddedPostgres = builder.start();

            builder.setConnectConfig("currentSchema", "public");

            DataSource postgresDatabase = embeddedPostgres.getPostgresDatabase();

            return postgresDatabase;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Bean
    public Properties hibernateProperties() {
        Properties hibernateProp = new Properties();
        hibernateProp.put("hibernate.hbm2ddl.auto", "validate");
        hibernateProp.put("hibernate.format sql", true);
        hibernateProp.put("hibernate.use sql comments", true);
        hibernateProp.put("hibernate.show_sql", true);
        return hibernateProp;
    }

}
