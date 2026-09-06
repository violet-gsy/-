package myplugin;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.simulation.SimulationManager;
import com.nomagic.magicdraw.simulation.execution.SimulationResult;
import com.nomagic.magicdraw.simulation.execution.session.SimulationSession;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.Activity;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.ActivityPartition;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.symbols.paths.PathElement;
import com.nomagic.magicdraw.uml.symbols.shapes.ShapeElement;
import com.nomagic.magicdraw.uml.symbols.shapes.SwimlaneView;
import com.nomagic.magicdraw.uml.symbols.shapes.SwimlaneHeaderView;
import com.nomagic.magicdraw.uml.symbols.shapes.SwimlaneCellView;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;
import myplugin.OperatorResourceManager;
import java.util.Map;
import com.nomagic.magicdraw.simulation.execution.SimulationOptions;
import com.nomagic.magicdraw.simulation.execution.SimulationOptionsProvider;

import com.nomagic.magicdraw.simulation.utils.ALH;
import java.util.Collection;
import fUML.Semantics.Classes.Kernel.StructuredValue;
import fUML.Semantics.Classes.Kernel.Object_;
import org.json.JSONObject;

import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.State;

import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.Activity;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

/**
 * MagicDraw 仿真同步 WebSocket 服务器 - 合并最终版本
 *
 * 集成功能：
 *  - 图列表、图 JSON 导出（含泳道支持）
 *  - 仿真启动 / 停止 / Run with Context / Simulation Config 启动
 *  - 节点变量获取（基于反射）
 *  - 子流程绑定（反射调用 setBehavior）
 *  - 航天器外部交互
 *  - 仿真回放记录（含内存缓存与文件落盘）
 *  - 运行时变量推送（动态读取 featureValues）
 *  - 事件日志推送
 *  - 状态机 Trigger 信号获取与发送（含状态内部行为）
 *  - 节点约束（Constraint/DurationConstraint）获取
 *
 * @author MagicDraw Plugin Team
 * @version 3.2 (Constraint Support + Swimlane Enhancement)
 */
public class SimulationSyncServer extends WebSocketServer {
    public static final int PORT = 8765;
    private static volatile SimulationSyncServer INSTANCE;
    private final Set<WebSocket> clients = Collections.synchronizedSet(new LinkedHashSet<>());
    private final GUILog log;
    private SimulationResult currentResult;
    private SimulationSession currentSession;
    private volatile double simulationSpeed = 1.0;
    private SimulationOptionsProvider speedOptionsProvider;
    private String lastSpacecraftResultJson;
    private volatile boolean running = false;

    // ========================= 仿真回放记录 =========================
    private boolean replayRecording = false;
    private long replayStartMillis = 0L;
    private String currentReplayRunId = "";
    private String replayDiagramId = "";
    private String replayDiagramName = "";
    private String replayStartMode = "";
    private String replayStartTimeText = "";
    private String replayDiagramSnapshotJson = "{}";
    private StringBuilder replayEventsJson = new StringBuilder("[");
    private String latestReplayJsonCache = null;
    private String latestReplayRunId = "";
    private final Object replayLock = new Object();

