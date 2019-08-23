package ru.alamics.sso.util;

import java.io.File;

public class StandResolver {

    public static final EStand ENV =  notNull(System.getProperty("SITE"), EStand.LOCAL); // площадка

    public static final String CONFIG_FOLDER = "config";
    public static final String ENV_CONFIG = CONFIG_FOLDER + File.separator + ENV.name().toLowerCase(); // конфиг

    private static EStand notNull(String env, EStand dflt) {

        if (env == null)
            return dflt;

        EStand st = EStand.get(env);
        if (st != null)
            return st;

        return dflt;
    }
}
