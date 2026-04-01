package com.example.demo.partition;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.util.CollectionUtils;

import com.example.demo.domain.DataSourceProperties;
import com.example.demo.domain.Reservation;

public class MemberPartitioner implements Partitioner {

	private final JdbcOperations jdbcOperations;

	public MemberPartitioner(JdbcOperations jdbcOperations) {
		this.jdbcOperations = jdbcOperations;
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
		for (DataSourceProperties dataSourceProperties : dataSourcePropertiesList) {
			ExecutionContext executionContext = new ExecutionContext();
			executionContext.put("url",
					"jdbc:postgresql://" + dataSourceProperties.host() + ":5432/" + dataSourceProperties.name());
			executionContext.put("userName", dataSourceProperties.userName());
			executionContext.put("password", dataSourceProperties.passwword());
			executionContext.put("driverClassName", "org.postgresql.Driver");

			map.put(dataSourceProperties.name(), executionContext);
		}

		return map;
	}

}