    public static synchronized SimulationSyncServer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SimulationSyncServer();
        }
        return INSTANCE;
    }

    private SimulationSyncServer() {
        super(new InetSocketAddress(ConfigLoader.getHost(), ConfigLoader.getPort()));
        this.log = Application.getInstance().getGUILog();
        setReuseAddr(true);
        setConnectionLostTimeout(60);
        info("[SimSync] WebSocket 服务器绑定地址: " + ConfigLoader.getAddressString());
        initOperatorResourceStats();
    }

    private void initOperatorResourceStats() {
        OperatorResourceManager.setStatsSender(new OperatorResourceManager.StatsSender() {
            @Override
            public void sendNodeStats(String taskName, Map<String, Object> stats) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("event", "node_stats");
                    json.put("nodeName", taskName);
                    for (Map.Entry<String, Object> entry : stats.entrySet()) {
                        json.put(entry.getKey(), entry.getValue());
                    }
                    broadcast(json.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void sendLog(String message, String level, String time) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("event", "resource_log");
                    json.put("message", message);
                    json.put("level", level);
                    json.put("time", time);
                    broadcast(json.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void sendResourcePoolStatus(int total, int available, int waitingQueueSize) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("event", "resource_pool_status");
                    json.put("total", total);
                    json.put("available", available);
                    json.put("waitingQueueSize", waitingQueueSize);
                    broadcast(json.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        info("[SimSync] OperatorResourceManager StatsSender 已初始化");
    }

    public synchronized void setCurrentSession(SimulationSession session) {
        this.currentSession = session;
        if (session != null) {
            info("[SimSync] 已记录当前仿真会话");
        } else {
            info("[SimSync] 当前仿真会话已清空");
        }
    }

    public boolean isRunning() {
        return running;
    }

    private void info(String msg) {
        log.log("[INFO] " + msg);
    }

    private void err(String msg) {
        log.log("[ERROR] " + msg);
    }

    private void pushFrontendInfo(String msg) {
        info(msg);
        broadcast("{\"event\":\"backend_info\",\"message\":\"" + escapeJson(msg) + "\"}");
    }

    private void pushFrontendError(String msg) {
        err(msg);
        broadcast("{\"event\":\"backend_error\",\"message\":\"" + escapeJson(msg) + "\"}");
    }

    @Override
    public void onStart() {
        running = true;
        info("[SimSync] WebSocket 服务器已启动，地址: " + ConfigLoader.getAddressString());
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        clients.add(conn);
        info("[SimSync] 前端已连接: " + conn.getRemoteSocketAddress());
        pushEventLog("info", "[SimSync] 前端已连接");
        conn.send("{\"event\":\"status\",\"status\":\"ready\"}");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        clients.remove(conn);
        info("[SimSync] 前端已断开");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            if (message.contains("\"command\":\"list_diagrams\"")) {
                sendDiagramList(conn);
            } else if (message.contains("\"command\":\"get_diagram_json\"")) {
                String diagramId = getStringFromJson(message, "diagramId");
                sendDiagramJson(conn, diagramId);
            } else if (message.contains("\"command\":\"get_latest_replay\"")) {
                sendLatestReplay(conn);
            } else if (message.contains("\"command\":\"start_simulation\"")) {
                String diagramId = getStringFromJson(message, "diagramId");
                String mode = getStringFromJson(message, "mode");
                boolean useConfig = getBooleanFromJson(message, "useConfig", false);
                String configName = getStringFromJson(message, "configName");
                if ("run_with_context".equalsIgnoreCase(mode)) {
                    startSimulationWithContext(diagramId, conn);
                } else {
                    startSimulation(diagramId, useConfig, configName);
                }
            } else if (message.contains("\"command\":\"stop_simulation\"")) {
                stopSimulation();
            } else if (message.contains("\"command\":\"get_node_variables\"")) {
                String nodeId = getStringFromJson(message, "nodeId");
                String nodeName = getStringFromJson(message, "nodeName");
                sendNodeVariables(conn, nodeId, nodeName);
            } else if (message.contains("\"command\":\"get_activities_list\"")) {
                sendActivitiesList(conn);
            } else if (message.contains("\"command\":\"bind_subprocess\"")) {
                String nodeId = getStringFromJson(message, "nodeId");
                String activityId = getStringFromJson(message, "activityId");
                bindSubprocessToNode(conn, nodeId, activityId);
            } else if (message.contains("\"event\":\"external_interaction_result\"")
                    && message.contains("\"type\":\"spacecraft_check_result\"")) {
                saveSpacecraftResult(message);
                triggerSpacecraftCompleteSignal();
            } else if (message.contains("\"command\":\"get_node_config\"")) {
                String taskName = getStringFromJson(message, "taskName");
                String callbackId = getStringFromJson(message, "_callbackId");
                sendNodeConfig(conn, taskName, callbackId);
            } else if (message.contains("\"command\":\"get_resource_pool_config\"")) {
                sendResourcePoolConfig(conn);
            } else if (message.contains("\"command\":\"update_resource_pool_config\"")) {
                Integer totalOperators = null;
                Double timeScaleMs = null;
                String totalStr = getStringFromJson(message, "totalOperators");
                if (totalStr != null && !totalStr.isEmpty()) {
                    try { totalOperators = Integer.parseInt(totalStr); } catch (NumberFormatException ignored) {}
                }
                String scaleStr = getStringFromJson(message, "timeScaleMs");
                if (scaleStr != null && !scaleStr.isEmpty()) {
                    try { timeScaleMs = Double.parseDouble(scaleStr); } catch (NumberFormatException ignored) {}
                }
                updateResourcePoolConfig(conn, totalOperators, timeScaleMs);
            } else if (message.contains("\"command\":\"get_init_node_body\"")) {
                String nodeId = getStringFromJson(message, "nodeId");
                sendInitNodeBody(conn, nodeId);
            } else if (message.contains("\"command\":\"update_init_node_body\"")) {
                String nodeId = getStringFromJson(message, "nodeId");
                String newBody = getStringFromJson(message, "body");
                updateInitNodeBody(conn, nodeId, newBody);
            } else if (message.contains("\"command\":\"get_all_node_stats\"")) {
                sendAllNodeStats(conn);
            } else if (message.contains("\"command\":\"get_simulation_summary\"")) {
                sendSimulationSummary(conn);
            } else if (message.contains("\"command\":\"get_node_full_config\"")) {
                String taskName = getStringFromJson(message, "taskName");
                String callbackId = getStringFromJson(message, "_callbackId");
                sendNodeFullConfig(conn, taskName, callbackId);
            } else if (message.contains("\"command\":\"get_all_node_configs\"")) {
                String callbackId = getStringFromJson(message, "_callbackId");
                sendAllNodeConfigs(conn, callbackId);
            } else if (message.contains("\"command\":\"update_node_full_config\"")) {
                try {
                    JSONObject json = new JSONObject(message);
                    String taskName = json.optString("taskName", "");
                    String callbackId = json.optString("_callbackId", "");
                    JSONObject properties = json.optJSONObject("properties");
                    if (properties == null) {
                        sendConnError(conn, "缺少 properties 参数");
                        return;
                    }
                    System.out.println("[NodeConfig] 📤 收到完整配置更新请求:");
                    System.out.println("  taskName: " + taskName);
                    System.out.println("  properties: " + properties.toString(2));
                    System.out.println("  callbackId: " + callbackId);
                    updateNodeFullConfig(conn, taskName, properties, callbackId);
                } catch (Exception e) {
                    err("[NodeConfig] 解析 update_node_full_config 命令失败: " + e.getMessage());
                    e.printStackTrace();
                    sendConnError(conn, "解析命令失败: " + e.getMessage());
                }
            } else if (message.contains("\"command\":\"get_node_subprocess\"")) {
                String nodeId = getStringFromJson(message, "nodeId");
                sendNodeSubprocessInfo(conn, nodeId);
            } else if (message.contains("\"command\":\"open_subprocess_diagram\"")) {
                String nodeId = getStringFromJson(message, "nodeId");
                openSubprocessDiagram(conn, nodeId);
            } else if (message.contains("\"command\":\"get_subprocess_diagram_json\"")) {
                String nodeId = getStringFromJson(message, "nodeId");
                String callbackId = getStringFromJson(message, "_callbackId");
                getSubprocessDiagramJson(conn, nodeId, callbackId);
            } else if (message.contains("\"command\":\"get_state_machine_triggers\"")) {
                String diagramId = getStringFromJson(message, "diagramId");
                sendStateMachineTriggers(conn, diagramId);
            } else if (message.contains("\"command\":\"send_signal\"")) {
                String signalName = getStringFromJson(message, "signalName");
                sendSignalToSimulation(conn, signalName);
            } else if (message.contains("\"command\":\"get_node_constraints\"")) {
                String nodeId = getStringFromJson(message, "nodeId");
                sendNodeConstraints(conn, nodeId);
            }
        } catch (Exception e) {
            err("[SimSync] 解析命令失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendNodeConstraints(WebSocket conn, String nodeId) {
        try {
            Project project = Application.getInstance().getProject();
            if (project == null) {
                sendConnError(conn, "未打开项目");
                return;
            }
            Element el = findElementById(project, nodeId);
            if (el == null) {
                sendConnError(conn, "未找到节点: " + nodeId);
                return;
            }

            List<Map<String, Object>> constraints = new ArrayList<>();

            try {
                Element primaryModel = project.getPrimaryModel();
                if (primaryModel != null) {
                    List<Element> allConstraints = new ArrayList<>();
                    findAllConstraintsRecursive(primaryModel, allConstraints);
                    info("[Constraints] 项目中找到 " + allConstraints.size() + " 个约束元素");

                    for (Element constraintElem : allConstraints) {
                        boolean isConstrained = isElementConstrainedBy(constraintElem, el);
                        if (isConstrained) {
                            Map<String, Object> constraintInfo = extractConstraintInfo(constraintElem);
                            if (constraintInfo != null) {
                                constraints.add(constraintInfo);
                                info("[Constraints] 找到关联当前节点的约束: " + constraintInfo.get("name"));
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                info("[Constraints] 全局查找约束失败: " + t.getMessage());
                t.printStackTrace();
            }

            JSONObject response = new JSONObject();
            response.put("event", "node_constraints");
            response.put("nodeId", nodeId);
            response.put("constraints", constraints);
            response.put("count", constraints.size());

            if (conn != null && conn.isOpen()) {
                conn.send(response.toString());
            }
            info("[Constraints] 已发送节点 " + nodeId + " 的 " + constraints.size() + " 个约束");

        } catch (Exception e) {
            err("[Constraints] 获取约束失败: " + e.getMessage());
            e.printStackTrace();
            sendConnError(conn, "获取约束失败: " + e.getMessage());
        }
    }

    private void findAllConstraintsRecursive(Element element, List<Element> result) {
        if (element == null) return;

        String className = element.getClass().getSimpleName();
        if (className.contains("Constraint") || className.contains("DurationConstraint")) {
            result.add(element);
        }

        try {
            Collection<Element> children = element.getOwnedElement();
            if (children != null) {
                for (Element child : children) {
                    findAllConstraintsRecursive(child, result);
                }
            }
        } catch (Throwable ignored) {}
    }

    private boolean isElementConstrainedBy(Element constraint, Element target) {
        if (constraint == null || target == null) return false;

        try {
            try {
                java.lang.reflect.Method getConstrainedMethod = constraint.getClass().getMethod("getConstrainedElement");
                Object constrained = getConstrainedMethod.invoke(constraint);
                if (constrained instanceof Collection) {
                    for (Object ce : (Collection<?>) constrained) {
                        if (ce == target || ce.equals(target)) {
                            return true;
                        }
                        if (ce instanceof Element) {
                            String ceId = ((Element) ce).getID();
                            String targetId = target.getID();
                            if (ceId != null && ceId.equals(targetId)) {
                                return true;
                            }
                        }
                    }
                }
            } catch (NoSuchMethodException e) {
            } catch (Throwable ignored) {}

            try {
                java.lang.reflect.Field constrainedField = null;
                Class<?> clazz = constraint.getClass();
                while (clazz != null && constrainedField == null) {
                    try {
                        constrainedField = clazz.getDeclaredField("constrainedElement");
                    } catch (NoSuchFieldException e) {
                        clazz = clazz.getSuperclass();
                    }
                }
                if (constrainedField != null) {
                    constrainedField.setAccessible(true);
                    Object constrained = constrainedField.get(constraint);
                    if (constrained instanceof Collection) {
                        for (Object ce : (Collection<?>) constrained) {
                            if (ce == target || ce.equals(target)) {
                                return true;
                            }
                            if (ce instanceof Element) {
                                String ceId = ((Element) ce).getID();
                                String targetId = target.getID();
                                if (ceId != null && ceId.equals(targetId)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {}

            try {
                java.lang.reflect.Method getContextMethod = constraint.getClass().getMethod("getContext");
                Object context = getContextMethod.invoke(constraint);
                if (context == target || context.equals(target)) {
                    return true;
                }
                if (context instanceof Element) {
                    String contextId = ((Element) context).getID();
                    String targetId = target.getID();
                    if (contextId != null && contextId.equals(targetId)) {
                        return true;
                    }
                }
            } catch (NoSuchMethodException e) {
            } catch (Throwable ignored) {}

        } catch (Throwable t) {
            info("[Constraints] 检查约束关联失败: " + t.getMessage());
        }

        return false;
    }

    private String extractSpecFromString(String str) {
        if (str == null) return null;
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{[^}]*\\}");
        java.util.regex.Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private Map<String, Object> extractConstraintInfo(Element constraint) {
        if (constraint == null) return null;

        Map<String, Object> info = new LinkedHashMap<>();

        try {
            String name = null;
            if (constraint instanceof NamedElement) {
                name = ((NamedElement) constraint).getName();
            }
            if (name == null || name.trim().isEmpty()) {
                name = getNameByReflection(constraint);
            }
            info.put("name", name != null ? name : "未命名约束");

            String className = constraint.getClass().getSimpleName();
            info.put("type", className);
            info.put("isDurationConstraint", className.contains("DurationConstraint"));

            String spec = extractSpecificationValue(constraint);
            info.put("specification", spec != null ? spec : "—");

            if (className.contains("DurationConstraint")) {
                String minVal = null;
                String maxVal = null;

                try {
                    java.lang.reflect.Method getMinMethod = constraint.getClass().getMethod("getMin");
                    Object minObj = getMinMethod.invoke(constraint);
                    if (minObj != null) {
                        minVal = extractValueFromSpecification(minObj);
                    }
                } catch (Throwable ignored) {}

                try {
                    java.lang.reflect.Method getMaxMethod = constraint.getClass().getMethod("getMax");
                    Object maxObj = getMaxMethod.invoke(constraint);
                    if (maxObj != null) {
                        maxVal = extractValueFromSpecification(maxObj);
                    }
                } catch (Throwable ignored) {}

                if (minVal == null || minVal.isEmpty()) {
                    try {
                        java.lang.reflect.Field minField = constraint.getClass().getDeclaredField("min");
                        minField.setAccessible(true);
                        Object minObj = minField.get(constraint);
                        if (minObj != null) {
                            minVal = extractValueFromSpecification(minObj);
                        }
                    } catch (Throwable ignored) {}
                }
                if (maxVal == null || maxVal.isEmpty()) {
                    try {
                        java.lang.reflect.Field maxField = constraint.getClass().getDeclaredField("max");
                        maxField.setAccessible(true);
                        Object maxObj = maxField.get(constraint);
                        if (maxObj != null) {
                            maxVal = extractValueFromSpecification(maxObj);
                        }
                    } catch (Throwable ignored) {}
                }

                if ((minVal == null || minVal.isEmpty() || maxVal == null || maxVal.isEmpty()) && spec != null && spec.contains("..")) {
                    try {
                        String clean = spec.replaceAll("[^0-9.\\-..]", "");
                        String[] parts = clean.split("\\.\\.");
                        if (parts.length == 2) {
                            if (minVal == null || minVal.isEmpty()) minVal = parts[0].trim();
                            if (maxVal == null || maxVal.isEmpty()) maxVal = parts[1].trim();
                        }
                    } catch (Throwable ignored) {}
                }

                info.put("min", (minVal != null && !minVal.isEmpty()) ? minVal : "—");
                info.put("max", (maxVal != null && !maxVal.isEmpty()) ? maxVal : "—");
            }

        } catch (Throwable t) {
            info("[Constraints] 提取约束信息失败: " + t.getMessage());
            return null;
        }

        return info;
    }

    private String extractSpecificationValue(Element constraint) {
        if (constraint == null) return null;

        try {
            try {
                java.lang.reflect.Method getSpecMethod = constraint.getClass().getMethod("getSpecification");
                Object specObj = getSpecMethod.invoke(constraint);
                if (specObj != null) {
                    String value = extractValueFromSpecification(specObj);
                    if (value != null && !value.isEmpty()) {
                        return value;
                    }
                }
            } catch (NoSuchMethodException e) {
            } catch (Throwable ignored) {}

            try {
                java.lang.reflect.Method getBodyMethod = constraint.getClass().getMethod("getBody");
                Object body = getBodyMethod.invoke(constraint);
                if (body instanceof Collection) {
                    Collection<?> bodyList = (Collection<?>) body;
                    if (!bodyList.isEmpty()) {
                        String value = extractValueFromSpecification(bodyList.iterator().next());
                        if (value != null && !value.isEmpty()) {
                            return value;
                        }
                    }
                }
            } catch (Throwable ignored) {}

            String str = constraint.toString();
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{[^}]*\\}");
            java.util.regex.Matcher matcher = pattern.matcher(str);
            if (matcher.find()) {
                return matcher.group();
            }

        } catch (Throwable t) {
            info("[Constraints] 提取 Specification 失败: " + t.getMessage());
        }

        return null;
    }

    private String extractValueFromSpecification(Object specObj) {
        if (specObj == null) return null;

        try {
            if (specObj instanceof String) {
                String str = (String) specObj;
                if (!str.isEmpty()) return str;
            }

            if (specObj instanceof Number) {
                return String.valueOf(specObj);
            }

            String className = specObj.getClass().getName();

            if (className.contains("DurationInterval")) {
                Object minVal = null;
                Object maxVal = null;

                try {
                    java.lang.reflect.Method getMinMethod = specObj.getClass().getMethod("getMin");
                    minVal = getMinMethod.invoke(specObj);
                } catch (Throwable ignored) {}

                try {
                    java.lang.reflect.Method getMaxMethod = specObj.getClass().getMethod("getMax");
                    maxVal = getMaxMethod.invoke(specObj);
                } catch (Throwable ignored) {}

                if (minVal == null) {
                    try {
                        java.lang.reflect.Field minField = specObj.getClass().getDeclaredField("min");
                        minField.setAccessible(true);
                        minVal = minField.get(specObj);
                    } catch (Throwable ignored) {}
                }
                if (maxVal == null) {
                    try {
                        java.lang.reflect.Field maxField = specObj.getClass().getDeclaredField("max");
                        maxField.setAccessible(true);
                        maxVal = maxField.get(specObj);
                    } catch (Throwable ignored) {}
                }

                String minStr = null;
                if (minVal != null) {
                    minStr = extractDurationValue(minVal);
                }

                String maxStr = null;
                if (maxVal != null) {
                    maxStr = extractDurationValue(maxVal);
                }

                if (minStr != null && maxStr != null) {
                    return "{" + minStr + ".." + maxStr + "}";
                } else if (minStr != null) {
                    return minStr;
                } else if (maxStr != null) {
                    return maxStr;
                }

                String str = specObj.toString();
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{[^}]*\\}");
                java.util.regex.Matcher matcher = pattern.matcher(str);
                if (matcher.find()) {
                    return matcher.group();
                }
            }

            if (className.contains("Duration")) {
                return extractDurationValue(specObj);
            }

            if (className.contains("TimeExpression")) {
                try {
                    java.lang.reflect.Method getExprMethod = specObj.getClass().getMethod("getExpr");
                    Object expr = getExprMethod.invoke(specObj);
                    if (expr != null) {
                        String result = extractValueFromSpecification(expr);
                        if (result != null && !result.isEmpty()) {
                            return result;
                        }
                    }
                } catch (Throwable ignored) {}

                try {
                    java.lang.reflect.Method getBodyMethod = specObj.getClass().getMethod("getBody");
                    Object body = getBodyMethod.invoke(specObj);
                    if (body instanceof Collection) {
                        Collection<?> bodyList = (Collection<?>) body;
                        if (!bodyList.isEmpty()) {
                            String result = extractValueFromSpecification(bodyList.iterator().next());
                            if (result != null && !result.isEmpty()) {
                                return result;
                            }
                        }
                    }
                } catch (Throwable ignored) {}

                try {
                    java.lang.reflect.Method getValueMethod = specObj.getClass().getMethod("getValue");
                    Object value = getValueMethod.invoke(specObj);
                    if (value != null) {
                        String result = extractValueFromSpecification(value);
                        if (result != null && !result.isEmpty()) {
                            return result;
                        }
                    }
                } catch (Throwable ignored) {}
            }

            if (className.contains("OpaqueExpression")) {
                try {
                    java.lang.reflect.Method getBodyMethod = specObj.getClass().getMethod("getBody");
                    Object body = getBodyMethod.invoke(specObj);
                    if (body instanceof Collection) {
                        Collection<?> bodyList = (Collection<?>) body;
                        if (!bodyList.isEmpty()) {
                            return String.valueOf(bodyList.iterator().next());
                        }
                    }
                } catch (Throwable ignored) {}
            }

            if (className.contains("Literal")) {
                try {
                    java.lang.reflect.Method getValueMethod = specObj.getClass().getMethod("getValue");
                    Object value = getValueMethod.invoke(specObj);
                    if (value != null) {
                        return String.valueOf(value);
                    }
                } catch (Throwable ignored) {}
            }

            try {
                java.lang.reflect.Field[] fields = specObj.getClass().getDeclaredFields();
                for (java.lang.reflect.Field field : fields) {
                    String fieldName = field.getName();
                    if (fieldName.equals("value") || fieldName.equals("body") ||
                            fieldName.equals("expr") || fieldName.equals("duration") ||
                            fieldName.equals("stringValue") || fieldName.equals("literal") ||
                            fieldName.equals("seconds") || fieldName.equals("minutes") ||
                            fieldName.equals("hours") || fieldName.equals("numericValue") ||
                            fieldName.equals("min") || fieldName.equals("max")) {
                        field.setAccessible(true);
                        Object val = field.get(specObj);
                        if (val != null) {
                            String valStr = extractValueFromSpecification(val);
                            if (valStr != null && !valStr.isEmpty() && !valStr.contains("@") && !valStr.contains("impl")) {
                                return valStr;
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {}

            String str = specObj.toString();
            if (str != null && !str.contains("@") && !str.contains("impl") && !str.contains("com.nomagic")) {
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[0-9]+\\s*[sS]|[0-9]+\\s*[mM][iI][nN]|[0-9]+\\s*[hH]");
                java.util.regex.Matcher matcher = pattern.matcher(str);
                if (matcher.find()) {
                    return matcher.group();
                }
                return str;
            }

        } catch (Throwable t) {
            info("[Constraints] 从 ValueSpecification 提取值失败: " + t.getMessage());
        }

        return null;
    }

    private String extractDurationValue(Object durationObj) {
        if (durationObj == null) return null;

        try {
            String className = durationObj.getClass().getName();

            try {
                java.lang.reflect.Method getNumericValueMethod = durationObj.getClass().getMethod("getNumericValue");
                Object numeric = getNumericValueMethod.invoke(durationObj);
                if (numeric != null) {
                    String numStr = String.valueOf(numeric);
                    try {
                        java.lang.reflect.Method getUnitMethod = durationObj.getClass().getMethod("getUnit");
                        Object unit = getUnitMethod.invoke(durationObj);
                        if (unit != null) {
                            String unitStr = String.valueOf(unit);
                            if (unitStr.contains("second") || unitStr.contains("s")) {
                                return numStr + "s";
                            } else if (unitStr.contains("minute") || unitStr.contains("min")) {
                                return numStr + "min";
                            } else if (unitStr.contains("hour") || unitStr.contains("h")) {
                                return numStr + "h";
                            }
                            return numStr + " " + unitStr;
                        }
                    } catch (Throwable ignored) {}
                    return numStr;
                }
            } catch (Throwable ignored) {}

            try {
                java.lang.reflect.Method getValueMethod = durationObj.getClass().getMethod("getValue");
                Object value = getValueMethod.invoke(durationObj);
                if (value != null) {
                    return extractValueFromSpecification(value);
                }
            } catch (Throwable ignored) {}

            try {
                java.lang.reflect.Method getSecondsMethod = durationObj.getClass().getMethod("getSeconds");
                Object seconds = getSecondsMethod.invoke(durationObj);
                if (seconds != null) {
                    return String.valueOf(seconds) + "s";
                }
            } catch (Throwable ignored) {}

            try {
                java.lang.reflect.Method getMinutesMethod = durationObj.getClass().getMethod("getMinutes");
                Object minutes = getMinutesMethod.invoke(durationObj);
                if (minutes != null) {
                    return String.valueOf(minutes) + "min";
                }
            } catch (Throwable ignored) {}

            try {
                java.lang.reflect.Method getExprMethod = durationObj.getClass().getMethod("getExpr");
                Object expr = getExprMethod.invoke(durationObj);
                if (expr != null) {
                    return extractValueFromSpecification(expr);
                }
            } catch (Throwable ignored) {}

            try {
                java.lang.reflect.Field[] fields = durationObj.getClass().getDeclaredFields();
                for (java.lang.reflect.Field field : fields) {
                    String fieldName = field.getName();
                    if (fieldName.equals("numericValue") || fieldName.equals("value") ||
                            fieldName.equals("seconds") || fieldName.equals("minutes") ||
                            fieldName.equals("hours") || fieldName.equals("expr")) {
                        field.setAccessible(true);
                        Object val = field.get(durationObj);
                        if (val != null) {
                            String valStr = extractValueFromSpecification(val);
                            if (valStr != null && !valStr.isEmpty() && !valStr.contains("@") && !valStr.contains("impl")) {
                                return valStr;
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {}

            String str = durationObj.toString();
            if (str != null) {
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[0-9]+\\s*[sS]|[0-9]+\\s*[mM][iI][nN]|[0-9]+\\s*[hH]");
                java.util.regex.Matcher matcher = pattern.matcher(str);
                if (matcher.find()) {
                    return matcher.group();
                }
                if (!str.contains("@") && !str.contains("impl")) {
                    return str;
                }
            }

        } catch (Throwable t) {
            info("[Constraints] 提取 Duration 值失败: " + t.getMessage());
        }

        return null;
    }

    private void sendAllNodeStats(WebSocket conn) {
        try {
            Map<String, Map<String, Object>> allStats = OperatorResourceManager.getAllNodeStats();
            for (Map.Entry<String, Map<String, Object>> entry : allStats.entrySet()) {
                JSONObject statsMsg = new JSONObject();
                statsMsg.put("event", "node_stats");
                statsMsg.put("nodeName", entry.getKey());
                for (Map.Entry<String, Object> stat : entry.getValue().entrySet()) {
                    statsMsg.put(stat.getKey(), stat.getValue());
                }
                if (conn != null && conn.isOpen()) {
                    conn.send(statsMsg.toString());
                }
            }
            info("[SimSync] 已发送所有节点状态，共 " + allStats.size() + " 个节点");
        } catch (Exception e) {
            err("[SimSync] 发送节点状态失败: " + e.getMessage());
        }
    }


    /**
     * 发送本次资源仿真的方案汇总。
     *
     * duration 的单位已经在 OperatorResourceManager 中统一为“天”；
     * dailyResources 是按自然仿真日统计的平均资源使用数量；
     * tasks 保存每个 ResourceTask 的仿真时间线与输入参数快照。
     */
    private void sendSimulationSummary(WebSocket conn) {
        try {
            JSONObject summaryMsg = buildSimulationSummaryMessage();
            if (conn != null && conn.isOpen()) {
                conn.send(summaryMsg.toString());
            }
            info("[SimSync] 已发送本次仿真方案汇总");
        } catch (Exception e) {
            err("[SimSync] 发送仿真方案汇总失败: " + e.getMessage());
        }
    }

    private JSONObject buildSimulationSummaryMessage() {
        Map<String, Object> summary = OperatorResourceManager.getSimulationSummary();
        JSONObject json = new JSONObject();
        json.put("event", "simulation_summary");
        for (Map.Entry<String, Object> entry : summary.entrySet()) {
            json.put(entry.getKey(), entry.getValue());
        }
        return json;
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        err("[SimSync] WebSocket 错误: " + ex.getMessage());
        pushEventLog("error", "[SimSync] WebSocket 错误: " + ex.getMessage());
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 节点变量获取功能
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private void sendNodeVariables(WebSocket conn, String nodeId, String nodeName) {
        try {
            if (currentSession == null || currentSession.isClosed()) {
                sendVariablesEmpty(conn, nodeId, "当前没有运行中的仿真会话");
                return;
            }
            if (nodeId == null || nodeId.trim().isEmpty()) {
                sendVariablesEmpty(conn, nodeId, "节点ID为空");
                return;
            }
            Object_ rootContext = SimulationManager.getRootContext(currentSession);
            if (rootContext == null) {
                sendVariablesEmpty(conn, nodeId, "无法获取运行时上下文");
                return;
            }
            StringBuilder varsJson = new StringBuilder();
            varsJson.append("[");
            ALH alh = new ALH(currentSession);
            int varCount = 0;
            List<StructuredValue> allValues = findAllStructuredValues(rootContext);
            info("[NodeVariables] 找到 " + allValues.size() + " 个运行时对象");
            for (StructuredValue sv : allValues) {
                try {
                    String typeName = getStructuredValueTypeName(sv);
                    Map<String, Object> features = extractFeatures(alh, sv);
                    if (!features.isEmpty()) {
                        for (Map.Entry<String, Object> entry : features.entrySet()) {
                            if (varCount > 0) varsJson.append(",");
                            varsJson.append("{");
                            varsJson.append("\"name\":\"").append(escapeJson(typeName + "." + entry.getKey())).append("\",");
                            varsJson.append("\"value\":\"").append(escapeJson(String.valueOf(entry.getValue()))).append("\"");
                            varsJson.append("}");
                            varCount++;
                        }
                    }
                } catch (Throwable t) {
                    info("[NodeVariables] 提取对象变量失败: " + t.getMessage());
                }
            }
            varsJson.append("]");
            String response = "{"
                    + "\"event\":\"node_variables\","
                    + "\"nodeId\":\"" + escapeJson(nodeId) + "\","
                    + "\"variables\":" + varsJson.toString() + ","
                    + "\"count\":" + varCount
                    + "}";
            conn.send(response);
            info("[NodeVariables] 已发送节点 " + nodeId + " 的 " + varCount + " 个变量");
        } catch (Throwable t) {
            err("[NodeVariables] 获取节点变量失败: " + t.getMessage());
            t.printStackTrace();
            sendVariablesEmpty(conn, nodeId, "获取变量失败: " + t.getMessage());
        }
    }

    private void sendVariablesEmpty(WebSocket conn, String nodeId, String reason) {
        String response = "{"
                + "\"event\":\"node_variables\","
                + "\"nodeId\":\"" + escapeJson(nodeId != null ? nodeId : "") + "\","
                + "\"variables\":[],"
                + "\"count\":0,"
                + "\"message\":\"" + escapeJson(reason) + "\""
                + "}";
        if (conn != null && conn.isOpen()) {
            conn.send(response);
        }
        info("[NodeVariables] " + reason);
    }

    private List<StructuredValue> findAllStructuredValues(Object obj) {
        List<StructuredValue> result = new ArrayList<>();
        findAllStructuredValuesRecursive(obj, result, 0, new java.util.HashSet<Integer>());
        return result;
    }

    private void findAllStructuredValuesRecursive(Object obj, List<StructuredValue> result, int depth, java.util.Set<Integer> visited) {
        if (obj == null || depth > 6) return;
        int identity = System.identityHashCode(obj);
        if (visited.contains(identity)) return;
        visited.add(identity);
        if (obj instanceof StructuredValue) {
            StructuredValue sv = (StructuredValue) obj;
            String className = sv.getClass().getName();
            if (!className.contains("ActivityExecution") && !className.contains("ExecutionImpl")) {
                result.add(sv);
            }
        }
        if (obj instanceof java.util.Collection) {
            for (Object item : (java.util.Collection<?>) obj) {
                findAllStructuredValuesRecursive(item, result, depth + 1, visited);
            }
        }
        if (obj.getClass().isArray()) {
            int len = java.lang.reflect.Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                Object item = java.lang.reflect.Array.get(obj, i);
                findAllStructuredValuesRecursive(item, result, depth + 1, visited);
            }
        }
    }

    private String getStructuredValueTypeName(StructuredValue sv) {
        if (sv == null) return "Unknown";
        try {
            java.lang.reflect.Method getTypesMethod = sv.getClass().getMethod("getTypes");
            Object types = getTypesMethod.invoke(sv);
            if (types instanceof java.util.Collection) {
                java.util.Collection<?> typeList = (java.util.Collection<?>) types;
                if (!typeList.isEmpty()) {
                    Object firstType = typeList.iterator().next();
                    String typeName = getNameByReflection(firstType);
                    if (typeName != null && !typeName.isEmpty()) return typeName;
                }
            }
        } catch (Throwable ignored) {}
        String text = String.valueOf(sv);
        if (text.contains("{")) {
            return text.substring(0, text.indexOf("{")).trim();
        }
        return sv.getClass().getSimpleName();
    }

    private Map<String, Object> extractFeatures(ALH alh, StructuredValue sv) {
        Map<String, Object> features = new LinkedHashMap<>();
        try {
            java.lang.reflect.Method getFeatureValuesMethod = sv.getClass().getMethod("getFeatureValues");
            Object featureValues = getFeatureValuesMethod.invoke(sv);
            if (featureValues instanceof java.util.Collection) {
                for (Object fv : (java.util.Collection<?>) featureValues) {
                    try {
                        String featureName = getFeatureName(fv);
                        Object value = getFeatureValue(fv);
                        if (featureName != null && !featureName.isEmpty()) {
                            String valueStr = formatValue(value);
                            if (valueStr != null && !valueStr.trim().isEmpty()) {
                                features.put(featureName, valueStr);
                            }
                        }
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable t) {
            info("[NodeVariables] 提取特征失败: " + t.getMessage());
        }
        return features;
    }

    private String getFeatureName(Object featureValue) {
        try {
            java.lang.reflect.Method getFeatureMethod = featureValue.getClass().getMethod("getFeature");
            Object feature = getFeatureMethod.invoke(featureValue);
            return getNameByReflection(feature);
        } catch (Throwable ignored) {}
        return null;
    }

    private Object getFeatureValue(Object featureValue) {
        try {
            java.lang.reflect.Method getValuesMethod = featureValue.getClass().getMethod("getValues");
            Object values = getValuesMethod.invoke(featureValue);
            if (values instanceof java.util.Collection) {
                java.util.Collection<?> valueList = (java.util.Collection<?>) values;
                if (!valueList.isEmpty()) {
                    return valueList.iterator().next();
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private String formatValue(Object value) {
        if (value == null) return "";
        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        try {
            if (value.getClass().getName().contains("StringValue")) {
                java.lang.reflect.Method getValueMethod = value.getClass().getMethod("getValue");
                Object result = getValueMethod.invoke(value);
                return String.valueOf(result);
            }
        } catch (Throwable ignored) {}
        try {
            if (value.getClass().getName().contains("IntegerValue")) {
                java.lang.reflect.Method getValueMethod = value.getClass().getMethod("getValue");
                Object result = getValueMethod.invoke(value);
                return String.valueOf(result);
            }
        } catch (Throwable ignored) {}
        try {
            if (value.getClass().getName().contains("RealValue") || value.getClass().getName().contains("UnlimitedNaturalValue")) {
                java.lang.reflect.Method getValueMethod = value.getClass().getMethod("getValue");
                Object result = getValueMethod.invoke(value);
                return String.valueOf(result);
            }
        } catch (Throwable ignored) {}
        String text = String.valueOf(value);
        if (text.length() > 100) {
            text = text.substring(0, 97) + "...";
        }
        return text;
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 子流程绑定功能
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private void sendActivitiesList(WebSocket conn) {
        try {
            Project project = Application.getInstance().getProject();
            if (project == null) {
                sendConnError(conn, "未打开项目，无法获取Activity列表");
                return;
            }
            List<Map<String, String>> activities = new ArrayList<>();
            collectActivitiesRecursive(project.getPrimaryModel(), activities);
            StringBuilder json = new StringBuilder();
            json.append("{\"event\":\"activities_list\",\"activities\":[");
            for (int i = 0; i < activities.size(); i++) {
                if (i > 0) json.append(",");
                Map<String, String> act = activities.get(i);
                json.append("{");
                json.append("\"id\":\"").append(escapeJson(act.get("id"))).append("\",");
                json.append("\"name\":\"").append(escapeJson(act.get("name"))).append("\"");
                json.append("}");
            }
            json.append("]}");
            conn.send(json.toString());
            info("[Subprocess] 已发送 " + activities.size() + " 个Activity");
        } catch (Throwable t) {
            err("[Subprocess] 获取Activity列表失败: " + t.getMessage());
            t.printStackTrace();
            sendConnError(conn, "获取Activity列表失败: " + t.getMessage());
        }
    }

    private void collectActivitiesRecursive(Element element, List<Map<String, String>> activities) {
        if (element == null) return;
        if (element instanceof Activity) {
            Activity activity = (Activity) element;
            String id = activity.getID();
            String name = activity.getName();
            if (name != null && !name.trim().isEmpty()) {
                Map<String, String> actInfo = new LinkedHashMap<>();
                actInfo.put("id", id);
                actInfo.put("name", name);
                activities.add(actInfo);
            }
        }
        try {
            Collection<Element> children = element.getOwnedElement();
            if (children != null) {
                for (Element child : children) {
                    collectActivitiesRecursive(child, activities);
                }
            }
        } catch (Throwable ignored) {}
    }

    private void bindSubprocessToNode(WebSocket conn, String nodeId, String activityId) {
        try {
            Project project = Application.getInstance().getProject();
            if (project == null) {
                sendBindSubprocessResult(conn, false, nodeId, null, "未打开项目");
                return;
            }
            Element nodeElement = findElementById(project, nodeId);
            if (nodeElement == null) {
                sendBindSubprocessResult(conn, false, nodeId, null, "未找到节点");
                return;
            }
            String nodeClassName = nodeElement.getClass().getName();
            boolean isCallBehaviorAction = nodeClassName.contains("CallBehaviorAction") || nodeClassName.endsWith("CallBehaviorActionImpl");
            if (!isCallBehaviorAction) {
                sendBindSubprocessResult(conn, false, nodeId, null, "节点不是CallBehaviorAction类型");
                return;
            }
            Element activityElement = findElementById(project, activityId);
            if (activityElement == null || !(activityElement instanceof Activity)) {
                sendBindSubprocessResult(conn, false, nodeId, null, "未找到Activity");
                return;
            }
            Activity activity = (Activity) activityElement;
            try {
                com.nomagic.magicdraw.openapi.uml.SessionManager.getInstance().createSession(project, "Bind Subprocess");
                try {
                    java.lang.reflect.Method setBehaviorMethod = null;
                    for (java.lang.reflect.Method method : nodeElement.getClass().getMethods()) {
                        if ("setBehavior".equals(method.getName()) && method.getParameterCount() == 1) {
                            setBehaviorMethod = method;
                            break;
                        }
                    }
                    if (setBehaviorMethod == null) {
                        throw new Exception("未找到setBehavior方法");
                    }
                    setBehaviorMethod.invoke(nodeElement, activity);
                    com.nomagic.magicdraw.openapi.uml.SessionManager.getInstance().closeSession(project);
                    sendBindSubprocessResult(conn, true, nodeId, activity.getName(), "绑定成功");
                } catch (Throwable t) {
                    com.nomagic.magicdraw.openapi.uml.SessionManager.getInstance().cancelSession(project);
                    throw t;
                }
            } catch (Throwable t) {
                sendBindSubprocessResult(conn, false, nodeId, null, "绑定失败: " + t.getMessage());
            }
        } catch (Throwable t) {
            sendBindSubprocessResult(conn, false, nodeId, null, "绑定异常");
        }
    }

    private void sendBindSubprocessResult(WebSocket conn, boolean success, String nodeId, String behaviorName, String message) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"event\":\"bind_subprocess_result\",");
        json.append("\"success\":").append(success).append(",");
        json.append("\"nodeId\":\"").append(escapeJson(nodeId != null ? nodeId : "")).append("\",");
        json.append("\"behaviorName\":\"").append(escapeJson(behaviorName != null ? behaviorName : "")).append("\",");
        json.append("\"message\":\"").append(escapeJson(message != null ? message : "")).append("\"");
        json.append("}");
        if (conn != null && conn.isOpen()) {
            conn.send(json.toString());
        }
        info("[Subprocess] " + message);
    }

    private Element findElementById(Project project, String elementId) {
        if (project == null || elementId == null || elementId.trim().isEmpty()) return null;
        try {
            Element element = (Element) project.getElementByID(elementId);
            if (element != null) return element;
        } catch (Throwable ignored) {}
        return findElementByIdRecursive(project.getPrimaryModel(), elementId);
    }

    private Element findElementByIdRecursive(Element element, String targetId) {
        if (element == null || targetId == null) return null;
        if (targetId.equals(element.getID())) return element;
        try {
            Collection<Element> children = element.getOwnedElement();
            if (children != null) {
                for (Element child : children) {
                    Element found = findElementByIdRecursive(child, targetId);
                    if (found != null) return found;
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private String getElementName(Element element) {
        if (element instanceof NamedElement) {
            return ((NamedElement) element).getName();
        }
        return element.getClass().getSimpleName();
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 事件日志推送
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private String getCurrentTimeText() {
        try {
            return new SimpleDateFormat("HH:mm:ss").format(new Date());
        } catch (Throwable ignored) {
            return "";
        }
    }

    private void pushEventLog(String level, String message) {
        String timeText = getCurrentTimeText();
        broadcast("{\"event\":\"log\","
                + "\"level\":\"" + escapeJson(level) + "\","
                + "\"time\":\"" + escapeJson(timeText) + "\","
                + "\"message\":\"" + escapeJson(message) + "\"}");
        recordReplayEvent("{"
                + "\"time\":" + replayTime() + ","
                + "\"event\":\"log\","
                + "\"level\":\"" + escapeJson(level) + "\","
                + "\"timeText\":\"" + escapeJson(timeText) + "\","
                + "\"message\":\"" + escapeJson(message) + "\""
                + "}");
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 仿真回放记录
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private synchronized void startReplayRecording(String diagramId, String diagramName, String startMode) {
        try {
            this.replayRecording = true;
            this.replayStartMillis = System.currentTimeMillis();
            this.currentReplayRunId = "run_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            this.replayDiagramId = diagramId == null ? "" : diagramId;
            this.replayDiagramName = diagramName == null ? "" : diagramName;
            this.replayStartMode = startMode == null ? "" : startMode;
            this.replayStartTimeText = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            this.replayDiagramSnapshotJson = buildDiagramSnapshotJson(diagramId);
            this.replayEventsJson = new StringBuilder("[");
            recordReplayEvent("{"
                    + "\"time\":0,"
                    + "\"event\":\"sim_start\","
                    + "\"mode\":\"" + escapeJson(this.replayStartMode) + "\""
                    + "}");
            info("[SimSync] 开始记录仿真回放: " + currentReplayRunId);
        } catch (Throwable t) {
            err("[SimSync] 初始化回放记录失败: " + t.getMessage());
        }
    }

    private long replayTime() {
        if (!replayRecording || replayStartMillis <= 0L) return 0L;
        return Math.max(0L, System.currentTimeMillis() - replayStartMillis);
    }

    private synchronized void recordReplayEvent(String eventJson) {
        if (!replayRecording || eventJson == null || eventJson.trim().isEmpty()) return;
        if (replayEventsJson == null) replayEventsJson = new StringBuilder("[");
        if (replayEventsJson.length() > 1) replayEventsJson.append(",");
        replayEventsJson.append(eventJson);
    }

    private synchronized void finishReplayRecording(String status, String message) {
        if (!replayRecording) return;
        recordReplayEvent("{"
                + "\"time\":" + replayTime() + ","
                + "\"event\":\"sim_end\","
                + "\"status\":\"" + escapeJson(status == null ? "completed" : status) + "\","
                + "\"message\":\"" + escapeJson(message == null ? "" : message) + "\""
                + "}");
        replayEventsJson.append("]");
        String endTimeText = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String fullJson = "{"
                + "\"runId\":\"" + escapeJson(currentReplayRunId) + "\","
                + "\"diagramId\":\"" + escapeJson(replayDiagramId) + "\","
                + "\"diagramName\":\"" + escapeJson(replayDiagramName) + "\","
                + "\"startMode\":\"" + escapeJson(replayStartMode) + "\","
                + "\"startTime\":\"" + escapeJson(replayStartTimeText) + "\","
                + "\"endTime\":\"" + escapeJson(endTimeText) + "\","
                + "\"diagramSnapshot\":" + (replayDiagramSnapshotJson == null || replayDiagramSnapshotJson.trim().isEmpty() ? "{}" : replayDiagramSnapshotJson) + ","
                + "\"events\":" + replayEventsJson.toString()
                + "}";
        synchronized (replayLock) {
            latestReplayJsonCache = fullJson;
            latestReplayRunId = currentReplayRunId;
        }
        saveReplayFile(currentReplayRunId, fullJson);
        String savedRunId = currentReplayRunId;
        replayRecording = false;
        pushEventLog("info", "[SimSync] 回放记录已保存: " + savedRunId);
        broadcast("{\"event\":\"replay_saved\",\"runId\":\"" + escapeJson(savedRunId) + "\"}");
    }

    private String buildDiagramSnapshotJson(String diagramId) {
        try {
            if (diagramId == null || diagramId.trim().isEmpty()) return "{}";
            Project project = Application.getInstance().getProject();
            if (project == null) return "{}";
            DiagramPresentationElement dpe = findDiagramById(project, diagramId);
            if (dpe == null) return "{}";
            return exportDiagramToJson(dpe);
        } catch (Throwable t) {
            info("[SimSync] 生成回放图快照失败: " + t.getMessage());
            return "{}";
        }
    }

    private void saveReplayFile(String runId, String content) {
        try {
            java.io.File dir = getReplayDir();
            java.io.File runFile = new java.io.File(dir, runId + ".json");
            writeTextFile(runFile, content);
            java.io.File latestFile = getLatestReplayFile();
            writeTextFile(latestFile, content);
            info("[SimSync] 回放文件保存位置: " + runFile.getAbsolutePath());
            info("[SimSync] 最近一次回放文件保存位置: " + latestFile.getAbsolutePath());
        } catch (Throwable t) {
            err("[SimSync] 保存回放文件失败: " + t.getMessage());
            t.printStackTrace();
        }
    }

    private void writeTextFile(java.io.File file, String content) throws java.io.IOException {
        try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write(content == null ? "" : content);
        }
    }

    private void sendLatestReplay(WebSocket conn) {
        try {
            String content = null;
            synchronized (replayLock) {
                if (latestReplayJsonCache != null && !latestReplayJsonCache.trim().isEmpty()) {
                    content = latestReplayJsonCache;
                    info("[SimSync] 从内存缓存读取最近一次回放: " + latestReplayRunId);
                }
            }
            if (content == null || content.trim().isEmpty()) {
                java.io.File file = getLatestReplayFile();
                info("[SimSync] 尝试读取最近一次回放文件: " + file.getAbsolutePath());
                if (!file.exists()) {
                    sendConnError(conn, "还没有可用的仿真回放记录，请先运行一次仿真。查找路径: " + file.getAbsolutePath());
                    return;
                }
                content = readTextFile(file);
            }
            if (content == null || content.trim().isEmpty()) {
                sendConnError(conn, "最近一次仿真回放文件为空，无法回放");
                return;
            }
            if (conn != null && conn.isOpen()) {
                conn.send("{\"event\":\"replay_data\",\"record\":" + content + "}");
                info("[SimSync] 已发送最近一次仿真回放给前端");
            }
        } catch (Throwable t) {
            sendConnError(conn, "读取最近一次回放失败: " + t.getMessage());
            t.printStackTrace();
        }
    }

    private java.io.File getReplayDir() {
        java.io.File dir = new java.io.File("simulation-replays");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    private java.io.File getLatestReplayFile() {
        return new java.io.File(getReplayDir(), "latest_replay.json");
    }

    private String readTextFile(java.io.File file) throws java.io.IOException {
        byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 航天器外部交互功能
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    public synchronized String buildSpacecraftRequestJson(String nodeId, String nodeName, String elementClass) {
        String taskId = "T001";
        String spacecraftId = "SC001";
        double currentVoltage = 0.0;
        double currentPower = 0.0;
        double currentTemperature = 0.0;
        double healthThreshold = 0.0;
        try {
            if (currentSession != null && !currentSession.isClosed()) {
                Object_ root = SimulationManager.getRootContext(currentSession);
                if (root != null) {
                    StructuredValue inputObj = findStructuredValueInObject(root, "航天器健康评估输入");
                    if (inputObj != null) {
                        ALH alh = new ALH(currentSession);
                        taskId = valueToString(alh.getValue(inputObj, "任务编号"));
                        spacecraftId = valueToString(alh.getValue(inputObj, "航天器编号"));
                        currentVoltage = valueToDouble(alh.getValue(inputObj, "当前电压"), currentVoltage);
                        currentPower = valueToDouble(alh.getValue(inputObj, "当前功率"), currentPower);
                        currentTemperature = valueToDouble(alh.getValue(inputObj, "当前温度"), currentTemperature);
                        healthThreshold = valueToDouble(alh.getValue(inputObj, "健康阈值"), healthThreshold);
                    }
                }
            }
        } catch (Throwable t) {
            info("[ExternalInteraction] 构造航天器请求数据失败: " + t.getMessage());
        }
        return "{"
                + "\"event\":\"external_interaction_request\","
                + "\"type\":\"spacecraft_check_request\","
                + "\"nodeId\":\"" + escapeJson(nodeId) + "\","
                + "\"nodeName\":\"" + escapeJson(nodeName) + "\","
                + "\"elementClass\":\"" + escapeJson(elementClass) + "\","
                + "\"taskId\":\"" + escapeJson(taskId) + "\","
                + "\"spacecraftId\":\"" + escapeJson(spacecraftId) + "\","
                + "\"currentVoltage\":" + currentVoltage + ","
                + "\"currentPower\":" + currentPower + ","
                + "\"currentTemperature\":" + currentTemperature + ","
                + "\"healthThreshold\":" + healthThreshold + ","
                + "\"message\":\"MagicDraw仿真运行到发送航天器检查请求节点\""
                + "}";
    }

    public synchronized void saveSpacecraftResult(String json) {
        this.lastSpacecraftResultJson = json;
    }

    public synchronized String getLastSpacecraftResultJson() {
        return lastSpacecraftResultJson;
    }

    public synchronized void clearLastSpacecraftResultJson() {
        lastSpacecraftResultJson = null;
    }

    private void triggerSpacecraftCompleteSignal() {
        try {
            if (currentSession == null || currentSession.isClosed()) {
                pushFrontendError("[SimSync] 当前没有可用的仿真会话，无法触发航天器检查完成信号");
                return;
            }
            ALH alh = new ALH(currentSession);
            alh.sendSignal("航天器检查完成信号", "");
        } catch (Throwable t) {
            pushFrontendError("[SimSync] ❌ 自动触发完成信号失败: " + t.getMessage());
            t.printStackTrace();
        }
    }

    public synchronized boolean applySpacecraftResultToRuntimeValues(Collection<?> values) {
        if (lastSpacecraftResultJson == null || lastSpacecraftResultJson.trim().isEmpty()) {
            err("[ExternalInteraction] 没有缓存的航天器外部返回数据，无法写入运行时值");
            return false;
        }
        if (currentSession == null || currentSession.isClosed()) {
            err("[ExternalInteraction] 当前没有可用仿真会话，无法写入运行时值");
            return false;
        }
        if (values == null || values.isEmpty()) {
            err("[ExternalInteraction] 当前节点没有运行时 values，无法写入");
            return false;
        }
        double voltage = getDoubleFromJson(lastSpacecraftResultJson, "voltage", Double.NaN);
        double power = getDoubleFromJson(lastSpacecraftResultJson, "power", Double.NaN);
        double temperature = getDoubleFromJson(lastSpacecraftResultJson, "temperature", Double.NaN);
        String healthStatus = getStringFromJson(lastSpacecraftResultJson, "healthStatus");
        String spacecraftId = getStringFromJson(lastSpacecraftResultJson, "spacecraftId");
        ALH alh = new ALH(currentSession);
        int changed = 0;
        for (Object v : values) {
            StructuredValue sv = toStructuredValue(v);
            if (sv == null) continue;
            if (!isSpacecraftBusinessValue(sv)) continue;
            changed += trySetValue(alh, sv, "修改后电压", voltage);
            changed += trySetValue(alh, sv, "修改后功率", power);
            changed += trySetValue(alh, sv, "修改后温度", temperature);
            changed += trySetValue(alh, sv, "健康状态", healthStatus);
            changed += trySetValue(alh, sv, "当前电压", voltage);
            changed += trySetValue(alh, sv, "当前功率", power);
            changed += trySetValue(alh, sv, "当前温度", temperature);
            changed += trySetValue(alh, sv, "航天器编号", spacecraftId);
            changed += trySetValue(alh, sv, "结果说明", "外部返回健康状态：" + healthStatus);
        }
        info("[ExternalInteraction] 已尝试写入航天器外部返回值，成功写入字段数 = " + changed);
        return changed > 0;
    }

    public synchronized boolean applySpacecraftResultToRootContext() {
        if (lastSpacecraftResultJson == null || lastSpacecraftResultJson.trim().isEmpty()) {
            err("[ExternalInteraction] 没有缓存的航天器外部返回数据，无法从 root context 写入");
            return false;
        }
        if (currentSession == null || currentSession.isClosed()) {
            err("[ExternalInteraction] 当前没有可用仿真会话，无法从 root context 写入");
            return false;
        }
        Object_ root = SimulationManager.getRootContext(currentSession);
        if (root == null) {
            err("[ExternalInteraction] root context 为空，无法定位业务对象");
            return false;
        }
        double voltage = getDoubleFromJson(lastSpacecraftResultJson, "voltage", Double.NaN);
        double power = getDoubleFromJson(lastSpacecraftResultJson, "power", Double.NaN);
        double temperature = getDoubleFromJson(lastSpacecraftResultJson, "temperature", Double.NaN);
        String healthStatus = getStringFromJson(lastSpacecraftResultJson, "healthStatus");
        String spacecraftId = getStringFromJson(lastSpacecraftResultJson, "spacecraftId");
        ALH alh = new ALH(currentSession);
        int changed = 0;
        StructuredValue inputObj = findStructuredValueInObject(root, "航天器健康评估输入");
        if (inputObj != null) {
            changed += trySetValue(alh, inputObj, "当前电压", voltage);
            changed += trySetValue(alh, inputObj, "当前功率", power);
            changed += trySetValue(alh, inputObj, "当前温度", temperature);
        }
        StructuredValue resultObj = findStructuredValueInObject(root, "航天器健康评估结果");
        if (resultObj != null) {
            changed += trySetValue(alh, resultObj, "航天器编号", spacecraftId);
            changed += trySetValue(alh, resultObj, "健康状态", healthStatus);
            changed += trySetValue(alh, resultObj, "结果说明", "外部返回健康状态：" + healthStatus);
        }
        info("[ExternalInteraction] root context 写入完成，成功写入字段数 = " + changed);
        return changed > 0;
    }

    private int trySetValue(ALH alh, StructuredValue object, String featureName, Object value) {
        if (value == null) return 0;
        if (value instanceof Double && Double.isNaN((Double) value)) return 0;
        try {
            alh.setValue(object, featureName, value);
            info("[ExternalInteraction] 写入成功: " + featureName + " = " + value);
            return 1;
        } catch (Throwable t) {
            info("[ExternalInteraction] 跳过字段 " + featureName + ": " + t.getMessage());
            return 0;
        }
    }

    private boolean isSpacecraftBusinessValue(StructuredValue sv) {
        if (sv == null) return false;
        String text = String.valueOf(sv);
        String cls = sv.getClass().getName();
        return cls.contains("SignalInstance") || text.contains("航天器检查完成信号") || text.contains("航天器健康评估输入") || text.contains("航天器外部返回数据") || text.contains("航天器健康评估结果");
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // fUML 运行时对象工具方法
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private StructuredValue toStructuredValue(Object obj) {
        return toStructuredValue(obj, 0, new java.util.HashSet<Integer>());
    }

    private StructuredValue toStructuredValue(Object obj, int depth, java.util.Set<Integer> visited) {
        if (obj == null || depth > 6) return null;
        int identity = System.identityHashCode(obj);
        if (visited.contains(identity)) return null;
        visited.add(identity);
        if (obj instanceof StructuredValue) {
            String className = obj.getClass().getName();
            if (className.contains("ActivityExecution")) return null;
            return (StructuredValue) obj;
        }
        if (obj instanceof java.util.Collection) {
            for (Object item : (java.util.Collection<?>) obj) {
                StructuredValue sv = toStructuredValue(item, depth + 1, visited);
                if (sv != null) return sv;
            }
        }
        if (obj.getClass().isArray()) {
            int len = java.lang.reflect.Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                Object item = java.lang.reflect.Array.get(obj, i);
                StructuredValue sv = toStructuredValue(item, depth + 1, visited);
                if (sv != null) return sv;
            }
        }
        String[] commonFields = {"value", "object", "baseToken", "token", "target", "source", "referent"};
        for (String fieldName : commonFields) {
            Object fieldValue = readField(obj, fieldName);
            StructuredValue sv = toStructuredValue(fieldValue, depth + 1, visited);
            if (sv != null) return sv;
        }
        String[] commonMethods = {"getValue", "getObject", "getBaseToken", "getToken", "getTarget", "getSource", "getReferent"};
        for (String methodName : commonMethods) {
            Object methodValue = invokeNoArg(obj, methodName);
            StructuredValue sv = toStructuredValue(methodValue, depth + 1, visited);
            if (sv != null) return sv;
        }
        Class<?> cls = obj.getClass();
        while (cls != null) {
            java.lang.reflect.Field[] fields = cls.getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                try {
                    field.setAccessible(true);
                    Object fieldValue = field.get(obj);
                    if (fieldValue == null) continue;
                    if (isSimpleValue(fieldValue)) continue;
                    StructuredValue sv = toStructuredValue(fieldValue, depth + 1, visited);
                    if (sv != null) return sv;
                } catch (Throwable ignored) {}
            }
            cls = cls.getSuperclass();
        }
        return null;
    }

    private StructuredValue findStructuredValueInObject(Object obj, String keyword) {
        return findStructuredValueInObject(obj, keyword, 0, new java.util.HashSet<Integer>());
    }

    private StructuredValue findStructuredValueInObject(Object obj, String keyword, int depth, java.util.Set<Integer> visited) {
        if (obj == null || keyword == null || depth > 8) return null;
        int identity = System.identityHashCode(obj);
        if (visited.contains(identity)) return null;
        visited.add(identity);
        if (isSimpleValue(obj)) return null;
        if (obj instanceof StructuredValue) {
            StructuredValue sv = (StructuredValue) obj;
            String text = String.valueOf(sv);
            String header = text;
            int braceIndex = text.indexOf("{");
            if (braceIndex >= 0) header = text.substring(0, braceIndex);
            if (header.contains(keyword)) return sv;
        }
        if (obj instanceof java.util.Collection) {
            for (Object item : (java.util.Collection<?>) obj) {
                StructuredValue found = findStructuredValueInObject(item, keyword, depth + 1, visited);
                if (found != null) return found;
            }
        }
        if (obj.getClass().isArray()) {
            int len = java.lang.reflect.Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                Object item = java.lang.reflect.Array.get(obj, i);
                StructuredValue found = findStructuredValueInObject(item, keyword, depth + 1, visited);
                if (found != null) return found;
            }
        }
        Class<?> cls = obj.getClass();
        while (cls != null) {
            java.lang.reflect.Field[] fields = cls.getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                try {
                    field.setAccessible(true);
                    Object fieldValue = field.get(obj);
                    if (fieldValue == null || isSimpleValue(fieldValue)) continue;
                    StructuredValue found = findStructuredValueInObject(fieldValue, keyword, depth + 1, visited);
                    if (found != null) return found;
                } catch (Throwable ignored) {}
            }
            cls = cls.getSuperclass();
        }
        return null;
    }

    private boolean isSimpleValue(Object obj) {
        return obj instanceof String || obj instanceof Number || obj instanceof Boolean || obj instanceof Character || obj.getClass().isPrimitive() || obj.getClass().isEnum();
    }

    private Object readField(Object obj, String fieldName) {
        if (obj == null || fieldName == null) return null;
        Class<?> cls = obj.getClass();
        while (cls != null) {
            try {
                java.lang.reflect.Field field = cls.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (Throwable ignored) {
                cls = cls.getSuperclass();
            }
        }
        return null;
    }

    private Object invokeNoArg(Object obj, String methodName) {
        if (obj == null || methodName == null) return null;
        Class<?> cls = obj.getClass();
        while (cls != null) {
            try {
                java.lang.reflect.Method method = cls.getDeclaredMethod(methodName);
                method.setAccessible(true);
                return method.invoke(obj);
            } catch (Throwable ignored) {
                cls = cls.getSuperclass();
            }
        }
        return null;
    }

    private String valueToString(Object raw) {
        if (raw == null) return "";
        String text = String.valueOf(raw).trim();
        if (text.startsWith("[") && text.endsWith("]") && text.length() >= 2) {
            text = text.substring(1, text.length() - 1).trim();
        }
        return text;
    }

    private double valueToDouble(Object raw, double defaultValue) {
        String text = valueToString(raw);
        if (text == null || text.trim().isEmpty()) return defaultValue;
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // JSON 解析工具方法
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private boolean getBooleanFromJson(String json, String key, boolean defaultValue) {
        String raw = getRawJsonValue(json, key);
        if (raw == null || raw.trim().isEmpty()) return defaultValue;
        raw = raw.trim();
        return "true".equalsIgnoreCase(raw) || "1".equals(raw) || "yes".equalsIgnoreCase(raw);
    }

    private double getDoubleFromJson(String json, String key, double defaultValue) {
        String text = getRawJsonValue(json, key);
        if (text == null || text.trim().isEmpty()) return defaultValue;
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String getStringFromJson(String json, String key) {
        String text = getRawJsonValue(json, key);
        return text == null ? "" : text;
    }

    private String getRawJsonValue(String json, String key) {
        if (json == null || key == null) return null;
        String marker = "\"" + key + "\"";
        int keyIndex = json.indexOf(marker);
        if (keyIndex < 0) return null;
        int colonIndex = json.indexOf(":", keyIndex + marker.length());
        if (colonIndex < 0) return null;
        int start = colonIndex + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        if (start >= json.length()) return null;
        if (json.charAt(start) == '"') {
            int end = json.indexOf("\"", start + 1);
            if (end < 0) return null;
            return json.substring(start + 1, end);
        }
        int end = start;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (c == ',' || c == '}') break;
            end++;
        }
        return json.substring(start, end).trim();
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n");
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ★★★ 图列表和导出功能（含泳道支持）- 核心修改 ★★★
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private void sendDiagramList(WebSocket conn) {
        try {
            Project project = Application.getInstance().getProject();
            if (project == null) {
                sendConnError(conn, "未打开项目，请先在 MagicDraw 中打开项目");
                return;
            }

            long begin = System.currentTimeMillis();
            Collection<DiagramPresentationElement> diagrams = project.getDiagrams();

            /*
             * ★ 列表性能优化
             *
             * 旧逻辑：每遇到一个 Activity 都调用 resolveRunWithContextTarget()，
             * 当 Activity 本身没有直接 Context/Owner 时，会从 PrimaryModel 根节点
             * 递归扫描整个模型。几十个 Activity 就可能重复扫描几十遍大模型。
             *
             * 新逻辑：本次 list_diagrams 只扫描模型一次，建立：
             *      classifierBehavior Activity ID -> Context Classifier
             * 的索引。之后所有 Activity O(1) 查表。
             */
            Map<String, Element> contextByBehaviorId =
                    buildClassifierBehaviorIndex(project.getPrimaryModel());

            StringBuilder sb = new StringBuilder();
            sb.append("{\"event\":\"diagram_list\",\"diagrams\":[");

            int index = 0;
            int skippedNotPrimary = 0;
            int skippedByFilter = 0;

            for (DiagramPresentationElement dpe : diagrams) {
                if (dpe == null || dpe.getDiagram() == null) {
                    continue;
                }

                Element owner = dpe.getDiagram().getOwner();
                if (!isUnderPrimaryModel(project, owner)) {
                    skippedNotPrimary++;
                    continue;
                }

                String diagramId = dpe.getDiagram().getID();
                String diagramName = dpe.getName();
                String diagramTypeLabel = getDiagramTypeLabel(dpe, owner);
                String diagramTypeKey = normalizeDiagramTypeKey(diagramTypeLabel);

                if (!isBusinessDiagramWanted(
                        diagramName,
                        diagramTypeLabel,
                        owner != null ? owner.getClass().getSimpleName() : "")) {
                    skippedByFilter++;
                    continue;
                }

                boolean executable = owner instanceof Activity;
                String ownerName = "";
                if (owner instanceof NamedElement) {
                    ownerName = ((NamedElement) owner).getName();
                }

                boolean canRunWithContext = false;
                String contextName = "";

                if (owner instanceof Activity) {
                    Element runWithContextTarget =
                            resolveRunWithContextTargetFast(owner, contextByBehaviorId);

                    Object classifierBehavior = runWithContextTarget == null
                            ? null
                            : invokeNoArg(runWithContextTarget, "getClassifierBehavior");

                    canRunWithContext = runWithContextTarget != null
                            && isSameElement(classifierBehavior, (Activity) owner);

                    if (canRunWithContext) {
                        contextName = getElementDisplayName(runWithContextTarget);
                    }
                }

                if (index > 0) {
                    sb.append(",");
                }

                sb.append("{")
                        .append("\"id\":\"").append(escapeJson(diagramId)).append("\",")
                        .append("\"name\":\"").append(escapeJson(diagramName)).append("\",")
                        .append("\"diagramType\":\"").append(escapeJson(diagramTypeLabel)).append("\",")
                        .append("\"diagramTypeKey\":\"").append(escapeJson(diagramTypeKey)).append("\",")
                        .append("\"ownerName\":\"").append(escapeJson(ownerName)).append("\",")
                        .append("\"executable\":").append(executable).append(",")
                        .append("\"canRunWithContext\":").append(canRunWithContext).append(",")
                        .append("\"contextName\":\"").append(escapeJson(contextName)).append("\"")
                        .append("}");

                index++;
            }

            sb.append("]}");
            conn.send(sb.toString());

            long elapsed = System.currentTimeMillis() - begin;
            info("[SimSync] 已发送主项目图列表，共 " + index
                    + " 个，过滤非主项目图 " + skippedNotPrimary
                    + " 个，过滤非业务图 " + skippedByFilter
                    + " 个，Context索引 " + contextByBehaviorId.size()
                    + " 条，耗时 " + elapsed + " ms");

        } catch (Throwable t) {
            sendConnError(conn, "获取 MagicDraw 图列表失败: " + t.getMessage());
            t.printStackTrace();
        }
    }

    /**
     * 为流程列表一次性建立 classifierBehavior -> Context Classifier 索引。
     * 只遍历 PrimaryModel 一次，避免每个 Activity 都重新全模型递归。
     */
    private Map<String, Element> buildClassifierBehaviorIndex(Element root) {
        Map<String, Element> result = new LinkedHashMap<>();
        if (root == null) {
            return result;
        }

        collectClassifierBehaviorIndex(
                root,
                result,
                new java.util.HashSet<String>()
        );
        return result;
    }

    private void collectClassifierBehaviorIndex(
            Element element,
            Map<String, Element> result,
            java.util.Set<String> visited) {

        if (element == null) {
            return;
        }

        String elementId = element.getID();
        if (elementId != null && !visited.add(elementId)) {
            return;
        }

        // 只有真正存在 classifierBehavior 的元素才加入索引。
        Object classifierBehavior = invokeNoArg(element, "getClassifierBehavior");
        if (classifierBehavior instanceof Element
                && !(element instanceof Activity)
                && isContextClassifierLike(element)) {

            String behaviorId = ((Element) classifierBehavior).getID();
            if (behaviorId != null && !behaviorId.trim().isEmpty()) {
                // 同一 behavior 如果意外有多个 Context，保留第一次找到的，
                // 与旧递归逻辑“找到第一个就返回”保持一致。
                if (!result.containsKey(behaviorId)) {
                    result.put(behaviorId, element);
                }
            }
        }

        try {
            Collection<Element> children = element.getOwnedElement();
            if (children != null) {
                for (Element child : children) {
                    collectClassifierBehaviorIndex(child, result, visited);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * 仅用于流程列表的快速 Context 解析。
     * 不做任何全模型递归：
     * 1. Activity.getContext()；
     * 2. Activity.owner；
     * 3. 本轮 list_diagrams 预先建立的 behavior 索引。
     *
     * 真正执行 Run with Context 时仍保留原 resolveRunWithContextTarget()，
     * 因此不会削弱仿真启动时的兼容兜底能力。
     */
    private Element resolveRunWithContextTargetFast(
            Element diagramOwner,
            Map<String, Element> contextByBehaviorId) {

        if (!(diagramOwner instanceof Activity)) {
            return isContextClassifierLike(diagramOwner) ? diagramOwner : null;
        }

        Object context = invokeNoArg(diagramOwner, "getContext");
        if (context instanceof Element) {
            Element contextElement = (Element) context;
            if (isContextClassifierLike(contextElement)) {
                return contextElement;
            }
        }

        Element owner = diagramOwner.getOwner();
        if (isContextClassifierLike(owner)) {
            return owner;
        }

        if (contextByBehaviorId != null) {
            String behaviorId = diagramOwner.getID();
            if (behaviorId != null && !behaviorId.trim().isEmpty()) {
                Element indexed = contextByBehaviorId.get(behaviorId);
                if (indexed != null) {
                    return indexed;
                }
            }
        }

        return null;
    }

    private boolean isUnderPrimaryModel(Project project, Element element) {
        if (project == null || element == null) return false;
        Element primaryModel = project.getPrimaryModel();
        Element current = element;
        while (current != null) {
            if (current == primaryModel) return true;
            current = current.getOwner();
        }
        return false;
    }

    private boolean isBusinessDiagramWanted(String diagramName, String diagramTypeLabel, String ownerName) {
        String name = diagramName == null ? "" : diagramName.trim();
        String lowerName = name.toLowerCase();
        String type = diagramTypeLabel == null ? "" : diagramTypeLabel.toLowerCase();
        String owner = ownerName == null ? "" : ownerName.toLowerCase();
        if (type.contains("state machine") || type.contains("状态机") || type.contains("状态图")) return true;
        if (lowerName.contains("pattern") || lowerName.contains("rollup") || lowerName.contains("commandline") ||
                lowerName.contains("commandprompt") || lowerName.contains("maccommand") || lowerName.contains("windowscommand") ||
                lowerName.contains("textfilereplace")) return false;
        if (owner.contains("pattern") || owner.contains("rollup") || owner.contains("commandline") ||
                owner.contains("commandprompt") || owner.contains("textfilereplace")) return false;
        if (type.contains("glossary") || type.contains("table") || type.contains("matrix") || type.contains("relation map")) return false;
        if ("Simulation Config".equals(name) || "UI".equals(name)) return true;
        if (name.matches(".*[\\u4e00-\\u9fa5].*")) return true;
        if (type.contains("activity") || type.contains("state")) return true;
        return false;
    }

    private String getDiagramTypeLabel(DiagramPresentationElement dpe, Element owner) {
        if (owner instanceof Activity) return "Activity Diagram";
        String ownerClass = owner == null ? "" : owner.getClass().getSimpleName();
        if (ownerClass.contains("StateMachine")) return "State Machine Diagram";
        if (ownerClass.contains("Interaction")) return "Sequence Diagram";
        Object diagramType = null;
        try {
            diagramType = dpe.getDiagramType();
        } catch (Throwable ignored) {}
        String fromDiagramType = readDiagramTypeByReflection(diagramType);
        if (fromDiagramType != null && !fromDiagramType.trim().isEmpty()) return fromDiagramType;
        String name = dpe.getName() == null ? "" : dpe.getName().toLowerCase();
        if (name.contains("bdd") || name.contains("block definition")) return "Block Definition Diagram";
        if (name.contains("ibd") || name.contains("internal block")) return "Internal Block Diagram";
        if (name.contains("parametric") || name.contains("par")) return "Parametric Diagram";
        if (name.contains("requirement") || name.contains("req")) return "Requirement Diagram";
        if (ownerClass.contains("Package")) return "Package / Structure Diagram";
        return "Other Diagram";
    }

    private String readDiagramTypeByReflection(Object diagramType) {
        if (diagramType == null) return "";
        String[] methodNames = {"getType", "getName", "getHumanName", "getDisplayName", "getPresentationName"};
        for (String methodName : methodNames) {
            try {
                java.lang.reflect.Method m = diagramType.getClass().getMethod(methodName);
                Object value = m.invoke(diagramType);
                if (value != null) {
                    String text = String.valueOf(value).trim();
                    if (!text.isEmpty() && !text.contains("@")) return text;
                }
            } catch (Throwable ignored) {}
        }
        String text = String.valueOf(diagramType);
        if (text != null && !text.contains("@")) return text;
        return "";
    }

    private String normalizeDiagramTypeKey(String label) {
        if (label == null) return "other";
        String s = label.toLowerCase();
        if (s.contains("activity")) return "activity";
        if (s.contains("state")) return "state";
        if (s.contains("sequence") || s.contains("interaction")) return "sequence";
        if (s.contains("block definition") || s.contains("bdd")) return "bdd";
        if (s.contains("internal block") || s.contains("ibd")) return "ibd";
        if (s.contains("parametric")) return "parametric";
        if (s.contains("requirement")) return "requirement";
        if (s.contains("package")) return "package";
        if (s.contains("use case")) return "usecase";
        if (s.contains("class")) return "class";
        return "other";
    }

    private void sendDiagramJson(WebSocket conn, String diagramId) {
        try {
            Project project = Application.getInstance().getProject();
            if (project == null) {
                sendConnError(conn, "未打开项目，请先在 MagicDraw 中打开项目");
                return;
            }
            if (diagramId == null || diagramId.trim().isEmpty()) {
                sendConnError(conn, "前端没有传入 diagramId，无法加载图");
                return;
            }
            DiagramPresentationElement target = findDiagramById(project, diagramId);
            if (target == null) {
                sendConnError(conn, "未找到指定图: " + diagramId);
                return;
            }
            try {
                target.open();
                info("[SimSync] 已打开图用于导出: " + target.getName());
            } catch (Throwable t) {
                info("[SimSync] 打开图失败，但继续尝试导出: " + t.getMessage());
            }
            String diagramJson = exportDiagramToJson(target);
            String msg = "{"
                    + "\"event\":\"diagram_json\","
                    + "\"diagramId\":\"" + escapeJson(diagramId) + "\","
                    + "\"name\":\"" + escapeJson(target.getName()) + "\","
                    + "\"data\":" + diagramJson
                    + "}";
            conn.send(msg);
            info("[SimSync] 已发送图 JSON: " + target.getName());
        } catch (Throwable t) {
            sendConnError(conn, "导出图 JSON 失败: " + t.getMessage());
            t.printStackTrace();
        }
    }

    private DiagramPresentationElement findDiagramById(Project project, String diagramId) {
        if (project == null || diagramId == null) return null;
        Collection<DiagramPresentationElement> diagrams = project.getDiagrams();
        for (DiagramPresentationElement dpe : diagrams) {
            if (dpe == null || dpe.getDiagram() == null) continue;
            String id = dpe.getDiagram().getID();
            if (diagramId.equals(id)) return dpe;
        }
        return null;
    }

    /**
     * ★★★ 核心：导出图 JSON，泳道名称使用 Represents 对应的 Block 名称 ★★★
     */
    private String exportDiagramToJson(DiagramPresentationElement diagram) {
        List<PresentationElement> pes = collectAllPresentationElements(diagram);
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();

        for (PresentationElement pe : pes) {
            Element modelElement = pe.getElement();
            String pv = pe.getClass().getSimpleName();

            // 跳过所有构造型、标签、文本等辅助元素
            if (pv.contains("Stereotype") || pv.contains("Label") || pv.contains("Text") ||
                    pv.contains("Header") || pv.contains("Compartment") ||
                    pv.equals("NoteView") || pv.contains("Comment") ||
                    pv.contains("StereotypeView") || pv.contains("StereotypeLabel") ||
                    pv.contains("StereotypeCompartment") || pv.equals("StereotypeShapeElement")) {
                continue;
            }

            if (modelElement instanceof Stereotype) {
                continue;
            }

            String elementId = (modelElement != null) ? safe(modelElement.getID()) : null;
            String elementType = (modelElement != null) ? modelElement.getClass().getSimpleName() : pe.getClass().getSimpleName();

            // ★★★★★ SwimlaneView 使用 MagicDraw 官方映射直接导出泳道 ★★★★★
            //
            // SwimlaneView 自身的 bounds 是整个泳道框架的 bounds，不能把它当成某一条泳道。
            // 官方 API 已经维护了：ActivityPartition -> SwimlaneHeaderView / SwimlaneCellView 的映射。
            // 因此这里直接从 SwimlaneView.getPartitionToHeaderMap() 读取每一条真正的泳道，
            // 再用对应 Header + Cell 的 bounds 计算该泳道矩形。
            if (pe instanceof SwimlaneView) {
                appendSwimlanesFromView((SwimlaneView) pe, nodes);
                continue;
            }

            // SwimlaneCellView 是 SwimlaneView 内部的单元格，不再作为普通 Shape 再导出，
            // 否则会与上面按官方映射生成的泳道重复。
            if (pe instanceof SwimlaneCellView) {
                continue;
            }

            // ★★★ 处理普通形状节点 ★★★
            if (pe instanceof ShapeElement) {
                ShapeElement se = (ShapeElement) pe;
                Rectangle r = se.getBounds();
                if (r == null || r.width <= 0 || r.height <= 0) continue;

                // ---------- ActivityPartition 泳道检测 ----------
                //
                // 关键说明：Cameo/MagicDraw 的泳道 Presentation 结构中，外层容器和
                // 单个泳道矩形都可能“看到”同一组 ActivityPartition 子 Presentation。
                // 因此不能再使用“子元素里只要发现多个 Partition 就直接判为外层容器”
                // 这种办法。
                //
                // 新逻辑：
                // 1. Shape 自己直接绑定 ActivityPartition -> 直接使用；
                // 2. Shape 下有多个候选 ActivityPartition -> 根据每个 Partition.getNode()
                //    对应节点的图形中心是否落在当前 Shape 的矩形内来唯一匹配；
                // 3. 如果当前 Shape 同时包含多个 Partition 的节点 -> 它才是真正的外层容器，跳过；
                // 4. 不再把“无法唯一解析的泳道 Shape”导出成“未命名泳道”。
                ActivityPartition partition = resolveActivityPartitionForShape(se, pes);

                boolean isSwimlane = partition != null;
                String orientation = "horizontal";
                String represents = null;

                String peClassName = pe.getClass().getSimpleName();
                boolean looksLikeSwimlane = peClassName.contains("Swimlane")
                        || peClassName.contains("Partition");

                // 如果这是明显的泳道/Partition Presentation，但没有唯一解析出模型 Partition，
                // 说明它通常是外层容器、标题区或共享框架。直接跳过，避免生成“未命名泳道”。
                if (!isSwimlane && looksLikeSwimlane) {
                    int candidateCount = countActivityPartitionCandidates(se);
                    if (candidateCount > 0) {
                        info("[Swimlane] 跳过未能唯一绑定 ActivityPartition 的 Presentation: "
                                + peClassName
                                + "，候选 Partition 数 = " + candidateCount
                                + "，bounds=" + r);
                        continue;
                    }
                }

                if (isSwimlane) {
                    // 如果已经找到真正的 ActivityPartition，后续全部以它为准。
                    if (partition != null) {
                        modelElement = partition;
                        elementId = safe(partition.getID());
                        elementType = partition.getClass().getSimpleName();

                        // 官方 ActivityPartition 层级关系使用 getSubpartition()。
                        // 有子 Partition 的外层容器不导出，避免网页多画一个大泳道。
                        try {
                            Collection<ActivityPartition> subPartitions = partition.getSubpartition();
                            if (subPartitions != null && !subPartitions.isEmpty()) {
                                info("[Swimlane] 跳过父容器泳道: "
                                        + safe(partition.getName())
                                        + "，子泳道数量 = " + subPartitions.size());
                                continue;
                            }
                        } catch (Throwable t) {
                            info("[Swimlane] 读取 subpartition 失败: " + t.getMessage());
                        }
                    }

                    orientation = isVerticalSwimlane(se) ? "vertical" : "horizontal";

                    // 第一优先级：ActivityPartition.getRepresents() 对应元素/Block 名称。
                    if (partition != null) {
                        represents = getSwimlaneRepresents(se, partition);
                    }

                    // 最终显示名称：
                    // 1. Represents 对应 Block/结构名称
                    // 2. ActivityPartition 自身 Name
                    // 3. Presentation 标题
                    // 4. “未命名泳道”
                    String nodeName = represents;

                    if ((nodeName == null || nodeName.trim().isEmpty()) && partition != null) {
                        String partitionName = partition.getName();
                        if (partitionName != null && !partitionName.trim().isEmpty()) {
                            nodeName = partitionName.trim();
                        }
                    }

                    if (nodeName == null || nodeName.trim().isEmpty()) {
                        nodeName = getSwimlaneTitle(se);
                    }

                    if (nodeName == null || nodeName.trim().isEmpty()
                            || "null".equalsIgnoreCase(nodeName.trim())) {
                        nodeName = "未命名泳道";
                    }

                    info("[Swimlane] ================================");
                    info("[Swimlane] Partition Name = "
                            + (partition != null ? safe(partition.getName()) : "<未解析到ActivityPartition>"));
                    info("[Swimlane] Represents = " + safe(represents));
                    info("[Swimlane] 最终显示名称 = " + nodeName);
                    info("[Swimlane] 方向 = " + orientation);
                    info("[Swimlane] ================================");

                    Map<String, Object> n = new LinkedHashMap<>();
                    n.put("id", elementId != null && !elementId.isEmpty()
                            ? elementId
                            : "swimlane_" + System.identityHashCode(se));
                    n.put("type", partition != null
                            ? "ActivityPartition"
                            : se.getClass().getSimpleName());
                    n.put("presentationType", se.getClass().getSimpleName());
                    n.put("name", nodeName);

                    if (partition != null) {
                        n.put("partitionName", safe(partition.getName()));
                    }

                    if (represents != null && !represents.trim().isEmpty()) {
                        n.put("represents", represents);
                    }

                    n.put("x", r.x);
                    n.put("y", r.y);
                    n.put("w", r.width);
                    n.put("h", r.height);
                    n.put("isSwimlane", true);
                    n.put("orientation", orientation);

                    nodes.add(n);
                    continue;
                }

                // ---------- 普通活动节点 ----------
                String nodeName = "";
                if (modelElement instanceof NamedElement) {
                    nodeName = safe(((NamedElement) modelElement).getName());
                }

                // 检查是否是 CallBehaviorAction，过滤空标签
                boolean isCallBehavior = elementType != null &&
                        (elementType.contains("CallBehaviorAction") || elementType.endsWith("CallBehaviorActionImpl"));
                boolean skipNode = false;
                String behaviorName = null;
                if (isCallBehavior) {
                    boolean hasBehaviorName = false;
                    try {
                        java.lang.reflect.Method getBehaviorMethod = modelElement.getClass().getMethod("getBehavior");
                        Object behavior = getBehaviorMethod.invoke(modelElement);
                        if (behavior instanceof Activity) {
                            Activity activity = (Activity) behavior;
                            behaviorName = activity.getName();
                            if (behaviorName != null && !behaviorName.trim().isEmpty()) {
                                hasBehaviorName = true;
                            }
                        }
                    } catch (Throwable ignored) {}
                    boolean hasValidName = nodeName != null && !nodeName.trim().isEmpty() && !"action".equals(nodeName);
                    if (!hasValidName && !hasBehaviorName) {
                        skipNode = true;
                    }
                }
                if (skipNode) continue;

                Map<String, Object> n = new LinkedHashMap<>();
                n.put("id", elementId);
                n.put("type", elementType);
                n.put("presentationType", se.getClass().getSimpleName());
                n.put("name", nodeName);
                n.put("x", r.x);
                n.put("y", r.y);
                n.put("w", r.width);
                n.put("h", r.height);
                if (isCallBehavior && behaviorName != null && !behaviorName.trim().isEmpty()) {
                    n.put("behaviorName", behaviorName);
                }
                if ("OutputPinImpl".equals(elementType) || "InputPinImpl".equals(elementType)) {
                    Collection<PresentationElement> children = se.getPresentationElements();
                    if (children != null) {
                        for (PresentationElement child : children) {
                            if (child.getClass().getSimpleName().contains("Text") || child.getClass().getSimpleName().contains("Label")) {
                                Rectangle lb = child.getBounds();
                                if (lb != null && lb.width > 0) {
                                    Map<String, Object> labelBounds = new LinkedHashMap<>();
                                    labelBounds.put("x", lb.x);
                                    labelBounds.put("y", lb.y);
                                    labelBounds.put("w", lb.width);
                                    labelBounds.put("h", lb.height);
                                    n.put("labelBounds", labelBounds);
                                    break;
                                }
                            }
                        }
                    }
                }
                nodes.add(n);
            } else if (pe instanceof PathElement) {
                // 处理连线
                PathElement path = (PathElement) pe;
                List<Point> pts = path.getAllBreakPoints();

                // ★★★ 确保有有效的点 ★★★
                if (pts == null || pts.size() < 2) continue;

                // ★★★ 如果 modelElement 为 null，尝试从 pe 获取 ID ★★★
                String edgeId = elementId;
                if (edgeId == null || edgeId.trim().isEmpty()) {
                    // 使用 pe 的 ID 或其他唯一标识
                    try {
                        edgeId = path.getID();
                    } catch (Throwable ignored) {}
                    if (edgeId == null || edgeId.trim().isEmpty()) {
                        edgeId = "edge_" + System.identityHashCode(path);
                    }
                }

                // ★★★ 获取连线类型 ★★★
                String edgeType = elementType;
                if (edgeType == null || edgeType.trim().isEmpty()) {
                    edgeType = path.getClass().getSimpleName();
                    if (edgeType == null || edgeType.trim().isEmpty()) {
                        edgeType = "Transition";
                    }
                }

                Map<String, Object> ed = new LinkedHashMap<>();
                ed.put("id", edgeId);
                ed.put("type", edgeType);
                ed.put("presentationType", path.getClass().getSimpleName());
                ed.put("points", pointsToList(pts));

                // ★★★ 尝试获取 sourceId 和 targetId ★★★
                // 对于状态机，Transition 的 source 和 target 是 State
                try {
                    // 方法1：通过 modelElement 获取
                    if (modelElement != null) {
                        // 尝试获取 source 和 target
                        try {
                            java.lang.reflect.Method getSourceMethod = modelElement.getClass().getMethod("getSource");
                            Object source = getSourceMethod.invoke(modelElement);
                            if (source instanceof Element) {
                                ed.put("sourceId", ((Element) source).getID());
                            }
                        } catch (Throwable ignored) {}

                        try {
                            java.lang.reflect.Method getTargetMethod = modelElement.getClass().getMethod("getTarget");
                            Object target = getTargetMethod.invoke(modelElement);
                            if (target instanceof Element) {
                                ed.put("targetId", ((Element) target).getID());
                            }
                        } catch (Throwable ignored) {}
                    }

                    // 方法2：如果 modelElement 为 null，尝试从 path 获取端点
                    if (modelElement == null || !ed.containsKey("sourceId") || !ed.containsKey("targetId")) {
                        // 尝试获取连接的形状元素
                        try {
                            java.lang.reflect.Method getSourceShapeMethod = path.getClass().getMethod("getSourceShape");
                            Object sourceShape = getSourceShapeMethod.invoke(path);
                            if (sourceShape instanceof PresentationElement) {
                                Element sourceElement = ((PresentationElement) sourceShape).getElement();
                                if (sourceElement != null) {
                                    ed.put("sourceId", sourceElement.getID());
                                }
                            }
                        } catch (Throwable ignored) {}

                        try {
                            java.lang.reflect.Method getTargetShapeMethod = path.getClass().getMethod("getTargetShape");
                            Object targetShape = getTargetShapeMethod.invoke(path);
                            if (targetShape instanceof PresentationElement) {
                                Element targetElement = ((PresentationElement) targetShape).getElement();
                                if (targetElement != null) {
                                    ed.put("targetId", targetElement.getID());
                                }
                            }
                        } catch (Throwable ignored) {}
                    }
                } catch (Throwable ignore) {}

                edges.add(ed);
            }
        }

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("diagram", new LinkedHashMap<String, Object>() {{
            put("name", safe(diagram.getName()));
            put("diagramType", String.valueOf(diagram.getDiagramType()));
        }});
        root.put("nodes", nodes);
        root.put("edges", edges);
        info("[SimSync] 导出图 JSON: " + safe(diagram.getName()) + "，nodes=" + nodes.size() + "，edges=" + edges.size());
        return toJson(root);
    }

    /**
     * ★★★ 判断泳道是否为垂直方向 ★★★
     */
    private boolean isVerticalSwimlane(ShapeElement se) {
        if (se == null) return false;

        // 方法1：通过类名判断
        String className = se.getClass().getSimpleName();
        if (className.contains("Vertical")) {
            return true;
        }

        // 方法2：通过 getOrientation 方法
        try {
            java.lang.reflect.Method getOrientationMethod = se.getClass().getMethod("getOrientation");
            Object orientation = getOrientationMethod.invoke(se);
            if (orientation != null) {
                String orientStr = orientation.toString().toLowerCase();
                if (orientStr.contains("vertical") || orientStr.contains("vert")) {
                    return true;
                }
            }
        } catch (Throwable ignored) {}

        // 方法3：通过宽高比判断（垂直泳道高度 > 宽度 * 1.3）
        Rectangle bounds = se.getBounds();
        if (bounds != null && bounds.height > bounds.width * 1.3) {
            return true;
        }

        // 方法4：检查子元素中是否有标题在顶部
        try {
            Collection<PresentationElement> children = se.getPresentationElements();
            if (children != null) {
                for (PresentationElement child : children) {
                    String childType = child.getClass().getSimpleName();
                    if (childType.contains("Text") || childType.contains("Label")) {
                        Rectangle childBounds = child.getBounds();
                        if (childBounds != null) {
                            double titleY = childBounds.y - bounds.y;
                            if (titleY < bounds.height * 0.3 && bounds.height > bounds.width * 1.2) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}

        return false;
    }

    /**
     * 使用 MagicDraw/Cameo SwimlaneView 官方维护的映射导出泳道。
     *
     * 关键点：
     * 1. SwimlaneView.getBounds() 是整个泳道框架，不是单条泳道；
     * 2. getPartitionToHeaderMap() 直接给出 ActivityPartition -> SwimlaneHeaderView；
     * 3. getHeaderToCellsMap() / getCellsForHeader() 给出该 Partition 对应的泳道主体 Cell；
     * 4. Header 与 Cell 的 bounds 合并后，才是前端应该绘制的单条泳道矩形。
     */
    private void appendSwimlanesFromView(
            SwimlaneView swimlaneView,
            List<Map<String, Object>> nodes) {

        if (swimlaneView == null || nodes == null) {
            return;
        }

        try {
            // 某些刚加载的图中映射尚未初始化。官方提供 initializeByChildren() 用于按子图元重建这些映射。
            Map<ActivityPartition, SwimlaneHeaderView> partitionToHeader =
                    swimlaneView.getPartitionToHeaderMap();

            if (partitionToHeader == null || partitionToHeader.isEmpty()) {
                try {
                    swimlaneView.initializeByChildren(false);
                    partitionToHeader = swimlaneView.getPartitionToHeaderMap();
                    info("[SwimlaneMap] partitionToHeaderMap 为空，已执行 initializeByChildren(false)");
                } catch (Throwable t) {
                    info("[SwimlaneMap] initializeByChildren(false) 失败: " + t.getMessage());
                }
            }

            if (partitionToHeader == null || partitionToHeader.isEmpty()) {
                info("[SwimlaneMap] SwimlaneView 未提供 Partition -> Header 映射，bounds="
                        + swimlaneView.getBounds());
                return;
            }

            List<SwimlaneHeaderView> verticalHeaders = null;
            List<SwimlaneHeaderView> horizontalHeaders = null;

            try {
                verticalHeaders = swimlaneView.getVerticalSwimlanes();
            } catch (Throwable ignored) {
            }

            try {
                horizontalHeaders = swimlaneView.getHorizontalSwimlanes();
            } catch (Throwable ignored) {
            }

            Rectangle wholeBounds = swimlaneView.getBounds();

            info("[SwimlaneMap] ================================");
            info("[SwimlaneMap] SwimlaneView bounds = " + wholeBounds);
            info("[SwimlaneMap] Partition 映射数量 = " + partitionToHeader.size());

            for (Map.Entry<ActivityPartition, SwimlaneHeaderView> entry
                    : partitionToHeader.entrySet()) {

                ActivityPartition partition = entry.getKey();
                SwimlaneHeaderView header = entry.getValue();

                if (partition == null || header == null) {
                    continue;
                }

                // 只导出叶子泳道。外层父 Partition 仅用于分组，不在网页重复绘制。
                try {
                    Collection<ActivityPartition> children = partition.getSubpartition();
                    if (children != null && !children.isEmpty()) {
                        info("[SwimlaneMap] 跳过父 Partition: "
                                + safe(partition.getName())
                                + "，subpartition=" + children.size());
                        continue;
                    }
                } catch (Throwable t) {
                    info("[SwimlaneMap] 读取 subpartition 失败: "
                            + safe(partition.getName()) + "，" + t.getMessage());
                }

                Rectangle headerBounds = null;
                try {
                    headerBounds = header.getBounds();
                } catch (Throwable ignored) {
                }

                List<SwimlaneCellView> cells = null;
                try {
                    cells = swimlaneView.getCellsForHeader(header);
                } catch (Throwable t) {
                    info("[SwimlaneMap] getCellsForHeader 失败: "
                            + safe(partition.getName()) + "，" + t.getMessage());
                }

                // 保险：如果 getCellsForHeader 没拿到，再从官方 headerToCellsMap 读取。
                if (cells == null || cells.isEmpty()) {
                    try {
                        Map<ActivityPartition, List<SwimlaneCellView>> map =
                                swimlaneView.getHeaderToCellsMap();
                        if (map != null) {
                            cells = map.get(partition);
                        }
                    } catch (Throwable t) {
                        info("[SwimlaneMap] getHeaderToCellsMap 失败: "
                                + safe(partition.getName()) + "，" + t.getMessage());
                    }
                }

                Rectangle laneBounds = copyRectangle(headerBounds);

                if (cells != null) {
                    for (SwimlaneCellView cell : cells) {
                        if (cell == null) continue;

                        Rectangle cellBounds = null;
                        try {
                            cellBounds = cell.getBounds();
                        } catch (Throwable ignored) {
                        }

                        laneBounds = unionRectangle(laneBounds, cellBounds);
                    }
                }

                boolean vertical = verticalHeaders != null && verticalHeaders.contains(header);
                boolean horizontal = horizontalHeaders != null && horizontalHeaders.contains(header);

                // 部分版本/图状态下 Header 列表可能没有及时初始化，利用 header 与总框架的几何关系兜底。
                if (!vertical && !horizontal) {
                    vertical = inferVerticalLane(headerBounds, wholeBounds);
                    horizontal = !vertical;
                }

                // 如果只有 Header 没有 Cell，按总 SwimlaneView 范围补齐泳道主体。
                if (laneBounds == null && headerBounds != null) {
                    laneBounds = copyRectangle(headerBounds);
                }

                if (laneBounds != null && wholeBounds != null) {
                    if (vertical) {
                        laneBounds.y = Math.min(laneBounds.y, wholeBounds.y);
                        laneBounds.height = Math.max(
                                laneBounds.height,
                                wholeBounds.y + wholeBounds.height - laneBounds.y
                        );
                    } else {
                        laneBounds.x = Math.min(laneBounds.x, wholeBounds.x);
                        laneBounds.width = Math.max(
                                laneBounds.width,
                                wholeBounds.x + wholeBounds.width - laneBounds.x
                        );
                    }
                }

                if (laneBounds == null
                        || laneBounds.width <= 0
                        || laneBounds.height <= 0) {
                    info("[SwimlaneMap] 跳过 bounds 无效的 Partition: "
                            + safe(partition.getName()));
                    continue;
                }

                String represents = getSwimlaneRepresents(swimlaneView, partition);

                // 用户当前希望泳道标题显示 Represents 对应 Block 的 Name。
                // Represents 解析失败时，再回退 ActivityPartition 自身 Name。
                String displayName = represents;

                if (displayName == null || displayName.trim().isEmpty()) {
                    String partitionName = partition.getName();
                    if (partitionName != null && !partitionName.trim().isEmpty()) {
                        displayName = partitionName.trim();
                    }
                }

                if (displayName == null || displayName.trim().isEmpty()) {
                    displayName = "未命名泳道";
                }

                Map<String, Object> n = new LinkedHashMap<>();
                n.put("id", safe(partition.getID()));
                n.put("type", "ActivityPartition");
                n.put("presentationType", "SwimlaneView");
                n.put("name", displayName);
                n.put("partitionName", safe(partition.getName()));

                // 如果 Represents 与最终标题相同，不再给前端重复显示“↳ 同名”。
                if (represents != null
                        && !represents.trim().isEmpty()
                        && !represents.trim().equals(displayName)) {
                    n.put("represents", represents.trim());
                }

                n.put("x", laneBounds.x);
                n.put("y", laneBounds.y);
                n.put("w", laneBounds.width);
                n.put("h", laneBounds.height);
                n.put("isSwimlane", true);
                n.put("orientation", vertical ? "vertical" : "horizontal");

                nodes.add(n);

                info("[SwimlaneMap] Partition=" + safe(partition.getName())
                        + " / " + safe(partition.getID())
                        + "，Represents=" + safe(represents)
                        + "，显示名称=" + displayName
                        + "，方向=" + (vertical ? "vertical" : "horizontal")
                        + "，headerBounds=" + headerBounds
                        + "，cellCount=" + (cells != null ? cells.size() : 0)
                        + "，laneBounds=" + laneBounds);
            }

            info("[SwimlaneMap] ================================");

        } catch (Throwable t) {
            err("[SwimlaneMap] 使用官方 SwimlaneView 映射导出失败: " + t.getMessage());
            t.printStackTrace();
        }
    }

    private Rectangle copyRectangle(Rectangle r) {
        if (r == null) return null;
        return new Rectangle(r.x, r.y, r.width, r.height);
    }

    private Rectangle unionRectangle(Rectangle a, Rectangle b) {
        if (a == null) return copyRectangle(b);
        if (b == null) return copyRectangle(a);

        int x1 = Math.min(a.x, b.x);
        int y1 = Math.min(a.y, b.y);
        int x2 = Math.max(a.x + a.width, b.x + b.width);
        int y2 = Math.max(a.y + a.height, b.y + b.height);

        return new Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

    private boolean inferVerticalLane(Rectangle headerBounds, Rectangle wholeBounds) {
        if (headerBounds == null || wholeBounds == null) {
            return true;
        }

        // 竖向泳道（列）的 Header 通常横跨单列宽度、位于 SwimlaneView 顶部；
        // 横向泳道（行）的 Header 通常位于左侧，宽度较窄、高度接近单行高度。
        boolean nearTop = Math.abs(headerBounds.y - wholeBounds.y) <= 20;
        boolean narrowerThanWhole = headerBounds.width < wholeBounds.width * 0.90;
        boolean shorterThanWhole = headerBounds.height < wholeBounds.height * 0.90;

        if (nearTop && narrowerThanWhole) {
            return true;
        }

        if (shorterThanWhole && headerBounds.width >= wholeBounds.width * 0.50) {
            return false;
        }

        return headerBounds.width <= headerBounds.height;
    }

    /**
     * 解析当前 ShapeElement 真正对应的 ActivityPartition。
     *
     * Cameo 的泳道 Presentation 经常不是“一条泳道 Shape -> 一个 Partition Element”的
     * 简单结构。一个外层泳道框架、标题区、甚至单条泳道的 Presentation，都可能包含
     * 多个 ActivityPartition 的子 Presentation。因此仅统计子元素数量无法区分外层容器
     * 和真实的单条泳道。
     *
     * 本方法使用“模型关系 + 图形位置”联合判断：
     * 1. Shape 自己直接绑定 ActivityPartition：直接返回；
     * 2. 收集 Shape 下可见的 ActivityPartition 候选；
     * 3. 对每个候选 Partition 读取 partition.getNode()；
     * 4. 找到这些 ActivityNode 在当前 diagram 中的 Shape bounds；
     * 5. 如果只有一个 Partition 的节点中心落在当前泳道矩形内，则当前 Shape 就属于它；
     * 6. 如果多个 Partition 都有节点落入，当前 Shape 是外层容器/公共框架，不绑定任何一个。
     */
    private ActivityPartition resolveActivityPartitionForShape(
            ShapeElement shape,
            List<PresentationElement> allPresentationElements) {

        if (shape == null) {
            return null;
        }

        // 1. 最可靠：当前 Shape 直接绑定 ActivityPartition。
        try {
            Element direct = shape.getElement();
            if (direct instanceof ActivityPartition) {
                ActivityPartition partition = (ActivityPartition) direct;
                info("[Swimlane] Shape 直接对应 ActivityPartition: "
                        + safe(partition.getName())
                        + " / " + safe(partition.getID())
                        + "，bounds=" + shape.getBounds());
                return partition;
            }
        } catch (Throwable t) {
            info("[Swimlane] 读取 Shape.getElement() 失败: " + t.getMessage());
        }

        Map<String, ActivityPartition> candidates = collectActivityPartitionCandidates(shape);
        if (candidates.isEmpty()) {
            return null;
        }

        if (candidates.size() == 1) {
            ActivityPartition only = candidates.values().iterator().next();
            info("[Swimlane] 当前 Shape 只有一个 ActivityPartition 候选，直接绑定: "
                    + safe(only.getName())
                    + " / " + safe(only.getID())
                    + "，bounds=" + shape.getBounds());
            return only;
        }

        Rectangle laneBounds = null;
        try {
            laneBounds = shape.getBounds();
        } catch (Throwable ignored) {
        }

        if (laneBounds == null || laneBounds.width <= 0 || laneBounds.height <= 0) {
            info("[Swimlane] 当前 Shape 有 " + candidates.size()
                    + " 个 Partition 候选，但 bounds 无效，无法做几何匹配");
            return null;
        }

        Map<String, Integer> hitCountByPartition = new LinkedHashMap<>();
        Map<String, Integer> locatedNodeCountByPartition = new LinkedHashMap<>();

        ActivityPartition onlyHitPartition = null;
        int positivePartitionCount = 0;

        for (Map.Entry<String, ActivityPartition> entry : candidates.entrySet()) {
            ActivityPartition candidate = entry.getValue();
            int hitCount = 0;
            int locatedNodeCount = 0;

            try {
                Collection<ActivityNode> containedNodes = candidate.getNode();
                if (containedNodes != null) {
                    for (ActivityNode activityNode : containedNodes) {
                        if (activityNode == null) {
                            continue;
                        }

                        Rectangle nodeBounds = findShapeBoundsForModelElement(
                                activityNode,
                                allPresentationElements
                        );

                        if (nodeBounds == null) {
                            continue;
                        }

                        locatedNodeCount++;

                        int centerX = nodeBounds.x + nodeBounds.width / 2;
                        int centerY = nodeBounds.y + nodeBounds.height / 2;

                        if (containsPointWithTolerance(laneBounds, centerX, centerY, 4)) {
                            hitCount++;
                        }
                    }
                }
            } catch (Throwable t) {
                info("[Swimlane] 读取 Partition.getNode() 失败: "
                        + safe(candidate.getName())
                        + "，原因=" + t.getMessage());
            }

            hitCountByPartition.put(entry.getKey(), hitCount);
            locatedNodeCountByPartition.put(entry.getKey(), locatedNodeCount);

            info("[Swimlane] 几何匹配候选: Partition="
                    + safe(candidate.getName())
                    + " / " + safe(candidate.getID())
                    + "，可定位节点=" + locatedNodeCount
                    + "，落入当前 Shape 的节点=" + hitCount
                    + "，shapeBounds=" + laneBounds);

            if (hitCount > 0) {
                positivePartitionCount++;
                onlyHitPartition = candidate;
            }
        }

        // 只有一个 Partition 的节点进入当前矩形：可以唯一确定。
        if (positivePartitionCount == 1 && onlyHitPartition != null) {
            info("[Swimlane] 通过 containedNode + bounds 唯一匹配到 ActivityPartition: "
                    + safe(onlyHitPartition.getName())
                    + " / " + safe(onlyHitPartition.getID())
                    + "，bounds=" + laneBounds);
            return onlyHitPartition;
        }

        // 多个 Partition 都命中：这正是外层容器/公共框架的典型特征。
        if (positivePartitionCount > 1) {
            info("[Swimlane] 当前 Shape 同时包含 " + positivePartitionCount
                    + " 个 ActivityPartition 的节点，判定为外层容器/公共框架，不绑定某一个 Partition"
                    + "，bounds=" + laneBounds);
            return null;
        }

        // 没有节点命中时再做一次“中心最近”兜底。
        // 只有候选 Partition 至少有可定位节点，并且最近者明显优于第二名时才采用，
        // 防止空泳道或标题框被误绑定。
        ActivityPartition nearest = findNearestPartitionByContainedNodes(
                laneBounds,
                candidates,
                allPresentationElements
        );

        if (nearest != null) {
            info("[Swimlane] containedNode 未直接落入，使用最近中心兜底匹配: "
                    + safe(nearest.getName())
                    + " / " + safe(nearest.getID())
                    + "，bounds=" + laneBounds);
            return nearest;
        }

        info("[Swimlane] 当前 Shape 下有 " + candidates.size()
                + " 个 ActivityPartition，但无法唯一匹配，按容器/辅助框架跳过"
                + "，bounds=" + laneBounds);
        return null;
    }

    /**
     * 保留旧入口，供 getSwimlaneTitle 等兼容代码调用。
     * 此入口没有 diagram 全量 Presentation，因此只做“直接绑定/唯一候选”判断，
     * 不再错误地从多个候选中任选一个。
     */
    private ActivityPartition findActivityPartition(ShapeElement shape) {
        if (shape == null) {
            return null;
        }

        try {
            Element direct = shape.getElement();
            if (direct instanceof ActivityPartition) {
                return (ActivityPartition) direct;
            }
        } catch (Throwable ignored) {
        }

        Map<String, ActivityPartition> candidates = collectActivityPartitionCandidates(shape);
        if (candidates.size() == 1) {
            return candidates.values().iterator().next();
        }

        return null;
    }

    /**
     * 统计当前 Presentation 子树下出现的唯一 ActivityPartition 数量。
     */
    private int countActivityPartitionCandidates(ShapeElement shape) {
        return collectActivityPartitionCandidates(shape).size();
    }

    /**
     * 从当前 Shape 的 Presentation 子树中收集所有唯一 ActivityPartition。
     * 使用递归而不是只看直接子元素，因为不同 Cameo 图形版本的泳道层级深度不同。
     */
    private Map<String, ActivityPartition> collectActivityPartitionCandidates(ShapeElement shape) {
        Map<String, ActivityPartition> candidates = new LinkedHashMap<>();
        if (shape == null) {
            return candidates;
        }

        Deque<PresentationElement> stack = new ArrayDeque<>();
        stack.push(shape);
        Set<Integer> visitedPresentation = new java.util.HashSet<>();

        while (!stack.isEmpty()) {
            PresentationElement current = stack.pop();
            if (current == null) {
                continue;
            }

            int identity = System.identityHashCode(current);
            if (!visitedPresentation.add(identity)) {
                continue;
            }

            try {
                Element element = current.getElement();
                if (element instanceof ActivityPartition) {
                    ActivityPartition partition = (ActivityPartition) element;
                    String key = partition.getID();
                    if (key == null || key.trim().isEmpty()) {
                        key = "identity_" + System.identityHashCode(partition);
                    }
                    candidates.put(key, partition);
                }
            } catch (Throwable ignored) {
            }

            try {
                Collection<PresentationElement> children = current.getPresentationElements();
                if (children != null) {
                    for (PresentationElement child : children) {
                        if (child != null) {
                            stack.push(child);
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
        }

        return candidates;
    }

    /**
     * 找到某个模型元素在当前 diagram 中对应 ShapeElement 的 bounds。
     * 一个元素可能出现多个 Presentation，优先使用第一个有效图形。
     */
    private Rectangle findShapeBoundsForModelElement(
            Element modelElement,
            List<PresentationElement> allPresentationElements) {

        if (modelElement == null || allPresentationElements == null) {
            return null;
        }

        String targetId = modelElement.getID();

        for (PresentationElement pe : allPresentationElements) {
            if (!(pe instanceof ShapeElement)) {
                continue;
            }

            try {
                Element peElement = pe.getElement();
                if (peElement == null) {
                    continue;
                }

                boolean same = peElement == modelElement;
                if (!same && targetId != null && !targetId.trim().isEmpty()) {
                    same = targetId.equals(peElement.getID());
                }

                if (!same) {
                    continue;
                }

                Rectangle bounds = ((ShapeElement) pe).getBounds();
                if (bounds != null && bounds.width > 0 && bounds.height > 0) {
                    return bounds;
                }
            } catch (Throwable ignored) {
            }
        }

        return null;
    }

    private boolean containsPointWithTolerance(
            Rectangle rectangle,
            int x,
            int y,
            int tolerance) {

        if (rectangle == null) {
            return false;
        }

        int left = rectangle.x - tolerance;
        int top = rectangle.y - tolerance;
        int right = rectangle.x + rectangle.width + tolerance;
        int bottom = rectangle.y + rectangle.height + tolerance;

        return x >= left && x <= right && y >= top && y <= bottom;
    }

    /**
     * 当某个泳道暂时没有节点中心真正落入矩形时的保守兜底。
     *
     * 计算当前 Shape 中心与各 Partition 已定位节点“平均中心”的距离。
     * 只有最近候选明显优于第二名（<= 第二名的 65%）时才返回，避免外层框架误匹配。
     */
    private ActivityPartition findNearestPartitionByContainedNodes(
            Rectangle laneBounds,
            Map<String, ActivityPartition> candidates,
            List<PresentationElement> allPresentationElements) {

        if (laneBounds == null || candidates == null || candidates.isEmpty()) {
            return null;
        }

        double laneCenterX = laneBounds.getCenterX();
        double laneCenterY = laneBounds.getCenterY();

        ActivityPartition best = null;
        double bestDistance = Double.MAX_VALUE;
        double secondDistance = Double.MAX_VALUE;

        for (ActivityPartition candidate : candidates.values()) {
            if (candidate == null) {
                continue;
            }

            double sumX = 0.0;
            double sumY = 0.0;
            int count = 0;

            try {
                Collection<ActivityNode> nodes = candidate.getNode();
                if (nodes != null) {
                    for (ActivityNode node : nodes) {
                        Rectangle bounds = findShapeBoundsForModelElement(node, allPresentationElements);
                        if (bounds == null) {
                            continue;
                        }
                        sumX += bounds.getCenterX();
                        sumY += bounds.getCenterY();
                        count++;
                    }
                }
            } catch (Throwable ignored) {
            }

            if (count == 0) {
                continue;
            }

            double avgX = sumX / count;
            double avgY = sumY / count;
            double dx = avgX - laneCenterX;
            double dy = avgY - laneCenterY;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < bestDistance) {
                secondDistance = bestDistance;
                bestDistance = distance;
                best = candidate;
            } else if (distance < secondDistance) {
                secondDistance = distance;
            }
        }

        if (best == null) {
            return null;
        }

        // 只有一个候选拥有可定位节点。
        if (secondDistance == Double.MAX_VALUE) {
            return best;
        }

        // 最近者必须明显优于第二名，才能认为是唯一匹配。
        if (bestDistance <= secondDistance * 0.65) {
            return best;
        }

        return null;
    }

    /**
     * 获取 ActivityPartition 的 Represents 对应名称。
     *
     * 对 SysML Allocate Activity Partition 常见情况：
     * - Represents 直接指向 NamedElement/Block：返回它自己的 Name；
     * - Represents 指向 Property/Part Property：优先返回 Property.getType() 的名称，
     *   即真正对应的 Block 名称；如果 Type 没有名称，再返回 Property 自己的名称。
     */
    private String getSwimlaneRepresents(ShapeElement se, Element modelElement) {
        if (!(modelElement instanceof ActivityPartition)) {
            if (modelElement == null) {
                info("[Swimlane] getSwimlaneRepresents: modelElement = null");
            } else {
                info("[Swimlane] getSwimlaneRepresents: 当前元素不是 ActivityPartition: "
                        + modelElement.getClass().getName());
            }
            return null;
        }

        ActivityPartition partition = (ActivityPartition) modelElement;

        info("[Swimlane] ActivityPartition Name = " + safe(partition.getName()));
        info("[Swimlane] ActivityPartition ID = " + safe(partition.getID()));

        Element representsElement;
        try {
            representsElement = partition.getRepresents();
        } catch (Throwable t) {
            info("[Swimlane] ActivityPartition.getRepresents() 调用失败: " + t.getMessage());
            return null;
        }

        if (representsElement == null) {
            info("[Swimlane] " + safe(partition.getName()) + " 的 Represents = null");
            return null;
        }

        info("[Swimlane] Represents 实际类型 = " + representsElement.getClass().getName());
        info("[Swimlane] Represents ID = " + safe(representsElement.getID()));

        // Represents 是 Property / Part Property 时：
        // 页面希望显示它所代表结构的 Block 名称，因此优先取 Property.getType().getName()。
        if (representsElement instanceof Property) {
            Property property = (Property) representsElement;

            info("[Swimlane] Represents 是 Property，Property Name = "
                    + safe(property.getName()));

            try {
                Object type = property.getType();
                if (type instanceof NamedElement) {
                    String typeName = ((NamedElement) type).getName();
                    if (typeName != null && !typeName.trim().isEmpty()) {
                        info("[Swimlane] Represents Property Type / Block Name = " + typeName);
                        return typeName.trim();
                    }
                }
            } catch (Throwable t) {
                info("[Swimlane] 读取 Represents Property.getType() 失败: " + t.getMessage());
            }

            String propertyName = property.getName();
            if (propertyName != null && !propertyName.trim().isEmpty()) {
                info("[Swimlane] Property Type 无可用名称，回退 Property Name = " + propertyName);
                return propertyName.trim();
            }
        }

        // Represents 本身就是 Block/Class/其他 NamedElement。
        if (representsElement instanceof NamedElement) {
            String name = ((NamedElement) representsElement).getName();
            if (name != null && !name.trim().isEmpty()) {
                info("[Swimlane] Represents NamedElement Name = " + name);
                return name.trim();
            }
        }

        // 最后兜底：humanName。
        try {
            String humanName = representsElement.getHumanName();
            if (humanName != null && !humanName.trim().isEmpty()) {
                info("[Swimlane] Represents humanName = " + humanName);
                return humanName.trim();
            }
        } catch (Throwable t) {
            info("[Swimlane] 读取 Represents humanName 失败: " + t.getMessage());
        }

        info("[Swimlane] Represents 存在，但没有取得可显示名称");
        return null;
    }

    /**
     * ★★★ 获取泳道的显示文本（标题）★★★
     */
    private String getSwimlaneTitle(ShapeElement se) {
        if (se == null) return "未命名泳道";

        // 方法1：先解析真正的 ActivityPartition。
        ActivityPartition partition = findActivityPartition(se);
        if (partition != null) {
            String representsName = getSwimlaneRepresents(se, partition);
            if (representsName != null
                    && !representsName.trim().isEmpty()
                    && !"null".equalsIgnoreCase(representsName.trim())) {
                info("[SwimlaneTitle] 从 Represents 获取名称: " + representsName);
                return representsName.trim();
            }

            String partitionName = partition.getName();
            if (partitionName != null && !partitionName.trim().isEmpty()) {
                info("[SwimlaneTitle] 从 ActivityPartition Name 获取: " + partitionName);
                return partitionName.trim();
            }
        }

        // ★★★ 方法2：从子元素中查找文本（原来的逻辑）★★★
        try {
            Collection<PresentationElement> children = se.getPresentationElements();
            if (children != null) {
                for (PresentationElement child : children) {
                    String childType = child.getClass().getSimpleName();
                    if (childType.contains("Text") || childType.contains("Label") || childType.contains("Name")) {
                        // 尝试 getText()
                        try {
                            java.lang.reflect.Method getTextMethod = child.getClass().getMethod("getText");
                            Object text = getTextMethod.invoke(child);
                            if (text != null) {
                                String str = text.toString().trim();
                                if (!str.isEmpty() && !"«allocate»".equals(str)) {
                                    info("[SwimlaneTitle] 从子元素文本获取: " + str);
                                    return str;
                                }
                            }
                        } catch (Throwable ignored) {}

                        // 尝试获取模型元素名称
                        Element childElement = child.getElement();
                        if (childElement instanceof NamedElement) {
                            String name = ((NamedElement) childElement).getName();
                            if (name != null && !name.trim().isEmpty() && !"«allocate»".equals(name)) {
                                info("[SwimlaneTitle] 从子元素模型获取: " + name);
                                return name;
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}

        // ★★★ 方法3：尝试从 ShapeElement 自身获取 ★★★
        try {
            // 尝试 getText()
            java.lang.reflect.Method getTextMethod = se.getClass().getMethod("getText");
            Object text = getTextMethod.invoke(se);
            if (text != null) {
                String str = text.toString().trim();
                if (!str.isEmpty() && !"«allocate»".equals(str)) {
                    info("[SwimlaneTitle] 从 ShapeElement.getText() 获取: " + str);
                    return str;
                }
            }
        } catch (Throwable ignored) {}

        try {
            // 尝试 getName()
            java.lang.reflect.Method getNameMethod = se.getClass().getMethod("getName");
            Object name = getNameMethod.invoke(se);
            if (name != null) {
                String str = name.toString().trim();
                if (!str.isEmpty() && !"«allocate»".equals(str)) {
                    info("[SwimlaneTitle] 从 ShapeElement.getName() 获取: " + str);
                    return str;
                }
            }
        } catch (Throwable ignored) {}

        // ★★★ 方法4：尝试通过子元素文本获取（递归查找）★★★
        try {
            Collection<PresentationElement> allChildren = se.getPresentationElements();
            if (allChildren != null) {
                for (PresentationElement child : allChildren) {
                    String childType = child.getClass().getSimpleName();
                    // 查找任何包含文本的元素的子元素
                    try {
                        java.lang.reflect.Method getTextMethod = child.getClass().getMethod("getText");
                        Object text = getTextMethod.invoke(child);
                        if (text != null) {
                            String str = text.toString().trim();
                            if (!str.isEmpty() && !str.contains("«") && !str.contains("»") &&
                                    !"未命名泳道".equals(str) && !"«allocate»".equals(str)) {
                                info("[SwimlaneTitle] 从深层子元素获取: " + str);
                                return str;
                            }
                        }
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable ignored) {}

        info("[SwimlaneTitle] 所有方法均未能获取泳道名称，使用默认值");
        return "未命名泳道";
    }

    private List<PresentationElement> collectAllPresentationElements(PresentationElement root) {
        List<PresentationElement> out = new ArrayList<>();
        Deque<PresentationElement> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            PresentationElement cur = stack.pop();
            if (cur == null) continue;
            out.add(cur);
            Collection<PresentationElement> children = cur.getPresentationElements();
            if (children != null) {
                for (PresentationElement ch : children) {
                    if (ch != null) stack.push(ch);
                }
            }
        }
        return out;
    }

    private List<List<Integer>> pointsToList(List<Point> pts) {
        List<List<Integer>> out = new ArrayList<>();
        if (pts == null) return out;
        for (Point p : pts) {
            if (p == null) continue;
            out.add(Arrays.asList(p.x, p.y));
        }
        return out;
    }

    private static final String INDENT = "  ";

    private String toJson(Object obj) {
        StringBuilder sb = new StringBuilder();
        writeJson(sb, obj, 0);
        sb.append("\n");
        return sb.toString();
    }

    private void indent(StringBuilder sb, int level) {
        for (int i = 0; i < level; i++) sb.append(INDENT);
    }

    @SuppressWarnings("unchecked")
    private void writeJson(StringBuilder sb, Object obj, int level) {
        if (obj == null) { sb.append("null"); return; }
        if (obj instanceof String) { sb.append("\"").append(escape((String) obj)).append("\""); return; }
        if (obj instanceof Number || obj instanceof Boolean) { sb.append(obj.toString()); return; }
        if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            if (map.isEmpty()) { sb.append("{}"); return; }
            sb.append("{\n");
            int i = 0;
            for (Map.Entry<String, Object> e : map.entrySet()) {
                indent(sb, level + 1);
                sb.append("\"").append(escape(e.getKey())).append("\": ");
                writeJson(sb, e.getValue(), level + 1);
                if (++i < map.size()) sb.append(",");
                sb.append("\n");
            }
            indent(sb, level);
            sb.append("}");
            return;
        }
        if (obj instanceof Collection) {
            Collection<?> list = (Collection<?>) obj;
            if (list.isEmpty()) { sb.append("[]"); return; }
            sb.append("[\n");
            int i = 0;
            int n = list.size();
            for (Object it : list) {
                indent(sb, level + 1);
                writeJson(sb, it, level + 1);
                if (++i < n) sb.append(",");
                sb.append("\n");
            }
            indent(sb, level);
            sb.append("]");
            return;
        }
        sb.append("\"").append(escape(obj.toString())).append("\"");
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 仿真启动 / 停止
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private void startSimulation(String diagramId, boolean useConfig, String configName) {
        try {
            Project project = Application.getInstance().getProject();
            if (project == null) {
                pushFrontendError("未打开项目，请先在 MagicDraw 中打开项目后再开始仿真");
                return;
            }
            boolean hasDiagramId = diagramId != null && !diagramId.trim().isEmpty();
            if (!hasDiagramId && useConfig) {
                startSimulationConfig(project, configName);
                return;
            }
            Activity activity;
            if (hasDiagramId) {
                activity = getActivityByDiagramId(project, diagramId);
                if (activity == null) return;
            } else {
                info("[SimSync] 前端未传 diagramId，尝试从当前打开的活动图启动仿真");
                activity = getCurrentActivity(project);
                if (activity == null) {
                    pushFrontendError("前端没有传 diagramId，且 MagicDraw 当前没有打开可执行的活动图");
                    return;
                }
            }
            info("[SimSync] 普通方式启动 Activity: " + activity.getName());
            startReplayRecording(hasDiagramId ? diagramId : "", activity.getName(), "run");
            currentResult = SimulationManager.execute(activity, true);
            if (currentResult != null) {
                currentSession = tryGetSessionFromResult(currentResult);
                logSimulationRuntimeContext("普通 Run");
                pushFrontendInfo("[SimSync] ✅ 仿真已启动: " + activity.getName());
                broadcastSimStart();
            } else {
                finishReplayRecording("failed", "普通 Run 启动失败：SimulationResult 为空");
                pushFrontendError("[SimSync] ❌ 仿真启动失败");
            }
        } catch (Exception e) {
            pushFrontendError("[SimSync] 启动仿真异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startSimulationConfig(Project project, String configName) {
        try {
            if (configName == null || configName.trim().isEmpty()) configName = "Simulation Config";
            Element config = findSimulationConfigByName(project, configName);
            if (config == null) {
                pushFrontendError("未找到 Simulation Config：" + configName);
                return;
            }
            info("[SimSync] 直接启动 Simulation Config 元素: " + getElementDisplayName(config) + " / class=" + config.getClass().getName());
            startReplayRecording("", getElementDisplayName(config), "run_config");
            currentResult = SimulationManager.execute(config, true);
            if (currentResult != null) {
                currentSession = tryGetSessionFromResult(currentResult);
                pushFrontendInfo("[SimSync] ✅ 已按 Simulation Config 启动仿真: " + configName);
                broadcastSimStart();
            } else {
                finishReplayRecording("failed", "Simulation Config 启动失败：SimulationResult 为空");
                pushFrontendError("[SimSync] ❌ Simulation Config 启动失败");
            }
        } catch (Exception e) {
            pushFrontendError("[SimSync] Simulation Config 启动异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startSimulationWithContext(String diagramId, WebSocket conn) {
        try {
            Project project = Application.getInstance().getProject();
            if (project == null) {
                sendConnError(conn, "未打开项目，无法 Run with Context");
                return;
            }
            Activity activity = getActivityByDiagramId(project, diagramId);
            if (activity == null) {
                sendConnError(conn, "当前图不是活动图，无法 Run with Context");
                return;
            }
            Element contextTarget = resolveRunWithContextTarget(project, activity);
            if (contextTarget == null) {
                sendConnError(conn, "当前 Activity 没有可用运行上下文，不能 Run with Context：" + getElementDisplayName(activity) + "。请确认该 Activity 是否属于某个 Block，或是否被设置为 Block 的 Classifier Behavior。");
                return;
            }
            Object classifierBehavior = invokeNoArg(contextTarget, "getClassifierBehavior");
            if (!isSameElement(classifierBehavior, activity)) {
                sendConnError(conn, "找到上下文元素，但它的 Classifier Behavior 不是当前 Activity。当前 Activity = " + getElementDisplayName(activity) + "，Context = " + getElementDisplayName(contextTarget) + "。这种情况下不能认为是 Run with Context。");
                return;
            }
            info("[SimSync] Run with Context Activity = " + getElementDisplayName(activity) + " / class=" + activity.getClass().getName());
            info("[SimSync] Run with Context Target = " + getElementDisplayName(contextTarget) + " / class=" + contextTarget.getClass().getName());
            startReplayRecording(diagramId, getElementDisplayName(activity), "run_with_context");
            currentResult = SimulationManager.execute(contextTarget, true);
            if (currentResult != null) {
                currentSession = tryGetSessionFromResult(currentResult);
                logSimulationRuntimeContext("Run with Context");
                pushFrontendInfo("[SimSync] ✅ 已按 Run with Context 启动: " + getElementDisplayName(contextTarget));
                broadcastSimStart();
            } else {
                finishReplayRecording("failed", "Run with Context 启动失败：SimulationResult 为空");
                sendConnError(conn, "Run with Context 启动失败：SimulationResult 为空");
            }
        } catch (Throwable t) {
            sendConnError(conn, "Run with Context 启动异常: " + t.getMessage());
            t.printStackTrace();
        }
    }

    private Element resolveRunWithContextTarget(Project project, Element diagramOwner) {
        if (diagramOwner == null) return null;
        if (!(diagramOwner instanceof Activity) && isContextClassifierLike(diagramOwner)) return diagramOwner;
        if (diagramOwner instanceof Activity) {
            Object context = invokeNoArg(diagramOwner, "getContext");
            if (context instanceof Element) {
                Element contextElement = (Element) context;
                if (isContextClassifierLike(contextElement)) return contextElement;
            }
            Element owner = diagramOwner.getOwner();
            if (isContextClassifierLike(owner)) return owner;
            Element primaryModel = project == null ? null : project.getPrimaryModel();
            Element found = findClassifierWhoseBehaviorIs(primaryModel, diagramOwner);
            if (found != null) return found;
        }
        return null;
    }

    private boolean isContextClassifierLike(Element element) {
        if (element == null) return false;
        if (element instanceof Activity) return false;
        String cls = element.getClass().getName().toLowerCase();
        String simple = element.getClass().getSimpleName().toLowerCase();
        if (cls.contains("class") || cls.contains("classifier") || cls.contains("block") ||
                simple.contains("class") || simple.contains("classifier") || simple.contains("block")) return true;
        Object classifierBehavior = invokeNoArg(element, "getClassifierBehavior");
        return classifierBehavior != null;
    }

    private Element findClassifierWhoseBehaviorIs(Element root, Element behavior) {
        return findClassifierWhoseBehaviorIs(root, behavior, new java.util.HashSet<String>());
    }

    private Element findClassifierWhoseBehaviorIs(Element element, Element behavior, java.util.Set<String> visited) {
        if (element == null || behavior == null) return null;
        String id = element.getID();
        if (id != null && visited.contains(id)) return null;
        if (id != null) visited.add(id);
        Object classifierBehavior = invokeNoArg(element, "getClassifierBehavior");
        if (classifierBehavior == behavior && isContextClassifierLike(element)) return element;
        try {
            Collection<Element> children = element.getOwnedElement();
            if (children != null) {
                for (Element child : children) {
                    Element found = findClassifierWhoseBehaviorIs(child, behavior, visited);
                    if (found != null) return found;
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private void stopSimulation() {
        try {
            if (currentSession != null && !currentSession.isClosed()) {
                SimulationManager.terminateSession(currentSession);
                pushFrontendInfo("[SimSync] ✅ 仿真已终止");
                currentSession = null;
                currentResult = null;
                broadcastSimEnd();
                broadcast("{\"event\":\"clear_all\"}");
            } else {
                pushFrontendInfo("[SimSync] 没有正在运行的仿真");
            }
        } catch (Exception e) {
            pushFrontendError("[SimSync] 终止仿真异常: " + e.getMessage());
        }
    }

    private Activity getActivityByDiagramId(Project project, String diagramId) {
        try {
            DiagramPresentationElement dpe = findDiagramById(project, diagramId);
            if (dpe == null) {
                pushFrontendError("[SimSync] 未找到前端选择的图");
                return null;
            }
            com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram diagram = dpe.getDiagram();
            if (diagram == null) {
                pushFrontendError("[SimSync] 图对象为空，无法启动仿真");
                return null;
            }
            Element owner = diagram.getOwner();
            if (!(owner instanceof Activity)) {
                pushFrontendError("当前图不是活动图，不能直接启动仿真：" + dpe.getName());
                return null;
            }
            Activity activity = (Activity) owner;
            info("[SimSync] 根据前端选择的图获取 Activity: " + activity.getName());
            return activity;
        } catch (Exception e) {
            pushFrontendError("[SimSync] 根据 diagramId 获取 Activity 失败: " + e.getMessage());
            return null;
        }
    }

    private Activity getCurrentActivity(Project project) {
        try {
            DiagramPresentationElement activeDiagram = project.getActiveDiagram();
            if (activeDiagram != null) {
                com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram diagram = activeDiagram.getDiagram();
                if (diagram != null) {
                    Element owner = diagram.getOwner();
                    if (owner instanceof Activity) {
                        info("[SimSync] 从当前活动图获取 Activity: " + ((Activity) owner).getName());
                        return (Activity) owner;
                    }
                }
            }
            pushFrontendError("[SimSync] 未找到任何 Activity");
            return null;
        } catch (Exception e) {
            pushFrontendError("[SimSync] 获取 Activity 失败: " + e.getMessage());
            return null;
        }
    }

    private SimulationSession tryGetSessionFromResult(SimulationResult result) {
        if (result == null) return null;
        try {
            java.lang.reflect.Method m = SimulationManager.class.getMethod("getSession", SimulationResult.class);
            Object sess = m.invoke(null, result);
            return (SimulationSession) sess;
        } catch (Throwable ignore) {
            return null;
        }
    }

    private Element findSimulationConfigByName(Project project, String configName) {
        if (project == null || configName == null || configName.trim().isEmpty()) return null;
        return findSimulationConfigRecursive(project.getPrimaryModel(), configName.trim());
    }

    private Element findSimulationConfigRecursive(Element element, String targetName) {
        if (element == null || targetName == null) return null;
        if (element instanceof NamedElement) {
            NamedElement ne = (NamedElement) element;
            String name = ne.getName();
            if (targetName.equals(name)) {
                if (isSimulationConfigElement(element)) {
                    info("[SimSync] 找到 Simulation Config 元素: " + ne.getQualifiedName() + " / class=" + element.getClass().getName());
                    return element;
                }
            }
        }
        try {
            Collection<Element> children = element.getOwnedElement();
            if (children != null) {
                for (Element child : children) {
                    Element found = findSimulationConfigRecursive(child, targetName);
                    if (found != null) return found;
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private boolean isSimulationConfigElement(Element element) {
        if (element == null) return false;
        String cls = element.getClass().getName().toLowerCase();
        String text = String.valueOf(element).toLowerCase();
        if (cls.contains("simulationconfig") || text.contains("simulationconfig")) return true;
        try {
            java.lang.reflect.Method m = element.getClass().getMethod("getAppliedStereotype");
            Object result = m.invoke(element);
            if (result instanceof java.util.Collection) {
                for (Object st : (java.util.Collection<?>) result) {
                    String stName = getNameByReflection(st);
                    String stText = String.valueOf(st);
                    if ((stName != null && stName.toLowerCase().contains("simulationconfig")) || stText.toLowerCase().contains("simulationconfig")) {
                        return true;
                    }
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }

    private String getElementDisplayName(Element element) {
        if (element instanceof NamedElement) {
            NamedElement ne = (NamedElement) element;
            String qn = ne.getQualifiedName();
            if (qn != null && !qn.trim().isEmpty()) return qn;
            String name = ne.getName();
            if (name != null) return name;
        }
        return String.valueOf(element);
    }

    private void logSimulationRuntimeContext(String tag) {
        try {
            if (currentSession == null || currentSession.isClosed()) {
                info("[SimSync] " + tag + " currentSession 为空或已关闭");
                return;
            }
            Object context = SimulationManager.getContext(currentSession);
            Object rootContext = SimulationManager.getRootContext(currentSession);
            info("[SimSync] " + tag + " SimulationManager.getContext = " + (context == null ? "null" : context.getClass().getName() + " / " + String.valueOf(context)));
            info("[SimSync] " + tag + " SimulationManager.getRootContext = " + (rootContext == null ? "null" : rootContext.getClass().getName() + " / " + String.valueOf(rootContext)));
        } catch (Throwable t) {
            info("[SimSync] " + tag + " 打印仿真上下文失败: " + t.getMessage());
        }
    }

    private boolean isSameElement(Object a, Element b) {
        if (a == null || b == null) return false;
        if (a == b || a.equals(b)) return true;
        if (a instanceof Element) {
            String id1 = ((Element) a).getID();
            String id2 = b.getID();
            return id1 != null && id1.equals(id2);
        }
        return false;
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 广播和辅助方法
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private String getNameByReflection(Object obj) {
        if (obj == null) return "";
        try {
            java.lang.reflect.Method m = obj.getClass().getMethod("getName");
            Object value = m.invoke(obj);
            return value == null ? "" : String.valueOf(value);
        } catch (Throwable ignored) {}
        return "";
    }

    public void broadcast(String json) {
        synchronized (clients) {
            for (WebSocket c : clients) {
                if (c.isOpen()) {
                    try {
                        c.send(json);
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    public void broadcastSimStart() {
        broadcast("{\"event\":\"sim_start\"}");
    }

    public void broadcastSimEnd() {
        // 1. 先把每个节点的最终资源统计发送给前端。
        try {
            Map<String, Map<String, Object>> allStats = OperatorResourceManager.getAllNodeStats();
            for (Map.Entry<String, Map<String, Object>> entry : allStats.entrySet()) {
                JSONObject statsMsg = new JSONObject();
                statsMsg.put("event", "node_stats");
                statsMsg.put("nodeName", entry.getKey());
                for (Map.Entry<String, Object> stat : entry.getValue().entrySet()) {
                    statsMsg.put(stat.getKey(), stat.getValue());
                }
                broadcast(statsMsg.toString());
            }
            info("[SimSync] 已发送所有节点的最终状态，共 " + allStats.size() + " 个节点");
        } catch (Exception e) {
            err("[SimSync] 发送最终节点状态失败: " + e.getMessage());
        }

        // 2. 再发送一次完整方案汇总。
        //    这样不要求活动图最后必须额外放置 printSummary() 节点，
        //    前端仍然能稳定拿到总工期和每日资源直方图数据。
        try {
            JSONObject summaryMsg = buildSimulationSummaryMessage();
            broadcast(summaryMsg.toString());
            info("[SimSync] 已广播仿真方案汇总，totalDuration="
                    + summaryMsg.opt("totalDuration") + " 天");
        } catch (Exception e) {
            err("[SimSync] 广播仿真方案汇总失败: " + e.getMessage());
        }

        // 给前端一点时间先处理最终节点状态和方案汇总，再发送 sim_end。
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        pushEventLog("info", "[SimSync] 仿真结束");
        finishReplayRecording("completed", "");
        broadcast("{\"event\":\"sim_end\"}");
    }

    public void broadcastNodeActivate(String nodeId, String nodeName) {
        String safeNodeId = nodeId != null ? escapeJson(nodeId) : "";
        String safeNodeName = nodeName != null ? escapeJson(nodeName) : "";
        broadcast(String.format("{\"event\":\"node_activate\",\"nodeId\":\"%s\",\"nodeName\":\"%s\"}", safeNodeId, safeNodeName));
        recordReplayEvent("{\"time\":" + replayTime() + ",\"event\":\"node_activate\",\"nodeId\":\"" + safeNodeId + "\",\"nodeName\":\"" + safeNodeName + "\"}");
        pushEventLog("info", "[SimSync] 节点激活：" + (nodeName == null || nodeName.trim().isEmpty() ? nodeId : nodeName));
    }

    public void broadcastNodeDeactivate(String nodeId) {
        String safeNodeId = nodeId != null ? escapeJson(nodeId) : "";
        broadcast(String.format("{\"event\":\"node_deactivate\",\"nodeId\":\"%s\"}", safeNodeId));
        recordReplayEvent("{\"time\":" + replayTime() + ",\"event\":\"node_deactivate\",\"nodeId\":\"" + safeNodeId + "\"}");
    }

    private void sendConnError(WebSocket conn, String msg) {
        err("[SimSync] " + msg);
        if (conn != null && conn.isOpen()) {
            conn.send("{\"event\":\"backend_error\",\"message\":\"" + escapeJson(msg) + "\"}");
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 节点配置相关功能
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private void sendNodeConfig(WebSocket conn, String taskName, String callbackId) {
        try {
            System.out.println("[NodeConfig] 📤 收到配置请求: " + taskName + ", callbackId=" + callbackId);
            Map<String, Object> config = OperatorResourceManager.getNodeConfig(taskName);
            JSONObject response = new JSONObject();
            response.put("event", "node_config");
            response.put("taskName", taskName);
            response.put("config", config);
            if (callbackId != null && !callbackId.isEmpty()) {
                response.put("_callbackId", callbackId);
                System.out.println("[NodeConfig] ✅ 透传 callbackId: " + callbackId);
            }
            if (conn != null && conn.isOpen()) {
                conn.send(response.toString());
                System.out.println("[NodeConfig] ✅ 已发送响应");
            }
        } catch (Exception e) {
            System.err.println("[NodeConfig] ❌ 读取配置失败: " + e.getMessage());
            e.printStackTrace();
            sendConnError(conn, "读取配置失败: " + e.getMessage());
        }
    }

    private void sendResourcePoolConfig(WebSocket conn) {
        try {
            Map<String, Object> config = new LinkedHashMap<>();
            if (currentSession != null && !currentSession.isClosed()) {
                try {
                    ALH alh = new ALH(currentSession);
                    Map<String, Object> poolConfig = OperatorResourceManager.getResourcePoolConfig(alh);
                    config.putAll(poolConfig);
                    config.put("hasSession", true);
                } catch (Exception e) {
                    config.putAll(OperatorResourceManager.getResourcePoolConfig());
                    config.put("hasSession", false);
                    config.put("sessionError", e.getMessage());
                }
            } else {
                config.putAll(OperatorResourceManager.getResourcePoolConfig());
                config.put("hasSession", false);
            }
            JSONObject response = new JSONObject();
            response.put("event", "resource_pool_config");
            response.put("config", config);
            if (conn != null && conn.isOpen()) {
                conn.send(response.toString());
            }
        } catch (Exception e) {
            err("[ResourcePool] 获取资源池配置失败: " + e.getMessage());
            e.printStackTrace();
            sendConnError(conn, "获取资源池配置失败: " + e.getMessage());
        }
    }

    private void updateResourcePoolConfig(WebSocket conn, Integer totalOperators, Double timeScaleMs) {
        try {
            Map<String, Object> result = new LinkedHashMap<>();
            if (currentSession != null && !currentSession.isClosed()) {
                try {
                    ALH alh = new ALH(currentSession);
                    Map<String, Object> updateResult = OperatorResourceManager.updateResourcePoolConfig(alh, totalOperators, timeScaleMs);
                    result.putAll(updateResult);
                } catch (Exception e) {
                    result.put("success", false);
                    result.put("error", "更新失败: " + e.getMessage());
                }
            } else {
                result.put("success", false);
                result.put("error", "没有正在运行的仿真会话，无法更新资源池配置");
            }
            JSONObject response = new JSONObject();
            response.put("event", "resource_pool_config_updated");
            response.put("success", result.get("success"));
            response.put("totalOperators", result.get("totalOperators"));
            response.put("timeScaleMs", result.get("timeScaleMs"));
            if (result.containsKey("error")) {
                response.put("error", result.get("error"));
            }
            if (conn != null && conn.isOpen()) {
                conn.send(response.toString());
            }
            if (Boolean.TRUE.equals(result.get("success"))) {
                pushEventLog("info", "[ResourcePool] 资源池配置已更新: totalOperators=" + result.get("totalOperators") + ", timeScaleMs=" + result.get("timeScaleMs"));
            }
        } catch (Exception e) {
            err("[ResourcePool] 更新资源池配置失败: " + e.getMessage());
            e.printStackTrace();
            sendConnError(conn, "更新资源池配置失败: " + e.getMessage());
        }
    }

    private void sendInitNodeBody(WebSocket conn, String nodeId) {
        try {
            Project project = Application.getInstance().getProject();
            if (project == null) {
                sendConnError(conn, "未打开项目");
                return;
            }
            Element el = findElementById(project, nodeId);
            if (!(el instanceof com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.OpaqueAction)) {
                sendConnError(conn, "节点不是 OpaqueAction: " + nodeId);
                return;
            }
            com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.OpaqueAction oa =
                    (com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.OpaqueAction) el;
            List<String> bodies = new ArrayList<>(oa.getBody());
            List<String> languages = new ArrayList<>(oa.getLanguage());

            // ★★★ 修改：如果 bodies 为空或只有空字符串，返回空数组 ★★★
            boolean hasValidBody = false;
            for (String b : bodies) {
                if (b != null && !b.trim().isEmpty()) {
                    hasValidBody = true;
                    break;
                }
            }

            StringBuilder sb = new StringBuilder("[");
            if (hasValidBody) {
                for (int i = 0; i < bodies.size(); i++) {
                    String body = bodies.get(i);
                    if (body == null || body.trim().isEmpty()) continue;
                    if (sb.length() > 1) sb.append(",");
                    sb.append("{")
                            .append("\"index\":").append(i).append(",")
                            .append("\"language\":\"").append(escapeJson(i < languages.size() ? languages.get(i) : "")).append("\",")
                            .append("\"body\":\"").append(escapeJson(body)).append("\"")
                            .append("}");
                }
            }
            sb.append("]");
            String resp = "{\"event\":\"init_body_data\","
                    + "\"nodeId\":\"" + escapeJson(nodeId) + "\","
                    + "\"bodies\":" + sb + "}";
            if (conn != null && conn.isOpen()) conn.send(resp);
            info("[InitBody] 已读取 " + nodeId + " 的 Body，共 " + bodies.size() + " 条，有效: " + hasValidBody);
        } catch (Exception e) {
            sendConnError(conn, "读取 Body 失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateInitNodeBody(WebSocket conn, String nodeId, String newBody) {
        try {
            Project project = Application.getInstance().getProject();
            if (project == null) {
                sendConnError(conn, "未打开项目");
                return;
            }
            Element el = findElementById(project, nodeId);
            if (!(el instanceof com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.OpaqueAction)) {
                sendConnError(conn, "节点不是 OpaqueAction: " + nodeId);
                return;
            }
            com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.OpaqueAction oa =
                    (com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.OpaqueAction) el;
            com.nomagic.magicdraw.openapi.uml.SessionManager.getInstance().createSession(project, "Update Init Body");
            try {
                List<String> bodies = new ArrayList<>(oa.getBody());
                if (bodies.isEmpty()) {
                    bodies.add(newBody);
                } else {
                    bodies.set(0, newBody);
                }
                oa.getBody().clear();
                oa.getBody().addAll(bodies);
                com.nomagic.magicdraw.openapi.uml.SessionManager.getInstance().closeSession(project);
                String resp = "{\"event\":\"init_body_updated\","
                        + "\"nodeId\":\"" + escapeJson(nodeId) + "\","
                        + "\"success\":true,"
                        + "\"body\":\"" + escapeJson(newBody) + "\"}";
                if (conn != null && conn.isOpen()) conn.send(resp);
                pushEventLog("info", "[InitBody] 节点 " + nodeId + " Body 已更新");
                info("[InitBody] 写回成功: " + nodeId);
            } catch (Exception e) {
                com.nomagic.magicdraw.openapi.uml.SessionManager.getInstance().cancelSession(project);
                throw e;
            }
        } catch (Exception e) {
            String resp = "{\"event\":\"init_body_updated\","
                    + "\"nodeId\":\"" + escapeJson(nodeId) + "\","
                    + "\"success\":false,"
                    + "\"error\":\"" + escapeJson(e.getMessage()) + "\"}";
            if (conn != null && conn.isOpen()) conn.send(resp);
            e.printStackTrace();
        }
    }

    private void sendNodeFullConfig(WebSocket conn, String taskName, String callbackId) {
        try {
            Map<String, Object> fullConfig = OperatorResourceManager.getNodeFullConfig(taskName);
            JSONObject response = new JSONObject();
            response.put("event", "node_full_config");
            response.put("taskName", taskName);
            response.put("config", fullConfig);
            if (callbackId != null && !callbackId.isEmpty()) {
                response.put("_callbackId", callbackId);
            }
            if (conn != null && conn.isOpen()) {
                conn.send(response.toString());
            }
            info("[NodeConfig] 已发送节点完整配置: " + taskName);
        } catch (Exception e) {
            err("[NodeConfig] 获取完整配置失败: " + e.getMessage());
            sendConnError(conn, "获取完整配置失败: " + e.getMessage());
        }
    }

    private void sendAllNodeConfigs(WebSocket conn, String callbackId) {
        try {
            Map<String, Map<String, Object>> allConfigs = OperatorResourceManager.getAllNodeConfigs();
            JSONObject response = new JSONObject();
            response.put("event", "all_node_configs");
            response.put("configs", allConfigs);
            response.put("count", allConfigs.size());
            if (callbackId != null && !callbackId.isEmpty()) {
                response.put("_callbackId", callbackId);
            }
            if (conn != null && conn.isOpen()) {
                conn.send(response.toString());
            }
            info("[NodeConfig] 已发送所有节点配置，共 " + allConfigs.size() + " 个");
        } catch (Exception e) {
            err("[NodeConfig] 获取所有配置失败: " + e.getMessage());
            sendConnError(conn, "获取所有配置失败: " + e.getMessage());
        }
    }

    private void updateNodeFullConfig(WebSocket conn, String taskName, JSONObject properties, String callbackId) {
        try {
            Project project = Application.getInstance().getProject();
            if (project == null) {
                sendUpdateFullConfigResult(conn, taskName, false, "当前没有打开项目", callbackId);
                return;
            }
            Element model = project.getPrimaryModel();
            if (model == null) {
                sendUpdateFullConfigResult(conn, taskName, false, "无法获取主模型", callbackId);
                return;
            }
            Stereotype resourceTask = StereotypesHelper.getStereotype(project, "ResourceTask");
            if (resourceTask == null) {
                sendUpdateFullConfigResult(conn, taskName, false, "未找到 stereotype：ResourceTask", callbackId);
                return;
            }
            Element taskElement = OperatorResourceManager.findElementByName(model, taskName);
            if (taskElement == null) {
                sendUpdateFullConfigResult(conn, taskName, false, "未找到同名模型元素：" + taskName, callbackId);
                return;
            }
            if (!StereotypesHelper.hasStereotype(taskElement, resourceTask)) {
                sendUpdateFullConfigResult(conn, taskName, false, "节点没有应用 <<ResourceTask>>", callbackId);
                return;
            }
            System.out.println("[ResourceTask] 更新前属性:");
            for (String key : properties.keySet()) {
                List<?> currentValues = StereotypesHelper.getStereotypePropertyValue(taskElement, resourceTask, key);
                System.out.println("  " + key + " = " + (currentValues != null && !currentValues.isEmpty() ? currentValues.get(0) : "null"));
            }
            com.nomagic.magicdraw.openapi.uml.SessionManager sessionManager =
                    com.nomagic.magicdraw.openapi.uml.SessionManager.getInstance();
            boolean sessionCreated = false;
            try {
                if (!sessionManager.isSessionCreated()) {
                    sessionManager.createSession(project, "Update Full ResourceTask Config");
                    sessionCreated = true;
                }
                Map<String, Object> updatedProps = new LinkedHashMap<>();
                for (String key : properties.keySet()) {
                    Object value = properties.get(key);
                    if (value instanceof Integer || value instanceof Double || value instanceof String) {
                        StereotypesHelper.setStereotypePropertyValue(taskElement, resourceTask, key, value);
                        updatedProps.put(key, value);
                        System.out.println("[ResourceTask] 设置属性 " + key + " = " + value);
                    } else if (value instanceof Number) {
                        double numValue = ((Number) value).doubleValue();
                        StereotypesHelper.setStereotypePropertyValue(taskElement, resourceTask, key, numValue);
                        updatedProps.put(key, numValue);
                        System.out.println("[ResourceTask] 设置属性 " + key + " = " + numValue);
                    } else if (value == null) {
                        StereotypesHelper.setStereotypePropertyValue(taskElement, resourceTask, key, null);
                        updatedProps.put(key, null);
                        System.out.println("[ResourceTask] 清空属性 " + key);
                    } else if (value instanceof Boolean) {
                        StereotypesHelper.setStereotypePropertyValue(taskElement, resourceTask, key, value);
                        updatedProps.put(key, value);
                        System.out.println("[ResourceTask] 设置属性 " + key + " = " + value);
                    }
                }
                System.out.println("[ResourceTask] 更新后属性:");
                for (String key : updatedProps.keySet()) {
                    List<?> newValues = StereotypesHelper.getStereotypePropertyValue(taskElement, resourceTask, key);
                    System.out.println("  " + key + " = " + (newValues != null && !newValues.isEmpty() ? newValues.get(0) : "null"));
                }
                if (sessionCreated) {
                    sessionManager.closeSession(project);
                }
                OperatorResourceManager.clearConfigCache(taskName);
                sendUpdateFullConfigResult(conn, taskName, true, "所有属性已更新", callbackId);
                JSONObject broadcastMsg = new JSONObject();
                broadcastMsg.put("event", "node_config_changed");
                broadcastMsg.put("taskName", taskName);
                broadcastMsg.put("properties", updatedProps);
                broadcast(broadcastMsg.toString());
                pushEventLog("info", "[NodeConfig] ✅ 节点所有属性已更新: " + taskName + "，属性: " + updatedProps);
            } catch (Exception e) {
                if (sessionCreated) {
                    try {
                        sessionManager.cancelSession(project);
                    } catch (Exception ex) {}
                }
                throw e;
            }
        } catch (Exception e) {
            err("[NodeConfig] 更新完整配置失败: " + e.getMessage());
            e.printStackTrace();
            sendUpdateFullConfigResult(conn, taskName, false, e.getMessage(), callbackId);
        }
    }

    private void sendUpdateFullConfigResult(WebSocket conn, String taskName, boolean success, String message, String callbackId) {
        try {
            JSONObject response = new JSONObject();
            response.put("event", "node_full_config_updated");
            response.put("taskName", taskName);
            response.put("success", success);
            response.put("message", message);
            if (callbackId != null && !callbackId.isEmpty()) {
                response.put("_callbackId", callbackId);
            }
            if (conn != null && conn.isOpen()) {
                conn.send(response.toString());
            }
        } catch (Exception e) {
            err("[NodeConfig] 发送更新结果失败: " + e.getMessage());
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 子流程相关功能
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private void sendNodeSubprocessInfo(WebSocket conn, String nodeId) {
        try {
            Project project = Application.getInstance().getProject();
            if (project == null) {
                sendConnError(conn, "未打开项目");
                return;
            }
            Element nodeElement = findElementById(project, nodeId);
            if (nodeElement == null) {
                sendConnError(conn, "未找到节点: " + nodeId);
                return;
            }
            String className = nodeElement.getClass().getName();
            boolean isCallBehaviorAction = className.contains("CallBehaviorAction") || className.endsWith("CallBehaviorActionImpl");
            JSONObject response = new JSONObject();
            response.put("event", "node_subprocess_info");
            response.put("nodeId", nodeId);
            response.put("isCallBehaviorAction", isCallBehaviorAction);
            if (isCallBehaviorAction) {
                Object behavior = null;
                try {
                    java.lang.reflect.Method getBehaviorMethod = nodeElement.getClass().getMethod("getBehavior");
                    behavior = getBehaviorMethod.invoke(nodeElement);
                } catch (Throwable t) {
                    try {
                        java.lang.reflect.Method getBehaviorMethod = nodeElement.getClass().getMethod("getBehavior");
                        behavior = getBehaviorMethod.invoke(nodeElement);
                    } catch (Throwable t2) {}
                }
                if (behavior instanceof Activity) {
                    Activity activity = (Activity) behavior;
                    String activityId = activity.getID();
                    String activityName = activity.getName();
                    response.put("hasSubprocess", true);
                    response.put("subprocessId", activityId);
                    response.put("subprocessName", activityName != null ? activityName : "未命名子流程");
                    DiagramPresentationElement diagram = findDiagramByActivity(project, activity);
                    if (diagram != null) {
                        response.put("hasDiagram", true);
                        response.put("diagramId", diagram.getDiagram().getID());
                        response.put("diagramName", diagram.getName());
                    } else {
                        response.put("hasDiagram", false);
                        info("[Subprocess] 子流程 " + activityName + " 没有对应的图");
                    }
                } else {
                    response.put("hasSubprocess", false);
                    response.put("subprocessInfo", "未绑定子流程");
                }
            } else {
                response.put("hasSubprocess", false);
                response.put("reason", "节点不是 CallBehaviorAction 类型");
            }
            if (conn != null && conn.isOpen()) {
                conn.send(response.toString());
            }
        } catch (Exception e) {
            err("[Subprocess] 获取子流程信息失败: " + e.getMessage());
            sendConnError(conn, "获取子流程信息失败: " + e.getMessage());
        }
    }

    private DiagramPresentationElement findDiagramByActivity(Project project, Activity activity) {
        if (project == null || activity == null) return null;
        Collection<DiagramPresentationElement> diagrams = project.getDiagrams();
        for (DiagramPresentationElement dpe : diagrams) {
            if (dpe == null || dpe.getDiagram() == null) continue;
            Element owner = dpe.getDiagram().getOwner();
            if (owner == activity) return dpe;
        }
        return null;
    }

    private void openSubprocessDiagram(WebSocket conn, String nodeId) {
        try {
            Project project = Application.getInstance().getProject();
            if (project == null) {
                sendConnError(conn, "未打开项目");
                return;
            }
            Element nodeElement = findElementById(project, nodeId);
            if (nodeElement == null) {
                sendConnError(conn, "未找到节点: " + nodeId);
                return;
            }
            Object behavior = null;
            try {
                java.lang.reflect.Method getBehaviorMethod = nodeElement.getClass().getMethod("getBehavior");
                behavior = getBehaviorMethod.invoke(nodeElement);
            } catch (Throwable t) {
                sendConnError(conn, "无法获取子流程: " + t.getMessage());
                return;
            }
            if (!(behavior instanceof Activity)) {
                sendConnError(conn, "节点没有绑定子流程");
                return;
            }
            Activity activity = (Activity) behavior;
            DiagramPresentationElement diagram = findDiagramByActivity(project, activity);
            if (diagram == null) {
                sendConnError(conn, "子流程没有对应的图");
                return;
            }
            try {
                diagram.open();
                info("[Subprocess] 已打开子流程图: " + diagram.getName());
                JSONObject response = new JSONObject();
                response.put("event", "subprocess_diagram_opened");
                response.put("nodeId", nodeId);
                response.put("diagramId", diagram.getDiagram().getID());
                response.put("diagramName", diagram.getName());
                response.put("success", true);
                if (conn != null && conn.isOpen()) {
                    conn.send(response.toString());
                }
                broadcast(response.toString());
            } catch (Exception e) {
                sendConnError(conn, "打开子流程图失败: " + e.getMessage());
            }
        } catch (Exception e) {
            err("[Subprocess] 打开子流程图失败: " + e.getMessage());
            sendConnError(conn, "打开子流程图失败: " + e.getMessage());
        }
    }

    private void getSubprocessDiagramJson(WebSocket conn, String nodeId, String callbackId) {
        try {
            Project project = Application.getInstance().getProject();
            if (project == null) {
                sendConnError(conn, "未打开项目");
                return;
            }
            Element nodeElement = findElementById(project, nodeId);
            if (nodeElement == null) {
                sendConnError(conn, "未找到节点: " + nodeId);
                return;
            }
            Object behavior = null;
            try {
                java.lang.reflect.Method getBehaviorMethod = nodeElement.getClass().getMethod("getBehavior");
                behavior = getBehaviorMethod.invoke(nodeElement);
            } catch (Throwable t) {
                sendConnError(conn, "无法获取子流程: " + t.getMessage());
                return;
            }
            if (!(behavior instanceof Activity)) {
                sendConnError(conn, "节点没有绑定子流程");
                return;
            }
            Activity activity = (Activity) behavior;
            DiagramPresentationElement diagram = findDiagramByActivity(project, activity);
            if (diagram == null) {
                sendConnError(conn, "子流程没有对应的图");
                return;
            }
            String diagramJson = exportDiagramToJson(diagram);
            JSONObject response = new JSONObject();
            response.put("event", "subprocess_diagram_json");
            response.put("nodeId", nodeId);
            response.put("diagramId", diagram.getDiagram().getID());
            response.put("diagramName", diagram.getName());
            response.put("data", new JSONObject(diagramJson));
            response.put("success", true);
            if (callbackId != null && !callbackId.isEmpty()) {
                response.put("_callbackId", callbackId);
            }
            if (conn != null && conn.isOpen()) {
                conn.send(response.toString());
            }
            info("[Subprocess] 已发送子流程图 JSON: " + diagram.getName());
        } catch (Exception e) {
            err("[Subprocess] 获取子流程图 JSON 失败: " + e.getMessage());
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("event", "subprocess_diagram_json");
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            if (callbackId != null && !callbackId.isEmpty()) {
                errorResponse.put("_callbackId", callbackId);
            }
            if (conn != null && conn.isOpen()) {
                conn.send(errorResponse.toString());
            }
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 状态机 Trigger 功能
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private void sendStateMachineTriggers(WebSocket conn, String diagramId) {
        try {
            Project project = Application.getInstance().getProject();
            if (project == null) {
                sendConnError(conn, "未打开项目");
                return;
            }
            Element stateMachineElement = null;
            if (diagramId != null && !diagramId.trim().isEmpty()) {
                DiagramPresentationElement dpe = findDiagramById(project, diagramId);
                if (dpe != null && dpe.getDiagram() != null) {
                    Element owner = dpe.getDiagram().getOwner();
                    if (isStateMachine(owner)) {
                        stateMachineElement = owner;
                    }
                }
            }
            if (stateMachineElement == null) {
                DiagramPresentationElement activeDiagram = project.getActiveDiagram();
                if (activeDiagram != null && activeDiagram.getDiagram() != null) {
                    Element owner = activeDiagram.getDiagram().getOwner();
                    if (isStateMachine(owner)) {
                        stateMachineElement = owner;
                    }
                }
            }
            if (stateMachineElement == null) {
                stateMachineElement = findFirstStateMachineElement(project.getPrimaryModel());
            }
            if (stateMachineElement == null) {
                sendConnError(conn, "未找到状态机");
                return;
            }
            Set<String> triggers = new LinkedHashSet<>();
            Map<String, List<String>> stateBehaviors = new LinkedHashMap<>();
            collectTriggersAndBehaviorsReflectively(stateMachineElement, triggers, stateBehaviors);
            JSONObject response = new JSONObject();
            response.put("event", "state_machine_triggers");
            response.put("stateMachineName", getElementName(stateMachineElement));
            response.put("triggers", new ArrayList<>(triggers));
            response.put("stateBehaviors", stateBehaviors);
            response.put("triggerCount", triggers.size());
            response.put("behaviorCount", stateBehaviors.values().stream().mapToInt(List::size).sum());
            if (conn != null && conn.isOpen()) {
                conn.send(response.toString());
            }
            info("[StateMachine] 已发送 " + triggers.size() + " 个Trigger, " + stateBehaviors.values().stream().mapToInt(List::size).sum() + " 个状态行为");
            info("[StateMachine] Triggers: " + triggers);
            info("[StateMachine] StateBehaviors: " + stateBehaviors);
        } catch (Exception e) {
            err("[StateMachine] 获取Trigger失败: " + e.getMessage());
            e.printStackTrace();
            sendConnError(conn, "获取Trigger失败: " + e.getMessage());
        }
    }

    private boolean isStateMachine(Element element) {
        if (element == null) return false;
        String className = element.getClass().getSimpleName();
        return "StateMachine".equals(className) || className.contains("StateMachine");
    }

    private Element findFirstStateMachineElement(Element element) {
        if (element == null) return null;
        if (isStateMachine(element)) return element;
        try {
            Collection<Element> children = element.getOwnedElement();
            if (children != null) {
                for (Element child : children) {
                    Element found = findFirstStateMachineElement(child);
                    if (found != null) return found;
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private void collectTriggersAndBehaviorsReflectively(Element element, Set<String> triggers, Map<String, List<String>> stateBehaviors) {
        if (element == null) return;
        String className = element.getClass().getSimpleName();
        if (isStateMachine(element) || "State".equals(className)) {
            if ("State".equals(className)) {
                collectStateBehaviors(element, stateBehaviors);
            }
            try {
                java.lang.reflect.Method getRegionMethod = element.getClass().getMethod("getRegion");
                Object regions = getRegionMethod.invoke(element);
                if (regions instanceof Collection) {
                    for (Object region : (Collection<?>) regions) {
                        collectTriggersAndBehaviorsFromRegionReflectively(region, triggers, stateBehaviors);
                    }
                }
            } catch (Throwable ignored) {}
        }
    }

    private void collectStateBehaviors(Element stateElement, Map<String, List<String>> stateBehaviors) {
        if (stateElement == null) return;
        if (!(stateElement instanceof State)) return;
        State state = (State) stateElement;
        String stateName = state.getName();
        if (stateName == null || stateName.trim().isEmpty()) {
            stateName = "未命名状态";
        }
        List<String> behaviors = new ArrayList<>();
        Behavior entryBehavior = state.getEntry();
        if (entryBehavior != null) {
            String entryName = getBehaviorName(entryBehavior);
            if (entryName != null && !entryName.trim().isEmpty()) {
                behaviors.add("🔵 进入: " + entryName);
            }
        }
        Behavior doActivityBehavior = state.getDoActivity();
        if (doActivityBehavior != null) {
            String doName = getBehaviorName(doActivityBehavior);
            if (doName != null && !doName.trim().isEmpty()) {
                behaviors.add("🟢 执行: " + doName);
            }
        }
        Behavior exitBehavior = state.getExit();
        if (exitBehavior != null) {
            String exitName = getBehaviorName(exitBehavior);
            if (exitName != null && !exitName.trim().isEmpty()) {
                behaviors.add("🔴 退出: " + exitName);
            }
        }
        if (!behaviors.isEmpty()) {
            stateBehaviors.put(stateName, behaviors);
            info("[StateMachine] 状态 " + stateName + " 有 " + behaviors.size() + " 个行为: " + behaviors);
        } else {
            info("[StateMachine] 状态 " + stateName + " 没有找到任何行为");
        }
    }

    private Object invokeMethodSafe(Object obj, String methodName) {
        if (obj == null) return null;
        try {
            java.lang.reflect.Method method = obj.getClass().getMethod(methodName);
            method.setAccessible(true);
            return method.invoke(obj);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private String getBehaviorName(Behavior behavior) {
        if (behavior == null) return null;
        if (behavior instanceof Activity) {
            String name = ((Activity) behavior).getName();
            if (name != null && !name.trim().isEmpty()) return name;
            String qn = ((Activity) behavior).getQualifiedName();
            if (qn != null && !qn.trim().isEmpty()) return qn;
        }
        if (behavior instanceof NamedElement) {
            String name = ((NamedElement) behavior).getName();
            if (name != null && !name.trim().isEmpty()) return name;
        }
        return behavior.getClass().getSimpleName();
    }

    private void collectTriggersAndBehaviorsFromRegionReflectively(Object region, Set<String> triggers, Map<String, List<String>> stateBehaviors) {
        if (region == null) return;
        try {
            java.lang.reflect.Method getTransitionMethod = region.getClass().getMethod("getTransition");
            Object transitions = getTransitionMethod.invoke(region);
            if (transitions instanceof Collection) {
                for (Object transition : (Collection<?>) transitions) {
                    collectTriggersFromTransitionReflectively(transition, triggers);
                }
            }
            java.lang.reflect.Method getSubvertexMethod = region.getClass().getMethod("getSubvertex");
            Object vertices = getSubvertexMethod.invoke(region);
            if (vertices instanceof Collection) {
                for (Object vertex : (Collection<?>) vertices) {
                    String vertexClassName = vertex.getClass().getSimpleName();
                    if ("State".equals(vertexClassName)) {
                        Element stateElement = (Element) vertex;
                        collectStateBehaviors(stateElement, stateBehaviors);
                        collectTriggersAndBehaviorsReflectively(stateElement, triggers, stateBehaviors);
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    private void collectTriggersFromTransitionReflectively(Object transition, Set<String> triggers) {
        if (transition == null) return;
        try {
            java.lang.reflect.Method getTriggerMethod = transition.getClass().getMethod("getTrigger");
            Object triggerList = getTriggerMethod.invoke(transition);
            if (triggerList instanceof Collection) {
                for (Object trigger : (Collection<?>) triggerList) {
                    String triggerName = getTriggerNameReflectively(trigger);
                    if (triggerName != null && !triggerName.trim().isEmpty()) {
                        triggers.add(triggerName);
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    private String getTriggerNameReflectively(Object trigger) {
        if (trigger == null) return null;
        try {
            java.lang.reflect.Method getEventMethod = trigger.getClass().getMethod("getEvent");
            Object event = getEventMethod.invoke(trigger);
            if (event == null) return null;
            String eventClassName = event.getClass().getSimpleName();
            if ("SignalEvent".equals(eventClassName) || eventClassName.contains("SignalEvent")) {
                try {
                    java.lang.reflect.Method getSignalMethod = event.getClass().getMethod("getSignal");
                    Object signal = getSignalMethod.invoke(event);
                    if (signal != null) {
                        java.lang.reflect.Method getNameMethod = signal.getClass().getMethod("getName");
                        Object name = getNameMethod.invoke(signal);
                        return name == null ? null : name.toString();
                    }
                } catch (Throwable ignored) {}
            }
            try {
                java.lang.reflect.Method getNameMethod = event.getClass().getMethod("getName");
                Object name = getNameMethod.invoke(event);
                if (name != null && !name.toString().trim().isEmpty()) {
                    return name.toString();
                }
            } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
        return null;
    }

    private void sendSignalToSimulation(WebSocket conn, String signalName) {
        try {
            if (signalName == null || signalName.trim().isEmpty()) {
                sendConnError(conn, "信号名不能为空");
                return;
            }
            if (currentSession == null || currentSession.isClosed()) {
                sendConnError(conn, "没有正在运行的仿真会话");
                return;
            }
            ALH alh = new ALH(currentSession);
            boolean success = false;
            String method = "";
            try {
                alh.sendSignal(signalName, "");
                success = true;
                method = "Signal";
                pushFrontendInfo("[SimSync] 已发送信号: " + signalName);
            } catch (Exception e) {
                info("[SimSync] 作为 Signal 发送失败，尝试作为 Activity 调用: " + e.getMessage());
                try {
                    Project project = Application.getInstance().getProject();
                    if (project != null) {
                        Activity activity = findActivityByName(project, signalName);
                        if (activity != null) {
                            SimulationManager.execute(activity, false);
                            success = true;
                            method = "Activity";
                            pushFrontendInfo("[SimSync] 已执行 Activity: " + signalName);
                        } else {
                            Object behavior = findOpaqueBehaviorByName(project, signalName);
                            if (behavior != null) {
                                java.lang.reflect.Method executeMethod = behavior.getClass().getMethod("execute");
                                executeMethod.invoke(behavior);
                                success = true;
                                method = "OpaqueBehavior";
                                pushFrontendInfo("[SimSync] 已执行 OpaqueBehavior: " + signalName);
                            } else {
                                throw new Exception("未找到信号或活动: " + signalName);
                            }
                        }
                    } else {
                        throw new Exception("无法获取项目实例");
                    }
                } catch (Exception ex) {
                    throw new Exception("无法执行: " + signalName + " - " + ex.getMessage());
                }
            }
            JSONObject response = new JSONObject();
            response.put("event", "signal_sent");
            response.put("signalName", signalName);
            response.put("success", true);
            response.put("method", method);
            if (conn != null && conn.isOpen()) {
                conn.send(response.toString());
            }
            recordReplayEvent("{\"time\":" + replayTime() + ",\"event\":\"signal_sent\",\"signalName\":\"" + escapeJson(signalName) + "\",\"method\":\"" + escapeJson(method) + "\"}");
        } catch (Exception e) {
            err("[SimSync] 发送信号失败: " + e.getMessage());
            e.printStackTrace();
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("event", "signal_sent");
            errorResponse.put("signalName", signalName);
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            if (conn != null && conn.isOpen()) {
                conn.send(errorResponse.toString());
            }
        }
    }

    private Activity findActivityByName(Project project, String name) {
        if (project == null || name == null) return null;
        return findActivityByNameRecursive(project.getPrimaryModel(), name);
    }

    private Activity findActivityByNameRecursive(Element element, String name) {
        if (element == null) return null;
        if (element instanceof Activity) {
            Activity activity = (Activity) element;
            String activityName = activity.getName();
            if (name.equals(activityName)) return activity;
            String qn = activity.getQualifiedName();
            if (qn != null && qn.equals(name)) return activity;
        }
        try {
            Collection<Element> children = element.getOwnedElement();
            if (children != null) {
                for (Element child : children) {
                    Activity found = findActivityByNameRecursive(child, name);
                    if (found != null) return found;
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private Object findOpaqueBehaviorByName(Project project, String name) {
        if (project == null || name == null) return null;
        return findOpaqueBehaviorByNameRecursive(project.getPrimaryModel(), name);
    }

    private Object findOpaqueBehaviorByNameRecursive(Element element, String name) {
        if (element == null) return null;
        String className = element.getClass().getSimpleName();
        if (className.contains("OpaqueBehavior") || className.contains("Behavior")) {
            String elementName = getElementName(element);
            if (name.equals(elementName)) return element;
        }
        try {
            Collection<Element> children = element.getOwnedElement();
            if (children != null) {
                for (Element child : children) {
                    Object found = findOpaqueBehaviorByNameRecursive(child, name);
                    if (found != null) return found;
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }
}
