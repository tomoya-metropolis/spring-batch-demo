package com.example.demo.config;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.demo.datasource.MemberRoutingDataSource;

@Configuration
public class DataSourceConfig {

	@Bean("dataSource")
	@Primary
	@ConfigurationProperties("spring.datasource")
	DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean("memberRoutingDataSource")
	MemberRoutingDataSource memberRoutingDataSource() {
		return new MemberRoutingDataSource();
	}

	@Bean("memberDataSource")
	@StepScope
	DataSource memberDataSource(MemberRoutingDataSource memberRoutingDataSource) {
		return memberRoutingDataSource.determineTargetDataSource();
	}

	@Bean("transactionManager")
	@Primary
	PlatformTransactionManager transactionManager() {
		return new DataSourceTransactionManager(dataSource());
	}

	@Bean("memberTransactionManager")
	PlatformTransactionManager memberTransactionManager(MemberRoutingDataSource memberRoutingDataSource) {
		return new DataSourceTransactionManager(memberDataSource(memberRoutingDataSource));
	}

}
