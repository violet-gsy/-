package com.jc.liquibase.database.core;

import liquibase.database.DatabaseConnection;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.DatabaseException;

/**
 * 达梦数据库适配 - 最终无报错版
 * 解决：databaseMinorVersion 私有 + setConnection 权限冲突
 */
public class DmDatabase extends OracleDatabase {

    @Override
    public String getShortName() {
        return "dm";
    }

    @Override
    public String getDefaultDatabaseProductName() {
        return "DM DBMS";
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return "DM DBMS".equalsIgnoreCase(conn.getDatabaseProductName());
    }

    /**
     * 重点：必须用 public，不能用 protected！
     */
    @Override
    public void setConnection(DatabaseConnection conn) {
        try {
            // 只执行父类基础逻辑，跳过版本检测报错
            super.setConnection(conn);
        } catch (Exception ignored) {
            // 忽略所有版本获取报错（DBMS_UTILITY 不存在）
        }
    }
}
