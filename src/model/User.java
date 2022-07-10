package model;

public class User {
    private int mId;
    private BasicIdName mDepartment;
    private BasicIdName mShift;
    private int mPermissionLevel;
    private String mUsername;
    private boolean mPasswordChange;

    public User() {}

    public User(int id, BasicIdName department, BasicIdName shift, int permissionLevel, String username, boolean passwordChange) {
        this.mId = id;
        this.mDepartment = department;
        this.mShift = shift;
        this.mPermissionLevel = permissionLevel;
        this.mUsername = username;
        this.mPasswordChange = passwordChange;
    }

    public int getId() { return this.mId; }
    public void setId(int id) { this.mId = id; }

    public BasicIdName getDepartment() { return this.mDepartment; }
    public void setDepartment(BasicIdName department) { this.mDepartment = department; }

    public BasicIdName getShift() { return this.mShift; }
    public void setShift(BasicIdName shift) { this.mShift = shift; }

    public int getPermissionLevel() { return this.mPermissionLevel; }
    public void setPermissionLevel(int permissionLevel) { this.mPermissionLevel = permissionLevel; }

    public String getUsername() { return this.mUsername; }
    public void setUsername(String username) { this.mUsername = username; }

    public boolean getPasswordChange() { return this.mPasswordChange; }
    public void setPasswordChange(boolean passwordChange) { this.mPasswordChange = passwordChange; }
}
