package com.example.skillcert.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 逗号分隔字符串与 List 互转工具
 */
public final class ListUtil {

    private ListUtil() {}

    public static List<String> split(String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public static List<Long> splitLong(String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> list = new ArrayList<>();
        for (String s : csv.split(",")) {
            String t = s.trim();
            if (!t.isEmpty()) {
                try {
                    list.add(Long.valueOf(t));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return list;
    }

    public static String join(List<?> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }
}
