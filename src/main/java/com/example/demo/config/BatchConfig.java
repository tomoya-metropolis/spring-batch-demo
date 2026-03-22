package com.example.demo.config;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.FieldSetMapper;
import org.springframework.batch.infrastructure.item.file.transform.FieldSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.validation.BindException;

import com.example.demo.domain.Member;

@Configuration
public class BatchConfig {

	@Bean
	FlatFileItemReader<Member> itemReader() {
		return new FlatFileItemReaderBuilder<Member>().name("memberItemReader")
				.resource(new ClassPathResource("bon_jovi.csv"))
				.delimited(config -> config.delimiter(",").quoteCharacter('"').names("id", "first_name", "last_name"))
				.fieldSetMapper(new MemberMapper()).linesToSkip(1).build();
	}

	@Bean
	ItemWriter<Member> itemWriter() {
		return (chunk) -> {
			chunk.getItems().stream().forEach(System.out::println);
		};
	}

	@Bean
	Step step(JobRepository jobRepository, ItemReader<Member> itemReader, ItemWriter<Member> itemWriter) {
		return new StepBuilder(jobRepository).<Member, Member>chunk(1).reader(itemReader).writer(itemWriter).build();
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
