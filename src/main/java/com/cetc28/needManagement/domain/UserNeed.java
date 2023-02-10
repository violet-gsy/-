package com.cetc28.needManagement.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName TAB_DMS_USER_NEED
 */
@TableName(value ="TAB_DMS_USER_NEED")
@Data
public class UserNeed implements Serializable {
    /**
     * 需求id
     */
    @TableId(value = "NEED_ID")
    private String needId;

    /**
     * 需求来源
     */
    @TableField(value = "NEED_NAME")
    private String needName;

    /**
     * 需求阶段
     */
    @TableField(value = "NEED_SETP")
    private String needSetp;

    /**
     * 提出单位
     */
    @TableField(value = "CUT_COMPANY_NAME")
    private String cutCompanyName;

    /**
     * 提出人
     */
    @TableField(value = "CUT_PEOPLE_NAME")
    private String cutPeopleName;

    /**
     * 提出意见
     */
    @TableField(value = "CUT_OPINION")
    private String cutOpinion;

    /**
     * 提出时间
     */
    @TableField(value = "CUT_TIME")
    private Date cutTime;

    /**
     * 关联子系统
     */
    @TableField(value = "SS_NAME")
    private String ssName;

    /**
     * 关联配置项
     */
    @TableField(value = "CI_NAME")
    private String ciName;

    /**
     * 需求标识
     */
    @TableField(value = "NEED_CODE")
    private String needCode;

    /**
     * 提出类别
     */
    @TableField(value = "CUT_CATEGURY")
    private String cutCategury;

    /**
     * 处理措施
     */
    @TableField(value = "CLCS")
    private String clcs;

    /**
     * 责任单位
     */
    @TableField(value = "RESPONSIBLE_UNIT")
    private String responsibleUnit;

    /**
     * 完成时间
     */
    @TableField(value = "COMPLETE_DATE")
    private Date completeDate;

    /**
     * 现阶段状态
     */
    @TableField(value = "CURRENT_STATUS")
    private String currentStatus;

    /**
     * 责任人
     */
    @TableField(value = "RESPONSIBLE_PEOPLE")
    private String responsiblePeople;

    /**
     * 联系方式
     */
    @TableField(value = "PHONE")
    private Long phone;

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
        UserNeed other = (UserNeed) that;
        return (this.getNeedId() == null ? other.getNeedId() == null : this.getNeedId().equals(other.getNeedId()))
            && (this.getNeedName() == null ? other.getNeedName() == null : this.getNeedName().equals(other.getNeedName()))
            && (this.getNeedSetp() == null ? other.getNeedSetp() == null : this.getNeedSetp().equals(other.getNeedSetp()))
            && (this.getCutCompanyName() == null ? other.getCutCompanyName() == null : this.getCutCompanyName().equals(other.getCutCompanyName()))
            && (this.getCutPeopleName() == null ? other.getCutPeopleName() == null : this.getCutPeopleName().equals(other.getCutPeopleName()))
            && (this.getCutOpinion() == null ? other.getCutOpinion() == null : this.getCutOpinion().equals(other.getCutOpinion()))
            && (this.getCutTime() == null ? other.getCutTime() == null : this.getCutTime().equals(other.getCutTime()))
            && (this.getSsName() == null ? other.getSsName() == null : this.getSsName().equals(other.getSsName()))
            && (this.getCiName() == null ? other.getCiName() == null : this.getCiName().equals(other.getCiName()))
            && (this.getNeedCode() == null ? other.getNeedCode() == null : this.getNeedCode().equals(other.getNeedCode()))
            && (this.getCutCategury() == null ? other.getCutCategury() == null : this.getCutCategury().equals(other.getCutCategury()))
            && (this.getClcs() == null ? other.getClcs() == null : this.getClcs().equals(other.getClcs()))
            && (this.getResponsibleUnit() == null ? other.getResponsibleUnit() == null : this.getResponsibleUnit().equals(other.getResponsibleUnit()))
            && (this.getCompleteDate() == null ? other.getCompleteDate() == null : this.getCompleteDate().equals(other.getCompleteDate()))
            && (this.getCurrentStatus() == null ? other.getCurrentStatus() == null : this.getCurrentStatus().equals(other.getCurrentStatus()))
            && (this.getResponsiblePeople() == null ? other.getResponsiblePeople() == null : this.getResponsiblePeople().equals(other.getResponsiblePeople()))
            && (this.getPhone() == null ? other.getPhone() == null : this.getPhone().equals(other.getPhone()))
            && (this.getCreatePeople() == null ? other.getCreatePeople() == null : this.getCreatePeople().equals(other.getCreatePeople()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdatePeople() == null ? other.getUpdatePeople() == null : this.getUpdatePeople().equals(other.getUpdatePeople()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getNeedId() == null) ? 0 : getNeedId().hashCode());
        result = prime * result + ((getNeedName() == null) ? 0 : getNeedName().hashCode());
        result = prime * result + ((getNeedSetp() == null) ? 0 : getNeedSetp().hashCode());
        result = prime * result + ((getCutCompanyName() == null) ? 0 : getCutCompanyName().hashCode());
        result = prime * result + ((getCutPeopleName() == null) ? 0 : getCutPeopleName().hashCode());
        result = prime * result + ((getCutOpinion() == null) ? 0 : getCutOpinion().hashCode());
        result = prime * result + ((getCutTime() == null) ? 0 : getCutTime().hashCode());
        result = prime * result + ((getSsName() == null) ? 0 : getSsName().hashCode());
        result = prime * result + ((getCiName() == null) ? 0 : getCiName().hashCode());
        result = prime * result + ((getNeedCode() == null) ? 0 : getNeedCode().hashCode());
        result = prime * result + ((getCutCategury() == null) ? 0 : getCutCategury().hashCode());
        result = prime * result + ((getClcs() == null) ? 0 : getClcs().hashCode());
        result = prime * result + ((getResponsibleUnit() == null) ? 0 : getResponsibleUnit().hashCode());
        result = prime * result + ((getCompleteDate() == null) ? 0 : getCompleteDate().hashCode());
        result = prime * result + ((getCurrentStatus() == null) ? 0 : getCurrentStatus().hashCode());
        result = prime * result + ((getResponsiblePeople() == null) ? 0 : getResponsiblePeople().hashCode());
        result = prime * result + ((getPhone() == null) ? 0 : getPhone().hashCode());
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
        sb.append(", needId=").append(needId);
        sb.append(", needName=").append(needName);
        sb.append(", needSetp=").append(needSetp);
        sb.append(", cutCompanyName=").append(cutCompanyName);
        sb.append(", cutPeopleName=").append(cutPeopleName);
        sb.append(", cutOpinion=").append(cutOpinion);
        sb.append(", cutTime=").append(cutTime);
        sb.append(", ssName=").append(ssName);
        sb.append(", ciName=").append(ciName);
        sb.append(", needCode=").append(needCode);
        sb.append(", cutCategury=").append(cutCategury);
        sb.append(", clcs=").append(clcs);
        sb.append(", responsibleUnit=").append(responsibleUnit);
        sb.append(", completeDate=").append(completeDate);
        sb.append(", currentStatus=").append(currentStatus);
        sb.append(", responsiblePeople=").append(responsiblePeople);
        sb.append(", phone=").append(phone);
        sb.append(", createPeople=").append(createPeople);
        sb.append(", createTime.=").append(createTime);
        sb.append(", updatePeople=").append(updatePeople);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}