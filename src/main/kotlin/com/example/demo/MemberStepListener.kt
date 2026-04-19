package com.example.demo

import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.listener.StepExecutionListener
import org.springframework.batch.core.step.StepExecution
import org.springframework.jdbc.core.JdbcOperations

class MemberStepListener(private val jdbcOperations: JdbcOperations) : StepExecutionListener {

    override fun afterStep(stepExecution: StepExecution): ExitStatus? {
        val sql = "SELECT count(*) FROM member"

        val result = jdbcOperations.queryForObject(sql, Integer::class.java)
        println("result = $result")

        return super.afterStep(stepExecution)
    }

}
