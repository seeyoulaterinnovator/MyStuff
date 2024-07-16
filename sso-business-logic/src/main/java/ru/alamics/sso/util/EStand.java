package ru.alamics.sso.util;

public enum EStand {

    LOCAL("local", false),
    DEV("dev", false),
    TEST("test", true),
    STAGE("stage", true),
    PROD("prod", true);

    String val;
    boolean battle;

    EStand(String val, boolean battle) {
        this.val = val;
        this.battle = battle;
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

    public boolean isBattle() {
        return battle;
    }
}
