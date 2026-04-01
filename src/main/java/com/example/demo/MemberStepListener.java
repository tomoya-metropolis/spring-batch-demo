package com.example.demo;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.util.CollectionUtils;

import com.example.demo.datasource.MemberDataSource;
import com.example.demo.domain.DataSourceProperties;
import com.example.demo.domain.FullNameMember;
import com.example.demo.domain.Reservation;

public class MemberStepListener implements StepExecutionListener {

	private final MemberDataSource memberDataSource;

	private final JdbcOperations jdbcOperations;

	private final JdbcOperations businessJdbcOperations;

	public MemberStepListener(JdbcOperations jdbcOperations, JdbcOperations businessJdbcOperations) {
		this.memberDataSource = new MemberDataSource();
		this.jdbcOperations = jdbcOperations;
		this.businessJdbcOperations = businessJdbcOperations;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		String reservationSql = "SELECT name, file_name FROM reservation ORDER BY name";

		List<Reservation> preserveList = jdbcOperations.query(reservationSql,
				(rs, rowNum) -> (new Reservation(rs.getString("name"), rs.getString("file_name"))));
		List<String> nameList = preserveList.stream().map(Reservation::name).toList();

		StringBuilder whereClause = new StringBuilder();
		if (!CollectionUtils.isEmpty(nameList)) {
			whereClause.append("WHERE name IN (");
			whereClause.append(String.join(", ", Collections.nCopies(nameList.size(), "?")));
			whereClause.append(") ");
		}
		StringBuilder dataSourceSql = new StringBuilder("SELECT name, host, user_name, password FROM database ");
		dataSourceSql.append(whereClause);

		List<DataSourceProperties> dataSourcePropertiesList = jdbcOperations.query(
				dataSourceSql.toString(), (rs, rowNum) -> (new DataSourceProperties(rs.getString("name"),
						rs.getString("host"), rs.getString("user_name"), rs.getString("password"))),
				nameList.toArray());
		Map<Object, Object> dataSourceMap = dataSourcePropertiesList.stream()
				.collect(Collectors.toMap(DataSourceProperties::name,
						properties -> DataSourceBuilder.create().driverClassName("org.postgresql.Driver")
								.url("jdbc:postgresql://" + properties.host() + ":5432/" + properties.name())
								.username(properties.userName()).password(properties.passwword()).build()));
		memberDataSource.setTargetDataSources(dataSourceMap);
	}

	@Override
	public @Nullable ExitStatus afterStep(StepExecution stepExecution) {
		String sql = "SELECT id, first_name, last_name, full_name FROM member ORDER BY id";

		List<FullNameMember> memberList = businessJdbcOperations.query(sql,
				(rs, rowNum) -> (new FullNameMember(rs.getInt("id"), rs.getString("first_name"),
						rs.getString("last_name"), rs.getString("full_name"))));
		memberList.forEach(System.out::println);

		return StepExecutionListener.super.afterStep(stepExecution);
	}

}
