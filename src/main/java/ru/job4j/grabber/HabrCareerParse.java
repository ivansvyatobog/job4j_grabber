package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private static String retrieveDescription(String link) {
        Connection connection = Jsoup.connect(link);
        StringBuilder descText = new StringBuilder();
        try {
            Document document = connection.get();
            Elements rows = document.select(".style-ugc *");
            rows.forEach(row -> descText.append(row.ownText()).append('\n'));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return descText.toString();
    }

    public static void main(String[] args) throws IOException {
        for (int i = 1; i <= 5; i++) {
            Connection connection = Jsoup.connect(String.format("%s?page=%s", PAGE_LINK, i));
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element infoElement = row.select(".vacancy-card__date").first();
                String dateString = infoElement.child(0).attr("datetime");
                DateTimeParser dateParser = new HabrCareerDateTimeParser();
                LocalDateTime date = dateParser.parse(dateString);
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                System.out.printf("%s %s %s%n", date, vacancyName, link);
            });
        }
    }
}