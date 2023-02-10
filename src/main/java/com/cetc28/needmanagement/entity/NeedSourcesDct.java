package com.cetc28.needmanagement.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName TAB_DMS_NEED_SOURCES_DCT
 */
@TableName(value ="TAB_DMS_NEED_SOURCES_DCT")
@Data
public class NeedSourcesDct implements Serializable {
    /**
     * 需求编码
     */
    @TableId(value = "NEED_CODE")
    private String NEED_CODE;

    /**
     * 需求名称
     */
    @TableField(value = "NEED_NAME")
    private String NEED_NAME;

    /**
     * 需求阶段
     */
    @TableField(value = "NEED_SETP")
    private String NEED_SETP;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME")
    private Date CREATE_TIME;

    /**
     * 创建人
     */
    @TableField(value = "CREATE_PEOPLE")
    private String CREATE_PEOPLE;

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
        NeedSourcesDct other = (NeedSourcesDct) that;
        return (this.getNEED_CODE() == null ? other.getNEED_CODE() == null : this.getNEED_CODE().equals(other.getNEED_CODE()))
            && (this.getNEED_NAME() == null ? other.getNEED_NAME() == null : this.getNEED_NAME().equals(other.getNEED_NAME()))
            && (this.getNEED_SETP() == null ? other.getNEED_SETP() == null : this.getNEED_SETP().equals(other.getNEED_SETP()))
            && (this.getCREATE_TIME() == null ? other.getCREATE_TIME() == null : this.getCREATE_TIME().equals(other.getCREATE_TIME()))
            && (this.getCREATE_PEOPLE() == null ? other.getCREATE_PEOPLE() == null : this.getCREATE_PEOPLE().equals(other.getCREATE_PEOPLE()))
            && (this.getUPDATE_PEOPLE() == null ? other.getUPDATE_PEOPLE() == null : this.getUPDATE_PEOPLE().equals(other.getUPDATE_PEOPLE()))
            && (this.getUPDATE_TIME() == null ? other.getUPDATE_TIME() == null : this.getUPDATE_TIME().equals(other.getUPDATE_TIME()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getNEED_CODE() == null) ? 0 : getNEED_CODE().hashCode());
        result = prime * result + ((getNEED_NAME() == null) ? 0 : getNEED_NAME().hashCode());
        result = prime * result + ((getNEED_SETP() == null) ? 0 : getNEED_SETP().hashCode());
        result = prime * result + ((getCREATE_TIME() == null) ? 0 : getCREATE_TIME().hashCode());
        result = prime * result + ((getCREATE_PEOPLE() == null) ? 0 : getCREATE_PEOPLE().hashCode());
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
        sb.append(", NEED_CODE=").append(NEED_CODE);
        sb.append(", NEED_NAME=").append(NEED_NAME);
        sb.append(", NEED_SETP=").append(NEED_SETP);
        sb.append(", CREATE_TIME=").append(CREATE_TIME);
        sb.append(", CREATE_PEOPLE=").append(CREATE_PEOPLE);
        sb.append(", UPDATE_PEOPLE=").append(UPDATE_PEOPLE);
        sb.append(", UPDATE_TIME=").append(UPDATE_TIME);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}