package com.jc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 文档主表
 * @TableName T_DOCUMENT
 */
@TableName(value ="T_DOCUMENT")
@Data
public class Document implements Serializable {
    /**
     * 文档ID
     */
    @TableId(value = "DOCID")
    private String docid;

    /**
     * 文档名称
     */
    @TableField(value = "DOCNAME")
    private String docname;

    /**
     * 文档类型
     */
    @TableField(value = "DOCTYPE")
    private String doctype;

    /**
     * 关联业务类型
     */
    @TableField(value = "BUSINESSTYPE")
    private String businesstype;

    /**
     * 关联业务数据ID
     */
    @TableField(value = "BUSINESSID")
    private String businessid;

    /**
     * 文件存储路径
     */
    @TableField(value = "FILEPATH")
    private String filepath;

    /**
     * 文件大小（字节）
     */
    @TableField(value = "FILESIZE")
    private Long filesize;

    /**
     * 当前版本号
     */
    @TableField(value = "VERSION")
    private String version;

    /**
     * 状态
     */
    @TableField(value = "STATUS")
    private String status;

    /**
     * 备注
     */
    @TableField(value = "REMARK")
    private String remark;

    /**
     * 创建人
     */
    @TableField(value = "CREATOR")
    private String creator;

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