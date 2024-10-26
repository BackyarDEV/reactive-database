//package com.backyardev.reactivedatabase.config;
//
//import io.r2dbc.spi.ConnectionFactories;
//import io.r2dbc.spi.ConnectionFactory;
//import io.r2dbc.spi.ConnectionFactoryOptions;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
//import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
//
//@Configuration
//@EnableR2dbcRepositories
//public class DatabaseConfig extends AbstractR2dbcConfiguration {
//
//    @Autowired
//    R2dbcProperties r2dbcProperties;
//
//    @Override
//    @Bean
//    public ConnectionFactory connectionFactory() {
//        ConnectionFactoryOptions connectionFactoryOptions = ConnectionFactoryOptions.parse(r2dbcProperties.getUrl());
//        return ConnectionFactories.get(connectionFactoryOptions.mutate()
//                .option(ConnectionFactoryOptions.USER, r2dbcProperties.getUsername())
//                .option(ConnectionFactoryOptions.PASSWORD, r2dbcProperties.getPassword())
//                .option(MssqlConnectionFactoryProvider.PREFER_CURSORED_EXECUTION, false)
//                .option(PoolingConnectionFactoryProvider.PRE_RELEASE  , conn ->
//                        Mono.error(new Exception("Force Connection release.")))
//                .build());
//    }
//}
