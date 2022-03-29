package ru.alamics.sso.remote.rias.model;

public class RiasCheckStatus {
    /**
     * Указанные данные найдены как минимум в одном профиле
     */
    public static final int DATA_FOUND = 1;
    /**
     * Указанные данные не найдены в профилях
     */
    public static final int DATA_NOT_FOUND = 0;
    /**
     * Не переданы данные для проверки
     */
    public static final int EMPTY_DATA_FOR_CHECK = -1;
    /**
     * Невалидные данные для проверки (данные не являются телефоном или емэйлом нужного формата)
     */
    public static final int INVALID_DATA_FOR_CHECK = -2;
}
