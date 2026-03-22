package com.example.demo;

import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.jdbc.core.JdbcOperations;

public class MemberStepListener implements StepExecutionListener {

	private final JdbcOperations jdbcOperations;

	public MemberStepListener(JdbcOperations jdbcOperations) {
		this.jdbcOperations = jdbcOperations;
	}

	@Override
	public @Nullable ExitStatus afterStep(StepExecution stepExecution) {
		String sql = "SELECT count(*) FROM member";

		int result = jdbcOperations.queryForObject(sql, Integer.class);
		System.out.println("result = " + result);

		return StepExecutionListener.super.afterStep(stepExecution);
	}

}
