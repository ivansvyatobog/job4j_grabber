package ru.job4j.gc.leak;

import java.util.List;
import java.util.Objects;

public class Post {

    private Integer id;

    private String text;

    private List<Comment> comments;

    public Post(Integer id, String text, List<Comment> comments) {
        this.id = id;
        this.text = text;
        this.comments = comments;
    }

    public Post(String text, List<Comment> comments) {
        this.text = text;
        this.comments = comments;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /*остальные getter/setter*/

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return Objects.equals(id, post.id) && Objects.equals(text, post.text) && Objects.equals(comments, post.comments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, text, comments);
    }
}