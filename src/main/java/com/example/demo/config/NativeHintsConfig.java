package com.example.demo.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

import com.example.demo.domain.FullNameMember;
import com.example.demo.domain.Member;
import com.example.demo.domain.Reservation;

@Configuration
@ImportRuntimeHints(NativeHintsConfig.BatchRuntimeHints.class)
public class NativeHintsConfig {

	static class BatchRuntimeHints implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
			// Register resources
			hints.resources().registerPattern("bon_jovi.csv");
			hints.resources().registerPattern("metallica.csv");
			hints.resources().registerPattern("iron_maiden.csv");
			hints.resources().registerPattern("schema.sql");
			hints.resources().registerPattern("application.properties");

			// Register domain classes for reflection
			hints.reflection().registerType(Member.class,
					MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
					MemberCategory.INVOKE_DECLARED_METHODS,
					MemberCategory.ACCESS_DECLARED_FIELDS);

			hints.reflection().registerType(FullNameMember.class,
					MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
					MemberCategory.INVOKE_DECLARED_METHODS,
					MemberCategory.ACCESS_DECLARED_FIELDS);

			hints.reflection().registerType(Reservation.class,
					MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
					MemberCategory.INVOKE_DECLARED_METHODS,
					MemberCategory.ACCESS_DECLARED_FIELDS);

			// Register JDBC drivers
			hints.reflection().registerType(org.h2.Driver.class,
					MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
			hints.reflection().registerType(org.postgresql.Driver.class,
					MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
		}
	}
}
