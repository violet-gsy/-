package myplugin;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.simulation.SimulationManager;
import com.nomagic.magicdraw.simulation.execution.SimulationExecution;
import com.nomagic.magicdraw.simulation.execution.SimulationExecutionListener;
import com.nomagic.magicdraw.simulation.utils.ALH;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import java.util.Collection;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import fUML.Semantics.Classes.Kernel.StructuredValue;

/**
 * 监听 MagicDraw 仿真运行过程中的事件，然后把这些事件通过 SimulationSyncServer 广播给前端页面
 *
 * 功能：
 * 1. 仿真开始 / 结束时通知前端
 * 2. 活动图节点激活 / 停用时通知前端高亮或取消高亮
 * 3. 状态机状态进入 / 退出时通知前端高亮或取消高亮
 * 4. 识别特定节点，触发"航天器外部交互请求"和"外部结果回写"
 *
 * @author MagicDraw Plugin Team
 */
public class SimulationEventBroadcaster extends SimulationExecutionListener {

    private static SimulationEventBroadcaster INSTANCE;
    private final GUILog log;

    // 要识别的外部交互节点名，必须和活动图节点名称完全一致
    private static final String SPACECRAFT_REQUEST_NODE = "发送航天器检查请求";
    private static final String APPLY_SPACECRAFT_RESULT_NODE = "应用外部返回参数";
    private static final String GENERATE_SPACECRAFT_RESULT_NODE = "生成航天器健康评估结果";
    private static final String SPACECRAFT_RESULT_NODE = "航天器健康评估结果";
    private static final String CHECK_SUMMARY_NODE = "检查结果汇总";

    private SimulationEventBroadcaster() {
        this.log = Application.getInstance().getGUILog();
    }

    private static void info(String msg) {
        Application.getInstance().getGUILog().log("[INFO] " + msg);
    }

    private static void err(String msg) {
        Application.getInstance().getGUILog().log("[ERROR] " + msg);
    }

