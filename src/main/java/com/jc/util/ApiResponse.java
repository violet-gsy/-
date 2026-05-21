package com.jc.util;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 统一接口返回封装
 * 前端所有接口都用这个格式返回
 */
@Data
@Schema(description = "统一返回结果")
public class ApiResponse<T> {

    // 响应码：200成功 500失败
    @Schema(description = "状态码 200成功 500失败")
    private int code;

    // 响应消息
    @Schema(description = "返回消息")
    private String msg;

    // 响应数据
    @Schema(description = "返回数据")
    private T data;

    // ====================== 成功返回 ======================
    public static <T> ApiResponse<T> success() {
        ApiResponse<T> res = new ApiResponse<>();
        res.setCode(200);
        res.setMsg("操作成功");
        return res;
    }

    public static <T> ApiResponse<T> success(String msg) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setCode(200);
        res.setMsg(msg);
        return res;
    }

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setCode(200);
        res.setMsg("操作成功");
        res.setData(data);
        return res;
    }

    public static <T> ApiResponse<T> success(String msg, T data) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setCode(200);
        res.setMsg(msg);
        res.setData(data);
        return res;
    }

    // ====================== 失败返回 ======================
    public static <T> ApiResponse<T> error() {
        ApiResponse<T> res = new ApiResponse<>();
        res.setCode(500);
        res.setMsg("操作失败");
        return res;
    }

    public static <T> ApiResponse<T> error(String msg) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setCode(500);
        res.setMsg(msg);
        return res;
    }

    public static <T> ApiResponse<T> error(int code, String msg) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setCode(code);
        res.setMsg(msg);
        return res;
    }
}