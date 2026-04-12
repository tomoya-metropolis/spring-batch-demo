package com.example.demo.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

import com.example.demo.config.BatchConfig.MemberMapper;
import com.example.demo.datasource.DataSourceKey;
import com.example.demo.datasource.DatabaseCredential;
import com.example.demo.datasource.MemberRoutingDataSource;
import com.example.demo.domain.FullNameMember;
import com.example.demo.domain.Member;
import com.example.demo.domain.Reservation;
import com.example.demo.listener.MemberStepExecutionListener;
import com.example.demo.partition.MemberPartitioner;

/**
 * RuntimeHints configuration for GraalVM native image compilation. Registers
 * resources, reflection, and proxies needed at runtime.
 */
@Configuration
@ImportRuntimeHints(NativeHintsConfig.AppRuntimeHints.class)
public class NativeHintsConfig {

	static class AppRuntimeHints implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
			// Register CSV resources
			hints.resources().registerPattern("*.csv");
			hints.resources().registerPattern("bon_jovi.csv");
			hints.resources().registerPattern("metallica.csv");
			hints.resources().registerPattern("iron_maiden.csv");

			// Register schema.sql
			hints.resources().registerPattern("schema.sql");

			// Register JDBC drivers for reflection (using TypeReference for runtimeOnly
			// dependencies)
			hints.reflection()
					.registerType(TypeReference.of("org.h2.Driver"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
							MemberCategory.INVOKE_PUBLIC_METHODS)
					.registerType(TypeReference.of("org.postgresql.Driver"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
							MemberCategory.INVOKE_PUBLIC_METHODS);

			// Register record classes for reflection (used in JDBC mapping and batch
			// processing)
			hints.reflection()
					.registerType(Member.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
							MemberCategory.INVOKE_PUBLIC_METHODS)
					.registerType(FullNameMember.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
							MemberCategory.INVOKE_PUBLIC_METHODS)
					.registerType(Reservation.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
							MemberCategory.INVOKE_PUBLIC_METHODS)
					.registerType(DatabaseCredential.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
							MemberCategory.INVOKE_PUBLIC_METHODS);

			// Register FieldSetMapper inner class for reflection
			hints.reflection().registerType(MemberMapper.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
					MemberCategory.INVOKE_PUBLIC_METHODS);

			hints.reflection().registerType(StepContext.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
					MemberCategory.INVOKE_PUBLIC_METHODS);

			// Register application classes for reflection
			hints.reflection()
					.registerType(MemberPartitioner.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
							MemberCategory.INVOKE_PUBLIC_METHODS)
					.registerType(MemberRoutingDataSource.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
							MemberCategory.INVOKE_PUBLIC_METHODS)
					.registerType(MemberStepExecutionListener.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
							MemberCategory.INVOKE_PUBLIC_METHODS)
					.registerType(DataSourceKey.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
							MemberCategory.INVOKE_PUBLIC_METHODS);

			// Register DataSource-related classes
			hints.reflection().registerType(javax.sql.DataSource.class, MemberCategory.INVOKE_PUBLIC_METHODS)
					.registerType(TypeReference.of("com.zaxxer.hikari.HikariDataSource"),
							MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		}
	}
}
