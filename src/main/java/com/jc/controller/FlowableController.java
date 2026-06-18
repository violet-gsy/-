package com.jc.controller;

import cn.hutool.core.util.StrUtil;
import com.jc.allenum.MessageTypeEnum;
import com.jc.service.FlowAbleService;
import com.jc.util.*;
import com.jc.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.*;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.jc.util.ApiResponse;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/flowable")
@Slf4j
@Tag(name = "流程类")
public class FlowableController {
    @Resource
    private RepositoryService repositoryService;

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private TaskService taskService;

    @Resource
    private HistoryService historyService;

    @Resource
    private FlowChartToBpmnConverter converter;

    @Resource
    IdentityService identityService;

    @Resource
    FlowAbleService flowAbleService;

    private final BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();

    /**
     * 部署流程定义
     * 部署资源表：act_ge_bytearray
     * 部署ID表：act_re_deployment
     * 流程表：act_re_procdef
     * @return
     */
    @GetMapping("/def")
    @Operation(summary = "部署流程（测试）")
   public ApiResponse getDeployment(){
       Deployment deployment = repositoryService.createDeployment()
               .addClasspathResource("processes/flowableTest.bpmn20.xml")
               .name("运载器发射流程")
               .deploy();
       log.info("部署--{}--流程成功","运载器发射");
       return ApiResponse.success("部署流程成功",deployment);
   }


    @PostMapping("/saveEmptyFlow")
    @Operation(summary = "创建/修改空流程（仅名称）")
    public ApiResponse saveEmptyFlow(@RequestBody EmptyFlowVO vo) {
        // 1. 名称非空校验
        if (StrUtil.isBlank(vo.getFlowName())) {
            return ApiResponse.error("流程名称不能为空");
        }
        String flowName = vo.getFlowName().trim();

        // 2. 名称重复校验
        boolean nameExists = flowAbleService.checkFlowNameExists(flowName, vo.getProcessKey());
        if (nameExists) {
            return ApiResponse.error("流程名称已存在，请更换名称");
        }

        // 3. 修改名称（兼容所有Flowable版本）
        if (StrUtil.isNotBlank(vo.getProcessKey())) {
            return flowAbleService.updateFlowName(vo.getProcessKey(), flowName);
        }

        // 4. 创建空流程
        return flowAbleService.createEmptyFlow(flowName);
    }



    // 1. 部署流程
    @PostMapping("/deploy")
    @Operation(summary = "部署流程")
    public ApiResponse deploy(@RequestBody JsonToFlowVO vo) {
        Date start = new Date();
        boolean isOverride = false;
        String processName = "";
        if (StrUtil.isBlank(vo.getProcessKey())){
            vo.setProcessKey("key_"+DmUuidUtil.get32Uuid());
        }else {
            // 1. 查询旧的同Key流程定义
            ProcessDefinition oldDef = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionKey(vo.getProcessKey())
                    .latestVersion()
                    .singleResult();

            if (oldDef != null) {
                // 2. 删除旧部署（cascade=true：只删流程模板，不删运行中/历史实例！）
                processName = oldDef.getName();
                repositoryService.deleteDeployment(oldDef.getDeploymentId(), true);
                log.info("已删除旧版本部署，deploymentId: {}", oldDef.getDeploymentId());
                isOverride = true;
            }
        }
        vo.setProcessName(processName);
        Deployment deploy = repositoryService.createDeployment()
                .addBpmnModel(vo.getProcessKey() + ".bpmn20.xml", flowAbleService.convert(vo))
                .name(processName)
                .key(vo.getProcessKey())
                .deploy();

        Date end = new Date();

        long costMs = end.getTime() - start.getTime();
        log.info("耗时：" + costMs + "ms");
        String deployType = isOverride ? "覆盖" : "新增";
        log.info(deployType+"部署成功！\n" +
                "流程KEY：" + vo.getProcessKey() );
        return ApiResponse.success(deployType + "部署成功！\n" +
                "流程KEY：" + vo.getProcessKey() );
    }


