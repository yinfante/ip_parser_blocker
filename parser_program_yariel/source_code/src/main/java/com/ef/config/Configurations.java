package com.ef.config;

import com.zaxxer.hikari.HikariDataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Configuration class
 *
 * @author yinfante
 */
@Configuration
@PropertySource("classpath:/application.properties")
public class Configurations {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String userName;

    @Value("${spring.datasource.password}")
    private String password;

    @Autowired
    private Environment env;

    /**
     * Creation of data source. It reads url, username and password from our application.properties
     *
     * @return an instance of DataSource
     * @see DataSource
     */
    @Bean(destroyMethod = "close")
    public DataSource dataSource() {
        final HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(env.getProperty("datasource.url"));
        ds.setUsername(env.getProperty("datasource.username"));
        ds.setPassword(env.getProperty("datasource.password"));
        return ds;
    }

    /**
     * Creation of JdbcTemplate. It simplifies the use of JDBC and helps to avoid common errors.
     *
     * @param dataSource an instance of our DataSource needed for the creation.
     * @return an instance of JdbcTemplate
     * @see JdbcTemplate
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }


    /**
     * Creation of SpringLiquibase. It loads a .sql file to run.
     *
     * @param dataSource an instance of our DataSource needed for the creation.
     * @return an instance of SpringLiquibase
     * @see SpringLiquibase
     */
    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase sl = new SpringLiquibase();
        sl.setDataSource(dataSource);
        sl.setChangeLog("classpath:changelog/changelog-master.sql");
        return sl;
    }
}

