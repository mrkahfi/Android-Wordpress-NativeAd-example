package com.app.wprestapi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.wprestapi.R;
import com.app.wprestapi.adapters.AdapterPost;
import com.app.wprestapi.adapters.AdapterSubCategory;
import com.app.wprestapi.models.categories.Category;
import com.app.wprestapi.models.posts.Post;
import com.app.wprestapi.rests.RestAdapter;
import com.app.wprestapi.utils.Constant;
import com.app.wprestapi.utils.ItemOffsetDecoration;
import com.app.wprestapi.utils.SharedPref;
import com.app.wprestapi.utils.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.app.wprestapi.Config.CATEGORIES_PER_PAGE;
import static com.app.wprestapi.Config.POSTS_PER_PAGE;
import static com.app.wprestapi.utils.Constant.EXTRA_OBJC;

public class ActivityCategoryDetail extends AppCompatActivity {

    SwipeRefreshLayout swipe_refresh;
    private static final String TAG = "ActivityCategoryDetail";
    private Call<List<Post>> callbackCall = null;
    private Call<List<Category>> callbackCallCategory = null;
    private RecyclerView recyclerView;
    private ShimmerFrameLayout lyt_shimmer;
    private AdapterPost adapterPost;
    private int post_total = 0;
    private int failed_page = 0;
    private int category_id;
    private String category_name;
    AdapterSubCategory adapterSubCategory;
    RecyclerView recycler_view_sub_categories;
    SharedPref sharedPref;
    RelativeLayout lyt_sub_categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_detail);
        sharedPref = new SharedPref(this);

        category_id = getIntent().getIntExtra(Constant.EXTRA_ID, 0);
        category_name = getIntent().getStringExtra(Constant.EXTRA_NAME);

        initView();
        requestAction(1);
        requestActionSubCategory(1);
        setupToolbar();
    }

    private void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle(Tools.capitalize(category_name));
        }
    }

    public void initView() {
        lyt_shimmer = findViewById(R.id.shimmer_view_container);
        swipe_refresh = findViewById(R.id.swipe_refresh_layout);
        swipe_refresh.setColorSchemeResources(R.color.colorPrimary);
        lyt_sub_categories = findViewById(R.id.lyt_sub_categories);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(getApplicationContext(), R.dimen.spacing_small);
        if (0 == recyclerView.getItemDecorationCount()) {
            recyclerView.addItemDecoration(itemDecoration);
        }
        recyclerView.setHasFixedSize(true);

        //set data and list adapter
        adapterPost = new AdapterPost(ActivityCategoryDetail.this, recyclerView, new ArrayList<>());
        recyclerView.setAdapter(adapterPost);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView v, int state) {
                super.onScrollStateChanged(v, state);
            }
        });

        adapterPost.setOnItemClickListener((view, obj, position) -> {
            Intent intent = new Intent(getApplicationContext(), ActivityPostDetail.class);
            intent.putExtra(EXTRA_OBJC, obj);
            startActivity(intent);
        });

        // detect when scroll reach bottom
        adapterPost.setOnLoadMoreListener(current_page -> {
            if (post_total > adapterPost.getItemCount() && current_page != 0) {
                int next_page = current_page + 1;
                requestAction(next_page);
            } else {
                adapterPost.setLoaded();
            }
        });

        // on swipe list
        swipe_refresh.setOnRefreshListener(() -> {
            if (callbackCall != null && callbackCall.isExecuted()) callbackCall.cancel();
            adapterPost.resetListData();
            requestAction(1);
        });

        recycler_view_sub_categories = findViewById(R.id.recycler_view_sub_categories);
        recycler_view_sub_categories.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
        //recycler_view_sub_categories.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        //recycler_view_sub_categories.setHasFixedSize(true);
