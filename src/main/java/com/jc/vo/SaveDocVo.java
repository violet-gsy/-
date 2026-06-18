package com.jc.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.time.LocalDateTime;


@Schema(description = "保存文档实体")
@Data
public class SaveDocVo implements Serializable {

    @Schema(description = "文档类型")
    private String type;

    @Schema(description = "当前版本号")
    private String version;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人")
    private String creator;

    @Schema(description = "文件内容")
    MultipartFile file;
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}