package com.example.demo.listener;

import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;

import com.example.demo.datasource.DataSourceKey;

public class MemberStepExecutionListener implements StepExecutionListener {

	@Override
	public void beforeStep(StepExecution stepExecution) {
		ExecutionContext executionContext = stepExecution.getExecutionContext();
		String name = executionContext.getString("name");

		DataSourceKey.setDataSourceKey(name);
	}

	@Override
	public @Nullable ExitStatus afterStep(StepExecution stepExecution) {
		DataSourceKey.clearDataSourceKey();

		return ExitStatus.COMPLETED;
	}

}
