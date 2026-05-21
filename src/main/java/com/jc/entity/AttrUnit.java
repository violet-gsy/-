package com.jc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 属性单位表
 * @TableName T_ATTR_UNIT
 */
@TableName(value ="T_ATTR_UNIT")
@Data
public class AttrUnit implements Serializable {
    /**
     * 计量单位ID
     */
    @TableId(value = "UNITID")
    private String unitid;

    /**
     * 计量单位名称
     */
    @TableField(value = "UNITNAME")
    private String unitname;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}