package com.app.wprestapi.rests;

import com.app.wprestapi.models.categories.Category;
import com.app.wprestapi.models.posts.Post;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface ApiInterface {

    String CACHE = "Cache-Control: max-age=0";
    String AGENT = "Data-Agent: WP REST API";

    @Headers({CACHE, AGENT})
    @GET("posts")
    Call<List<Post>> getPosts(
            @Query("_embed") boolean _embed,
            @Query("page") int page,
            @Query("per_page") int per_page
    );

    @Headers({CACHE, AGENT})
    @GET("posts")
    Call<List<Post>> getPostsByCategory(
            @Query("categories") long categories,
            @Query("_embed") boolean _embed,
            @Query("page") int page,
            @Query("per_page") int per_page
    );

    @Headers({CACHE, AGENT})
    @GET("categories")
    Call<List<Category>> getCategories(
            @Query("page") int page,
            @Query("per_page") int per_page,
            @Query("parent") int parent
    );

    @Headers({CACHE, AGENT})
    @GET(".")
    Call<Post> getPostDetail(
            @Query("_embed") boolean _embed
    );

}