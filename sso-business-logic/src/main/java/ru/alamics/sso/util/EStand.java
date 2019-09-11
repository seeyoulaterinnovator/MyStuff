package ru.alamics.sso.util;

public enum EStand {

    LOCAL("local"),
    DEV("dev"),
    STAGE("stage"),
    PROD("prod");

    String val;

    EStand(String val) {
        this.val = val;
    }

    public static EStand get(String val) {
        if (val == null)
            return null;

        for (EStand st : EStand.values()) {
            if (st.val.equalsIgnoreCase(val))
                return st;
        }

        return null;
    }
}
