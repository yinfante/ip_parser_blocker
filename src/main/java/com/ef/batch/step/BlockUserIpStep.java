package com.ef.batch.step;

import com.ef.domain.BlockedUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.util.Assert;

import javax.sql.DataSource;

/**
 * Configuration for our step responsible of blocking IP addresses that exceeded the threshold within a specific time.
 *
 * @author yinfante
 */
@Configuration
@Slf4j
public class BlockUserIpStep {

    private DataSource dataSource;

    /**
     * Injecting our dependencies
     *
     * @param dataSource our Data source connection
     * @see DataSource
     */
    @Autowired
    public BlockUserIpStep(DataSource dataSource) {
        Assert.isNull(this.dataSource, "Data source was not initialized");
        this.dataSource = dataSource;
    }

    /**
     * Bean Reader, it's the first part of our step. It queries all IP that exceeded the threshold within a specific time.
     *
     * @param startDate this is the start date time of our period
     * @param duration  duration can be "HOUR" meaning that it will query for rows between startDate param and exactly 1 hour later.
     *                  It can also be "DAY" meaning it will query for rows between startDate param and exactly 1 day later.
     * @param threshold value used as our threshold
     * @return our Item reader fully constructed
     * @see JdbcCursorItemReader
     * @see BlockedUser
     */
    @Bean("blockedListLoaderStepReader")
    @StepScope
    public JdbcCursorItemReader<BlockedUser> reader(@Value("#{jobParameters['startDate']}") String startDate,
                                                    @Value("#{jobParameters['duration']}") String duration,
                                                    @Value("#{jobParameters['threshold']}") long threshold) {

        // template query for our search of IPs to block
        String templateQuery = "SELECT count(*) AS requests, ip, " +
                "current_timestamp AS blockedDate, " +
                " ? AS comment" +
                " FROM USER_LOG " +
                " WHERE date BETWEEN ? AND adddate(date_format(?, ?), INTERVAL 1 " + duration + ")" +
                " GROUP BY ip " +
                " HAVING requests >= ?";


        JdbcCursorItemReader<BlockedUser> databaseReader = new JdbcCursorItemReader<>();

        databaseReader.setDataSource(dataSource);
        databaseReader.setSql(templateQuery);
        databaseReader.setPreparedStatementSetter(ps -> {
            ps.setString(1, "blocked because it exceeded the threshold of " + threshold + " requests in 1 " + duration);
            ps.setString(2, startDate);
            ps.setString(3, startDate);
            ps.setString(4, "%Y-%m-%d.%H:%i:%s");
            ps.setLong(5, threshold);
        });
        databaseReader.setRowMapper(new BeanPropertyRowMapper<>(BlockedUser.class));

        return databaseReader;
    }


    /**
     * Bean processor of our step. It is the second part executed when step is run.
     *
     * @return Our item processor
     * @see ItemProcessor
     * @see BlockedUser
     */
    @Bean("blockedListLoaderStepProcessor")
    @StepScope
    public ItemProcessor<BlockedUser, BlockedUser> processor() {
        return item -> {
            System.out.println("IP BLOCKED " + item.getIp());
            return item;
        };
    }

    /**
     * Bean writer of our step. This is the last part executed when step is run.
     * After data has been read and processed here we are ready to insert it into our BLOCKED_USER table.
     *
     * @return our Writer
     * @see JdbcBatchItemWriter
     */
    @Bean("blockedListLoaderStepWriter")
    @StepScope
    public JdbcBatchItemWriter<BlockedUser> writer() {
        JdbcBatchItemWriter<BlockedUser> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql("INSERT INTO BLOCKED_USER (requests, ip, blocked_date, comment) VALUES (:requests, :ip, :blockedDate,:comment)");
        writer.setDataSource(dataSource);
        return writer;
    }


}
