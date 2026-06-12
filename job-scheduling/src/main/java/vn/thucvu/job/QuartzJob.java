package vn.thucvu.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Advanced Scheduling
 */
public class QuartzJob implements Job {

    Logger log = LoggerFactory.getLogger(QuartzJob.class);

    @Override
    public void execute(JobExecutionContext context) {
        log.info("Executing Quartz Job: {}", LocalDateTime.now());
    }
}

