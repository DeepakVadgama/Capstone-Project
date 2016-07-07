package com.deepakvadgama.radhekrishnabhakti.pojo;

public class Content {

    public int id;
    public String type;
    public String title;
    public String author;
    public String url;
    public String text;
    public boolean isFavorite;

    public Content() {

    }

    public Content(int id, String type, String title, String author, String url, String text) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.author = author;
        this.url = url;
        this.text = text;
    }

    @Override
    public String toString() {
        return "Content{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", url='" + url + '\'' +
                ", text='" + text + '\'' +
                ", isFavorite=" + isFavorite +
                '}';
    }

}