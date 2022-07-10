package model;

public class Subcategory extends Category {
    private int mCategoryId;

    public Subcategory() {}

    public Subcategory(int id, String name, int categoryId) {
        this.mId = id;
        this.mName = name;
        this.mCategoryId = categoryId;
    }

    public int getCategoryId() { return this.mCategoryId; }
    public void setCategoryId(int categoryId) { this.mCategoryId = categoryId; }

    @Override
    public String toString() {
        return this.mName;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Subcategory a) {
            return this.mName.equals(a.getName()) && this.mCategoryId == a.getCategoryId();
        }

        return false;
    }
}
