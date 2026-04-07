package com.example.demo.partition;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.util.CollectionUtils;

import com.example.demo.datasource.DataSourceProperties;
import com.example.demo.datasource.MemberRoutingDataSource;
import com.example.demo.domain.Reservation;

public class MemberPartitioner extends MultiResourcePartitioner {

	private final JdbcOperations jdbcOperations;

	private final MemberRoutingDataSource memberDataSource;

	public MemberPartitioner(JdbcOperations jdbcOperations, MemberRoutingDataSource memberDataSource) {
		this.jdbcOperations = jdbcOperations;
		this.memberDataSource = memberDataSource;
	}

	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
		String reservationSql = "SELECT name, file_name FROM reservation ORDER BY name";

		List<Reservation> reservationList = jdbcOperations.query(reservationSql,
				(rs, rowNum) -> (new Reservation(rs.getString("name"), rs.getString("file_name"))));
		List<String> nameList = reservationList.stream().map(Reservation::name).toList();

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

		Map<String, ExecutionContext> map = new HashMap<>();
		int index = 0;
		for (DataSourceProperties dataSourceProperties : dataSourcePropertiesList) {
			ExecutionContext executionContext = new ExecutionContext();
			executionContext.put("name", reservationList.get(index).name());
			executionContext.put("fileName", reservationList.get(index).fileName());

			map.put(dataSourceProperties.name(), executionContext);

			DataSource dataSource = DataSourceBuilder.create().driverClassName("org.postgresql.Driver")
					.url("jdbc:postgresql://" + dataSourceProperties.host() + ":5432/" + dataSourceProperties.name())
					.username(dataSourceProperties.userName()).password(dataSourceProperties.password()).build();
			this.memberDataSource.addDataSource(reservationList.get(index).name(), dataSource);

			index++;
		}

		return map;
	}

}
