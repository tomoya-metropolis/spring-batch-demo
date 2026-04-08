package com.example.demo.listener;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;

import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import io.prometheus.metrics.exporter.pushgateway.PushGateway;

public class MetricsPushJobListener implements JobExecutionListener {

	private static final Logger log = LoggerFactory.getLogger(MetricsPushJobListener.class);

	private final PrometheusMeterRegistry registry;

	private final String pushgatewayUrl;

	private final String jobName;

	public MetricsPushJobListener(PrometheusMeterRegistry registry, String pushgatewayUrl, String jobName) {
		this.registry = registry;
		this.pushgatewayUrl = pushgatewayUrl;
		this.jobName = jobName;
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		try {
			PushGateway.builder()
				.address(pushgatewayUrl.replaceFirst("https?://", ""))
				.job(jobName)
				.registry(registry.getPrometheusRegistry())
				.build()
				.push();
			log.info("Metrics pushed to Pushgateway: {}", pushgatewayUrl);
		} catch (IOException e) {
			log.error("Failed to push metrics to Pushgateway", e);
		}
	}

}
