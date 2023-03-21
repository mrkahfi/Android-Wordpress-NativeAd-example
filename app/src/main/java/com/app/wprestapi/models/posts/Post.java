package com.app.wprestapi.models.posts;

import com.app.wprestapi.models.posts.entities.Content;
import com.app.wprestapi.models.posts.entities.Embedded;
import com.app.wprestapi.models.posts.entities.Excerpt;
import com.app.wprestapi.models.posts.entities.Guid;
import com.app.wprestapi.models.posts.entities.Title;

import java.io.Serializable;

public class Post implements Serializable {

    public long id;
    public String date;
    public String date_gmt;
    public Guid guid = null;
    public String modified;
    public String modified_gmt;
    public String slug;
    public String status;
    public String type;
    public String link;
    public Title title = null;
    public Content content = null;
    public Excerpt excerpt = null;
    public String author;
    public String featured_media;
    public String comment_status;
    public String ping_status;
    public boolean sticky;
    public String template;
    public String format;
    public Embedded _embedded = null;

}
