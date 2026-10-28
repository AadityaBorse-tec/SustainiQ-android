package com.sustainiq.app.ui.onboarding;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sustainiq.app.R;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.PageViewHolder> {

    private static final int[] ICONS = {
            R.drawable.ic_reports,
            R.drawable.ic_history,
            R.drawable.ic_impact
    };

    private static final int[] TITLES = {
            R.string.onboarding_title_1,
            R.string.onboarding_title_2,
            R.string.onboarding_title_3
    };

    private static final int[] DESCRIPTIONS = {
            R.string.onboarding_desc_1,
            R.string.onboarding_desc_2,
            R.string.onboarding_desc_3
    };

    private final Context context;

    public OnboardingAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.fragment_onboarding_page, parent, false);
        return new PageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        holder.icon.setImageResource(ICONS[position]);
        holder.title.setText(TITLES[position]);
        holder.description.setText(DESCRIPTIONS[position]);
    }

    @Override
    public int getItemCount() {
        return TITLES.length;
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;
        TextView description;

        PageViewHolder(@NonNull View itemView) {
            super(itemView);
            icon        = itemView.findViewById(R.id.page_icon);
            title       = itemView.findViewById(R.id.page_title);
            description = itemView.findViewById(R.id.page_description);
        }
    }
}
