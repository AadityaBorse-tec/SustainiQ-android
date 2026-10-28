package com.sustainiq.app.data.model;

import com.sustainiq.app.R;

public enum ReportCategory {
    WASTE("Waste", "Littering, dumping", R.drawable.ic_waste, R.color.cat_waste, R.drawable.bg_category_waste),
    WATER("Water", "Leaks, drainage", R.drawable.ic_water, R.color.cat_water, R.drawable.bg_category_water),
    AIR("Air", "Pollution, smoke", R.drawable.ic_air, R.color.cat_air, R.drawable.bg_category_air),
    GREEN_SPACES("Green spaces", "Trees, parks", R.drawable.ic_green_spaces, R.color.cat_green, R.drawable.bg_category_green),
    OTHER("Other", "Environmental concerns", R.drawable.ic_leaf, R.color.cat_other, R.drawable.bg_category_waste);

    private final String displayName;
    private final String subtitle;
    private final int iconRes;
    private final int colorRes;
    private final int bgDrawableRes;

    ReportCategory(String displayName, String subtitle, int iconRes, int colorRes, int bgDrawableRes) {
        this.displayName = displayName;
        this.subtitle = subtitle;
        this.iconRes = iconRes;
        this.colorRes = colorRes;
        this.bgDrawableRes = bgDrawableRes;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public int getIconRes() {
        return iconRes;
    }

    public int getColorRes() {
        return colorRes;
    }

    public int getBgDrawableRes() {
        return bgDrawableRes;
    }
}
