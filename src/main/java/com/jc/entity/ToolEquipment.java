package com.jc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 工器具表
 * @TableName T_TOOL_EQUIPMENT
 */
@TableName(value ="T_TOOL_EQUIPMENT")
@Data
public class ToolEquipment implements Serializable {
    /**
     * 工器具id
     */
    @TableId(value = "TOOLEQPID")
    private String tooleqpid;

    /**
     * 专业ID
     */
    @TableField(value = "MAJORID")
    private String majorid;

    /**
     * 工器具名称
     */
    @TableField(value = "TOOLNAME")
    private String toolname;

    /**
     * 工器具类型
     */
    @TableField(value = "TOOLTYPE")
    private String tooltype;

    /**
     * 状态
     */
    @TableField(value = "STATUS")
    private String status;

    /**
     * 购置日期
     */
    @TableField(value = "PURCHASEDATE")
    private LocalDateTime purchasedate;

    /**
     * 存放位置
     */
    @TableField(value = "LOCATION")
    private String location;

    /**
     * 备注
     */
    @TableField(value = "REMARK")
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}