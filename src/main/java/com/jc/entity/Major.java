package com.jc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 专业表
 * @TableName T_MAJOR
 */
@TableName(value ="T_MAJOR")
@Data
public class Major implements Serializable {
    /**
     * 专业ID
     */
    @TableId(value = "MAJORID")
    private String majorid;

    /**
     * 专业名称
     */
    @TableField(value = "MAJORNAME")
    private String majorname;

    /**
     * 排序号
     */
    @TableField(value = "SORTNO")
    private Integer sortno;

    /**
     * 状态：1 启用 0 禁用
     */
    @TableField(value = "STATUS")
    private Integer status;

    /**
     * 备注
     */
    @TableField(value = "REMARK")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "CREATETIME")
    private LocalDateTime createtime;

    /**
     * 更新时间
     */
    @TableField(value = "UPDATETIME")
    private LocalDateTime updatetime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}