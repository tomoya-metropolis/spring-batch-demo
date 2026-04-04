package com.example.demo.config;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.FieldSetMapper;
import org.springframework.batch.infrastructure.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.BindException;

import com.example.demo.datasource.MemberRoutingDataSource;
import com.example.demo.domain.FullNameMember;
import com.example.demo.domain.Member;
import com.example.demo.listener.MemberStepExecutionListener;
import com.example.demo.partition.MemberPartitioner;

@Configuration
@Import({ DataSourceConfig.class })
public class BatchConfig {

	@Bean
	@StepScope
	FlatFileItemReader<Member> itemReader(@Value("#{stepExecutionContext['fileName']}") String fileName) {
		return new FlatFileItemReaderBuilder<Member>().name("memberItemReader")
				.resource(new ClassPathResource(fileName))
				.delimited(config -> config.delimiter(",").quoteCharacter('"').names("id", "first_name", "last_name"))
				.fieldSetMapper(new MemberMapper()).linesToSkip(1).build();
	}

	@Bean
	@StepScope
	ItemProcessor<Member, FullNameMember> itemProcessor() {
		return item -> {
			return new FullNameMember(item.id(), item.firstName(), item.lastName(),
					item.firstName() + " " + item.lastName());
		};
	}

	@Bean
	@StepScope
	ItemWriter<FullNameMember> itemWriter(@Qualifier("memberDataSource") DataSource memberDataSource) {
		return new JdbcBatchItemWriterBuilder<FullNameMember>()
				.namedParametersJdbcTemplate(new NamedParameterJdbcTemplate(memberDataSource))
				.sql("INSERT INTO member (id, first_name, last_name, full_name) VALUES (:id, :firstName, :lastName, :fullName)")
				.beanMapped().build();
	}

	@Bean
	Step masterStep(JobRepository jobRepository, @Qualifier("dataSource") DataSource dataSource,
			@Qualifier("memberRoutingDataSource") MemberRoutingDataSource memberRoutingDataSource,
			@Qualifier("memberDataSource") DataSource memberDataSource,
			@Qualifier("memberTransactionManager") PlatformTransactionManager memberTransactionManager) {
		return new StepBuilder("masterStep", jobRepository)
				.partitioner("memberPartitioner", partitioner(dataSource, memberRoutingDataSource))
				.step(workerStep(jobRepository, memberRoutingDataSource, memberTransactionManager)).gridSize(10)
				.taskExecutor(taskExecutor()).build();
	}

	@Bean
	Step workerStep(JobRepository jobRepository, @Qualifier("memberDataSource") DataSource memberDataSource,
			@Qualifier("memberTransactionManager") PlatformTransactionManager memberTransactionManager) {
		return new StepBuilder("slaveStep",jobRepository).<Member, FullNameMember>chunk(100)
				.transactionManager(memberTransactionManager).listener(memberStepExecutionListener())
				.reader(itemReader(null)).processor(itemProcessor()).writer(itemWriter(memberDataSource)).build();
	}

	@Bean
	StepExecutionListener memberStepExecutionListener() {
		return new MemberStepExecutionListener();
	}

	@Bean
	Job job(JobRepository jobRepository, @Qualifier("dataSource") DataSource dataSource,
			@Qualifier("memberRoutingDataSource") MemberRoutingDataSource memberRoutingDataSource,
			@Qualifier("memberDataSource") DataSource memberDataSource,
			@Qualifier("memberTransactionManager") PlatformTransactionManager memberTransactionManager) {
		return new JobBuilder(jobRepository).start(
				masterStep(jobRepository, dataSource, memberRoutingDataSource, memberDataSource, memberTransactionManager))
				.build();
	}

	@Bean
	Partitioner partitioner(@Qualifier("dataSource") DataSource dataSource,
			@Qualifier("memberRoutingDataSource") MemberRoutingDataSource memberDataSource) {
		return new MemberPartitioner(new JdbcTemplate(dataSource), memberDataSource);
	}

	@Bean
	TaskExecutor taskExecutor() {
		return new VirtualThreadTaskExecutor();
	}

	private static class MemberMapper implements FieldSetMapper<Member> {

		@Override
		public Member mapFieldSet(FieldSet fieldSet) throws BindException {
			return new Member(fieldSet.readInt("id"), fieldSet.readString("first_name"),
					fieldSet.readString("last_name"));
		}

	}

}
