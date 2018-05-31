package com.ef.batch.step;


import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

/**
 * This step is responsible of emptying USER_LOG table. There we truncate our table so that when loading our log file it will be a lot much faster.
 *
 * @author yinfante
 */
@Configuration
@Slf4j
public class EmptyUserLogTableStep implements Tasklet {

    private JdbcTemplate jdbcTemplate;

    /**
     * Injecting our dependencies
     *
     * @param jdbcTemplate It simplifies the use of JDBC and helps to avoid common errors.
     * @see JdbcTemplate
     */
    @Autowired
    public EmptyUserLogTableStep(JdbcTemplate jdbcTemplate) {
        Assert.isNull(this.jdbcTemplate, "Data source was not initialized");
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Executes a TRUNCATE statement against USER_LOG
     *
     * @param contribution Represents a contribution to a {@link StepExecution}, buffering changes until
     *                     they can be applied at a chunk boundary.
     * @param chunkContext Context object for weakly typed data stored for the duration of a chunk
     *                     (usually a group of items processed together in a transaction)
     * @return RepeatStatus
     * @throws Exception
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        log.info("EMPTY TABLE USER_LOG ");
        jdbcTemplate.execute("TRUNCATE TABLE USER_LOG");

        return RepeatStatus.FINISHED;
    }
}
