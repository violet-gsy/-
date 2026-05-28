package com.jc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 自定义表头
 * @TableName T_TABLE_COLUMN
 */
@TableName(value ="T_TABLE_COLUMN")
@Schema(description = "自定义表头实体")
@Data
public class TableColumn implements Serializable {
    /**
     * 类型
     */
    @TableField(value = "CODE")
    @Schema(description = "类型")
    private String code;

    /**
     * 字段标识
     */
    @TableField(value = "KEY")
    @Schema(description = "字段标识")
    private String key;

    /**
     * 字段名称
     */
    @TableField(value = "NAME")
    @Schema(description = "字段名称")
    private String name;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}