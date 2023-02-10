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

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        CompanyDct other = (CompanyDct) that;
        return (this.getCOMPANY_CODE() == null ? other.getCOMPANY_CODE() == null : this.getCOMPANY_CODE().equals(other.getCOMPANY_CODE()))
            && (this.getCOMPANY_NAME() == null ? other.getCOMPANY_NAME() == null : this.getCOMPANY_NAME().equals(other.getCOMPANY_NAME()))
            && (this.getCOMPANY_CLASS() == null ? other.getCOMPANY_CLASS() == null : this.getCOMPANY_CLASS().equals(other.getCOMPANY_CLASS()))
            && (this.getCREATE_PEOPLE() == null ? other.getCREATE_PEOPLE() == null : this.getCREATE_PEOPLE().equals(other.getCREATE_PEOPLE()))
            && (this.getCREATE_TIME() == null ? other.getCREATE_TIME() == null : this.getCREATE_TIME().equals(other.getCREATE_TIME()))
            && (this.getUPDATE_PEOPLE() == null ? other.getUPDATE_PEOPLE() == null : this.getUPDATE_PEOPLE().equals(other.getUPDATE_PEOPLE()))
            && (this.getUPDATE_TIME() == null ? other.getUPDATE_TIME() == null : this.getUPDATE_TIME().equals(other.getUPDATE_TIME()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getCOMPANY_CODE() == null) ? 0 : getCOMPANY_CODE().hashCode());
        result = prime * result + ((getCOMPANY_NAME() == null) ? 0 : getCOMPANY_NAME().hashCode());
        result = prime * result + ((getCOMPANY_CLASS() == null) ? 0 : getCOMPANY_CLASS().hashCode());
        result = prime * result + ((getCREATE_PEOPLE() == null) ? 0 : getCREATE_PEOPLE().hashCode());
        result = prime * result + ((getCREATE_TIME() == null) ? 0 : getCREATE_TIME().hashCode());
        result = prime * result + ((getUPDATE_PEOPLE() == null) ? 0 : getUPDATE_PEOPLE().hashCode());
        result = prime * result + ((getUPDATE_TIME() == null) ? 0 : getUPDATE_TIME().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", COMPANY_CODE=").append(COMPANY_CODE);
        sb.append(", COMPANY_NAME=").append(COMPANY_NAME);
        sb.append(", COMPANY_CLASS=").append(COMPANY_CLASS);
        sb.append(", CREATE_PEOPLE=").append(CREATE_PEOPLE);
        sb.append(", CREATE_TIME=").append(CREATE_TIME);
        sb.append(", UPDATE_PEOPLE=").append(UPDATE_PEOPLE);
        sb.append(", UPDATE_TIME=").append(UPDATE_TIME);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}