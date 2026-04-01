package com.example.demo.datasource;

import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class MemberDataSource extends AbstractRoutingDataSource {

	@Override
	public void initialize() {
		// no op
	}

	@Override
	protected @Nullable Object determineCurrentLookupKey() {
		return DataSourceKey.getDataSourceKey();
	}

}
