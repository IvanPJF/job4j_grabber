package ru.job4j.parser;

import org.junit.Test;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class StoreSQLTest {

    public Connection init() throws Exception {
        try (InputStream in = StoreSQL.class.getClassLoader().getResourceAsStream("app.properties")) {
            Properties config = new Properties();
            config.load(in);
            Class.forName(config.getProperty("jdbc.driver"));
            return DriverManager.getConnection(
                    config.getProperty("jdbc.url"),
                    config.getProperty("jdbc.username"),
                    config.getProperty("jdbc.password")
            );
        }
    }

    @Test
    public void whenIsConnectedThenTrue() throws Exception {
        try (StoreSQL sql = new StoreSQL(ConnectionRollback.create(this.init()))) {
            assertThat(sql.isConnected(), is(true));
        }
    }

    @Test
    public void whenInsertThenTwo() throws Exception {
        try (StoreSQL sql = new StoreSQL(ConnectionRollback.create(this.init()))) {
            Set<Vacancy> vacancies = new TreeSet<>(Arrays.asList(
                    new Vacancy("JavaName1", "JavaText1", "http://java.com/1", LocalDateTime.now().minusDays(1)),
                    new Vacancy("JavaName2", "JavaText2", "http://java.com/2", LocalDateTime.now().minusDays(2))
            ));
            int result = sql.insert(vacancies);
            assertThat(result, is(2));
        }
    }

    @Test
    public void whenGetLastDateAndStoreIsEmptyThenCurrentDateMinusOneYears() throws Exception {
        try (StoreSQL sql = new StoreSQL(ConnectionRollback.create(this.init()))) {
            LocalDateTime result = sql.getLastDate().truncatedTo(ChronoUnit.MINUTES);
            LocalDateTime expected = LocalDateTime.now().minusYears(1).truncatedTo(ChronoUnit.MINUTES);
            assertThat(result, is(expected));
        }
    }

    @Test
    public void whenGetLastDateAndStoreContainsOneVacancyThenDateVacancy() throws Exception {
        try (StoreSQL sql = new StoreSQL(ConnectionRollback.create(this.init()))) {
            LocalDateTime expected = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
            Set<Vacancy> vacancies = new TreeSet<>(Collections.singletonList(
                    new Vacancy("JavaName", "JavaText", "http://java.com", expected)
            ));
            int count = sql.insert(vacancies);
            LocalDateTime result = sql.getLastDate().truncatedTo(ChronoUnit.MINUTES);
            assertThat(count, is(1));
            assertThat(result, is(expected));
        }
    }
}