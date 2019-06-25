package ru.job4j.parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * The parser site.
 *@author IvanPJF (teaching-light@yandex.ru)
 *@since 24.06.2019
 *@version 0.1
 */
public class ParseSite {

    private static final Logger LOG = LogManager.getLogger(ParseSite.class.getName());

    private final String siteLink = "https://www.sql.ru/forum/job-offers";
    private final Set<Vacancy> validVacancies = new TreeSet<>();
    private final Set<String> allLinksVacancies = new HashSet<>();
    private LocalDateTime lastDate;
    private boolean isStopParse = false;


    public ParseSite(LocalDateTime lastDate) {
        this.lastDate = lastDate;
        LOG.trace("Date of last vacancy {}", lastDate);
    }

    /**
     * Get a document containing HTML code.
     * @param link
     * @return
     * @throws IOException
     */
    private Document takeDocument(String link) throws IOException {
        LOG.trace("Get the document by the link: {}", link);
        return Jsoup.connect(link).get();
    }

    /**
     * Parses the site.
     * Gets a list of suitable vacancies.
     */
    public void run() {
        try {
            Document doc = this.takeDocument(this.siteLink);
            int countPages = this.countPages(doc);
            for (int numberPage = 1; numberPage <= countPages; numberPage++) {
                Document document = this.takeDocument(String.format("%s/%d", this.siteLink, numberPage));
                this.findAllVacancies(document);
                if (this.isStopParse) {
                    break;
                }
            }
            this.filterValidVacancies();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * The total number of pages with the list of vacancies.
     * @param document The document contains the HTML code of the vacancies list and the number of pages.
     * @return
     */
    public int countPages(Document document) {
        Element lastPageElem = document.body().getElementsByClass("sort_options").last();
        return Integer.parseInt(lastPageElem.select("a").last().text());
    }

    /**
     * Filters out suitable vacancies.
     * @throws IOException
     */
    private void filterValidVacancies() throws IOException {
        for (String link : this.allLinksVacancies) {
            this.checkVacancy(this.takeDocument(link));
        }
    }

    /**
     * Looking for all the links to the vacancies until then, until the latest date.
     * @param document The document contains the HTML code of the vacancies list.
     */
    public void findAllVacancies(Document document) {
        Elements listTopics = document.body().getElementsByClass("postslisttopic");
        for (Element element : listTopics) {
            if (!element.children().hasClass("closedTopic")) {
                String dateUpd = element.parent().getElementsByClass("altCol").last().text();
                if (this.isCheckDate(dateUpd)) {
                    this.isStopParse = true;
                    break;
                }
                String link = element.select("a").attr("href");
                this.allLinksVacancies.add(link);
                LOG.trace("Found new/updated vacancy : {}", link);
            }
        }
    }

    /**
     * Checking the vacancy.
     * @param document The document contains the HTML code of the vacancy.
     */
    public void checkVacancy(Document document) {
        Element firstMessage = document.getElementsByClass("msgTable").first().selectFirst("tbody");
        String name = firstMessage.getElementsByClass("messageHeader").first().textNodes().get(0).text().trim();
        String text = firstMessage.getElementsByClass("msgBody").last().text();
        String date = firstMessage.getElementsByClass("msgFooter").first().text().split("\\[")[0].trim();
        if (!this.isCheckDate(date) && (this.isValidText(name) || this.isValidText(text))) {
            String link = document.location();
            LocalDateTime dateCreate = this.stringToDate(date);
            this.validVacancies.add(new Vacancy(name, text, link, dateCreate));
            LOG.trace(String.format("Suitable vacancy: [date create] %s [name] %s [link] %s", dateCreate, name, link));
        }
    }

    /**
     * Check the text of the vacancy for the word Java and the absence of the word Javascript.
     * @param text The text of the vacancy.
     * @return
     */
    private boolean isValidText(String text) {
        Pattern includeWord = Pattern.compile("java", Pattern.CASE_INSENSITIVE);
        Pattern excludedWord = Pattern.compile("java\\s*script", Pattern.CASE_INSENSITIVE);
        return text != null
                && includeWord.matcher(text).find()
                && !excludedWord.matcher(text).find();
    }

    /**
     * The date string is converted to the LocaleDateTime type.
     * @param date A string with a date.
     * @return
     */
    public LocalDateTime stringToDate(String date) {
        String pattern = "d MMM yy";
        String today = "сегодня";
        String yesterday = "вчера";
        String mayInvalid = "май";
        String mayValid = "мая";
        if (date.contains(today)) {
            date = date.replace(today, LocalDate.now().format(DateTimeFormatter.ofPattern(pattern)));
        } else if (date.contains(yesterday)) {
            date = date.replace(yesterday, LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern(pattern)));
        }
        if (date.contains(mayInvalid)) {
            date = date.replace(mayInvalid, mayValid);
        }
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern("d MMM yy, HH:mm"));
    }

    /**
     * Finds out if the date was checked.
     * @param targetDate Date to check.
     * @return
     */
    private boolean isCheckDate(String targetDate) {
        return this.stringToDate(targetDate).compareTo(this.lastDate) <= 0;
    }

    public Set<Vacancy> takeValidVacancies() {
        return this.validVacancies;
    }

    public Set<String> takeAllLinksVacancies() {
        return this.allLinksVacancies;
    }
}
