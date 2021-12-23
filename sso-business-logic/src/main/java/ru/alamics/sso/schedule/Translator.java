package ru.alamics.sso.schedule;

import java.util.concurrent.TimeUnit;

// TODO make like linkExpirationFormatter
public class Translator {
    private final static String[]  DECLENSIONS_DAYS = {"день", "дня", "дней"};
    private final static String[]  DECLENSIONS_HOURS = {"час", "часа", "часов"};
    private final static String[]  DECLENSIONS_MINUTES = {"минуту", "минуты", "минут"};
    private final static String[]  DECLENSIONS_SECONDS = {"секунду", "секунды", "секунд"};

    public static String getRusTranslateTimeUnit(String value, TimeUnit unit) {
        int time = Integer.parseInt(value);
        return getRusTimeUnit(time, unit);
    }

    public static String getRusTranslateTimeUnitBySec(int time) {
        TimeUnit unit = TimeUnit.SECONDS;
        if (time > 0 && time % 60 == 0) {
            unit = TimeUnit.MINUTES;
            time = time / 60;
            if (time % 60 == 0) {
                unit = TimeUnit.HOURS;
                time = time / 60;
                if (time % 24 == 0) {
                    unit = TimeUnit.DAYS;
                    time = time / 24;
                }
            }
        }
        return time + " " + getRusTimeUnit(time, unit);
    }

    private static String getRusTimeUnit(int time, TimeUnit unit){
        switch (unit) {
            case DAYS:
                return getDeclantion(time, DECLENSIONS_DAYS);
            case HOURS:
                return getDeclantion(time, DECLENSIONS_HOURS);
            case MINUTES:
                return getDeclantion(time, DECLENSIONS_MINUTES);
            case SECONDS:
                return getDeclantion(time, DECLENSIONS_SECONDS);
            default:
                return "";
        }
    }

    private static String getDeclantion(int num, String[] declensions) {
        int preLastDigit = (num % 100) / 10;
        if (preLastDigit == 1) {
            return declensions[2];
        }

        switch (num % 10) {
            case 1:
                return declensions[0];
            case 2:
            case 3:
            case 4:
                return declensions[1];
            default:
                return declensions[2];
        }
    }
}
