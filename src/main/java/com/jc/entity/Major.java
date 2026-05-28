package com.jc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 专业表
 * @TableName T_MAJOR
 */
@TableName(value ="T_MAJOR")
@Schema(description = "专业表实体")
@Data
public class Major implements Serializable {
    /**
     * 专业ID
     */
    @TableId(value = "MAJORID",type = IdType.ASSIGN_UUID)
    @Schema(description = "专业ID")
    private String majorid;

    /**
     * 专业名称
     */
    @TableField(value = "MAJORNAME")
    @Schema(description = "专业名称")
    private String majorname;

    /**
     * 排序号
     */
    @TableField(value = "SORTNO")
    @Schema(description = "排序号")
    private Integer sortno;

    /**
     * 状态：1 启用 0 禁用
     */
    @TableField(value = "STATUS")
    @Schema(description = "状态：1 启用 0 禁用")
    private Integer status;

    /**
     * 备注
     */
    @TableField(value = "REMARK")
    @Schema(description = "备注")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "CREATETIME")
    @Schema(description = "创建时间")
    private LocalDateTime createtime;

    /**
     * 更新时间
     */
    @TableField(value = "UPDATETIME")
    @Schema(description = "更新时间")
    private LocalDateTime updatetime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}