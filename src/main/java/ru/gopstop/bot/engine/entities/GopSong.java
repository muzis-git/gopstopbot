package ru.gopstop.bot.engine.entities;

import java.util.List;

/**
 * Песня из корпуса шансона
 *
 * Created by aam on 30.07.16.
 */
public class GopSong {

    private final String name;
    private final String author;
    private final List<String> lyrics;

    public GopSong(String name, String author, List<String> lyrics) {
        this.name = name;
        this.author = author;
        this.lyrics = lyrics;
    }

    public List<String> getLyrics() {
        return lyrics;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String toString() {
        return "GopSong{title=" + name + ", author=" + author + ", " +
                (lyrics == null ? "no" : lyrics.size()) + " lines}";
    }
}
