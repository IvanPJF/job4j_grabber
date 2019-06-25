package ru.job4j.parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Running the application on schedule.
 *@author IvanPJF (teaching-light@yandex.ru)
 *@since 24.06.2019
 *@version 0.1
 */
public class StartSchedule {

    private static final Logger LOG = LogManager.getLogger(StartSchedule.class.getName());

    /**
     * Key to get the name of the configuration file.
     */
    private static final String KEY_FILE_CONFIG = "pathProperties";
    private Config config;

    public StartSchedule(String[] args) {
        this.config = new Config(args[0]);
    }

    public void run() {
        try {
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(KEY_FILE_CONFIG, this.config);
            JobDetail job = newJob(StartParser.class).setJobData(jobDataMap)
                    .build();
            Trigger trigger = newTrigger()
                    .withIdentity("CronTrigger")
                    .withSchedule(cronSchedule(this.config.get("cron.time")))
                    .build();
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException se) {
            LOG.error(se.getMessage(), se);
        }
    }

    public static void main(String[] args) {
        LOG.trace("Program start");
        new StartSchedule(args).run();
    }
}
