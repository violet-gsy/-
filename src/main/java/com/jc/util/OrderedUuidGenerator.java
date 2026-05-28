package com.jc.util;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OrderedUuidGenerator implements IdentifierGenerator {

    // 用于数字型 ID（不用）
    @Override
    public Number nextId(Object entity) {
        return null;
    }

    // 核心：生成 32 位、无中划线、时间有序的 UUID v7
    @Override
    public String nextUUID(Object entity) {
        return UuidCreator.getTimeOrdered()
                .toString()
                .replace("-", "");
    }

}
