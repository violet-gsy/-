package com.jc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页通用返回结果")
public class PageResult<T> {

    @Schema(description = "总条数")
    private long total;

    @Schema(description = "总页数")
    private long pages;

    @Schema(description = "当前页码")
    private long current;

    @Schema(description = "每页条数")
    private long size;

    @Schema(description = "数据列表")
    private List<T> records;
}
