package com.ef.parser;

import com.ef.config.SpringRegistry;
import com.ef.domain.ParserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;

import java.util.Date;

/**
 * Responsible of launching our Parser job.
 */
@Slf4j
public class ParserInvoker {

    private ApplicationContext context;

    /**
     * It is tightly coupled to our SpringRegistry because it retrieves the instanace of spring
     * context needed to find Parser job instance and run it.
     *
     * @see SpringRegistry
     */
    public ParserInvoker() {
        context = SpringRegistry.getContext();
    }

    /**
     * It creates and invokes our ParserJob.
     *
     * @param parserDTO on intance of ParserDTO containing parameters needed for the invoker
     * @return batch status
     * @see BatchStatus
     * @see ParserDTO
     * @see JobLauncher
     * @see Job
     * @see JobParametersBuilder
     * @see JobExecution
     */
    public BatchStatus invoke(ParserDTO parserDTO) {

        // start time of job
        Date jobStartTime = new Date();

        try {
            // get JobLauncher instance
            JobLauncher jobLauncher = (JobLauncher) context.getBean("jobLauncher");
            // get ParserJob instance
            Job job = (Job) context.getBean("ParserJob");

            // sets parameters needed for the execution of ParserJob
            JobParametersBuilder jobBuilder = new JobParametersBuilder();
            jobBuilder.addString("fileUrl", parserDTO.getFileUrl());
            jobBuilder.addString("startDate", parserDTO.getStartDate());
            jobBuilder.addString("duration", parserDTO.getDuration());
            jobBuilder.addLong("threshold", parserDTO.getThreshold());
            jobBuilder.addString("date", new Date().toString());

            JobParameters jobParameters = jobBuilder.toJobParameters();
            // launch Parser job with the specified parameters
            JobExecution execution = jobLauncher.run(job, jobParameters);

            // end time of job
            Date jobEndTime = new Date();

            log.info("Job started at : " + jobStartTime.toString());
            log.info("Job ended at : " + jobEndTime.toString());

            return execution.getStatus();

        } catch (Exception e) {
            Date jobEndTime = new Date();

            log.info("Job started at : " + jobStartTime.toString());
            log.info("Job ended at : " + jobEndTime.toString());

            log.error(e.getMessage());
            e.printStackTrace();
            return BatchStatus.FAILED;
        }

    }
}
