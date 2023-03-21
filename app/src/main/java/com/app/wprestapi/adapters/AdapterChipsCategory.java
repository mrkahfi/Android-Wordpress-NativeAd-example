package com.app.wprestapi.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.wprestapi.R;
import com.app.wprestapi.models.posts.entities.Term;

import java.util.List;

public class AdapterChipsCategory extends RecyclerView.Adapter<AdapterChipsCategory.ViewHolder> {

    Context context;
    List<Term> items;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view,  List<Term> items, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterChipsCategory(Context context, List<Term> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public AdapterChipsCategory.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chips_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AdapterChipsCategory.ViewHolder holder, int position) {
        final Term term = (Term) items.get(position);

        holder.title.setText(term.name);

        holder.title.setOnClickListener(view -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, items, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        LinearLayout lyt_label;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.txt_label);
            lyt_label = view.findViewById(R.id.lyt_label);
        }
    }

}