package com.jc.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "组件树形结构实体")
@Data
public class TreeComponentVo implements Serializable {

    @Schema(description = "组件ID")
    private String id;

    @Schema(description = "产品ID")
    private String productid;

    @Schema(description = "组件名称")
    private String name;

    @Schema(description = "组件类型")
    private String type;

    @Schema(description = "父组件 ID")
    private String parentid;

    @Schema(description = "生产单位")
    private String manufacturer;

    @Schema(description = "出厂日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime producedate;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createtime;

    @Schema(description = "子节点")
    private List<TreeComponentVo> children = new ArrayList<>();

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}