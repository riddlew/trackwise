package model;

import java.time.Duration;

public class Downtime {
    protected Category mCategory;
    protected Subcategory mSubcategory;
    private Duration mDuration;

    public Downtime() {}

    public Downtime(Category category, Subcategory subcategory, Duration duration) {
        this.mCategory = category;
        this.mSubcategory = subcategory;
        this.mDuration = duration;
    }

    public Category getCategory() { return this.mCategory; }
    public void setCategory(Category category) { this.mCategory = category; }

    public Subcategory getSubcategory() { return this.mSubcategory; }
    public void setSubcategory(Subcategory subcategory) { this.mSubcategory = subcategory; }

    public Duration getDuration() {return this.mDuration;}
    public void setDuration(Duration duration) {this.mDuration = duration;}

    public void addDuration(Duration duration) { this.setDuration(this.mDuration.plus(duration));}
    public void minusDuration(Duration duration) { this.setDuration(this.mDuration.minus(duration));}
}