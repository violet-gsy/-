package com.cetc28.needManagement.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName TAB_DMS_RELATION_SS_DCT
 */
@TableName(value ="TAB_DMS_RELATION_SS_DCT")
@Data
public class RelationSsDct implements Serializable {
    /**
     * 子系统编码
     */
    @TableId(value = "SS_CODE")
    private String ssCode;

    /**
     * 子系统名称
     */
    @TableField(value = "SS_NAME")
    private String ssName;

    /**
     * 创建人
     */
    @TableField(value = "CREATE_PEOPLE")
    private String createPeople;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME")
    private Date createTime;

    /**
     * 修改人
     */
    @TableField(value = "UPDATE_PEOPLE")
    private String updatePeople;

    /**
     * 修改时间
     */
    @TableField(value = "UPDATE_TIME")
    private Date updateTime;

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
        RelationSsDct other = (RelationSsDct) that;
        return (this.getSsCode() == null ? other.getSsCode() == null : this.getSsCode().equals(other.getSsCode()))
            && (this.getSsName() == null ? other.getSsName() == null : this.getSsName().equals(other.getSsName()))
            && (this.getCreatePeople() == null ? other.getCreatePeople() == null : this.getCreatePeople().equals(other.getCreatePeople()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdatePeople() == null ? other.getUpdatePeople() == null : this.getUpdatePeople().equals(other.getUpdatePeople()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getSsCode() == null) ? 0 : getSsCode().hashCode());
        result = prime * result + ((getSsName() == null) ? 0 : getSsName().hashCode());
        result = prime * result + ((getCreatePeople() == null) ? 0 : getCreatePeople().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdatePeople() == null) ? 0 : getUpdatePeople().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", ssCode=").append(ssCode);
        sb.append(", ssName=").append(ssName);
        sb.append(", createPeople=").append(createPeople);
        sb.append(", createTime=").append(createTime);
        sb.append(", updatePeople=").append(updatePeople);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}