package com.cetc28.needManagement.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName TAB_DMS_NEED_DETAILED
 */
@TableName(value ="TAB_DMS_NEED_DETAILED")
@Data
public class NeedDetailed implements Serializable {
    /**
     * 需求编码
     */
    @TableField(value = "NEED_CODE")
    private String needCode;

    /**
     * 需求名称
     */
    @TableField(value = "NEED_NAME")
    private String needName;

    /**
     * 需求序号
     */
    @TableField(value = "NEED_NS")
    private Object needNs;

    /**
     * 分解需求描述
     */
    @TableField(value = "NEED_DETAILED")
    private String needDetailed;

    /**
     * 计划完成时间
     */
    @TableField(value = "PLAN_COMPLETE_DATE")
    private Date planCompleteDate;

    /**
     * 需求完成版本号
     */
    @TableField(value = "VERSION")
    private String version;

    /**
     * 需求issue关联
     */
    @TableField(value = "ISSUE")
    private String issue;

    /**
     * 需求响应状态(0:未完成，1:已完成，2:部分完成,3:需要沟通/协调)
     */
    @TableField(value = "STATE")
    private Object state;

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
     * 最后修改时间
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
        NeedDetailed other = (NeedDetailed) that;
        return (this.getNeedCode() == null ? other.getNeedCode() == null : this.getNeedCode().equals(other.getNeedCode()))
            && (this.getNeedName() == null ? other.getNeedName() == null : this.getNeedName().equals(other.getNeedName()))
            && (this.getNeedNs() == null ? other.getNeedNs() == null : this.getNeedNs().equals(other.getNeedNs()))
            && (this.getNeedDetailed() == null ? other.getNeedDetailed() == null : this.getNeedDetailed().equals(other.getNeedDetailed()))
            && (this.getPlanCompleteDate() == null ? other.getPlanCompleteDate() == null : this.getPlanCompleteDate().equals(other.getPlanCompleteDate()))
            && (this.getVersion() == null ? other.getVersion() == null : this.getVersion().equals(other.getVersion()))
            && (this.getIssue() == null ? other.getIssue() == null : this.getIssue().equals(other.getIssue()))
            && (this.getState() == null ? other.getState() == null : this.getState().equals(other.getState()))
            && (this.getCreatePeople() == null ? other.getCreatePeople() == null : this.getCreatePeople().equals(other.getCreatePeople()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdatePeople() == null ? other.getUpdatePeople() == null : this.getUpdatePeople().equals(other.getUpdatePeople()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getNeedCode() == null) ? 0 : getNeedCode().hashCode());
        result = prime * result + ((getNeedName() == null) ? 0 : getNeedName().hashCode());
        result = prime * result + ((getNeedNs() == null) ? 0 : getNeedNs().hashCode());
        result = prime * result + ((getNeedDetailed() == null) ? 0 : getNeedDetailed().hashCode());
        result = prime * result + ((getPlanCompleteDate() == null) ? 0 : getPlanCompleteDate().hashCode());
        result = prime * result + ((getVersion() == null) ? 0 : getVersion().hashCode());
        result = prime * result + ((getIssue() == null) ? 0 : getIssue().hashCode());
        result = prime * result + ((getState() == null) ? 0 : getState().hashCode());
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
        sb.append(", needCode=").append(needCode);
        sb.append(", needName=").append(needName);
        sb.append(", needNs=").append(needNs);
        sb.append(", needDetailed=").append(needDetailed);
        sb.append(", planCompleteDate=").append(planCompleteDate);
        sb.append(", version=").append(version);
        sb.append(", issue=").append(issue);
        sb.append(", state=").append(state);
        sb.append(", createPeople=").append(createPeople);
        sb.append(", createTime=").append(createTime);
        sb.append(", updatePeople=").append(updatePeople);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}