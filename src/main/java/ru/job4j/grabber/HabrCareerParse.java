package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.models.Post;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private final DateTimeParser dateTimeParser;
    private static final String SOURCE_LINK = "https://career.habr.com";

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

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

    @Override
    public List<Post> list(String link) {
        List<Post> postList = new ArrayList<>();
        Connection connection = Jsoup.connect(link);
        try {
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element infoElement = row.select(".vacancy-card__date").first();
                String dateString = infoElement.child(0).attr("datetime");
                DateTimeParser dateParser = new HabrCareerDateTimeParser();
                LocalDateTime date = dateParser.parse(dateString);
                Element linkElement = titleElement.child(0);
                String title = titleElement.text();
                String vacancyLink = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                String description = retrieveDescription(vacancyLink);
                postList.add(new Post(title, vacancyLink, description, date));
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return postList;
    }
}