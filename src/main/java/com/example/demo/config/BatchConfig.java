package com.example.demo.config;

import javax.sql.DataSource;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.FieldSetMapper;
import org.springframework.batch.infrastructure.item.file.transform.FieldSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.validation.BindException;

import com.example.demo.MemberStepListener;
import com.example.demo.domain.FullNameMember;
import com.example.demo.domain.Member;

@Configuration
@Import({ DataSourceConfig.class })
public class BatchConfig {

	@Bean
	FlatFileItemReader<Member> itemReader() {
		return new FlatFileItemReaderBuilder<Member>().name("memberItemReader")
				.resource(new ClassPathResource("bon_jovi.csv"))
				.delimited(config -> config.delimiter(",").quoteCharacter('"').names("id", "first_name", "last_name"))
				.fieldSetMapper(new MemberMapper()).linesToSkip(1).build();
	}

	@Bean
	ItemProcessor<Member, FullNameMember> itemProcessor() {
		return item -> {
			return new FullNameMember(item.id(), item.firstName(), item.lastName(),
					item.firstName() + " " + item.lastName());
		};
	}

	@Bean
	ItemWriter<FullNameMember> itemWriter(DataSource dataSource) {
		return new JdbcBatchItemWriterBuilder<FullNameMember>().dataSource(dataSource).sql(
				"INSERT INTO member (id, first_name, last_name, full_name) VALUES (:id, :firstName, :lastName, :fullName)")
				.beanMapped().build();
	}

	@Bean
	Step step(JobRepository jobRepository, ItemReader<Member> itemReader,
			ItemProcessor<Member, FullNameMember> itemProcessor, ItemWriter<FullNameMember> itemWriter,
			StepExecutionListener stepExecutionListener) {
		return new StepBuilder(jobRepository).<Member, FullNameMember>chunk(1).reader(itemReader)
				.processor(itemProcessor).writer(itemWriter).listener(stepExecutionListener).build();
	}

	@Bean
	StepExecutionListener memberStepListener(DataSource dataSource) {
		return new MemberStepListener(new JdbcTemplate(dataSource));
	}

	@Bean
	Job job(JobRepository jobRepository, Step step) {
		return new JobBuilder(jobRepository).start(step).build();
	}

	private static class MemberMapper implements FieldSetMapper<Member> {

		@Override
		public Member mapFieldSet(FieldSet fieldSet) throws BindException {
			return new Member(fieldSet.readInt("id"), fieldSet.readString("first_name"),
					fieldSet.readString("last_name"));
		}

	}

}
