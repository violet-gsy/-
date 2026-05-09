package com.jc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * T_TASK
 * @TableName T_TASK
 */
@TableName(value ="T_TASK")
@Data
public class Task implements Serializable {
    /**
     * 任务id
     */
    @TableId(value = "TASKID")
    private String taskid;

    /**
     * 任务名称
     */
    @TableField(value = "TASKNAME")
    private String taskname;

    /**
     * 任务开始时间
     */
    @TableField(value = "STARTTIME")
    private LocalDateTime starttime;

    /**
     * 任务结束时间
     */
    @TableField(value = "ENDTIME")
    private LocalDateTime endtime;

    /**
     * 任务描述
     */
    @TableField(value = "DESCRIBE")
    private String describe;

    /**
     * 发射场ID
     */
    @TableField(value = "LSID")
    private String lsid;

    /**
     * 任务所需产品id
     */
    @TableField(value = "PRODUCTIDS")
    private String productids;

    /**
     * 任务创建时间
     */
    @TableField(value = "CREATTIME")
    private LocalDateTime creattime;

    /**
     * 备注
     */
    @TableField(value = "REMARK")
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}