package vn.thucvu.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SchedulerTask {

    Logger logger = LoggerFactory.getLogger(SchedulerTask.class);

    @Scheduled(fixedRate = 5000)  // Runs every 5 seconds
    public void fixedRateTask() {
        logger.info("Fixed Rate Task: {}", LocalDateTime.now());
    }

    @Scheduled(fixedDelay = 5000)  // Runs 5 seconds after the last execution finishes
    public void fixedDelayTask() {
        logger.info("Fixed Delay Task: {}", LocalDateTime.now());
    }

    @Scheduled(cron = "0 0/1 * * * ?") // Runs every 1 minute
    public void cronTask() {
        logger.info("Cron Task: {}", LocalDateTime.now());
    }
}

