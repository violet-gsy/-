package com.jc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 节点记录结果表
 * @TableName T_NODE_RECORD
 */
@TableName(value ="T_NODE_RECORD")
@Data
public class NodeRecord implements Serializable {
    /**
     * 节点记录ID
     */
    @TableId(value = "NODERECORDID",type = IdType.ASSIGN_UUID)
    private String noderecordid;

    /**
     * 流程记录ID
     */
    @TableField(value = "RECORDID")
    private String recordid;

    /**
     * 流程节点ID
     */
    @TableField(value = "ACT_ID_")
    private String actId;

    /**
     * 节点是否完成
     */
    @TableField(value = "ISSUCCESS")
    private Integer issuccess;

    /**
     * 节点开始时间
     */
    @TableField(value = "STARTTIME")
    private LocalDateTime starttime;

    /**
     * 节点结束时间
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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}