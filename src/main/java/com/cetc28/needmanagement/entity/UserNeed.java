package com.cetc28.needmanagement.entity;

import com.alibaba.excel.annotation.ExcelProperty;
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
    private String NEED_ID;

    /**
     * 需求来源
     */
    @TableField(value = "NEED_NAME")
    @ExcelProperty(value = "需求来源",index = 0)
    private String NEED_NAME;

    /**
     * 需求阶段
     */
    @TableField(value = "NEED_SETP")
    @ExcelProperty(value = "需求阶段",index = 1)
    private String NEED_SETP;

    /**
     * 提出单位
     */
    @TableField(value = "CUT_COMPANY_NAME")
    @ExcelProperty(value = "提出单位",index = 2)
    private String CUT_COMPANY_NAME;

    /**
     * 提出人
     */
    @TableField(value = "CUT_PEOPLE_NAME")
    @ExcelProperty(value = "提出人",index = 3)
    private String CUT_PEOPLE_NAME;

    /**
     * 提出意见
     */
    @TableField(value = "CUT_OPINION")
    @ExcelProperty(value = "提出意见",index = 4)
    private String CUT_OPINION;

    /**
     * 提出时间
     */
    @TableField(value = "CUT_TIME")
    @ExcelProperty(value = "提出时间",index = 5)
    private Date CUT_TIME;

    /**
     * 关联子系统
     */
    @TableField(value = "SS_NAME")
    @ExcelProperty(value = "关联子系统",index = 6)
    private String SS_NAME;

    /**
     * 关联配置项
     */
    @TableField(value = "CI_NAME")
    @ExcelProperty(value = "关联配置项",index = 7)
    private String CI_NAME;

    /**
     * 需求标识
     */
    @TableField(value = "NEED_CODE")
    @ExcelProperty(value = "需求标识",index = 8)
    private String NEED_CODE;

    /**
     * 提出类别
     */
    @TableField(value = "CUT_CATEGURY")
    @ExcelProperty(value = "提出类别",index = 9)
    private String CUT_CATEGURY;

    /**
     * 处理措施
     */
    @TableField(value = "CLCS")
    @ExcelProperty(value = "处理措施",index = 10)
    private String CLCS;

    /**
     * 责任单位
     */
    @TableField(value = "RESPONSIBLE_UNIT")
    @ExcelProperty(value = "责任单位",index = 11)
    private String RESPONSIBLE_UNIT;

    /**
     * 完成时间
     */
    @TableField(value = "COMPLETE_DATE")
    @ExcelProperty(value = "完成时间",index = 12)
    private Date COMPLETE_DATE;

    /**
     * 现阶段状态
     */
    @TableField(value = "CURRENT_STATUS")
    @ExcelProperty(value = "现阶段状态",index = 13)
    private String CURRENT_STATUS;

    /**
     * 责任人
     */
    @TableField(value = "RESPONSIBLE_PEOPLE")
    @ExcelProperty(value = "责任人",index = 14)
    private String RESPONSIBLE_PEOPLE;

    /**
     * 联系方式
     */
    @TableField(value = "PHONE")
    @ExcelProperty(value = "联系方式",index = 15)
    private Long PHONE;

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
        UserNeed other = (UserNeed) that;
        return (this.getNEED_ID() == null ? other.getNEED_ID() == null : this.getNEED_ID().equals(other.getNEED_ID()))
            && (this.getNEED_NAME() == null ? other.getNEED_NAME() == null : this.getNEED_NAME().equals(other.getNEED_NAME()))
            && (this.getNEED_SETP() == null ? other.getNEED_SETP() == null : this.getNEED_SETP().equals(other.getNEED_SETP()))
            && (this.getCUT_COMPANY_NAME() == null ? other.getCUT_COMPANY_NAME() == null : this.getCUT_COMPANY_NAME().equals(other.getCUT_COMPANY_NAME()))
            && (this.getCUT_PEOPLE_NAME() == null ? other.getCUT_PEOPLE_NAME() == null : this.getCUT_PEOPLE_NAME().equals(other.getCUT_PEOPLE_NAME()))
            && (this.getCUT_OPINION() == null ? other.getCUT_OPINION() == null : this.getCUT_OPINION().equals(other.getCUT_OPINION()))
            && (this.getCUT_TIME() == null ? other.getCUT_TIME() == null : this.getCUT_TIME().equals(other.getCUT_TIME()))
            && (this.getSS_NAME() == null ? other.getSS_NAME() == null : this.getSS_NAME().equals(other.getSS_NAME()))
            && (this.getCI_NAME() == null ? other.getCI_NAME() == null : this.getCI_NAME().equals(other.getCI_NAME()))
            && (this.getNEED_CODE() == null ? other.getNEED_CODE() == null : this.getNEED_CODE().equals(other.getNEED_CODE()))
            && (this.getCUT_CATEGURY() == null ? other.getCUT_CATEGURY() == null : this.getCUT_CATEGURY().equals(other.getCUT_CATEGURY()))
            && (this.getCLCS() == null ? other.getCLCS() == null : this.getCLCS().equals(other.getCLCS()))
            && (this.getRESPONSIBLE_UNIT() == null ? other.getRESPONSIBLE_UNIT() == null : this.getRESPONSIBLE_UNIT().equals(other.getRESPONSIBLE_UNIT()))
            && (this.getCOMPLETE_DATE() == null ? other.getCOMPLETE_DATE() == null : this.getCOMPLETE_DATE().equals(other.getCOMPLETE_DATE()))
            && (this.getCURRENT_STATUS() == null ? other.getCURRENT_STATUS() == null : this.getCURRENT_STATUS().equals(other.getCURRENT_STATUS()))
            && (this.getRESPONSIBLE_PEOPLE() == null ? other.getRESPONSIBLE_PEOPLE() == null : this.getRESPONSIBLE_PEOPLE().equals(other.getRESPONSIBLE_PEOPLE()))
            && (this.getPHONE() == null ? other.getPHONE() == null : this.getPHONE().equals(other.getPHONE()))
            && (this.getCREATE_PEOPLE() == null ? other.getCREATE_PEOPLE() == null : this.getCREATE_PEOPLE().equals(other.getCREATE_PEOPLE()))
            && (this.getCREATE_TIME() == null ? other.getCREATE_TIME() == null : this.getCREATE_TIME().equals(other.getCREATE_TIME()))
            && (this.getUPDATE_PEOPLE() == null ? other.getUPDATE_PEOPLE() == null : this.getUPDATE_PEOPLE().equals(other.getUPDATE_PEOPLE()))
            && (this.getUPDATE_TIME() == null ? other.getUPDATE_TIME() == null : this.getUPDATE_TIME().equals(other.getUPDATE_TIME()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getNEED_ID() == null) ? 0 : getNEED_ID().hashCode());
        result = prime * result + ((getNEED_NAME() == null) ? 0 : getNEED_NAME().hashCode());
        result = prime * result + ((getNEED_SETP() == null) ? 0 : getNEED_SETP().hashCode());
        result = prime * result + ((getCUT_COMPANY_NAME() == null) ? 0 : getCUT_COMPANY_NAME().hashCode());
        result = prime * result + ((getCUT_PEOPLE_NAME() == null) ? 0 : getCUT_PEOPLE_NAME().hashCode());
        result = prime * result + ((getCUT_OPINION() == null) ? 0 : getCUT_OPINION().hashCode());
        result = prime * result + ((getCUT_TIME() == null) ? 0 : getCUT_TIME().hashCode());
        result = prime * result + ((getSS_NAME() == null) ? 0 : getSS_NAME().hashCode());
        result = prime * result + ((getCI_NAME() == null) ? 0 : getCI_NAME().hashCode());
        result = prime * result + ((getNEED_CODE() == null) ? 0 : getNEED_CODE().hashCode());
        result = prime * result + ((getCUT_CATEGURY() == null) ? 0 : getCUT_CATEGURY().hashCode());
        result = prime * result + ((getCLCS() == null) ? 0 : getCLCS().hashCode());
        result = prime * result + ((getRESPONSIBLE_UNIT() == null) ? 0 : getRESPONSIBLE_UNIT().hashCode());
        result = prime * result + ((getCOMPLETE_DATE() == null) ? 0 : getCOMPLETE_DATE().hashCode());
        result = prime * result + ((getCURRENT_STATUS() == null) ? 0 : getCURRENT_STATUS().hashCode());
        result = prime * result + ((getRESPONSIBLE_PEOPLE() == null) ? 0 : getRESPONSIBLE_PEOPLE().hashCode());
        result = prime * result + ((getPHONE() == null) ? 0 : getPHONE().hashCode());
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
        sb.append(", NEED_ID=").append(NEED_ID);
        sb.append(", NEED_NAME=").append(NEED_NAME);
        sb.append(", NEED_SETP=").append(NEED_SETP);
        sb.append(", CUT_COMPANY_NAME=").append(CUT_COMPANY_NAME);
        sb.append(", CUT_PEOPLE_NAME=").append(CUT_PEOPLE_NAME);
        sb.append(", CUT_OPINION=").append(CUT_OPINION);
        sb.append(", CUT_TIME=").append(CUT_TIME);
        sb.append(", SS_NAME=").append(SS_NAME);
        sb.append(", CI_NAME=").append(CI_NAME);
        sb.append(", NEED_CODE=").append(NEED_CODE);
        sb.append(", CUT_CATEGURY=").append(CUT_CATEGURY);
        sb.append(", CLCS=").append(CLCS);
        sb.append(", RESPONSIBLE_UNIT=").append(RESPONSIBLE_UNIT);
        sb.append(", COMPLETE_DATE=").append(COMPLETE_DATE);
        sb.append(", CURRENT_STATUS=").append(CURRENT_STATUS);
        sb.append(", RESPONSIBLE_PEOPLE=").append(RESPONSIBLE_PEOPLE);
        sb.append(", PHONE=").append(PHONE);
        sb.append(", CREATE_PEOPLE=").append(CREATE_PEOPLE);
        sb.append(", CREATE_TIME=").append(CREATE_TIME);
        sb.append(", UPDATE_PEOPLE=").append(UPDATE_PEOPLE);
        sb.append(", UPDATE_TIME=").append(UPDATE_TIME);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}