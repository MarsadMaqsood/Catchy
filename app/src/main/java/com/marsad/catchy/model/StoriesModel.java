package com.marsad.catchy.model;

public class StoriesModel {

    String url, id, name, uid, type;

    public StoriesModel() {
    }

    public StoriesModel(String url, String id, String name, String uid, String type) {
        this.url = url;
        this.id = id;
        this.name = name;
        this.uid = uid;
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
