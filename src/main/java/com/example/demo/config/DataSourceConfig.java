package com.example.demo.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.demo.datasource.MemberDataSource;

@Configuration
public class DataSourceConfig {

	@Bean("dataSource")
	@Primary
	@ConfigurationProperties("spring.datasource")
	DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean("memberDataSource")
	MemberDataSource memberDataSource() {
		return new MemberDataSource();
	}

	@Bean("transactionManaber")
	PlatformTransactionManager transactionManager() {
		return new DataSourceTransactionManager(dataSource());
	}

}
