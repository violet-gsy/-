package com.jc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 人员表
 * @TableName T_PERSONNEL
 */
@TableName(value ="T_PERSONNEL")
@Data
public class Personnel implements Serializable {
    /**
     * 工号ID
     */
    @TableId(value = "USERID")
    private String userid;

    /**
     * 专业ID
     */
    @TableField(value = "MAJORID")
    private String majorid;

    /**
     * 名称
     */
    @TableField(value = "USERNAME")
    private String username;

    /**
     * 性别
     */
    @TableField(value = "SEX")
    private String sex;

    /**
     * 岗位
     */
    @TableField(value = "POST")
    private String post;

    /**
     * 工作地点
     */
    @TableField(value = "WORKLOCATION")
    private String worklocation;

    /**
     * 工作状态
     */
    @TableField(value = "WORKSTATUS")
    private String workstatus;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}