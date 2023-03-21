package com.app.wprestapi.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.wprestapi.Config;
import com.app.wprestapi.R;
import com.app.wprestapi.adapters.AdapterChipsCategory;
import com.app.wprestapi.models.posts.Post;
import com.app.wprestapi.rests.RestAdapter;
import com.app.wprestapi.utils.AppBarLayoutBehavior;
import com.app.wprestapi.utils.Constant;
import com.app.wprestapi.utils.SharedPref;
import com.app.wprestapi.utils.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.AppBarLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.app.wprestapi.utils.Constant.EXTRA_OBJC;

public class ActivityPostDetail extends AppCompatActivity {

    private Call<Post> callbackCall = null;
    private SwipeRefreshLayout swipe_refresh;
    private ShimmerFrameLayout lyt_shimmer;
    private View lyt_main_content;
    private View lyt_uncategorized;
    TextView post_title;
    TextView post_date;
    ImageView post_image;
    RecyclerView post_categories;
    AdapterChipsCategory adapter;
    String original_html_data;
    String bg_paragraph;
    private WebView post_content;
    SharedPref sharedPref;
    Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        sharedPref = new SharedPref(this);

        post = (Post) getIntent().getSerializableExtra(EXTRA_OBJC);

        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        post_title = findViewById(R.id.post_title);
        post_date = findViewById(R.id.post_date);
        post_image = findViewById(R.id.post_image);
        post_categories = findViewById(R.id.recycler_view_categories);
        post_content = findViewById(R.id.post_content);

        lyt_main_content = findViewById(R.id.lyt_main_content);
        lyt_uncategorized = findViewById(R.id.view_uncategorized);

        lyt_shimmer = findViewById(R.id.shimmer_view_container);
        swipe_refresh = findViewById(R.id.swipe_refresh_layout);
        swipe_refresh.setColorSchemeResources(R.color.colorPrimary);
        swipe_refresh.setRefreshing(false);
        swipe_refresh.setOnRefreshListener(() -> {
            lyt_shimmer.setVisibility(View.VISIBLE);
            lyt_shimmer.startShimmer();
            lyt_main_content.setVisibility(View.GONE);
            requestAction();
        });

