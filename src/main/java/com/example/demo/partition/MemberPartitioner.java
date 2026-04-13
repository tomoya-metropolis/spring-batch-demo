package com.example.demo.partition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.boot.jdbc.DataSourceBuilder;

import com.example.demo.datasource.DatabaseCredential;
import com.example.demo.datasource.MemberRoutingDataSource;
import com.example.demo.domain.Reservation;

import jakarta.persistence.EntityManager;

public class MemberPartitioner extends MultiResourcePartitioner {

	private final EntityManager entityManager;

	private final MemberRoutingDataSource memberDataSource;

	public MemberPartitioner(EntityManager entityManager, MemberRoutingDataSource memberDataSource) {
		this.entityManager = entityManager;
		this.memberDataSource = memberDataSource;
	}

	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
		List<Reservation> reservationList = this.entityManager
			.createQuery("SELECT r FROM reservation r ORDER BY name", Reservation.class)
			.getResultList();

		List<String> nameList = reservationList.stream()
			.map(Reservation::getName)
			.toList();

		List<DatabaseCredential> dataSourcePropertiesList = this.entityManager
			.createQuery("SELECT d FROM DatabaseCredential d WHERE d.name IN :nameList", DatabaseCredential.class)
			.setParameter("nameList", nameList)
			.getResultList();

		Map<String, ExecutionContext> map = new HashMap<>();
		int index = 0;
		for (DatabaseCredential dataSourceProperties : dataSourcePropertiesList) {
			ExecutionContext executionContext = new ExecutionContext();
			executionContext.put("name", reservationList.get(index)
				.getName());
			executionContext.put("fileName", reservationList.get(index)
				.getFileName());

			map.put(dataSourceProperties.getName(), executionContext);

			DataSource dataSource = DataSourceBuilder.create()
				.driverClassName("org.postgresql.Driver")
				.url("jdbc:postgresql://" + dataSourceProperties.getHost() + ":5432/" + dataSourceProperties.getName())
				.username(dataSourceProperties.getUserName())
				.password(dataSourceProperties.getPassword())
				.build();
			this.memberDataSource.addDataSource(reservationList.get(index)
				.getName(), dataSource);

			index++ ;
		}

		return map;
	}

}
