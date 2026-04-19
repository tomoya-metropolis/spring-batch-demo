package com.example.demo

import com.example.demo.domain.FullNameMember
import org.jspecify.annotations.Nullable
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.listener.StepExecutionListener
import org.springframework.batch.core.step.StepExecution
import org.springframework.jdbc.core.JdbcOperations

class MemberStepListener(private val jdbcOperations: JdbcOperations) : StepExecutionListener {

    override fun afterStep(stepExecution: StepExecution): @Nullable ExitStatus? {
        val sql = "SELECT id, first_name, last_name, full_name FROM member ORDER BY id"

        val memberList = jdbcOperations.query(sql) { rs, _ ->
            FullNameMember(rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"), rs.getString("full_name"))
        }
        memberList.forEach(::println)

        return super.afterStep(stepExecution)
    }

}
