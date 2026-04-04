package com.example.demo.datasource;

public class DataSourceKey {

	private static final ThreadLocal<String> KEY = new ThreadLocal<>();

	public static String getDataSourceKey() {
		return KEY.get();
	}

	public static void setDataSourceKey(String key) {
		KEY.set(key);
	}

	public static void clearDataSourceKey() {
		KEY.remove();
	}

}
