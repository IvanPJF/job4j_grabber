package ru.job4j.parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

/**
 * Application launch.
 *@author IvanPJF (teaching-light@yandex.ru)
 *@since 24.06.2019
 *@version 0.1
 */
public class StartParser implements Job {

    /**
     * Key to get the name of the configuration file.
     */
    private static final String KEY_FILE_CONFIG = "pathProperties";

    private static final Logger LOG = LogManager.getLogger(StartParser.class.getName());

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        LOG.trace("The beginning of the next run");
        Config config = (Config) jobExecutionContext.getJobDetail().getJobDataMap().get(KEY_FILE_CONFIG);
        try (StoreSQL store = new StoreSQL(config)) {
            ParseSite parser = new ParseSite(store.getLastDate());
            parser.run();
            store.insert(parser.takeValidVacancies());
            LOG.trace("The end of the next run");
        }
    }
}