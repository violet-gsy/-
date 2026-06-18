package com.jc.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 业务流程表
 * @TableName T_PROCESS
 */
@TableName(value ="T_PROCESS")
@Data
public class Process implements Serializable {
    /**
     * 业务流程id
     */
    @TableId(value = "PROCESSID",type = IdType.ASSIGN_UUID)
    private String processid;

    /**
     * 流程定义ID
     */
    @TableField(value = "KEY_")
    private String key;

    /**
     * 评估模板id
     */
    @TableField(value = "TEMPLATEID")
    private String templateid;

    /**
     * 任务id
     */
    @TableField(value = "TASKID")
    private String taskid;

    /**
     * 流程名称
     */
    @TableField(value = "PROCESSNAME")
    private String processname;

    /**
     * 流程描述
     */
    @TableField(value = "PROCESSDES")
    private String processdes;

    /**
     * 发射区域id
     */
    @TableField(value = "AREAID")
    private String areaid;

    /**
     * 三维场景文件id
     */
    @TableField(value = "MODELFLIEID")
    private String modelflieid;

    /**
     * 备注
     */
    @TableField(value = "REMARK")
    private String remark;

    /**
     * 业务流程创建时间
     */
    @TableField(value = "CREATETIME",fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createtime;

    /**
     * 是否为模板
     */
    @TableField(value = "ISTEMP")
    private Integer istemp;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}