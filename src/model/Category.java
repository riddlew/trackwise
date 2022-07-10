package model;

public class Category extends BasicIdName {
    public Category() {}

    public Category(int id, String name) {
        super(id, name);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Category a) {
            return this.mName.equals(a.getName());
        }

        return false;
    }
}
