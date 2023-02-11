package com.cetc28.needmanagement.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName TAB_DMS_COMPANY_DCT
 */
@TableName(value ="TAB_DMS_COMPANY_DCT")
@Data
public class CompanyDct implements Serializable {
    /**
     * 单位编码
     */
    @TableId(value = "COMPANY_CODE")
    private String COMPANY_CODE;

    /**
     * 单位名称
     */
    @TableField(value = "COMPANY_NAME")
    private String COMPANY_NAME;

    /**
     * 单位类别(1:提出单位,0:责任单位)
     */
    @TableField(value = "COMPANY_CLASS")
    private Object COMPANY_CLASS;

    /**
     * 创建人
     */
    @TableField(value = "CREATE_PEOPLE")
    private String CREATE_PEOPLE;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME")
    private Date CREATE_TIME;

    /**
     * 修改人
     */
    @TableField(value = "UPDATE_PEOPLE")
    private String UPDATE_PEOPLE;

    /**
     * 修改时间
     */
    @TableField(value = "UPDATE_TIME")
    private Date UPDATE_TIME;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;




}