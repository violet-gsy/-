package com.jc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 文档历史版本表
 * @TableName T_DOCUMENT_HIS
 */
@TableName(value ="T_DOCUMENT_HIS")
@Data
public class DocumentHis implements Serializable {
    /**
     * 历史文档id
     */
    @TableId(value = "HISID")
    private String hisid;

    /**
     * 文档ID
     */
    @TableField(value = "DOCID")
    private String docid;

    /**
     * 文档名称
     */
    @TableField(value = "HISDOCNAME")
    private String hisdocname;

    /**
     * 版本号
     */
    @TableField(value = "VERSION")
    private String version;

    /**
     * 文件存储路径
     */
    @TableField(value = "HISFILEPATH")
    private String hisfilepath;

    /**
     * 文件大小（字节）
     */
    @TableField(value = "HISFILESIZE")
    private Long hisfilesize;

    /**
     * 版本操作人
     */
    @TableField(value = "OPERATOR")
    private String operator;

    /**
     * 操作类型
     */
    @TableField(value = "OPERATE_TYPE")
    private String operateType;

    /**
     * 版本说明
     */
    @TableField(value = "VERSIONREMARK")
    private String versionremark;

    /**
     * 版本生成时间
     */
    @TableField(value = "CREATETIME")
    private LocalDateTime createtime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}