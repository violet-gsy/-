package com.cetc28.needmanagement.entity;

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
    private String NEED_CODE;

    /**
     * 需求名称
     */
    @TableField(value = "NEED_NAME")
    private String NEED_NAME;

    /**
     * 需求序号
     */
    @TableField(value = "NEED_NS")
    private Object NEED_NS;

    /**
     * 分解需求描述
     */
    @TableField(value = "NEED_DETAILED")
    private String NEED_DETAILED;

    /**
     * 计划完成时间
     */
    @TableField(value = "PLAN_COMPLETE_DATE")
    private Date PLAN_COMPLETE_DATE;

    /**
     * 需求完成版本号
     */
    @TableField(value = "VERSION")
    private String VERSION;

    /**
     * 需求issue关联
     */
    @TableField(value = "ISSUE")
    private String ISSUE;

    /**
     * 需求响应状态(0:未完成，1:已完成，2:部分完成,3:需要沟通/协调)
     */
    @TableField(value = "STATE")
    private Object STATE;

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
     * 最后修改时间
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
        NeedDetailed other = (NeedDetailed) that;
        return (this.getNEED_CODE() == null ? other.getNEED_CODE() == null : this.getNEED_CODE().equals(other.getNEED_CODE()))
            && (this.getNEED_NAME() == null ? other.getNEED_NAME() == null : this.getNEED_NAME().equals(other.getNEED_NAME()))
            && (this.getNEED_NS() == null ? other.getNEED_NS() == null : this.getNEED_NS().equals(other.getNEED_NS()))
            && (this.getNEED_DETAILED() == null ? other.getNEED_DETAILED() == null : this.getNEED_DETAILED().equals(other.getNEED_DETAILED()))
            && (this.getPLAN_COMPLETE_DATE() == null ? other.getPLAN_COMPLETE_DATE() == null : this.getPLAN_COMPLETE_DATE().equals(other.getPLAN_COMPLETE_DATE()))
            && (this.getVERSION() == null ? other.getVERSION() == null : this.getVERSION().equals(other.getVERSION()))
            && (this.getISSUE() == null ? other.getISSUE() == null : this.getISSUE().equals(other.getISSUE()))
            && (this.getSTATE() == null ? other.getSTATE() == null : this.getSTATE().equals(other.getSTATE()))
            && (this.getCREATE_PEOPLE() == null ? other.getCREATE_PEOPLE() == null : this.getCREATE_PEOPLE().equals(other.getCREATE_PEOPLE()))
            && (this.getCREATE_TIME() == null ? other.getCREATE_TIME() == null : this.getCREATE_TIME().equals(other.getCREATE_TIME()))
            && (this.getUPDATE_PEOPLE() == null ? other.getUPDATE_PEOPLE() == null : this.getUPDATE_PEOPLE().equals(other.getUPDATE_PEOPLE()))
            && (this.getUPDATE_TIME() == null ? other.getUPDATE_TIME() == null : this.getUPDATE_TIME().equals(other.getUPDATE_TIME()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getNEED_CODE() == null) ? 0 : getNEED_CODE().hashCode());
        result = prime * result + ((getNEED_NAME() == null) ? 0 : getNEED_NAME().hashCode());
        result = prime * result + ((getNEED_NS() == null) ? 0 : getNEED_NS().hashCode());
        result = prime * result + ((getNEED_DETAILED() == null) ? 0 : getNEED_DETAILED().hashCode());
        result = prime * result + ((getPLAN_COMPLETE_DATE() == null) ? 0 : getPLAN_COMPLETE_DATE().hashCode());
        result = prime * result + ((getVERSION() == null) ? 0 : getVERSION().hashCode());
        result = prime * result + ((getISSUE() == null) ? 0 : getISSUE().hashCode());
        result = prime * result + ((getSTATE() == null) ? 0 : getSTATE().hashCode());
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
        sb.append(", NEED_CODE=").append(NEED_CODE);
        sb.append(", NEED_NAME=").append(NEED_NAME);
        sb.append(", NEED_NS=").append(NEED_NS);
        sb.append(", NEED_DETAILED=").append(NEED_DETAILED);
        sb.append(", PLAN_COMPLETE_DATE=").append(PLAN_COMPLETE_DATE);
        sb.append(", VERSION=").append(VERSION);
        sb.append(", ISSUE=").append(ISSUE);
        sb.append(", STATE=").append(STATE);
        sb.append(", CREATE_PEOPLE=").append(CREATE_PEOPLE);
        sb.append(", CREATE_TIME=").append(CREATE_TIME);
        sb.append(", UPDATE_PEOPLE=").append(UPDATE_PEOPLE);
        sb.append(", UPDATE_TIME=").append(UPDATE_TIME);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}