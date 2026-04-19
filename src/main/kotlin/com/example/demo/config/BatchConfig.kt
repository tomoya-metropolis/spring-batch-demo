package com.example.demo.config

import com.example.demo.MemberStepListener
import com.example.demo.domain.FullNameMember
import com.example.demo.domain.Member
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.listener.StepExecutionListener
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.item.ItemProcessor
import org.springframework.batch.infrastructure.item.ItemReader
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.infrastructure.item.file.mapping.FieldSetMapper
import org.springframework.batch.infrastructure.item.file.transform.FieldSet
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.validation.BindException
import javax.sql.DataSource

@Configuration
@Import(DataSourceConfig::class)
class BatchConfig {

    @Bean
    fun itemReader(): FlatFileItemReader<Member> =
        FlatFileItemReaderBuilder<Member>().name("memberItemReader")
            .resource(ClassPathResource("bon_jovi.csv"))
            .delimited { config -> config.delimiter(",").quoteCharacter('"').names("id", "first_name", "last_name") }
            .fieldSetMapper(MemberMapper())
            .linesToSkip(1)
            .build()

    @Bean
    fun itemProcessor(): ItemProcessor<Member, FullNameMember> =
        ItemProcessor { item ->
            FullNameMember(item.id, item.firstName, item.lastName, "${item.firstName} ${item.lastName}")
        }

    @Bean
    fun itemWriter(@Qualifier("businessDataSource") businessDataSource: DataSource): ItemWriter<FullNameMember> =
        JdbcBatchItemWriterBuilder<FullNameMember>()
            .dataSource(businessDataSource)
            .sql("INSERT INTO member (id, first_name, last_name, full_name) VALUES (:id, :firstName, :lastName, :fullName)")
            .beanMapped()
            .build()

    @Bean
    fun step(
        jobRepository: JobRepository,
        itemReader: ItemReader<Member>,
        itemProcessor: ItemProcessor<Member, FullNameMember>,
        itemWriter: ItemWriter<FullNameMember>,
        stepExecutionListener: StepExecutionListener,
    ): Step =
        StepBuilder(jobRepository).chunk<Member, FullNameMember>(1)
            .reader(itemReader)
            .processor(itemProcessor)
            .writer(itemWriter)
            .listener(stepExecutionListener)
            .build()

    @Bean
    fun memberStepListener(@Qualifier("businessDataSource") businessDataSource: DataSource): StepExecutionListener =
        MemberStepListener(JdbcTemplate(businessDataSource))

    @Bean
    fun job(jobRepository: JobRepository, step: Step): Job =
        JobBuilder(jobRepository).start(step).build()

    private class MemberMapper : FieldSetMapper<Member> {
        @Throws(BindException::class)
        override fun mapFieldSet(fieldSet: FieldSet): Member =
            Member(fieldSet.readInt("id"), fieldSet.readString("first_name"), fieldSet.readString("last_name"))
    }
}
