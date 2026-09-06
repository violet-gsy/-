package com.jc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jc.entity.LaunchMode;
import com.jc.entity.LaunchModeDiagram;

import java.util.List;

public interface LaunchModeService extends IService<LaunchMode> {

    /**
     * 获取测发模式及其关联的流程列表
     */
    LaunchMode getLaunchModeWithDiagrams(String launchModeId);

    /**
     * 获取所有启用的测发模式
     */
    List<LaunchMode> getEnabledLaunchModes();

    /**
     * 保存测发模式并关联流程
     */
    boolean saveLaunchModeWithDiagrams(LaunchMode launchMode, List<String> diagramIds);

    /**
     * 更新测发模式的流程关联
     */
    boolean updateLaunchModeDiagrams(String launchModeId, List<String> diagramIds);

    /**
     * 获取指定测发模式下的流程列表
     */
    List<LaunchModeDiagram> getDiagramsByLaunchMode(String launchModeId);
}