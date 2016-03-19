package com.yang.jigsaw.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/3/6.
 */
public class FileInfo implements Serializable {
    private int id;
    private String url;
    private String name;
    private int length;
    private int finished;

    public FileInfo() {

    }

    public FileInfo(int id, String url, int length, int finished) {
        this.id = id;
        this.url = url;
        this.length = length;
        this.finished = finished;
        this.name = "ImageDownLoad_" + url.substring(url.lastIndexOf("/") + 1);
    }

    public FileInfo(int id, String url, String name, int length, int finished) {
        this.id = id;
        this.url = url;
        this.length = length;
        this.finished = finished;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        this.name = "ImageDownLoad_" + url.substring(url.lastIndexOf("/") + 1);
    }

    public String getName() {
        return name;
    }


    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getFinished() {
        return finished;
    }

    public void setFinished(int finished) {
        this.finished = finished;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", length=" + length +
                ", finished=" + finished +
                '}';
    }
}
