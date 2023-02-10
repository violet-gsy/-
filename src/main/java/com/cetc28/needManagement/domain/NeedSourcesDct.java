package com.cetc28.needManagement.domain;

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
    private String needCode;

    /**
     * 需求名称
     */
    @TableField(value = "NEED_NAME")
    private String needName;

    /**
     * 需求阶段
     */
    @TableField(value = "NEED_SETP")
    private String needSetp;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME")
    private Date createTime;

    /**
     * 创建人
     */
    @TableField(value = "CREATE_PEOPLE")
    private String createPeople;

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
        NeedSourcesDct other = (NeedSourcesDct) that;
        return (this.getNeedCode() == null ? other.getNeedCode() == null : this.getNeedCode().equals(other.getNeedCode()))
            && (this.getNeedName() == null ? other.getNeedName() == null : this.getNeedName().equals(other.getNeedName()))
            && (this.getNeedSetp() == null ? other.getNeedSetp() == null : this.getNeedSetp().equals(other.getNeedSetp()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getCreatePeople() == null ? other.getCreatePeople() == null : this.getCreatePeople().equals(other.getCreatePeople()))
            && (this.getUpdatePeople() == null ? other.getUpdatePeople() == null : this.getUpdatePeople().equals(other.getUpdatePeople()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getNeedCode() == null) ? 0 : getNeedCode().hashCode());
        result = prime * result + ((getNeedName() == null) ? 0 : getNeedName().hashCode());
        result = prime * result + ((getNeedSetp() == null) ? 0 : getNeedSetp().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getCreatePeople() == null) ? 0 : getCreatePeople().hashCode());
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
        sb.append(", needCode=").append(needCode);
        sb.append(", needName=").append(needName);
        sb.append(", needSetp=").append(needSetp);
        sb.append(", createTime=").append(createTime);
        sb.append(", createPeople=").append(createPeople);
        sb.append(", updatePeople=").append(updatePeople);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}