    @Operation(summary = "分页查询流程定义列表", description = "支持流程名称、流程KEY、部署名称模糊查询")
    @GetMapping("/selProcessPageList")
    public PageResult<ProcessDefinitionVO> selProcessPageList(
            @RequestParam(defaultValue = "1") @Schema(description = "页码") Integer pageNum,
            @RequestParam(defaultValue = "10") @Schema(description = "每页条数") Integer pageSize,
            @RequestParam(required = false) @Schema(description = "模糊查询关键词：流程名称") String processName
    ) {
        // 1. 构建查询条件
        var query = repositoryService.createProcessDefinitionQuery();

        // 仅按流程名称模糊查询
        if (processName != null && !processName.trim().isEmpty()) {
            query.processDefinitionNameLike("%" + processName.trim() + "%");
        }

        // 2. 先查询所有符合条件的数据（用于排序）
        List<ProcessDefinition> definitionList = query.list();

        // 3. 获取部署信息，按部署时间倒序（核心修复点！）
        Map<String, Deployment> deploymentMap = repositoryService.createDeploymentQuery()
                .list()
                .stream()
                .collect(Collectors.toMap(Deployment::getId, d -> d));

        definitionList = definitionList.stream()
                .sorted((a, b) -> {
                    Deployment d1 = deploymentMap.get(a.getDeploymentId());
                    Deployment d2 = deploymentMap.get(b.getDeploymentId());
                    if (d1 == null || d2 == null) return 0;
                    // 倒序：最新的排第一
                    return d2.getDeploymentTime().compareTo(d1.getDeploymentTime());
                })
                .collect(Collectors.toList());

        // 4. 手动分页
        long total = definitionList.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, definitionList.size());
        List<ProcessDefinition> pageList = definitionList.subList(start, end);

        // 5. 封装VO
        List<ProcessDefinitionVO> records = new ArrayList<>();
        for (ProcessDefinition def : pageList) {
            ProcessDefinitionVO vo = new ProcessDefinitionVO();
            vo.setProcessDefinitionId(def.getId());
            vo.setProcessKey(def.getKey());
            vo.setProcessName(def.getName());
            vo.setVersion(def.getVersion());
            vo.setResourceName(def.getResourceName());
            vo.setDiagramResourceName(def.getDiagramResourceName());
            vo.setDescription(def.getDescription());
            vo.setTenantId(def.getTenantId());

            boolean suspended = def.isSuspended();
            vo.setSuspended(suspended);
            vo.setStatus(suspended ? "已挂起" : "正常");

            Deployment deployment = deploymentMap.get(def.getDeploymentId());
            if (deployment != null) {
                vo.setDeploymentId(deployment.getId());
                vo.setDeploymentName(deployment.getName());
                vo.setDeploymentTime(deployment.getDeploymentTime());
            }

            records.add(vo);
        }

