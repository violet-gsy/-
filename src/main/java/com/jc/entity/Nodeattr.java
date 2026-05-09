package com.jc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

/**
 * 节点属性表
 * @TableName T_NODEATTR
 */
@TableName(value ="T_NODEATTR")
@Data
@Builder
public class Nodeattr implements Serializable {
    /**
     * 节点属性ID
     */
    @TableId(value = "NODEATTRID")
    private String nodeattrid;

    /**
     * 流程定义ID
     */
    @TableField(value = "KEY_")
    private String key;

    /**
     * 流程节点ID
     */
    @TableField(value = "PNODEID")
    private String pnodeid;

    /**
     * 流程节点名称
     */
    @TableField(value = "PNODENAME")
    private String pnodename;

    /**
     * 节点类型(0中层/1底层)
     */
    @TableField(value = "NODETYPE")
    private Integer nodetype;

    /**
     * 节点设备IDS
     */
    @TableField(value = "EQPIDS")
    private String eqpids;

    /**
     * 节点设施IDS
     */
    @TableField(value = "FACILITYIDS")
    private String facilityids;

    /**
     * 节点工装IDS
     */
    @TableField(value = "TOOLIDS")
    private String toolids;

    /**
     * 节点工器具IDS
     */
    @TableField(value = "TOOLEQPIDS")
    private String tooleqpids;

    /**
     * 节点动作类型
     */
    @TableField(value = "ACTIONTYPE")
    private String actiontype;

    /**
     * 节点描述
     */
    @TableField(value = "DESCRIPTION")
    private String description;

    /**
     * 节点目标
     */
    @TableField(value = "GOAL")
    private String goal;

    /**
     * 节点属性(三维小步骤)
     */
    @TableField(value = "MODELATTR")
    private String modelattr;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}