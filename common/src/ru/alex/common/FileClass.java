package ru.alex.common;

import java.io.Serializable;

public class FileClass implements Serializable {
    String name;
    String description;
    String path;

    public FileClass(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPath() {
        return path;
    }
}
