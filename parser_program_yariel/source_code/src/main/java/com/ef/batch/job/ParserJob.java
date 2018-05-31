package com.ef.batch.job;

import com.ef.batch.listener.JobCompletionNotificationListener;
import com.ef.batch.step.BlockUserIpStep;
import com.ef.batch.step.EmptyUserLogTableStep;
import com.ef.batch.step.FileLoaderStep;
import com.ef.domain.BlockedUser;
import com.ef.domain.UserLog;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * <p>Spring batch job in charge of parsing a .log file for analyzing it.
 * In this the goal is to read a web server access log file, loads the log
 * to MySQL and checks if a given IP makes more than a certain number of requests for the given duration.</p
 * <p>Here we define the configuration needed in order to construct our job setting it all the steps it will take to accomplish its goal.</p>
 * <p>We define our chunk size in a file called application.properties</p>
 *
 * @author yinfante
 */
@Configuration
@EnableBatchProcessing
@PropertySource("classpath:/application.properties")
public class ParserJob {

    private Environment env;
    private EmptyUserLogTableStep emptyUserLogTableStep;
    private FileLoaderStep fileLoaderStep;
    private StepBuilderFactory stepBuilderFactory;
    private BlockUserIpStep blockUserIpStep;
    private JobCompletionNotificationListener listener;


    /**
     * Injecting our dependencies
     *
     * @param env                       to read configuration properties
     * @param emptyUserLogTableStep     step in charge of emptying our USER_LOG table
     * @param fileLoaderStep            step in charge of loading our .log file and writing it to our USER_LOG table
     * @param stepBuilderFactory        Convenient factory for a {@link StepBuilder} which sets the {@link JobRepository} and {@link PlatformTransactionManager} automatically.
     * @param blockUserIpStep           step in charge blocking IP addresses that exceed the threshold parameter passed
     * @param listener                  If the job was run successfully, it will print the results of all the rows inserted and what IP was blocked
     */
    @Autowired
    public ParserJob(Environment env, EmptyUserLogTableStep emptyUserLogTableStep, FileLoaderStep fileLoaderStep, StepBuilderFactory stepBuilderFactory, BlockUserIpStep blockUserIpStep, JobCompletionNotificationListener listener) {
        this.env = env;
        this.emptyUserLogTableStep = emptyUserLogTableStep;
        this.fileLoaderStep = fileLoaderStep;
        this.stepBuilderFactory = stepBuilderFactory;
        this.blockUserIpStep = blockUserIpStep;
        this.listener = listener;
    }

    /**
     * Constructing our spring batch job setting the order or "flow" in which it will run each step.
     *
     * @param jobBuilderFactory injected Convenient factory for a {@link JobBuilder} which sets the {@link JobRepository} automatically.
     * @return our Spring batch Job fully loaded
     * @see Job
     * @see JobBuilderFactory
     */
    @Bean("ParserJob")
    public Job job(JobBuilderFactory jobBuilderFactory) {
        return jobBuilderFactory.get("Parser Job")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(emptyUserLogTableStep())
                .next(fileLogLoaderStep())
                .next(blockUserStep())
                .build();
    }

    /**
     * Constructing empty USER_LOG table Step
     *
     * @return our empty USER_LOG table Step
     * @see StepBuilderFactory
     * @see EmptyUserLogTableStep
     */
    private Step emptyUserLogTableStep() {
        return stepBuilderFactory.get("empty USER_LOG table Step")
                .tasklet(emptyUserLogTableStep)
                .allowStartIfComplete(true)
                .build();
    }

    /**
     * Constructing file .log Loader Step
     *
     * @return our file .log Loader Step
     * @see StepBuilderFactory
     * @see FileLoaderStep
     */
    private Step fileLogLoaderStep() {
        return stepBuilderFactory.get("file .log Loader Step")
                .<UserLog, UserLog>chunk(Integer.valueOf(env.getProperty("application.job.chunkSize")))
                .reader(fileLoaderStep.reader(null))
                .processor(fileLoaderStep.processor())
                .writer(fileLoaderStep.writer())
                .allowStartIfComplete(true)
                .build();
    }


    /**
     * Constructing our block IP addresses Step
     *
     * @return our block IP addresses Step
     * @see BlockUserIpStep
     */
    private Step blockUserStep() {
        return stepBuilderFactory.get("block IP addresses Step")
                .<BlockedUser, BlockedUser>chunk(Integer.valueOf(env.getProperty("application.job.chunkSize")))
                .reader(blockUserIpStep.reader(null, null, 0))
                .processor(blockUserIpStep.processor())
                .writer(blockUserIpStep.writer())
                .allowStartIfComplete(true)
                .build();
    }
}
