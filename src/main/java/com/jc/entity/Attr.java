package com.jc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName T_ATTR
 */
@TableName(value ="T_ATTR")
@Data
public class Attr implements Serializable {
    /**
     * ID
     */
    @TableId(value = "ID")
    private String id;

    /**
     * 属性ID
     */
    @TableField(value = "ATTRID")
    private String attrid;

    /**
     * 属性名称
     */
    @TableField(value = "ATTRNAME")
    private String attrname;

    /**
     * 属性值
     */
    @TableField(value = "ATTRVALUE")
    private String attrvalue;

    /**
     * 属性数据类型id
     */
    @TableField(value = "DATETYPEID")
    private String datetypeid;

    /**
     * 计量单位ID
     */
    @TableField(value = "UNITID")
    private String unitid;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}