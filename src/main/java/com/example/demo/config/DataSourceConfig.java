package com.example.demo.config;

import javax.sql.DataSource;

import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import com.example.demo.datasource.MemberRoutingDataSource;

import jakarta.persistence.EntityManagerFactory;

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

	@Bean("entityManagerFactory")
	@Primary
	LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean(
			@Qualifier("dataSource") DataSource dataSource) {
		LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
		factoryBean.setPackagesToScan("com.example.demo.domain", "com.example.demo.datasource");
		factoryBean.setPersistenceProvider(new HibernatePersistenceProvider());
		factoryBean.setDataSource(dataSource);

		return factoryBean;
	}

	@Bean("memberDataSource")
	@StepScope
	DataSource memberDataSource(MemberRoutingDataSource memberRoutingDataSource) {
		return memberRoutingDataSource.determineTargetDataSource();
	}

	@Bean("memberEntityManagerFactory")
	@StepScope
	LocalContainerEntityManagerFactoryBean memberLocalContainerEntityManagerFactoryBean(
			MemberRoutingDataSource memberRoutingDataSource) {
		LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
		factoryBean.setPackagesToScan("com.example.demo.domain", "com.example.demo.datasource");
		factoryBean.setPersistenceProvider(new HibernatePersistenceProvider());
		factoryBean.setDataSource(memberDataSource(memberRoutingDataSource));

		return factoryBean;
	}

	@Bean("jpaTransactionManager")
	JpaTransactionManager jpaTransactionManager(
			@Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}

	@Bean("memberJpaTransactionManager")
	JpaTransactionManager memberJpaTransactionManager(
			@Qualifier("memberEntityManagerFactory") EntityManagerFactory memberEtityManagerFactory) {
		return new JpaTransactionManager(memberEtityManagerFactory);
	}

}
