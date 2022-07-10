package application;

public abstract class PermissionHelper {
    public static int PERMISSION_LEVEL = 0;
    public static final int LEVEL_ADMIN = 1;
    public static final int LEVEL_USER = 2;

    public static void setPermissionLevel(int permissionLevel) {
        PERMISSION_LEVEL = permissionLevel;
    }

    public static String getStringFromPermissionLevel() {
        switch(PERMISSION_LEVEL) {
            case LEVEL_ADMIN:
                return Strings.PERMISSION_ADMIN;
            case LEVEL_USER:
            default:
                return Strings.PERMISSION_USER;
        }
    }

    public static String getStringFromPermissionLevel(int level) {
        switch(level) {
            case LEVEL_ADMIN:
                return Strings.PERMISSION_ADMIN;
            case LEVEL_USER:
            default:
                return Strings.PERMISSION_USER;
        }
    }

    public static int getIntFromPermissionString(String permission) {
        switch(permission) {
            case "Admin":
                return LEVEL_ADMIN;
            case "User":
            default:
                return LEVEL_USER;
        }
    }

    public static boolean isAdmin() {
        return PERMISSION_LEVEL == LEVEL_ADMIN;
    }
}
