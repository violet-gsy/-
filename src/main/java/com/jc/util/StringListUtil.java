package com.jc.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StringListUtil {

    /**
     * List<String> 转 分号; 分隔的字符串（存数据库）
     */
    public static String listToSemicolonStr(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return String.join(";", list);
    }

    /**
     * 分号; 分隔的字符串 转 List<String>（查库，JDK8 可用）
     */
    public static List<String> semicolonStrToList(String str) {
        if (str == null || str.trim().isEmpty()) {
            return Arrays.asList();
        }

        return Arrays.stream(str.split(";"))
                .map(String::trim)
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.toList());
    }
}