        long pages = (total + pageSize - 1) / pageSize;
        return new PageResult<>(total, pages, pageNum, pageSize, records);
    }

    @Operation(summary = "清空所有流程")
    @GetMapping("/cleanAll")
    public ApiResponse<String> cleanAll() {
        try {
            // 1. 查询所有部署ID
            repositoryService.createDeploymentQuery().list().forEach(deployment -> {
                repositoryService.deleteDeployment(deployment.getId(), true);
            });
            log.info("清空所有流程数据成功");
            return ApiResponse.success("清空所有流程数据成功！");
        } catch (Exception e) {
            return ApiResponse.error("清空失败：" + e.getMessage());
        }
    }

    @Operation(summary = "删除流程（部署 + 实例 + 历史）")
    @GetMapping("/cleanProcess")
    public ApiResponse cleanProcess(@RequestParam @Schema(description = "流程key") String processKey) {

        // 1. 删除【运行中】的流程实例
        runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(processKey)
                .list()
                .forEach(instance -> runtimeService.deleteProcessInstance(instance.getId(), "清空流程"));

        // 2. 删除【历史】流程实例（Flowable 6.x 正确写法）
        List<HistoricProcessInstance> historicList = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(processKey)
                .list();

        for (HistoricProcessInstance hpi : historicList) {
            historyService.deleteHistoricProcessInstance(hpi.getId());
        }

        // 3. 删除流程部署（所有版本 + 级联删除）
        repositoryService.createDeploymentQuery()
                .processDefinitionKey(processKey)
                .list()
                .forEach(deployment -> {
                    repositoryService.deleteDeployment(deployment.getId(), true);
                });
        log.info("✅ 流程【" + processKey + "】已全部清空");
        return ApiResponse.success("✅ 流程【" + processKey + "】已全部清空：\n" +
                "• 运行实例已删除\n" +
                "• 历史流程已删除\n" +
                "• 流程模板已删除");
    }



    @PostMapping("/startProcess")
    @Operation(summary="二维启动流程（admin）")
    public ApiResponse startProcess(@RequestParam @Schema(description = "流程key") String processKey) {
        Map<String, Object> result = new HashMap<>();
        try {
            identityService.setAuthenticatedUserId("admin");
            ProcessInstance instance = runtimeService.startProcessInstanceByKey(processKey);
            //清除上下文
            identityService.setAuthenticatedUserId(null);
            result.put("code", 200);
            result.put("msg", "流程启动成功");
            result.put("流程实例ID", instance.getId());
            log.info("流程启动成功,流程实例ID为"+instance.getId());
            WebSocketServer.sendMsg(MessageTypeEnum.STARTENODE,"startNode",0);
            // 2. 启动该流程的自动审批（异步，不阻塞当前接口）
            flowAbleService.startAutoApproval(instance.getId());

        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "启动失败：" + e.getMessage());
            log.info("流程启动失败："+e.getMessage());
        }
        return ApiResponse.success(result);
    }


    @GetMapping("/getMyTasks")
    @Operation(summary="查询我的待办审批")
    public ApiResponse<List<TaskVO>> getMyTasks( @RequestParam @Schema(description = "审批人") String assignee) {
        try {
            List<Task> taskList = taskService.createTaskQuery()
                    .taskAssignee(assignee)
                    .orderByTaskCreateTime().desc()
                    .list();
            if (taskList.size() == 0){
                return ApiResponse.error("暂无待办审批");
            }
            List<TaskVO> voList = new ArrayList<>();
            for (Task task : taskList) {
                TaskVO vo = new TaskVO();
                vo.setTaskId(task.getId());
                vo.setTaskName(task.getName());
                vo.setProcessInstanceId(task.getProcessInstanceId());
                vo.setProcessDefinitionId(task.getProcessDefinitionId());
                vo.setAssignee(task.getAssignee());
                vo.setCreateTime(task.getCreateTime());
                voList.add(vo);
            }

            return ApiResponse.success(voList);
        } catch (Exception e) {
            return ApiResponse.error();
        }
    }


    @PostMapping("/audit")
    @Operation(summary="审批同意拒绝（注：auditResult 必须严格传：agree /reject）")
    public ApiResponse audit(@RequestBody ProcessAuditVo dto) {
        String taskId = dto.getTaskId();
        String auditResult = dto.getAuditResult();

        // ====== 关键校验：前端传错直接提示 ======
        if (!"agree".equals(auditResult) && !"reject".equals(auditResult)) {
            return ApiResponse.error("审批结果错误！只能传 agree 或 reject！");
        }

        // 正常审批逻辑...
        Map<String, Object> vars = new HashMap<>();
        vars.put("auditResult", auditResult);
        try {
            taskService.complete(taskId, vars);
            log.info("任务"+taskId+"：审批成功，进入下一步");
            return ApiResponse.success("审批成功：" + ("agree".equals(auditResult) ? "同意" : "拒绝"));
        }catch (Exception e){
            log.info("任务"+taskId+"：审批失败："+e.getMessage());
            return ApiResponse.error("审批失败："+e.getMessage());
        }
    }


    /**
     * 前端传入：当前任务ID + 目标节点ID 即可完成跳转
     */
    @PostMapping("/jumpToSpecifiedNode")
    @Operation(summary = "流程跳转至指定节点(驳回/退回用)")
    public ApiResponse jumpToSpecifiedNode(@RequestBody ProcessJumpVo vo) {
        try {
            // 1. 获取当前任务
            Task currentTask = taskService.createTaskQuery()
                    .taskId(vo.getTaskId())
                    .singleResult();

            if (currentTask == null) {
                log.info("跳转失败：当前任务不存在");
                return ApiResponse.error("当前任务不存在");
            }

            // 2. 添加跳转批注
            if (vo.getComment() != null && !vo.getComment().isEmpty()) {
                taskService.addComment(
                        currentTask.getId(),
                        currentTask.getProcessInstanceId(),
                        "跳转备注：" + vo.getComment()
                );
            }

            // 3. 核心：Flowable 官方节点跳跃
            runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(currentTask.getProcessInstanceId())
                    // 当前节点 → 跳转到目标节点
                    .moveActivityIdTo(currentTask.getTaskDefinitionKey(), vo.getTargetNodeId())
                    .changeState();
            log.info("跳转成功：当前已跳至："+ vo.getTargetNodeId() + "节点");
            return ApiResponse.success("跳转成功！当前已跳至：" + vo.getTargetNodeId());
        } catch (Exception e) {
            log.info("跳转失败："+ e.getMessage());
            return ApiResponse.error("跳转失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取流程图数据（前端画图+高亮进度，自动兼容模板/运行实例）")
    @GetMapping("/getFlowChart")
    public ApiResponse<FlowChartVO> getFlowChart(@RequestParam String processInstanceId) {

        Date start = new Date();
        ApiResponse<FlowChartVO> apiResponse = flowAbleService.getFlowChart(processInstanceId);
        log.info("查询流程图耗时：{}ms", (new Date().getTime() - start.getTime()));
        return ApiResponse.success("获取成功", apiResponse.getData());
    }

    @GetMapping("/exportBpmn")
    @Operation(summary = "导出流程为 bpmn20.xml")
    public void exportBpmn(@RequestParam @Schema(description = "流程key") String processKey, HttpServletResponse response) throws Exception {
        // 1. 获取最新版本流程定义
        var procDef = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processKey)
                .latestVersion()
                .singleResult();

        // 2. 获取 BPMN 模型并转 XML
        BpmnModel bpmnModel = repositoryService.getBpmnModel(procDef.getId());
        byte[] xmlBytes = bpmnXMLConverter.convertToXML(bpmnModel);

        // 3. 设置下载响应头
        response.setContentType("application/octet-stream");
        String fileName = URLEncoder.encode(processKey + ".bpmn20.xml", StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + fileName);

        // 4. 写出到浏览器
        response.getOutputStream().write(xmlBytes);
        response.getOutputStream().flush();
    }


    @PostMapping("/uploadBpmn")
    @Operation(summary = "上传bpmn20.xml并部署")
    public ApiResponse uploadBpmn(@RequestParam("file") MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".bpmn20.xml") && !fileName.endsWith(".bpmn"))) {
            return ApiResponse.error("文件格式错误，请上传 bpmn20.xml 或 bpmn 文件");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Deployment deployment = repositoryService.createDeployment()
                    .addInputStream(fileName, inputStream)
                    .name("流程部署:" + fileName)
                    .deploy();
            // 2. 获取刚部署的流程定义
            ProcessDefinition processDefinition = repositoryService
                    .createProcessDefinitionQuery()
                    .deploymentId(deployment.getId())
                    .singleResult();

            if (processDefinition == null) {
                return ApiResponse.error("部署成功，但未找到流程定义");
            }

            // 3. 自动启动流程
            String processInstanceId = runtimeService
                    .startProcessInstanceById(processDefinition.getId())
                    .getId();

            return ApiResponse.success("部署成功！部署ID：" + deployment.getId() +
                    "\n流程已自动启动！实例ID：" + processInstanceId);
        } catch (IOException e) {
            return ApiResponse.error("部署失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取节点具体属性")
    @GetMapping("/getNodeAttr")
    public ApiResponse<NodeAttrOutputVo> getNodeAttr(@RequestParam String pnodeid) {
        ApiResponse<NodeAttrOutputVo> apiResponse = flowAbleService.getNodeAttr(pnodeid);
        return ApiResponse.success("获取成功", apiResponse.getData());
    }

}
