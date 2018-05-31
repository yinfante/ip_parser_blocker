package com.ef.batch.step;

import com.ef.domain.UserLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;
import org.springframework.util.Assert;

import javax.sql.DataSource;

/**
 * <p> Configuration for our step responsible of loading a .log file of users' accesses to a web server.
 * It gets a file url of the file to process and inserts them into our USER_LOG table.</p>
 * <p>Log file format must: Date, IP, Request, Status, User Agent (pipe delimited, open the example file in text editor) </p>
 *
 * @author yinfante
 */
@Configuration
@Slf4j
public class FileLoaderStep {

    private DataSource dataSource;

    /**
     * Injecting dependencies
     *
     * @param dataSource our Data source connection
     * @see DataSource
     */
    @Autowired
    public FileLoaderStep(DataSource dataSource) {
        Assert.isNull(this.dataSource, "Data source was not initialized");
        this.dataSource = dataSource;
    }

    private final String delimiter = "|";

    /**
     * Bean Reader, it's the first part of the step. It reads our log file based on a file url passed when executing.
     *
     * @param fileUrl file url of our .log file passed at execution time
     * @return our item reader implementation to read our .log file
     * @see org.springframework.batch.item.ItemReader
     * @see FlatFileItemReader
     */
    @Bean("fileLoaderStepReader")
    @StepScope
    public FlatFileItemReader<UserLog> reader(@Value("#{jobParameters['fileUrl']}") String fileUrl) {

        FlatFileItemReader<UserLog> reader = new FlatFileItemReader<>();

        reader.setResource(new PathResource(fileUrl));
        reader.setLineMapper(new DefaultLineMapper<UserLog>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setDelimiter(delimiter);
                // setting columns' names
                setNames("date", "ip", "request", "status", "userAgent");
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<UserLog>() {{
                setTargetType(UserLog.class);
            }});
        }});
        return reader;
    }

    /**
     * Bean processor of our step. It is the second part executed when step is run.
     *
     * @return Our item processor
     * @see ItemProcessor
     * @see UserLog
     */
    @Bean("fileLoaderStepProcessor")
    @StepScope
    public ItemProcessor<UserLog, UserLog> processor() {
        return new ItemProcessor<UserLog, UserLog>() {

            private int id;

            @Override
            public UserLog process(UserLog item) throws Exception {
                // setting it an id so that when inserted we have each log identified
                // and to know really easy how many rows were inserted
                item.setId(++id);
                // trimming each field in case it came with spaces
                item.setIp(item.getIp().trim());
                item.setRequest(item.getRequest().trim());
                item.setStatus(item.getStatus().trim());
                item.setUserAgent(item.getUserAgent().trim());

                return item;
            }
        };
    }

    /**
     * Bean writer of our step. This is the last part executed when step is run.
     * After data has been read and processed here we are ready to insert it into our USER_LOG table.
     *
     * @return our Writer
     * @see JdbcBatchItemWriter
     */
    @Bean("fileLoaderStepWriter")
    @StepScope
    public JdbcBatchItemWriter<UserLog> writer() {
        JdbcBatchItemWriter<UserLog> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql("INSERT INTO USER_LOG (id, date, ip, request, status, user_agent) VALUES (:id, :date, :ip, :request, :status, :userAgent)");
        writer.setDataSource(dataSource);
        return writer;
    }
}
