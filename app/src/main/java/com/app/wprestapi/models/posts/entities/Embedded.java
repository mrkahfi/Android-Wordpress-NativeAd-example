package com.app.wprestapi.models.posts.entities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Embedded implements Serializable {

    public List<Author> author = new ArrayList<>();

    @SerializedName("wp:featuredmedia")
    @Expose
    public List<FeaturedMedia> wp_featured_media = new ArrayList<>();

    @SerializedName("wp:term")
    @Expose
    public List<List<Term>> wp_term = new ArrayList<>();

}