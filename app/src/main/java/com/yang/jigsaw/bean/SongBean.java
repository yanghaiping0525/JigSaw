package com.yang.jigsaw.bean;

/**
 * Created by Administrator on 2016/2/26.
 */
public class SongBean {
    private int id;
    private String name;
    private String singer;
    private String album;
    private String path;
    private String display_name;
    private String year;
    private Character firstLetter;
    private int duration;
    private long size;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Character getFirstLetter() {
        return firstLetter;
    }

    public void setFirstLetter(Character firstLetter) {
        this.firstLetter = firstLetter;
    }

    @Override
    public String toString() {
        return "SongBean{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", singer='" + singer + '\'' +
                ", album='" + album + '\'' +
                ", path='" + path + '\'' +
                ", display_name='" + display_name + '\'' +
                ", year='" + year + '\'' +
                ", firstLetter=" + firstLetter +
                ", duration=" + duration +
                ", size=" + size +
                '}';
    }
}
