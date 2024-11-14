package com.backyardev.reactivedatabase.config;

import jakarta.persistence.Persistence;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.reactive.mutiny.Mutiny;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
@Slf4j
public class HibernateReactiveConfig {
    static final String DEFAULT_PERSISTENCE_UNIT_NAME = "backyardPU";

    @Autowired
    Environment environment;

    @Bean
    public Mutiny.SessionFactory sessionFactory() {
        return Persistence.createEntityManagerFactory(DEFAULT_PERSISTENCE_UNIT_NAME, getProperties())
            .unwrap(Mutiny.SessionFactory.class);
    }

    private Map<String, String> getProperties() {
        var properties = new HashMap<String, String>();
        for (var source : ((AbstractEnvironment) environment).getPropertySources())
            if (source.getName().contains("Config resource"))
                properties.putAll(((MapPropertySource) source).getSource().entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey, this::getEnvValues)));
        return properties;
    }

    private String getEnvValues(Map.Entry entry) {
        return environment.getProperty(String.valueOf(entry.getKey()));
    }
}
