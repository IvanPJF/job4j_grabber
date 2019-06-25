package ru.job4j.parser;

import java.time.LocalDateTime;
import java.util.Objects;

public class Vacancy implements Comparable<Vacancy> {

    private String name;
    private String text;
    private String link;
    private LocalDateTime dateCreate;

    public Vacancy(String name, String text, String link, LocalDateTime dateCreate) {
        this.name = name;
        this.text = text;
        this.link = link;
        this.dateCreate = dateCreate;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public String getLink() {
        return link;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Vacancy vacancy = (Vacancy) o;
        return Objects.equals(name, vacancy.name)
                && Objects.equals(link, vacancy.link)
                && Objects.equals(dateCreate, vacancy.dateCreate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, link, dateCreate);
    }

    public LocalDateTime getDateCreate() {
        return dateCreate;
    }

    @Override
    public int compareTo(Vacancy o) {
        return this.dateCreate.compareTo(o.getDateCreate());
    }
}
