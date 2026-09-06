package com.jc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.entity.LaunchMode;
import com.jc.entity.LaunchModeDiagram;
import com.jc.mapper.LaunchModeDiagramMapper;
import com.jc.mapper.LaunchModeMapper;
import com.jc.service.LaunchModeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LaunchModeServiceImpl extends ServiceImpl<LaunchModeMapper, LaunchMode> implements LaunchModeService {

    private final LaunchModeDiagramMapper diagramMapper;

    @Override
    public LaunchMode getLaunchModeWithDiagrams(String launchModeId) {
        LaunchMode launchMode = this.getById(launchModeId);
        if (launchMode != null) {
            List<LaunchModeDiagram> diagrams = getDiagramsByLaunchMode(launchModeId);
            launchMode.setDiagrams(diagrams);
        }
        return launchMode;
    }

    @Override
    public List<LaunchMode> getEnabledLaunchModes() {
        LambdaQueryWrapper<LaunchMode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LaunchMode::getStatus, "1")
                .orderByAsc(LaunchMode::getSortOrder);
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveLaunchModeWithDiagrams(LaunchMode launchMode, List<String> diagramIds) {
        boolean saved = this.save(launchMode);
        if (saved && diagramIds != null && !diagramIds.isEmpty()) {
            saveDiagramAssociations(launchMode.getLaunchModeId(), diagramIds);
        }
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLaunchModeDiagrams(String launchModeId, List<String> diagramIds) {
        // 删除旧的关联
        LambdaQueryWrapper<LaunchModeDiagram> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LaunchModeDiagram::getLaunchModeId, launchModeId);
        diagramMapper.delete(wrapper);

        if (diagramIds != null && !diagramIds.isEmpty()) {
            saveDiagramAssociations(launchModeId, diagramIds);
        }
        return true;
    }

    @Override
    public List<LaunchModeDiagram> getDiagramsByLaunchMode(String launchModeId) {
        LambdaQueryWrapper<LaunchModeDiagram> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LaunchModeDiagram::getLaunchModeId, launchModeId)
                .orderByAsc(LaunchModeDiagram::getSortOrder);
        return diagramMapper.selectList(wrapper);
    }

    private void saveDiagramAssociations(String launchModeId, List<String> diagramIds) {
        List<LaunchModeDiagram> diagrams = new ArrayList<>();
        for (int i = 0; i < diagramIds.size(); i++) {
            LaunchModeDiagram diagram = new LaunchModeDiagram();
            diagram.setLaunchModeId(launchModeId);
            diagram.setActivityDiagramId(diagramIds.get(i));
            diagram.setSortOrder(i);
            diagrams.add(diagram);
        }
        for (LaunchModeDiagram diagram : diagrams) {
            diagramMapper.insert(diagram);
        }
    }
}