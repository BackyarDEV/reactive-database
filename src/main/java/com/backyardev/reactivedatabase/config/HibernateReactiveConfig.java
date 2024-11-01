package com.backyardev.reactivedatabase.config;

import jakarta.persistence.Persistence;
import org.hibernate.reactive.mutiny.Mutiny;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.HashMap;

import static org.hibernate.cfg.JdbcSettings.*;

@Configuration(proxyBeanMethods = false)
public class HibernateReactiveConfig {
    static final String DEFAULT_PERSISTENCE_UNIT_NAME = "backyardPU";

    @Autowired
    Environment environment;

    @Bean
    public Mutiny.SessionFactory sessionFactory() {
        var properties = new HashMap<String,String>();
        properties.put(JAKARTA_JDBC_URL, environment.getProperty(JAKARTA_JDBC_URL));
        properties.put(JAKARTA_JDBC_USER, environment.getProperty(JAKARTA_JDBC_USER));
        properties.put(JAKARTA_JDBC_PASSWORD, environment.getProperty(JAKARTA_JDBC_PASSWORD));
        properties.put(POOL_SIZE, environment.getProperty(POOL_SIZE));
        properties.put(SHOW_SQL, environment.getProperty(SHOW_SQL));
        properties.put(FORMAT_SQL, environment.getProperty(FORMAT_SQL));
        properties.put(HIGHLIGHT_SQL, environment.getProperty(HIGHLIGHT_SQL));
        return Persistence.createEntityManagerFactory(DEFAULT_PERSISTENCE_UNIT_NAME, properties)
            .unwrap(Mutiny.SessionFactory.class);
    }
}
