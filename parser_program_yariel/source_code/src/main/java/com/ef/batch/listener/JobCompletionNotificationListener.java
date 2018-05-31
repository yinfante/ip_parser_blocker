package com.ef.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * This is designed to execute when the job was completed.
 *
 * @author yinfante
 */
@Component
@Slf4j
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Injecting our dependencies
     *
     * @param jdbcTemplate It simplifies the use of JDBC and helps to avoid common errors.
     * @see JdbcTemplate
     */
    @Autowired
    public JobCompletionNotificationListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * It counts all the rows inserted in USER_LOG and logs the number.
     *
     * @param jobExecution Batch domain object representing the execution of a job.
     */
    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("!!! JOB FINISHED! Time to verify the results");

            Integer usersLogCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USER_LOG", Integer.class);

            log.info("ROWS INSERTED IN USER_LOG TABLE: " + usersLogCount);
            log.info("You can see the list of blocked users in BLOCKED_USER table");
        }
    }
}
