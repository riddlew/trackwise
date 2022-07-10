package model;

import java.time.LocalTime;

public class Issue {
    protected int mId;
    protected LocalTime mStartTime;
    protected LocalTime mEndTime;
    protected Category mCategory;
    protected Subcategory mSubcategory;
    protected String mNotes;

    public Issue() {}

    public Issue(int id, LocalTime startTime, LocalTime endTime, Category category, Subcategory subcategory) {
        this.mId = id;
        this.mStartTime = startTime;
        this.mEndTime = endTime;
        this.mCategory = category;
        this.mSubcategory = subcategory;
        this.mNotes = "";
    }

    public Issue(int id, LocalTime startTime, LocalTime endTime, Category category, Subcategory subcategory, String notes) {
        this(id, startTime, endTime, category, subcategory);
        this.mNotes = notes;
    }

    public int getId() { return this.mId; }
    public void setId(int id) { this.mId = id; }

    public LocalTime getStartTime() { return this.mStartTime; }
    public void setStartTime(LocalTime startTime) { this.mStartTime = startTime; }

    public LocalTime getEndTime() { return this.mEndTime; }
    public void setEndTime(LocalTime endTime) { this.mEndTime = endTime; }

    public Category getCategory() { return this.mCategory; }
    public void setCategory(Category category) { this.mCategory = category; }

    public Subcategory getSubcategory() { return this.mSubcategory; }
    public void setSubcategory(Subcategory subcategory) { this.mSubcategory = subcategory; }

    public String getNotes() { return this.mNotes; }
    public void setNotes(String notes) { this.mNotes = notes; }


}
