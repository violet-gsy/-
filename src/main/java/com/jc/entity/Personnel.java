package com.jc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 人员表
 * @TableName T_PERSONNEL
 */
@TableName(value ="T_PERSONNEL")
@Schema(description = "人员表实体")
@Data
public class Personnel implements Serializable {
    /**
     * 工号ID
     */
    @TableId(value = "USERID",type = IdType.ASSIGN_UUID)
    @Schema(description = "工号ID")
    private String userid;

    /**
     * 专业ID
     */
    @TableField(value = "MAJORID")
    @Schema(description = "专业ID")
    private String majorid;

    /**
     * 名称
     */
    @TableField(value = "USERNAME")
    @Schema(description = "名称")
    private String username;

    /**
     * 性别
     */
    @TableField(value = "SEX")
    @Schema(description = "性别")
    private String sex;

    /**
     * 岗位
     */
    @TableField(value = "POST")
    @Schema(description = "岗位")
    private String post;

    /**
     * 工作地点
     */
    @TableField(value = "WORKLOCATION")
    @Schema(description = "工作地点")
    private String worklocation;

    /**
     * 工作状态
     */
    @TableField(value = "WORKSTATUS")
    @Schema(description = "工作状态")
    private String workstatus;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}