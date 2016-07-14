package com.deepakvadgama.radhekrishnabhakti.pojo;

import android.os.Parcel;
import android.os.Parcelable;

public class Content implements Parcelable {

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

    protected Content(Parcel in) {
        id = in.readInt();
        type = in.readString();
        title = in.readString();
        author = in.readString();
        url = in.readString();
        text = in.readString();
        isFavorite = in.readByte() != 0;
    }

    public static final Creator<Content> CREATOR = new Creator<Content>() {
        @Override
        public Content createFromParcel(Parcel in) {
            return new Content(in);
        }

        @Override
        public Content[] newArray(int size) {
            return new Content[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(type);
        dest.writeString(title);
        dest.writeString(author);
        dest.writeString(url);
        dest.writeString(text);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
    }
}