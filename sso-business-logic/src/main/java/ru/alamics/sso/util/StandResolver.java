package ru.alamics.sso.util;

public class StandResolver {

    public static final EStand ENV = notNull(System.getenv("SITE"), EStand.LOCAL); // площадка
    public static final String CONFIG_FOLDER = "config";
    public static final String ENV_CONFIG = CONFIG_FOLDER + "/" + ENV.name().toLowerCase(); // конфиг
    private static final boolean BATTLE = ENV.isBattle() || System.getenv("BATTLE") != null;

    private static EStand notNull(String env, EStand dflt) {

        if (env == null)
            return dflt;

        EStand st = EStand.get(env);
        if (st != null)
            return st;

        return dflt;
    }

    public static boolean isBattle() {
        return BATTLE;
    }
}
