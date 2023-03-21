package com.app.wprestapi.models.posts.entities;

import java.io.Serializable;

public class FeaturedMedia implements Serializable {

    public long id;
    public String date;
    public String slug;
    public String type;
    public String link;
    public Title title;
    public String author;
    public String alt_text;
    public String media_type;
    public String mime_type;
    public MediaDetails media_details = null;
    public String source_url;

}