        requestAction();
        setupToolbar();

    }

    private void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle(post.title.rendered);
        }
    }

    private void requestAction() {
        showFailedView(false, "");
        swipeProgress(true);
        new Handler(Looper.getMainLooper()).postDelayed(this::requestPostData, Constant.DELAY_REFRESH);
    }

    private void requestPostData() {
        this.callbackCall = RestAdapter.createPostDetailAPI(post.id).getPostDetail(true);
        this.callbackCall.enqueue(new Callback<Post>() {
            public void onResponse(@NonNull Call<Post> call, @NonNull Response<Post> response) {
                Post resp = response.body();
                if (resp == null) {
                    onFailRequest();
                    Log.e("ActivityPostDetail", String.valueOf(response));
                    return;
                }
                displayData(resp);
                Log.e("ActivityPostDetail", "success");
                swipeProgress(false);
                lyt_main_content.setVisibility(View.VISIBLE);
            }

            public void onFailure(@NonNull Call<Post> call, @NonNull Throwable th) {
                Log.e("ActivityPostDetail", th.getMessage());
                if (!call.isCanceled()) {
                    onFailRequest();
                }
            }
        });
    }

    private void onFailRequest() {
        swipeProgress(false);
        lyt_main_content.setVisibility(View.GONE);
        if (Tools.isConnect(this)) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.failed_text));
        }
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed_home);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction());
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipe_refresh.setRefreshing(show);
            lyt_shimmer.setVisibility(View.GONE);
            lyt_shimmer.stopShimmer();
            lyt_main_content.setVisibility(View.VISIBLE);
            return;
        }
        swipe_refresh.post(() -> {
            swipe_refresh.setRefreshing(show);
            lyt_shimmer.setVisibility(View.VISIBLE);
            lyt_shimmer.startShimmer();
            lyt_main_content.setVisibility(View.GONE);
        });
    }

    private void displayData(Post post) {

        post_title.setText(Tools.capitalize(post.title.rendered));
        post_date.setText(Tools.getTimeAgo(post.date_gmt));

        if (!post.featured_media.equals("0")) {
            Glide.with(getApplicationContext())
                    .load(post._embedded.wp_featured_media.get(0).media_details.sizes.full.source_url.replace(" ", "%20"))
                    .thumbnail(0.1f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(post_image);
        } else {
            post_image.setImageResource(R.drawable.ic_no_image);
        }

        if (post._embedded.wp_term.get(0).size() > 0) {
            post_categories.setLayoutManager(new LinearLayoutManager(ActivityPostDetail.this, LinearLayoutManager.HORIZONTAL, false));
            adapter = new AdapterChipsCategory(ActivityPostDetail.this, post._embedded.wp_term.get(0));
            post_categories.setAdapter(adapter);

            adapter.setOnItemClickListener((view, items, position) -> {
                Intent intent = new Intent(getApplicationContext(), ActivityCategoryDetail.class);
                intent.putExtra(Constant.EXTRA_ID, items.get(position).id);
                intent.putExtra(Constant.EXTRA_NAME, items.get(position).name);
                startActivity(intent);
            });
        } else {
            lyt_uncategorized.setVisibility(View.VISIBLE);
        }

        Document html_data = Jsoup.parse(post.content.rendered);
        original_html_data = html_data.toString();
        String htmlText = original_html_data
                .replace("p class=\"arab\"", "p class=\"arab\" align=\"right\" style=\"font-size:150%;\" ")
                .replace("Oleh tim KhotbahJumat.com", "")
                .replace("Artikel www.KhotbahJumat.com", "");

        post_content.setBackgroundColor(Color.TRANSPARENT);
        post_content.getSettings().setDefaultTextEncodingName("UTF-8");
        post_content.setFocusableInTouchMode(false);
        post_content.setFocusable(false);

        post_content.getSettings().setJavaScriptEnabled(true);
        post_content.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        WebSettings webSettings = post_content.getSettings();
        if (sharedPref.getFontSize() == 0) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_XSMALL);
        } else if (sharedPref.getFontSize() == 1) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_SMALL);
        } else if (sharedPref.getFontSize() == 2) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
        } else if (sharedPref.getFontSize() == 3) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_LARGE);
        } else if (sharedPref.getFontSize() == 4) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_XLARGE);
        } else {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
        }

        String mimeType = "text/html; charset=UTF-8";
        String encoding = "utf-8";

        if (sharedPref.getIsDarkTheme()) {
            bg_paragraph = "<style type=\"text/css\">body{color: #eeeeee;} a{color:#ffffff; font-weight:bold;}";
        } else {
            bg_paragraph = "<style type=\"text/css\">body{color: #000000;} a{color:#1e88e5; font-weight:bold;}";
        }

//        bg_paragraph = "<style type=\"text/css\">body{color: #000000;}";
        String font_style_default = "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/fonts/custom_font.ttf\")}body {font-family: MyFont;font-size: medium; text-align: left;}</style>";
        //String font_style_justify = "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/fonts/custom_font.ttf\")}body {font-family: MyFont;font-size: medium;text-align: justify;}</style>";

        String text_default = "<html><head>"
                + font_style_default
                + "<style>img{max-width:100%;height:auto;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> "
                + bg_paragraph
                + "</style></head>"
                + "<body>"
                + htmlText
                + "</body></html>";

        String text_rtl = "<html dir='rtl'><head>"
                + "<style>img{max-width:100%;height:auto;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> "
                + bg_paragraph
                + "</style></head>"
                + "<body>"
                + htmlText
                + "</body></html>";

        FrameLayout customViewContainer = findViewById(R.id.customViewContainer);
        post_content.setWebChromeClient(new WebChromeClient() {
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                post_content.setVisibility(View.INVISIBLE);
                customViewContainer.setVisibility(View.VISIBLE);
                customViewContainer.addView(view);
                Tools.darkNavigation(ActivityPostDetail.this);
            }

            public void onHideCustomView() {
                super.onHideCustomView();
                post_content.setVisibility(View.VISIBLE);
                customViewContainer.setVisibility(View.GONE);
                Tools.lightNavigation(ActivityPostDetail.this);
            }
        });

        if (Config.ENABLE_RTL_MODE) {
            post_content.loadDataWithBaseURL(null, text_rtl, mimeType, encoding, null);
        } else {
            post_content.loadDataWithBaseURL(null, text_default, mimeType, encoding, null);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

}
