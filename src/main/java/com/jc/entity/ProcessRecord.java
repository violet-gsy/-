package com.jc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 流程记录结果表
 * @TableName T_PROCESS_RECORD
 */
@TableName(value ="T_PROCESS_RECORD")
@Data
public class ProcessRecord implements Serializable {
    /**
     * 流程记录ID
     */
    @TableId(value = "RECORDID")
    private String recordid;

    /**
     * 业务流程id
     */
    @TableField(value = "PROCESSID")
    private String processid;

    /**
     * 流程进度
     */
    @TableField(value = "PROCESSPROG")
    private Integer processprog;

    /**
     * 流程是否完成
     */
    @TableField(value = "ISSUCCESS")
    private Integer issuccess;

    /**
     * 流程开始时间
     */
    @TableField(value = "STARTTIME")
    private LocalDateTime starttime;

    /**
     * 流程结束时间
     */
    @TableField(value = "ENDTIME")
    private LocalDateTime endtime;

    /**
     * 总耗时
     */
    @TableField(value = "TOTALTIME")
    private Object totaltime;

    /**
     * 故障详情
     */
    @TableField(value = "FAULT")
    private String fault;

    /**
     * 是否达到预期结果
     */
    @TableField(value = "ISEXPECT")
    private Integer isexpect;

    /**
     * 流程实例ID
     */
    @TableField(value = "PROC_INST_ID_")
    private String procInstId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}