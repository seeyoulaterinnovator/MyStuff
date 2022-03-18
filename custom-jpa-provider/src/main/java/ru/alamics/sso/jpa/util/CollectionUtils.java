package ru.alamics.sso.jpa.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
public class CollectionUtils {

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <S, T extends List<S>> S nullOrGet(T list, int index) {
        return isEmpty(list) || list.size() <= index ? null : list.get(index);
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isNotEmpty(String str) {
        return !StringUtils.isEmpty(str);
    }
}
