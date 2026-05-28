package com.jc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 属性数据类型表
 * @TableName T_DATATYPE
 */
@TableName(value ="T_DATATYPE")
@Data
public class Datatype implements Serializable {
    /**
     * 属性数据类型id
     */
    @TableId(value = "DATATYPEID")
    private String datatypeid;

    /**
     * 属性数据类型名称
     */
    @TableField(value = "DATATYPENAME")
    private String datatypename;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}