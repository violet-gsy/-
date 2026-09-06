package com.jc.config;


import org.flowable.common.engine.impl.AbstractEngineConfiguration;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Properties;

/**
 * 让 Flowable 把达梦识别为 Oracle
 */
@Configuration
public class DamengDatabaseTypeConfig {

    @PostConstruct
    public void init() {
        Properties mappings = AbstractEngineConfiguration.getDefaultDatabaseTypeMappings();
        // 关键：DM DBMS → oracle
        mappings.setProperty("DM DBMS", "oracle");
    }
}