//        final LinearSnapHelper snapHelper = new LinearSnapHelper();
//        snapHelper.attachToRecyclerView(recycler_view_sub_categories);
//        recycler_view_sub_categories.setOnFlingListener(snapHelper);

        //set data and list adapter
        adapterSubCategory = new AdapterSubCategory(ActivityCategoryDetail.this, recycler_view_sub_categories, new ArrayList<>());
        recycler_view_sub_categories.setAdapter(adapterSubCategory);

        adapterSubCategory.setOnItemClickListener((view, obj, position) -> {
            adapterPost.resetListData();
            sharedPref.saveCategoryId(obj.id);
            new Handler(Looper.getMainLooper()).postDelayed(() -> requestAction(1), 10);
        });

        recycler_view_sub_categories.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView v, int state) {
                super.onScrollStateChanged(v, state);
            }
        });

        // detect when scroll reach bottom
        adapterSubCategory.setOnLoadMoreListener(current_page -> {
            if (post_total > adapterSubCategory.getItemCount() && current_page != 0) {
                int next_page = current_page + 1;
                requestActionSubCategory(next_page);
            } else {
                adapterSubCategory.setLoaded();
            }
        });

    }

    private void requestAction(final int page_no) {
        showFailedView(false, "");
        showNoItemView(false);
        if (page_no == 1) {
            swipeProgress(true);
        } else {
            adapterPost.setLoading();
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> requestPostAPI(page_no), 0);
    }

    private void requestActionSubCategory(final int page_no) {
        //showFailedView(false, "");
        //showNoItemView(false);
        if (page_no == 1) {
            swipeProgress(true);
        } else {
            adapterSubCategory.setLoading();
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> requestSubCategoryAPI(page_no), 0);
    }

    private void displayApiResult(final List<Post> posts) {
        adapterPost.insertData(posts);
        swipeProgress(false);
        if (posts.size() == 0) {
            showNoItemView(true);
        }
    }

    private void requestPostAPI(final int page_no) {
        callbackCall = RestAdapter.createAPI().getPostsByCategory(sharedPref.getCategoryId(), true, page_no, POSTS_PER_PAGE);
        callbackCall.enqueue(new Callback<List<Post>>() {
            public void onResponse(@NonNull Call<List<Post>> call, @NonNull Response<List<Post>> response) {
                List<Post> posts = response.body();
                Headers headers = response.headers();
                if (posts != null) {
                    String _post_total = headers.get("X-WP-Total");
                    assert _post_total != null;
                    post_total = Integer.parseInt(_post_total);
                    displayApiResult(posts);
                    Log.d(TAG, "" + response);
                } else {
                    onFailRequest(page_no);
                }
            }

            public void onFailure(@NonNull Call<List<Post>> call, @NonNull Throwable th) {
                Log.e(TAG, th + "initialize failed");
                if (!call.isCanceled()) {
                    onFailRequest(page_no);
                }
            }
        });
    }

    private void requestSubCategoryAPI(final int page_no) {
        callbackCallCategory = RestAdapter.createAPI().getCategories(page_no, CATEGORIES_PER_PAGE, category_id);
        callbackCallCategory.enqueue(new Callback<List<Category>>() {
            public void onResponse(@NonNull Call<List<Category>> call, @NonNull Response<List<Category>> response) {
                List<Category> categories = response.body();
                Headers headers = response.headers();
                if (categories != null) {
                    String _post_total = headers.get("X-WP-Total");
                    assert _post_total != null;
                    post_total = Integer.parseInt(_post_total);
                    adapterSubCategory.insertData(categories);

                    if (categories.size() > 0) {
                        lyt_sub_categories.setVisibility(View.VISIBLE);
                    } else {
                        lyt_sub_categories.setVisibility(View.GONE);
                    }

                    Log.d(TAG, "" + response);
                }
            }

            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable th) {
                call.isCanceled();
            }
        });
    }

    private void onFailRequest(int page_no) {
        failed_page = page_no;
        adapterPost.setLoaded();
        swipeProgress(false);
        if (Tools.isConnect(ActivityCategoryDetail.this)) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.failed_text));
        }
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction(failed_page));
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText(R.string.no_post_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipe_refresh.setRefreshing(show);
            lyt_shimmer.setVisibility(View.GONE);
            lyt_shimmer.stopShimmer();
            return;
        }
        swipe_refresh.post(() -> {
            swipe_refresh.setRefreshing(show);
            lyt_shimmer.setVisibility(View.VISIBLE);
            lyt_shimmer.startShimmer();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgress(false);
        if (callbackCall != null && callbackCall.isExecuted()) {
            callbackCall.cancel();
        }
        lyt_shimmer.stopShimmer();
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
