package com.example.demo;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.jdbc.core.JdbcOperations;

import com.example.demo.domain.FullNameMember;

public class MemberStepListener implements StepExecutionListener {

	private final JdbcOperations jdbcOperations;

	public MemberStepListener(JdbcOperations jdbcOperations) {
		this.jdbcOperations = jdbcOperations;
	}

	@Override
	public @Nullable ExitStatus afterStep(StepExecution stepExecution) {
		String sql = "SELECT id, first_name, last_name, full_name FROM member ORDER BY id";

		List<FullNameMember> memberList = jdbcOperations.query(sql, (rs, rowNum) -> (new FullNameMember(rs.getInt("id"),
				rs.getString("first_name"), rs.getString("last_name"), rs.getString("full_name"))));
		memberList.forEach(System.out::println);

		return StepExecutionListener.super.afterStep(stepExecution);
	}

}
