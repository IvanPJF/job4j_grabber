package ru.job4j.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ParseSiteTest {

    @Test
    public void whenUseCountPages() {
        String html = "<table class=\"sort_options\"><tbody><tr>"
                + "<td>Страницы: "
                + "<b>1</b>"
                + "<a href=\"https://www.sql.ru/forum/job-offers/2\">2</a>"
                + " .. <a href=\"https://www.sql.ru/forum/job-offers/682\">682</a>"
                + "</td>"
                + "</tr></tbody></table>";
        ParseSite parseSite = new ParseSite(null);
        Document doc = Jsoup.parse(html);
        int result = parseSite.countPages(doc);
        int expected = 682;
        assertThat(result, is(expected));
    }

    @Test
    public void whenUseFindAllVacancies() {
        String link = "https://www.sql.ru/forum/1313101/razrabotchik-c-bank-vtb-moskva-140k-200k";
        String html = "<table class=\"forumTable\"><tbody>"
                + "<tr>"
                + "<td class=\"postslisttopic\">"
                + "<a href=" + link + ">"
                + "Разработчик C#, Банк ВТБ, Москва, 140k-200k</a></td>"
                + "<td class=\"altCol\">вчера, 18:50</td>"
                + "</tr>"
                + "<tr>"
                + "<td class=\"postslisttopic\">"
                + "<a href=\"https://www.sql.ru/forum/1313834/sql-bi-udalenno-postoyannye-obemy\">"
                + "SQL + BI удаленно, постоянные объемы</a><span class=\"closedTopic\"> [закрыт]</span></td>"
                + "<td class=\"altCol\">вчера, 11:00</td>"
                + "</tr>"
                + "</tbody></table>";
        LocalDateTime lastDate = LocalDateTime.now().minusDays(2);
        ParseSite parseSite = new ParseSite(lastDate);
        Document doc = Jsoup.parse(html);
        parseSite.findAllVacancies(doc);
        Set<String> result = parseSite.takeAllLinksVacancies();
        Set<String> expected = new HashSet<>();
        expected.add(link);
        assertThat(result.size(), is(expected.size()));
        assertThat(result.iterator().next(), is(expected.iterator().next()));
    }

    @Test
    public void whenUseCheckVacancy() {
        String name = "JAVA разработчик. Москва";
        String text = "Java разработчик от 90 000 до 160 000 руб. на руки";
        String date = "вчера, 11:00";
        String html = "<table class=\"msgTable\"><tbody>"
                + "<tr><td class=\"messageHeader\">&nbsp;" + name + "&nbsp;</td></tr>"
                + "<tr>"
                + "<td class=\"msgBody\">sergesus</td>"
                + "<td class=\"msgBody\">" + text + "</td>"
                + "</tr>"
                + "<tr><td class=\"msgFooter\">" + date + "&nbsp;&nbsp;&nbsp;&nbsp;[]</td></tr>"
                + "</tbody></table>";
        LocalDateTime lastDate = LocalDateTime.now().minusDays(2);
        ParseSite parseSite = new ParseSite(lastDate);
        Document doc = Jsoup.parse(html);
        parseSite.checkVacancy(doc);
        Set<Vacancy> result = parseSite.takeValidVacancies();
        LocalDateTime dateCreate = parseSite.stringToDate(date);
        Set<Vacancy> expected = new TreeSet<>();
        Vacancy vacancy = new Vacancy(name, text, "", dateCreate);
        expected.add(vacancy);
        assertThat(result.size(), is(expected.size()));
        assertThat(result.iterator().next(), is(expected.iterator().next()));
    }
}