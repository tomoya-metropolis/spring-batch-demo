package com.example.demo.config

import com.example.demo.domain.Member
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.item.ItemReader
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.infrastructure.item.file.mapping.FieldSetMapper
import org.springframework.batch.infrastructure.item.file.transform.FieldSet
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.validation.BindException

@Configuration
class BatchConfig {

	@Bean
	fun itemReader(): FlatFileItemReader<Member> {
		return FlatFileItemReaderBuilder<Member>()
			.name("memberItemReader")
			.resource(ClassPathResource("bon_jovi.csv"))
			.delimited { config -> config.delimiter(",").quoteCharacter('"').names("id", "first_name", "last_name") }
			.fieldSetMapper(MemberMapper())
			.linesToSkip(1)
			.build()
	}

	@Bean
	fun itemWriter(): ItemWriter<Member> {
		return ItemWriter { chunk ->
			chunk.items.forEach { println(it) }
		}
	}

	@Bean
	fun step(jobRepository: JobRepository, itemReader: ItemReader<Member>, itemWriter: ItemWriter<Member>): Step {
		return StepBuilder(jobRepository)
			.chunk<Member, Member>(1)
			.reader(itemReader)
			.writer(itemWriter)
			.build()
	}

	@Bean
	fun job(jobRepository: JobRepository, step: Step): Job {
		return JobBuilder(jobRepository)
			.start(step)
			.build()
	}

	private class MemberMapper : FieldSetMapper<Member> {
		@Throws(BindException::class)
		override fun mapFieldSet(fieldSet: FieldSet): Member {
			return Member(
				fieldSet.readInt("id"),
				fieldSet.readString("first_name")!!,
				fieldSet.readString("last_name")!!
			)
		}
	}
}
