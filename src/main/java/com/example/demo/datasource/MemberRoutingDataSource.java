package com.example.demo.datasource;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class MemberRoutingDataSource extends AbstractRoutingDataSource {

	private final Map<String, DataSource> dataSourceMap = new HashMap<>();

	@Override
	public void initialize() {
		// no op
	}

	@Override
	public DataSource determineTargetDataSource() {
		String key = (String) determineCurrentLookupKey();
		DataSource dataSource = this.dataSourceMap.get(key);
		if (dataSource == null) {
			throw new IllegalStateException("No DataSource found for key: " + key);
		}

		return dataSource;
	}

	public void addDataSource(String key, DataSource dataSource) {
		this.dataSourceMap.put(key, dataSource);
	}

	@Override
	protected @Nullable Object determineCurrentLookupKey() {
		return DataSourceKey.getDataSourceKey();
	}

}
