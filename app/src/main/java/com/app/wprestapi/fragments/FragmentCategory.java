package com.app.wprestapi.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.wprestapi.R;
import com.app.wprestapi.activities.ActivityCategoryDetail;
import com.app.wprestapi.adapters.AdapterCategory;
import com.app.wprestapi.models.categories.Category;
import com.app.wprestapi.rests.RestAdapter;
import com.app.wprestapi.utils.Constant;
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

public class FragmentCategory extends Fragment {

    View root_view;
    SwipeRefreshLayout swipe_refresh;
    private static final String TAG = "FragmentCategory";
    private Call<List<Category>> callbackCall = null;
    private RecyclerView recyclerView;
    private ShimmerFrameLayout lyt_shimmer;
    private AdapterCategory adapterCategory;
    private int post_total = 0;
    private int failed_page = 0;
    SharedPref sharedPref;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_category, container, false);
        sharedPref = new SharedPref(getActivity());
        initView();
        requestAction(1);

        return root_view;
    }

    public void initView() {
        lyt_shimmer = root_view.findViewById(R.id.shimmer_view_container);
        swipe_refresh = root_view.findViewById(R.id.swipe_refresh_layout);
        swipe_refresh.setColorSchemeResources(R.color.colorPrimary);

        recyclerView = root_view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setHasFixedSize(true);

        //set data and list adapter
        adapterCategory = new AdapterCategory(getActivity(), recyclerView, new ArrayList<>());
        recyclerView.setAdapter(adapterCategory);

        adapterCategory.setOnItemClickListener((view, obj, position) -> {
            Intent intent = new Intent(getActivity(), ActivityCategoryDetail.class);
            intent.putExtra(Constant.EXTRA_ID, obj.id);
            intent.putExtra(Constant.EXTRA_NAME, obj.name);
            startActivity(intent);
            sharedPref.saveCategoryId(obj.id);
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView v, int state) {
                super.onScrollStateChanged(v, state);
            }
        });

        // detect when scroll reach bottom
        adapterCategory.setOnLoadMoreListener(current_page -> {
            if (post_total > adapterCategory.getItemCount() && current_page != 0) {
                int next_page = current_page + 1;
                requestAction(next_page);
            } else {
                adapterCategory.setLoaded();
            }
        });

        // on swipe list
        swipe_refresh.setOnRefreshListener(() -> {
            if (callbackCall != null && callbackCall.isExecuted()) callbackCall.cancel();
            adapterCategory.resetListData();
            requestAction(1);
        });

    }

    private void requestAction(final int page_no) {
        showFailedView(false, "");
        showNoItemView(false);
        if (page_no == 1) {
            swipeProgress(true);
        } else {
            adapterCategory.setLoading();
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> requestCategoryAPI(page_no), 0);
    }

    private void displayApiResult(final List<Category> categories) {
        adapterCategory.insertData(categories);
        swipeProgress(false);
        if (categories.size() == 0) {
            showNoItemView(true);
        }
    }

    private void requestCategoryAPI(final int page_no) {
        callbackCall = RestAdapter.createAPI().getCategories(page_no, CATEGORIES_PER_PAGE, 0);
        callbackCall.enqueue(new Callback<List<Category>>() {
            public void onResponse(@NonNull Call<List<Category>> call, @NonNull Response<List<Category>> response) {
                List<Category> categories = response.body();
                Headers headers = response.headers();
                if (categories != null) {
                    String _post_total = headers.get("X-WP-Total");
                    assert _post_total != null;
                    post_total = Integer.parseInt(_post_total);
                    displayApiResult(categories);
                } else {
                    onFailRequest(page_no);
                }
            }

            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable th) {
                Log.e(TAG, th + "initialize failed");
                if (!call.isCanceled()) {
                    onFailRequest(page_no);
                }
            }
        });
    }

    private void onFailRequest(int page_no) {
        failed_page = page_no;
        adapterCategory.setLoaded();
        swipeProgress(false);
        if (Tools.isConnect(getActivity())) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.failed_text));
        }
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = root_view.findViewById(R.id.lyt_failed);
        ((TextView) root_view.findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        root_view.findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction(failed_page));
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = root_view.findViewById(R.id.lyt_no_item);
        ((TextView) root_view.findViewById(R.id.no_item_message)).setText(R.string.no_post_found);
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

}
