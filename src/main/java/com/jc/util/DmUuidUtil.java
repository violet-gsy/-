package com.jc.util;

import com.fasterxml.uuid.Generators;

public class DmUuidUtil {
    // 生成达梦8最优的 32位有序UUID
    public static String get32Uuid() {
        return Generators.timeBasedGenerator()
                .generate()
                .toString()
                .replace("-", ""); // 去掉横杠
    }
}