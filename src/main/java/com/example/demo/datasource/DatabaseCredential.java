package com.example.demo.datasource;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity(name="DatabaseCredential")
@Table(name = "database_credential")
public class DatabaseCredential {

	@Id
	@Column(name = "name")
	private String name;

	@Column(name = "host")
	private String host;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "password")
	private String password;

	public DatabaseCredential() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
