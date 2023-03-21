package com.app.wprestapi.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.wprestapi.Config;
import com.app.wprestapi.R;
import com.app.wprestapi.activities.ActivityCategoryDetail;
import com.app.wprestapi.models.posts.Post;
import com.app.wprestapi.utils.Constant;
import com.app.wprestapi.utils.TemplateView;
import com.app.wprestapi.utils.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.MediaView;

import java.util.List;

import static com.app.wprestapi.Config.POSTS_PER_PAGE;

public class AdapterPost extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_PROG = 0;
    private final int VIEW_ITEM = 1;
    private final int VIEW_AD = 2;
    private List<Post> items;
    private Context context;
    private OnItemClickListener mOnItemClickListener;
    private OnItemOverflowClickListener mOnItemOverflowClickListener;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    private boolean scrolling = false;
    AdapterChipsCategory adapter;

    public interface OnItemClickListener {
        void onItemClick(View view, Post obj, int position);
    }

    public interface OnItemOverflowClickListener {
        void onItemOverflowClick(View view, Post obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public void setOnItemOverflowClickListener(final OnItemOverflowClickListener mItemOverflowClickListener) {
        this.mOnItemOverflowClickListener = mItemOverflowClickListener;
    }

    public AdapterPost(Context context, RecyclerView view, List<Post> items) {
        this.items = items;
        this.context = context;
        lastItemViewDetector(view);
        view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    scrolling = true;
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    scrolling = false;
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    public static class OriginalViewHolder extends RecyclerView.ViewHolder {

        public TextView post_title;
        public TextView post_excerpt;
        public TextView post_date;
        public RecyclerView post_categories;
        public ImageView post_image;
        public LinearLayout lyt_parent;
        public ImageView img_overflow;

        public OriginalViewHolder(View v) {
            super(v);
            post_title = v.findViewById(R.id.post_title);
            post_excerpt = v.findViewById(R.id.post_excerpt);
            post_date = v.findViewById(R.id.post_date);
            post_categories = v.findViewById(R.id.post_categories);
            post_image = v.findViewById(R.id.post_image);
            lyt_parent = v.findViewById(R.id.lyt_parent);
            img_overflow = v.findViewById(R.id.img_overflow);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {

        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }

    }

    class adViewHolder extends RecyclerView.ViewHolder {
        MediaView mediaView;
        TemplateView Adtemplate;

        public adViewHolder(@NonNull View itemView) {
            super(itemView);
            Adtemplate = itemView.findViewById(R.id.native_admob_container);
            mediaView = itemView.findViewById(R.id.media_view);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
            vh = new OriginalViewHolder(v);
        } else if (viewType == VIEW_AD) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.include_native_ad, parent, false);
            vh = new adViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_more, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final Post p = (Post) items.get(position);
            final OriginalViewHolder vItem = (OriginalViewHolder) holder;

            vItem.post_title.setText(Html.fromHtml(Tools.capitalize(p.title.rendered)));
            vItem.post_excerpt.setText(Html.fromHtml(p.excerpt.rendered));
            vItem.post_date.setText(Tools.getTimeAgo(p.date_gmt));

            vItem.post_categories.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            adapter = new AdapterChipsCategory(context, p._embedded.wp_term.get(0));
            vItem.post_categories.setAdapter(adapter);
            adapter.setOnItemClickListener((view, items, pos) -> {
                Intent intent = new Intent(context, ActivityCategoryDetail.class);
                intent.putExtra(Constant.EXTRA_ID, items.get(pos).id);
                intent.putExtra(Constant.EXTRA_NAME, items.get(pos).name);
                context.startActivity(intent);
            });

            if (Config.DISPLAY_SHORT_DESCRIPTION_IN_POST_LIST) {
                vItem.post_title.setMaxLines(2);
                vItem.post_excerpt.setVisibility(View.VISIBLE);
            } else {
                vItem.post_title.setMaxLines(2);
                vItem.post_excerpt.setVisibility(View.GONE);
            }

            if (Config.DISPLAY_CATEGORIES_IN_POST_LIST) {
                vItem.post_categories.setVisibility(View.VISIBLE);
            } else {
                vItem.post_categories.setVisibility(View.GONE);
            }

            if (p._embedded.wp_featured_media.size() > 0) {
                Glide.with(context)
                        .load(p._embedded.wp_featured_media.get(0).media_details.sizes.thumbnail.source_url.replace(" ", "%20"))
                        .thumbnail(0.1f)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(vItem.post_image);

            } else {
                vItem.post_image.setImageResource(R.drawable.ic_no_image);
            }

            vItem.lyt_parent.setOnClickListener(view -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, p, position);
                }
            });

            vItem.img_overflow.setOnClickListener(view -> {
                if (mOnItemOverflowClickListener != null) {
                    mOnItemOverflowClickListener.onItemOverflowClick(view, p, position);
                }
            });

        } else if (holder instanceof adViewHolder) {
            TemplateView template = ((adViewHolder) holder).Adtemplate;
            MediaView mediaView = ((adViewHolder) holder).mediaView;
            loadAdMobNativeAd(template, mediaView);
        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }

        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
        if (getItemViewType(position) == VIEW_PROG || getItemViewType(position) == VIEW_AD) {
            layoutParams.setFullSpan(true);
        } else {
            layoutParams.setFullSpan(false);
        }

    }

    public void loadAdMobNativeAd(TemplateView native_template, MediaView mediaView) {
        AdLoader adLoader = new AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110")
                .forNativeAd(NativeAd -> {
                    ColorDrawable colorDrawable = new ColorDrawable(ContextCompat.getColor(context, R.color.colorWhite));
                    com.app.wprestapi.utils.NativeTemplateStyle styles = new com.app.wprestapi.utils.NativeTemplateStyle.Builder().withMainBackgroundColor(colorDrawable).build();
                    native_template.setStyles(styles);
                    mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
                    native_template.setNativeAd(NativeAd);
                    native_template.setVisibility(View.VISIBLE);
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        native_template.setVisibility(View.GONE);
                    }
                })
                .build();

        adLoader.loadAd(Tools.getAdRequest());
    }

    public void insertData(List<Post> items) {
        setLoaded();
        int positionStart = getItemCount();

        for (Post post :
                items) {
            Log.d("item", "TITLE: " + post.title.rendered);
        }

        // if there are more than POST_LAST_POSITION_BEFORE_AD new posts
        // them insert a new fake Post to represent an Ad
        // Fake Post is Post that doesn't contain any data (title, desc, etc)
        if (items.size() >= Config.POST_LAST_POSITION_BEFORE_AD)
            items.add(Config.POST_LAST_POSITION_BEFORE_AD, new Post());

        int itemCount = items.size();

        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    public void setLoaded() {
        loading = false;
        for (int i = 0; i < getItemCount(); i++) {
            if (items.get(i) == null) {
                items.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public void setLoading() {
        if (getItemCount() != 0) {
            this.items.add(null);
            notifyItemInserted(getItemCount() - 1);
            loading = true;
        }
    }

    public void insertAd() {
        if (getItemCount() != 0) {
            this.items.add(new Post());
            notifyItemInserted(getItemCount() - 1);
        }
    }

    public void resetListData() {
        this.items.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        Post post = items.get(position);
        if (post != null) {
            // Real Post should contain some data such as title, desc, and so on.
            // A Post having no title etc is assumed to be a fake Post which represents an Ad view
            if (post.title == null) {
                return VIEW_AD;
            }
            return VIEW_ITEM;
        } else {
            return VIEW_PROG;
        }

    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void lastItemViewDetector(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            final StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int lastPos = getLastVisibleItem(layoutManager.findLastVisibleItemPositions(null));
                    if (!loading && lastPos == getItemCount() - 1 && onLoadMoreListener != null) {
                        int current_page = getItemCount() / (POSTS_PER_PAGE + 1); // Posts per page plus 1 Ad
                        onLoadMoreListener.onLoadMore(current_page);
                        loading = true;
                    }
                }
            });
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

    private int getLastVisibleItem(int[] into) {
        int last_idx = into[0];
        for (int i : into) {
            if (last_idx < i) last_idx = i;
        }
        return last_idx;
    }

}