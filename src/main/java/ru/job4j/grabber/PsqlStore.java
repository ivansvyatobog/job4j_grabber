package ru.job4j.grabber;

import ru.job4j.grabber.models.Post;
import ru.job4j.util.PropertiesUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {

    private  static Connection cnn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            cnn = DriverManager.getConnection(
                    cfg.getProperty("jdbc.url"),
                    cfg.getProperty("jdbc.user"),
                    cfg.getProperty("jdbc.password"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(Post post) {
        String sql = "insert into post (name, text, link, created) values (?, ? , ?, ?) "
                + "on conflict (link) do nothing";
        try (PreparedStatement statement = cnn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                post.setId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> rsl = new ArrayList<>();
        String sql = "select * from post";
        try (Statement statement = cnn.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                rsl.add(getPostFromDb(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException();
        }
        return rsl;
    }

    @Override
    public Post findById(int id) {
        Post rsl = null;
        String sql = "select * from post where id = ?";
        try (PreparedStatement statement = cnn.prepareStatement(sql)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                rsl = getPostFromDb(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rsl;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    private Post getPostFromDb(ResultSet resultSet) throws SQLException {
        int postId = resultSet.getInt("id");
        String postName = resultSet.getString("name");
        String postText = resultSet.getString("text");
        String linkPost = resultSet.getString("link");
        LocalDateTime createdDate = resultSet.getTimestamp("created").toLocalDateTime();
        return new Post(postId, postName, linkPost, postText, createdDate);
    }

    public static void main(String[] args) {
        Properties properties = PropertiesUtil.getProperties("app.properties");
        Post post = new Post("name", "link2", "text", LocalDateTime.now());
        new PsqlStore(properties).save(post);
        System.out.println(new PsqlStore(properties).getAll());
        System.out.println(new PsqlStore(properties).findById(1));
    }
}
