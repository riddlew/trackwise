package model;

public class BasicIdName {
    protected int mId;
    protected String mName;

    public BasicIdName() {}

    public BasicIdName(int id, String name) {
        this.mId = id;
        this.mName = name;
    }

    public int getId() { return this.mId; }
    public void setId(int id) { this.mId = id; }

    public String getName() { return this.mName; }
    public void setName(String name) { this.mName = name; }

    @Override
    public String toString() {
        return this.mName;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof BasicIdName a) {
            return this.mName.equals(a.getName());
        }

        return false;
    }
}