    public static void register() {
        info("[SimSync] register() 方法被调用");

        if (INSTANCE == null) {
            INSTANCE = new SimulationEventBroadcaster();

            try {
                SimulationManager.registerSimulationExecutionListener(INSTANCE);
                info("[SimSync] 监听器注册成功！");
            } catch (Exception e) {
                err("[SimSync] 注册失败: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            info("[SimSync] 监听器已存在");
        }
    }

    @Override
    public void executionStarted(SimulationExecution execution) {
        info("[SimSync] >>> 仿真开始！<<<");

        try {
            SimulationSyncServer.getInstance().setCurrentSession(execution.getMainSession());
        } catch (Throwable t) {
            err("[SimSync] 记录仿真会话失败: " + t.getMessage());
        }

        SimulationSyncServer.getInstance().broadcastSimStart();
    }

    @Override
    public void executionTerminated(SimulationExecution execution) {
        info("[SimSync] ⏹ 仿真终止");

        // 先发送最终节点状态和完整方案汇总，再清空当前会话。
        // 这样前端能够在 sim_end 之前稳定收到 totalDuration、dailyResources、tasks。
        SimulationSyncServer.getInstance().broadcastSimEnd();

        SimulationSyncServer.getInstance().setCurrentSession(null);
        SimulationSyncServer.getInstance().broadcast("{\"event\":\"clear_all\"}");
    }

    @Override
    public void elementActivated(Element element, Collection<?> values) {
        if (element == null) return;

        String elementClassName = element.getClass().getSimpleName();
        String elementName = element instanceof com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement
                ? ((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement) element).getName()
                : "";
        String elementId = element.getID();

        info("[SimSync] elementActivated: " + elementClassName + " / " + elementName + " / " + elementId);

        // ===== 1. 处理活动图节点 =====
        if (isActivityNode(element)) {
            handleActivityNodeActivation(element, elementId, elementName, values);
        }

        // ===== 2. 处理状态机状态 =====
        if (isState(element)) {
            handleStateActivation(element, elementId, elementName, values);
        }

        // ===== 3. 处理状态机伪状态 (初始/终止) =====
        if (isPseudostate(element)) {
            handlePseudostateActivation(element, elementId, elementName, values);
        }
    }

    @Override
    public void elementDeactivated(Element element, Collection<?> values) {
        if (element == null) return;

        String elementClassName = element.getClass().getSimpleName();
        String elementName = element instanceof com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement
                ? ((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement) element).getName()
                : "";
        String elementId = element.getID();

        info("[SimSync] elementDeactivated: " + elementClassName + " / " + elementName + " / " + elementId);

        // ===== 1. 处理活动图节点 =====
        if (isActivityNode(element)) {
            SimulationSyncServer.getInstance().broadcastNodeDeactivate(elementId);
        }

        // ===== 2. 处理状态机状态 =====
        if (isState(element)) {
            handleStateDeactivation(element, elementId, elementName, values);
        }
    }

    /**
     * 判断是否为活动图节点
     */
    private boolean isActivityNode(Element element) {
        if (element == null) return false;
        String className = element.getClass().getName();
        return className.contains("ActivityNode") ||
                className.contains("Action") ||
                className.contains("ActivityFinalNode") ||
                className.contains("InitialNode") ||
                className.contains("ForkNode") ||
                className.contains("JoinNode") ||
                className.contains("DecisionNode") ||
                className.contains("MergeNode") ||
                className.contains("OpaqueAction");
    }

    /**
     * 判断是否为状态机状态
     */
    private boolean isState(Element element) {
        if (element == null) return false;
        String className = element.getClass().getSimpleName();
        return "State".equals(className) || className.contains("State") && !className.contains("StateMachine");
    }

    /**
     * 判断是否为状态机伪状态 (初始/终止)
     */
    private boolean isPseudostate(Element element) {
        if (element == null) return false;
        String className = element.getClass().getSimpleName();
        return "Pseudostate".equals(className) || className.contains("Pseudostate");
    }

    /**
     * 处理活动图节点激活
     */
    private void handleActivityNodeActivation(Element element, String id, String name, Collection<?> values) {
        // 广播节点激活
        SimulationSyncServer.getInstance().broadcastNodeActivate(id, name);

        // 如果当前激活节点是"发送航天器检查请求"，就向外部页面发送 JSON
        if (SPACECRAFT_REQUEST_NODE.equals(name)) {
            info("[ExternalInteraction] 命中节点：" + name);
            broadcastSpacecraftExternalRequest(id, name, element.getClass().getSimpleName());
        }

        if (APPLY_SPACECRAFT_RESULT_NODE.equals(name)
                || GENERATE_SPACECRAFT_RESULT_NODE.equals(name)
                || SPACECRAFT_RESULT_NODE.equals(name)
                || CHECK_SUMMARY_NODE.equals(name)) {
            applySpacecraftExternalResult(values, name);
        }
    }

    /**
     * 处理状态机状态激活 (进入状态)
     */
    private void handleStateActivation(Element element, String id, String name, Collection<?> values) {
        String stateName = (name != null && !name.trim().isEmpty()) ? name : "未命名状态";

        // 广播状态激活 - 使用 state_entered 事件
        SimulationSyncServer.getInstance().broadcast(
                "{\"event\":\"state_entered\","
                        + "\"nodeId\":\"" + escapeJson(id) + "\","
                        + "\"nodeName\":\"" + escapeJson(stateName) + "\","
                        + "\"type\":\"state\""
                        + "}"
        );

        // 同时使用通用节点激活事件 (用于高亮)
        SimulationSyncServer.getInstance().broadcastNodeActivate(id, stateName);

        // 检查是否有 entry 行为
        checkAndExecuteStateBehavior(element, "entry", values);

        info("[StateMachine] 状态进入: " + stateName + " (ID: " + id + ")");
    }

    /**
     * 处理状态机状态退出
     */
    private void handleStateDeactivation(Element element, String id, String name, Collection<?> values) {
        String stateName = (name != null && !name.trim().isEmpty()) ? name : "未命名状态";

        // 检查是否有 exit 行为
        checkAndExecuteStateBehavior(element, "exit", values);

        // 广播状态退出
        SimulationSyncServer.getInstance().broadcast(
                "{\"event\":\"state_exited\","
                        + "\"nodeId\":\"" + escapeJson(id) + "\","
                        + "\"nodeName\":\"" + escapeJson(stateName) + "\","
                        + "\"type\":\"state\""
                        + "}"
        );

        // 同时使用通用节点停用事件
        SimulationSyncServer.getInstance().broadcastNodeDeactivate(id);

        info("[StateMachine] 状态退出: " + stateName + " (ID: " + id + ")");
    }

    /**
     * 处理伪状态激活 (初始/终止)
     */
    private void handlePseudostateActivation(Element element, String id, String name, Collection<?> values) {
        String pseudoName = (name != null && !name.trim().isEmpty()) ? name : "伪状态";

        // 检查是否为初始节点
        boolean isInitial = false;
        try {
            java.lang.reflect.Method getKindMethod = element.getClass().getMethod("getKind");
            Object kind = getKindMethod.invoke(element);
            if (kind != null) {
                String kindStr = kind.toString();
                isInitial = kindStr.contains("INITIAL") || kindStr.contains("initial");
            }
        } catch (Throwable ignored) {}

        String eventType = isInitial ? "initial_state" : "final_state";
        SimulationSyncServer.getInstance().broadcast(
                "{\"event\":\"pseudostate_activated\","
                        + "\"nodeId\":\"" + escapeJson(id) + "\","
                        + "\"nodeName\":\"" + escapeJson(pseudoName) + "\","
                        + "\"type\":\"" + eventType + "\""
                        + "}"
        );

        // 使用通用节点激活事件
        SimulationSyncServer.getInstance().broadcastNodeActivate(id, pseudoName);

        info("[StateMachine] 伪状态激活: " + pseudoName + " (ID: " + id + ", isInitial=" + isInitial + ")");
    }

    /**
     * 检查并执行状态内部行为
     */
    private void checkAndExecuteStateBehavior(Element stateElement, String behaviorType, Collection<?> values) {
        if (stateElement == null) return;

        try {
            Object behavior = null;
            if ("entry".equals(behaviorType)) {
                java.lang.reflect.Method getEntryMethod = stateElement.getClass().getMethod("getEntry");
                behavior = getEntryMethod.invoke(stateElement);
            } else if ("do".equals(behaviorType)) {
                java.lang.reflect.Method getDoActivityMethod = stateElement.getClass().getMethod("getDoActivity");
                behavior = getDoActivityMethod.invoke(stateElement);
            } else if ("exit".equals(behaviorType)) {
                java.lang.reflect.Method getExitMethod = stateElement.getClass().getMethod("getExit");
                behavior = getExitMethod.invoke(stateElement);
            }

            if (behavior != null) {
                String behaviorName = getBehaviorName(behavior);
                if (behaviorName != null && !behaviorName.trim().isEmpty()) {
                    SimulationSyncServer.getInstance().broadcast(
                            "{\"event\":\"state_behavior_executed\","
                                    + "\"behaviorType\":\"" + behaviorType + "\","
                                    + "\"behaviorName\":\"" + escapeJson(behaviorName) + "\""
                                    + "}"
                    );
                    info("[StateMachine] 状态行为执行: " + behaviorType + " -> " + behaviorName);
                }
            }
        } catch (Throwable ignored) {}
    }

    /**
     * 获取行为名称
     */
    private String getBehaviorName(Object behavior) {
        if (behavior == null) return null;
        try {
            java.lang.reflect.Method getNameMethod = behavior.getClass().getMethod("getName");
            Object name = getNameMethod.invoke(behavior);
            if (name != null) {
                return name.toString();
            }
        } catch (Throwable ignored) {}
        return behavior.getClass().getSimpleName();
    }

    /**
     * 发送航天器外部交互请求
     */
    private void broadcastSpacecraftExternalRequest(String id, String name, String className) {
        String json = SimulationSyncServer.getInstance()
                .buildSpacecraftRequestJson(id, name, className);

        log.log("[ExternalInteraction] 发送航天器外部交互请求: " + json);

        SimulationSyncServer.getInstance().broadcast(json);
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private void applySpacecraftExternalResult(Collection<?> values, String nodeName) {
        boolean changedByValues = SimulationSyncServer.getInstance()
                .applySpacecraftResultToRuntimeValues(values);

        boolean changedByRoot = SimulationSyncServer.getInstance()
                .applySpacecraftResultToRootContext();

        boolean changed = changedByValues || changedByRoot;
        SimulationSyncServer.getInstance().broadcast(
                "{\"event\":\"external_result_applied\","
                        + "\"type\":\"spacecraft_check_result\","
                        + "\"nodeName\":\"" + escapeJson(nodeName) + "\","
                        + "\"changed\":" + changed + "}"
        );

        log.log("[ExternalInteraction] 节点 " + nodeName
                + " 已尝试应用外部返回参数，changed=" + changed);

        if (CHECK_SUMMARY_NODE.equals(nodeName)) {
            SimulationSyncServer.getInstance().clearLastSpacecraftResultJson();
        }
    }
}
