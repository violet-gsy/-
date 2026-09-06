package myplugin;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.Activity;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;

public class OperatorResourceManager {

    private static final Object LOCK = new Object();

    private static final String GV_TOTAL_OPERATORS = "GV_TOTAL_OPERATORS";
    private static final String GV_AVAILABLE_OPERATORS = "GV_AVAILABLE_OPERATORS";
    private static final String GV_TIME_SCALE_MS = "GV_TIME_SCALE_MS";

    /*
     * ★ 同一次 MagicDraw execution 的资源池初始化保护标记。
     *
     * Global Variable 属于当前 execution，并且同一 execution 下的子 Simulation Session
     * 都能够访问。因此：
     *
     * - 主流程第一次执行 initResourcePool(...)：正常初始化，并写入 true；
     * - 后续子流程里的“初始化”节点再次执行 initResourcePool(...)：检测到 true，直接忽略；
     * - 下一次重新启动一个新的 execution：该变量不存在，第一次初始化又会正常执行。
     *
     * 这样可以防止子流程里的 initResourcePool(10/35/...) 把主流程已经设置好的资源池
     * 和前面累计的业务时间统计全部重置。
     */
    private static final String GV_RESOURCE_POOL_INITIALIZED =
            "GV_RESOURCE_POOL_INITIALIZED";

    /*
     * 真实时间只用于“播放速度 / Thread.sleep / 调试”。
     *
     * ★ 绝对不要再用：
     *   (System.currentTimeMillis() - processStartRealMs) / timeScaleMs
     * 来计算业务工期。
     *
     * 否则页面停留、WebSocket、进入子流程、人工等待等真实耗时都会被误算成“天”。
     */
    private static long processStartRealMs = 0L;

    /*
     * 1 个业务仿真日对应多少真实毫秒。
     * 例如 100 ms/天，只决定电脑上动画跑多快，不决定总工期。
     */
    private static double timeScaleMs = 200.0;

    /*
     * 每次 initResourcePool/resetStatistics 都会递增。
     * ThreadLocal 中保存 generation，避免线程池复用造成上一轮仿真时间串入下一轮。
     */
    private static long simulationGeneration = 0L;

    /*
     * FIFO 等待队列：
     * 谁先进入等待队列，谁先申请真实资源。
     *
     * 注意：
     * 真实线程仍然按这个队列阻塞；
     * 但业务 start/finish 时间使用下面的“逻辑排程”计算，不再使用电脑时钟。
     */
    private static final LinkedList<String> waitingQueue = new LinkedList<>();

    /*
     * 当任务因为资源不足进入 FIFO 时，记录它前面的任务。
     * 这样逻辑排程也能保持 FIFO：后来的任务不能在前一个等待任务的逻辑开始时间之前启动。
     */
    private static final Map<String, String> fifoPredecessorMap = new LinkedHashMap<>();

    /*
     * 真实时间仅保留作诊断，不再参与业务时间计算。
     */
    private static final Map<String, Long> readyRealMsMap = new LinkedHashMap<>();
    private static final Map<String, Long> startRealMsMap = new LinkedHashMap<>();

    /*
     * ★★★ 业务逻辑时间（单位：天） ★★★
     *
     * readyTime：
     *   由模型前驱 ResourceTask 的 finishTime 推导；
     *   普通节点没有 duration，因此不会推进业务时间。
     *
     * startTime：
     *   从 readyTime 开始，结合总资源容量 + FIFO 找到最早可执行时间。
     *
     * finishTime：
     *   startTime + duration。
     *
     * plannedFinishSimTimeMap：
     *   节点一旦真正申请到资源，就提前确定它的逻辑 finish；
     *   但只有真实执行结束 release() 时才写入 finishSimTimeMap，
     *   从而前端状态仍能区分 running / done。
     */
    private static final Map<String, Double> readySimTimeMap = new LinkedHashMap<>();
    private static final Map<String, Double> startSimTimeMap = new LinkedHashMap<>();
    private static final Map<String, Double> plannedFinishSimTimeMap = new LinkedHashMap<>();
    private static final Map<String, Double> finishSimTimeMap = new LinkedHashMap<>();
    private static final Map<String, Double> waitSimTimeMap = new LinkedHashMap<>();

    private static final Map<String, Integer> requiredPeopleMap = new LinkedHashMap<>();
    private static final Map<String, Double> durationMap = new LinkedHashMap<>();

    /*
     * ResourceTask 名称 <-> MagicDraw Element。
     * 主要用于从当前节点沿 Activity incoming 边向前查找业务前驱。
     */
    private static final Map<String, Element> taskElementMap = new LinkedHashMap<>();
    private static final Map<String, String> taskElementIdMap = new LinkedHashMap<>();
    private static final Map<String, Double> finishSimTimeByElementId = new LinkedHashMap<>();

    /*
     * 子流程支持：
     * behavior(Activity) ID -> 调用这个 Activity 的 CallBehaviorAction。
     * 第一次需要时懒加载，避免每个 ResourceTask 都全模型扫描。
     */
    private static final Map<String, List<Element>> callSitesByBehaviorId = new LinkedHashMap<>();
    private static boolean callSiteIndexBuilt = false;

    /*
     * 同一执行线程的逻辑时间兜底。
     *
     * 正常情况下 readyTime 优先由模型 incoming 关系推导；
     * 只有子流程边界等无法直接沿 incoming 找到前驱时才使用 ThreadLocal。
     */
    private static final ThreadLocal<ThreadLogicalClock> THREAD_LOGICAL_CLOCK =
            new ThreadLocal<>();

    /*
     * 当前资源池快照。
     * 主要用于仿真结束后生成“方案结果汇总”，避免没有 ALH 时拿不到资源池总量。
     */
    private static int currentTotalOperators = 0;
    private static int currentAvailableOperators = 0;

    /*
     * 本系统约定：
     * duration 的 1.0 = 1 天
     * requiredPeople = 该任务执行期间持续占用的资源数量
     */
    private static final String SIM_TIME_UNIT = "day";
    private static final double EPSILON = 1e-9;

    // ========== 配置缓存 ==========
    private static final Map<String, ResourceTaskConfig> configCache = new ConcurrentHashMap<>();

    // ========== WebSocket 通信接口 ==========

    /**
     * 统计数据发送器接口
     */
    public interface StatsSender {
        void sendNodeStats(String taskName, Map<String, Object> stats);
        void sendLog(String message, String level, String time);
        void sendResourcePoolStatus(int total, int available, int waitingQueueSize);
    }

    private static StatsSender statsSender = null;

    /**
     * 设置统计数据发送器（由 WebSocket 服务端调用）
     */
    public static void setStatsSender(StatsSender sender) {
        statsSender = sender;
    }

    private static class ResourceTaskConfig {
        int requiredPeople;
        double duration;

        ResourceTaskConfig(int requiredPeople, double duration) {
            this.requiredPeople = requiredPeople;
            this.duration = duration;
        }
    }

    private static class ThreadLogicalClock {
        long generation;
        double time;

        ThreadLogicalClock(long generation, double time) {
            this.generation = generation;
            this.time = time;
        }
    }

    private static void log(String msg) {
        Application.getInstance().getGUILog().log(msg);

        // 同时发送到 WebSocket 前端
        if (statsSender != null) {
            String time = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
            statsSender.sendLog(msg, "info", time);
        }
    }

    private static int toInt(Object value) {
        if (value == null) {
            return 0;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        String text = String.valueOf(value).trim();

        if (text.isEmpty() || "null".equalsIgnoreCase(text)) {
            return 0;
        }

        return (int) Double.parseDouble(text);
    }

    private static double toDouble(Object value) {
        if (value == null) {
            return 0.0;
        }

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        String text = String.valueOf(value).trim();

        if (text.isEmpty() || "null".equalsIgnoreCase(text)) {
            return 0.0;
        }

        return Double.parseDouble(text);
    }

    /**
     * 将 Cameo / ALH 返回的全局变量值转换为 boolean。
     * 兼容 Boolean、Number、字符串，以及某些运行时包装成 [true] 的文本形式。
     */
    private static boolean toBoolean(Object value) {
        if (value == null) {
            return false;
        }

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }

        if (value instanceof Collection) {
            Collection<?> values = (Collection<?>) value;
            if (values.isEmpty()) {
                return false;
            }
            return toBoolean(values.iterator().next());
        }

        String text = String.valueOf(value).trim();

        if (text.startsWith("[") && text.endsWith("]") && text.length() >= 2) {
            text = text.substring(1, text.length() - 1).trim();
        }

        return "true".equalsIgnoreCase(text)
                || "1".equals(text)
                || "yes".equalsIgnoreCase(text);
    }

    private static String fmt(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    /**
     * 仅供诊断真实播放时间使用。
     *
     * ★ 不能用于 ready/start/finish/总工期。
     */
    private static double realMsToPlaybackDays(long realMs) {
        if (timeScaleMs <= 0) {
            return 0.0;
        }
        return realMs / timeScaleMs;
    }

    private static Method findMethod(Object obj, String methodName, Class<?>... parameterTypes) throws Exception {
        Class<?> cls = obj.getClass();

        while (cls != null) {
            try {
                Method method = cls.getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException e) {
                cls = cls.getSuperclass();
            }
        }

        throw new NoSuchMethodException(methodName);
    }

    private static Object getGlobalVariable(Object alh, String name) {
        try {
            Method method = findMethod(alh, "getGlobalVariable", String.class);
            return method.invoke(alh, name);
        } catch (Exception e) {
            log("[资源管理器] 读取全局变量失败：" + name + "，原因：" + e.getMessage());
            return null;
        }
    }

    private static void setGlobalVariable(Object alh, String name, Object value) {
        try {
            Method method = findMethod(alh, "setGlobalVariable", String.class, Object.class);
            method.invoke(alh, name, value);
        } catch (Exception e) {
            log("[资源管理器] 写入全局变量失败：" + name + "，原因：" + e.getMessage());
        }
    }

    /*
     * 初始化资源池。
     *
     * 初始化节点中仍然直接调用：
     * Packages.myplugin.OperatorResourceManager.initResourcePool(ALH, 5, 200);
     *
     * ★ 重要修改：
     * 同一次 MagicDraw execution 中只允许第一次调用真正初始化。
     * 后续子流程里即使仍然存在 initResourcePool(10/35/...)，
     * 也只打印“忽略重复初始化”，不会：
     *
     * 1. 覆盖当前资源池人数；
     * 2. 覆盖网页已经修改后的资源池人数；
     * 3. 清空 ready/start/finish；
     * 4. 把业务逻辑时间重新归零。
     */
    public static void initResourcePool(
            Object alh,
            int totalOperators,
            double scaleMs) {

        synchronized (LOCK) {

            /*
             * Global Variable 是 execution 级共享的。
             * 主流程、子流程以及同一 execution 的不同 Simulation Session
             * 都能看到这个标记。
             */
            boolean alreadyInitialized =
                    toBoolean(
                            getGlobalVariable(
                                    alh,
                                    GV_RESOURCE_POOL_INITIALIZED
                            )
                    );

            if (alreadyInitialized) {

                int currentTotal =
                        toInt(
                                getGlobalVariable(
                                        alh,
                                        GV_TOTAL_OPERATORS
                                )
                        );

                int currentAvailable =
                        toInt(
                                getGlobalVariable(
                                        alh,
                                        GV_AVAILABLE_OPERATORS
                                )
                        );

                double currentScale =
                        toDouble(
                                getGlobalVariable(
                                        alh,
                                        GV_TIME_SCALE_MS
                                )
                        );

                /*
                 * 同步一下 Java 内存快照，但绝不重置统计。
                 */
                currentTotalOperators = currentTotal;
                currentAvailableOperators = currentAvailable;

                if (currentScale > 0) {
                    OperatorResourceManager.timeScaleMs = currentScale;
                }

                log("[资源池] 忽略重复初始化：同一次 execution 已经初始化过资源池"
                        + "；本次节点请求 totalOperators=" + totalOperators
                        + "，scaleMs=" + scaleMs
                        + "；继续保持当前总资源=" + currentTotal
                        + "，当前可用=" + currentAvailable
                        + "，时间缩放=" + OperatorResourceManager.timeScaleMs
                        + " ms/天"
                        + "；已有业务时间统计不会被清空");

                sendResourcePoolStatus(alh);
                return;
            }

            /*
             * 只有当前 execution 的第一次 initResourcePool 才走到这里。
             */
            if (totalOperators < 0) {
                totalOperators = 0;
            }

            if (scaleMs <= 0) {
                scaleMs = 200.0;
            }

            setGlobalVariable(
                    alh,
                    GV_TOTAL_OPERATORS,
                    totalOperators
            );

            setGlobalVariable(
                    alh,
                    GV_AVAILABLE_OPERATORS,
                    totalOperators
            );

            setGlobalVariable(
                    alh,
                    GV_TIME_SCALE_MS,
                    scaleMs
            );

            /*
             * 最后写初始化标志。
             * 由于整个方法在 LOCK 中，同一 JVM 内并行初始化节点不会同时穿过这里。
             */
            setGlobalVariable(
                    alh,
                    GV_RESOURCE_POOL_INITIALIZED,
                    Boolean.TRUE
            );

            currentTotalOperators = totalOperators;
            currentAvailableOperators = totalOperators;

            /*
             * ★ 只在本次 execution 第一次初始化时清空统计。
             */
            resetStatisticsInternal(scaleMs);

            log("[资源池] 首次初始化完成"
                    + "，总操作人员 = " + totalOperators
                    + "，当前可用 = " + totalOperators
                    + "，时间缩放 = " + scaleMs + " ms/天"
                    + "；本次 execution 后续 initResourcePool 调用将被忽略");

            sendResourcePoolStatus(alh);
        }
    }

    /*
     * 如果你还想保留原来的初始化节点写法，
     * 可以在初始化节点里单独调用 resetStatistics(timeScaleMs)。
     */
    public static void resetStatistics(double scaleMs) {
        synchronized (LOCK) {
            resetStatisticsInternal(scaleMs);
        }
    }

    private static void resetStatisticsInternal(double scaleMs) {
        processStartRealMs = System.currentTimeMillis();

        if (scaleMs <= 0) {
            scaleMs = 200.0;
        }

        timeScaleMs = scaleMs;
        simulationGeneration++;

        waitingQueue.clear();
        fifoPredecessorMap.clear();

        readyRealMsMap.clear();
        startRealMsMap.clear();

        readySimTimeMap.clear();
        startSimTimeMap.clear();
        plannedFinishSimTimeMap.clear();
        finishSimTimeMap.clear();
        waitSimTimeMap.clear();

        requiredPeopleMap.clear();
        durationMap.clear();

        taskElementMap.clear();
        taskElementIdMap.clear();
        finishSimTimeByElementId.clear();

        callSitesByBehaviorId.clear();
        callSiteIndexBuilt = false;

        /*
         * 只能 remove 当前线程的 ThreadLocal；
         * 其他线程可能来自线程池，因此通过 simulationGeneration 自动判定为失效。
         */
        THREAD_LOGICAL_CLOCK.remove();

        log("[统计] 已重置资源队列和业务逻辑时间，流程逻辑开始时间 = 0 天"
                + "，播放缩放 = " + timeScaleMs + " ms/天"
                + "；duration 单位 = 天"
                + "；真实等待时间不再计入业务工期");
    }

    /*
     * 发送节点统计信息到前端
     */
    private static void sendNodeStatsUpdate(String taskName) {
        if (statsSender == null) {
            return;
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("taskName", taskName);
        stats.put("requiredPeople", requiredPeopleMap.getOrDefault(taskName, 0));
        stats.put("duration", durationMap.getOrDefault(taskName, 0.0));
        stats.put("timeUnit", SIM_TIME_UNIT);

        Double readyTime = readySimTimeMap.get(taskName);
        Double startTime = startSimTimeMap.get(taskName);
        Double finishTime = finishSimTimeMap.get(taskName);
        Double waitTime = waitSimTimeMap.get(taskName);

        if (readyTime != null) {
            stats.put("readyTime", fmt(readyTime));
        }
        if (startTime != null) {
            stats.put("startTime", fmt(startTime));
        }
        if (finishTime != null) {
            stats.put("finishTime", fmt(finishTime));
            if (readyTime != null) {
                stats.put("totalTime", fmt(finishTime - readyTime));
            }
        }
        if (waitTime != null) {
            stats.put("waitTime", fmt(waitTime));
        }

        // 判断节点状态
        if (finishTime != null) {
            stats.put("status", "done");
        } else if (startTime != null) {
            stats.put("status", "running");
        } else {
            stats.put("status", "waiting");
        }

        // 添加等待队列位置信息
        if ("waiting".equals(stats.get("status")) && waitingQueue.contains(taskName)) {
            stats.put("queuePosition", waitingQueue.indexOf(taskName) + 1);
            stats.put("queueSize", waitingQueue.size());
        }

        statsSender.sendNodeStats(taskName, stats);
    }

    /*
     * 发送资源池状态到前端
     */
    private static void sendResourcePoolStatus(Object alh) {
        if (statsSender == null) {
            return;
        }

        int total = toInt(getGlobalVariable(alh, GV_TOTAL_OPERATORS));
        int available = toInt(getGlobalVariable(alh, GV_AVAILABLE_OPERATORS));
        int queueSize;

        synchronized (LOCK) {
            currentTotalOperators = total;
            currentAvailableOperators = available;
            queueSize = waitingQueue.size();
        }

        statsSender.sendResourcePoolStatus(total, available, queueSize);
    }

    /**
     * 直接使用当前正在执行的模型元素执行资源任务。
     *
     * OpaqueAction Body 中调用：
     *
     * Packages.myplugin.OperatorResourceManager
     *     .runCurrentResourceTask(ALH, _element_);
     */
    public static void runCurrentResourceTask(
            Object alh,
            Object currentElement) {

        /*
         * _element_ 应该对应当前正在执行的模型 Element。
         * 这里先做类型检查，避免脚本传入异常对象。
         */
        if (!(currentElement instanceof Element)) {

            String actualType =
                    currentElement == null
                            ? "null"
                            : currentElement.getClass().getName();

            log("[资源管理器] 当前脚本元素不是 MagicDraw Element，实际类型 = "
                    + actualType);

            return;
        }

        Element taskElement =
                (Element) currentElement;

        /*
         * 名称以后只用于日志显示。
         * 不再使用名称定位模型元素。
         */
        String taskName =
                getElementName(taskElement);

        if (taskName == null
                || taskName.trim().isEmpty()) {

            taskName = "<未命名ResourceTask>";
        }

        /*
         * 直接从当前 Element 读取 ResourceTask。
         */
        ResourceTaskConfig config =
                readResourceTaskConfig(taskElement);

        if (config == null) {
            return;
        }

        int requiredPeople =
                config.requiredPeople;

        double duration =
                config.duration;

        if (requiredPeople < 0) {
            requiredPeople = 0;
        }

        if (duration < 0) {
            duration = 0.0;
        }

        /*
         * 从 Cameo 全局变量读取时间缩放。
         */
        double scaleFromGlobal =
                toDouble(
                        getGlobalVariable(
                                alh,
                                GV_TIME_SCALE_MS
                        )
                );

        if (scaleFromGlobal > 0) {

            synchronized (LOCK) {
                timeScaleMs = scaleFromGlobal;
            }
        }

        /*
         * 记录节点进入时间。
         */
        markTaskEnter(
                taskElement,
                taskName,
                requiredPeople,
                duration
        );

        /*
         * 阻塞式申请资源。
         */
        boolean acquired =
                acquireBlocking(
                        alh,
                        taskName,
                        requiredPeople
                );

        if (!acquired) {

            log("[" + taskName
                    + "] 未能申请资源，节点终止执行");

            return;
        }

        /*
         * duration 转换为真实 sleep 时间。
         */
        long sleepTime =
                Math.round(
                        duration * timeScaleMs
                );

        log("[" + taskName
                + "] 开始执行，duration = "
                + duration
                + " 天，实际 sleep = "
                + sleepTime
                + " ms");

        try {

            Thread.sleep(sleepTime);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            log("[" + taskName
                    + "] 执行过程中被中断，准备释放资源");

        } finally {

            /*
             * 释放资源并记录 finishTime。
             */
            release(
                    alh,
                    taskName,
                    requiredPeople
            );

            int availableOperators =
                    getAvailableOperators(alh);

            log("[" + taskName
                    + "] 节点结束，当前可用人员 = "
                    + availableOperators);
        }
    }
    /*
     * 兼容旧调用。
     *
     * 推荐 runCurrentResourceTask() 使用带 Element 的重载，
     * 因为只有拿到当前 MagicDraw Element 才能准确沿 incoming 关系计算 readyTime。
     */
    public static void markTaskEnter(
            String taskName,
            int requiredPeople,
            double duration) {

        markTaskEnter(
                null,
                taskName,
                requiredPeople,
                duration
        );
    }

    /**
     * 记录 ResourceTask 进入 ready 状态。
     *
     * ★ readyTime 不再来自电脑真实时间。
     *
     * 计算顺序：
     * 1. 沿当前 ActivityNode 的 incoming 边向前递归；
     * 2. 找到本轮已经执行过的上游 ResourceTask，取最大 finishTime；
     * 3. 遇到 CallBehaviorAction 时，可读取其子 Activity 已完成 ResourceTask 的最大 finish；
     * 4. 子 Activity 第一个 ResourceTask 会尝试回溯调用它的 CallBehaviorAction；
     * 5. 实在无法通过模型关系定位时，才用当前执行线程的逻辑时钟兜底；
     * 6. 如果什么都没有，说明它是流程起始阶段，readyTime = 0。
     */
    private static void markTaskEnter(
            Element taskElement,
            String taskName,
            int requiredPeople,
            double duration) {

        synchronized (LOCK) {

            if (taskName == null || taskName.trim().isEmpty()) {
                taskName = "<未命名ResourceTask>";
            }

            if (requiredPeople < 0) {
                requiredPeople = 0;
            }

            if (duration < 0) {
                duration = 0.0;
            }

            /*
             * 现有实现按 taskName 作为统计 key。
             * 同一轮仿真如果同名节点重复执行，仍沿用原系统“一名一条记录”的约定。
             */
            if (readySimTimeMap.containsKey(taskName)) {
                return;
            }

            long now = System.currentTimeMillis();
            readyRealMsMap.put(taskName, now);

            requiredPeopleMap.put(taskName, requiredPeople);
            durationMap.put(taskName, duration);

            if (taskElement != null) {
                taskElementMap.put(taskName, taskElement);

                try {
                    String elementId = taskElement.getID();
                    if (elementId != null && !elementId.trim().isEmpty()) {
                        taskElementIdMap.put(taskName, elementId);
                    }
                } catch (Throwable ignored) {
                }
            }

            double readySimTime =
                    determineLogicalReadyTime(taskElement);

            if (readySimTime < 0) {
                readySimTime = 0.0;
            }

            readySimTimeMap.put(
                    taskName,
                    readySimTime
            );

            log("[" + taskName + "] 进入节点"
                    + "，readyTime = " + fmt(readySimTime) + " 天"
                    + "，需要人员 = " + requiredPeople
                    + "，duration = " + duration + " 天"
                    + "（业务时间不使用电脑真实耗时）");

            sendNodeStatsUpdate(taskName);
        }
    }

    /**
     * 计算当前 ResourceTask 的逻辑 readyTime。
     */
    private static double determineLogicalReadyTime(
            Element taskElement) {

        /*
         * 第一优先级：模型控制流前驱。
         */
        if (taskElement != null) {
            try {
                Double upstream =
                        findMaxUpstreamFinish(
                                taskElement,
                                new HashSet<String>(),
                                0
                        );

                if (upstream != null) {
                    return Math.max(0.0, upstream);
                }
            } catch (Throwable t) {
                log("[逻辑时间] 沿模型前驱计算 readyTime 失败："
                        + t.getMessage());
            }
        }

        /*
         * 第二优先级：当前执行线程的逻辑时钟。
         * 对同步调用子流程很有用。
         */
        Double threadTime =
                getCurrentThreadLogicalTime();

        if (threadTime != null) {
            return Math.max(0.0, threadTime);
        }

        /*
         * 没有任何业务前驱，视为流程逻辑 0 天。
         */
        return 0.0;
    }

    /**
     * 从 node 的 incoming 控制流向前递归，
     * 找到当前轮仿真已经有业务时间记录的上游 ResourceTask。
     */
    private static Double findMaxUpstreamFinish(
            Element node,
            Set<String> visited,
            int depth) {

        if (node == null || depth > 120) {
            return null;
        }

        String visitKey = buildElementVisitKey(node);

        if (!visited.add(visitKey)) {
            return null;
        }

        Double max = null;
        boolean hasIncoming = false;

        Object incomingObj =
                invokeNoArgQuiet(
                        node,
                        "getIncoming"
                );

        if (incomingObj instanceof Collection) {

            for (Object edge : (Collection<?>) incomingObj) {

                if (edge == null) {
                    continue;
                }

                Object sourceObj =
                        invokeNoArgQuiet(
                                edge,
                                "getSource"
                        );

                if (!(sourceObj instanceof Element)) {
                    continue;
                }

                hasIncoming = true;

                Double candidate =
                        resolveLogicalFinishThroughElement(
                                (Element) sourceObj,
                                visited,
                                depth + 1
                        );

                max = maxNullable(
                        max,
                        candidate
                );
            }
        }

        if (max != null) {
            return max;
        }

        /*
         * 当前 Activity 内部没有找到已经记录的 ResourceTask。
         *
         * 如果当前节点属于一个被 CallBehaviorAction 调用的子 Activity，
         * 则继续从“调用这个 Activity 的 Action”的前驱向前找。
         */
        Activity ownerActivity =
                findOwningActivity(node);

        if (ownerActivity != null) {

            List<Element> callers =
                    findCallSitesForBehavior(
                            ownerActivity
                    );

            for (Element caller : callers) {

                /*
                 * 注意这里直接查 caller 的 incoming，
                 * 不能再次把 caller.getBehavior()（也就是当前 Activity）
                 * 当作“已完成子流程”处理，否则会形成自循环。
                 */
                Double candidate =
                        findMaxUpstreamFinish(
                                caller,
                                visited,
                                depth + 1
                        );

                max = maxNullable(
                        max,
                        candidate
                );
            }
        }

        return max;
    }

    /**
     * 解析一个上游模型元素对当前节点 readyTime 的贡献。
     */
    private static Double resolveLogicalFinishThroughElement(
            Element element,
            Set<String> visited,
            int depth) {

        if (element == null) {
            return null;
        }

        /*
         * 1. 这个元素本身就是本轮执行过的 ResourceTask。
         */
        String elementId = safeElementId(element);

        if (!elementId.isEmpty()) {
            Double finish =
                    finishSimTimeByElementId.get(
                            elementId
                    );

            if (finish != null) {
                return finish;
            }
        }

        /*
         * 2. 上游元素是 CallBehaviorAction：
         *    父流程只有在子 Activity 返回后才会继续，
         *    因此其逻辑完成时间 = 子 Activity 中本轮已完成 ResourceTask 的最大 finish。
         */
        Object behaviorObj =
                invokeNoArgQuiet(
                        element,
                        "getBehavior"
                );

        if (behaviorObj instanceof Element) {

            Double behaviorFinish =
                    getMaxFinishedTaskInsideBehavior(
                            (Element) behaviorObj
                    );

            if (behaviorFinish != null) {
                return behaviorFinish;
            }
        }

        /*
         * 3. 普通 Decision/Fork/Join/Merge/普通 Action 等没有 duration，
         *    继续沿它自己的 incoming 向前找。
         */
        return findMaxUpstreamFinish(
                element,
                visited,
                depth + 1
        );
    }

    /**
     * 找到某个 Behavior/Activity 内本轮已经完成的 ResourceTask 最大 finishTime。
     */
    private static Double getMaxFinishedTaskInsideBehavior(
            Element behavior) {

        if (behavior == null) {
            return null;
        }

        Double max = null;

        for (Map.Entry<String, Element> entry
                : taskElementMap.entrySet()) {

            Element taskElement =
                    entry.getValue();

            if (taskElement == null) {
                continue;
            }

            if (!isOwnedBy(
                    taskElement,
                    behavior
            )) {
                continue;
            }

            Double finish =
                    finishSimTimeMap.get(
                            entry.getKey()
                    );

            max = maxNullable(
                    max,
                    finish
            );
        }

        return max;
    }

    private static boolean isOwnedBy(
            Element element,
            Element ancestor) {

        if (element == null || ancestor == null) {
            return false;
        }

        Element current = element;

        while (current != null) {

            if (sameElement(
                    current,
                    ancestor
            )) {
                return true;
            }

            try {
                current = current.getOwner();
            } catch (Throwable t) {
                return false;
            }
        }

        return false;
    }

    private static Activity findOwningActivity(
            Element element) {

        if (element == null) {
            return null;
        }

        Element current = element;

        while (current != null) {

            if (current instanceof Activity) {
                return (Activity) current;
            }

            try {
                current = current.getOwner();
            } catch (Throwable t) {
                return null;
            }
        }

        return null;
    }

    /**
     * 查找调用某个 Activity/Behavior 的 CallBehaviorAction。
     */
    private static List<Element> findCallSitesForBehavior(
            Element behavior) {

        if (behavior == null) {
            return Collections.emptyList();
        }

        ensureCallSiteIndex();

        String behaviorId =
                safeElementId(behavior);

        if (behaviorId.isEmpty()) {
            return Collections.emptyList();
        }

        List<Element> result =
                callSitesByBehaviorId.get(
                        behaviorId
                );

        if (result == null) {
            return Collections.emptyList();
        }

        return result;
    }

    private static void ensureCallSiteIndex() {

        if (callSiteIndexBuilt) {
            return;
        }

        callSiteIndexBuilt = true;
        callSitesByBehaviorId.clear();

        Project project =
                Application.getInstance().getProject();

        if (project == null
                || project.getPrimaryModel() == null) {
            return;
        }

        indexCallSitesRecursive(
                project.getPrimaryModel()
        );
    }

    private static void indexCallSitesRecursive(
            Element element) {

        if (element == null) {
            return;
        }

        try {
            String className =
                    element.getClass()
                            .getSimpleName();

            if (className.contains(
                    "CallBehaviorAction")) {

                Object behaviorObj =
                        invokeNoArgQuiet(
                                element,
                                "getBehavior"
                        );

                if (behaviorObj instanceof Element) {

                    String behaviorId =
                            safeElementId(
                                    (Element) behaviorObj
                            );

                    if (!behaviorId.isEmpty()) {

                        callSitesByBehaviorId
                                .computeIfAbsent(
                                        behaviorId,
                                        k -> new ArrayList<Element>()
                                )
                                .add(element);
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        try {
            Collection<Element> children =
                    element.getOwnedElement();

            if (children != null) {
                for (Element child : children) {
                    indexCallSitesRecursive(child);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static Object invokeNoArgQuiet(
            Object obj,
            String methodName) {

        if (obj == null || methodName == null) {
            return null;
        }

        Class<?> cls = obj.getClass();

        while (cls != null) {
            try {
                Method method =
                        cls.getDeclaredMethod(
                                methodName
                        );

                method.setAccessible(true);

                return method.invoke(obj);

            } catch (NoSuchMethodException e) {
                cls = cls.getSuperclass();

            } catch (Throwable t) {
                return null;
            }
        }

        return null;
    }

    private static String buildElementVisitKey(
            Element element) {

        String id =
                safeElementId(element);

        if (!id.isEmpty()) {
            return id;
        }

        return "identity_"
                + System.identityHashCode(
                element
        );
    }

    private static String safeElementId(
            Element element) {

        if (element == null) {
            return "";
        }

        try {
            String id = element.getID();
            return id == null
                    ? ""
                    : id;
        } catch (Throwable t) {
            return "";
        }
    }

    private static boolean sameElement(
            Object a,
            Object b) {

        if (a == b) {
            return true;
        }

        if (!(a instanceof Element)
                || !(b instanceof Element)) {
            return false;
        }

        String aId =
                safeElementId(
                        (Element) a
                );

        String bId =
                safeElementId(
                        (Element) b
                );

        return !aId.isEmpty()
                && aId.equals(bId);
    }

    private static Double maxNullable(
            Double a,
            Double b) {

        if (a == null) {
            return b;
        }

        if (b == null) {
            return a;
        }

        return Math.max(a, b);
    }

    private static Double getCurrentThreadLogicalTime() {

        ThreadLogicalClock clock =
                THREAD_LOGICAL_CLOCK.get();

        if (clock == null
                || clock.generation
                != simulationGeneration) {
            return null;
        }

        return clock.time;
    }

    private static void setCurrentThreadLogicalTime(
            double time) {

        THREAD_LOGICAL_CLOCK.set(
                new ThreadLogicalClock(
                        simulationGeneration,
                        Math.max(0.0, time)
                )
        );
    }

    /*
     * 阻塞式申请资源。
     *
     * 这里实现了 FIFO：
     * 1. 队列为空且资源够，直接执行；
     * 2. 资源不够，加入等待队列；
     * 3. 队列不为空时，只有队首任务可以申请资源；
     * 4. release 后 notifyAll，等待任务重新检查资源。
     */
    private static boolean acquireBlocking(Object alh, String taskName, int cost) {
        synchronized (LOCK) {
            int total = toInt(getGlobalVariable(alh, GV_TOTAL_OPERATORS));

            if (cost <= 0) {
                recordStartTime(taskName, cost, toInt(getGlobalVariable(alh, GV_AVAILABLE_OPERATORS)));
                log("[" + taskName + "] 不需要人员资源，直接开始执行");
                return true;
            }

            if (total > 0 && cost > total) {
                log("[" + taskName + "] 申请失败：需要 " + cost
                        + " 人，但资源池总人数只有 " + total + " 人");
                return false;
            }

            while (true) {
                int current = toInt(getGlobalVariable(alh, GV_AVAILABLE_OPERATORS));

                /*
                 * 队列为空，并且资源足够，直接申请成功。
                 */
                if (waitingQueue.isEmpty() && current >= cost) {
                    int afterAcquire = current - cost;

                    setGlobalVariable(alh, GV_AVAILABLE_OPERATORS, afterAcquire);

                    fifoPredecessorMap.remove(taskName);
                    recordStartTime(taskName, cost, afterAcquire);

                    log("[" + taskName + "] 申请成功，占用 " + cost
                            + " 人，剩余可用 = " + afterAcquire);

                    // 发送资源池状态更新
                    sendResourcePoolStatus(alh);

                    return true;
                }

                /*
                 * 否则加入 FIFO 等待队列。
                 */
                if (!waitingQueue.contains(taskName)) {
                    String previousWaiter = waitingQueue.peekLast();
                    if (previousWaiter != null) {
                        fifoPredecessorMap.put(taskName, previousWaiter);
                    } else {
                        fifoPredecessorMap.remove(taskName);
                    }

                    waitingQueue.add(taskName);

                    log("[" + taskName + "] 加入 FIFO 等待队列，当前位置 = "
                            + waitingQueue.size()
                            + "，当前可用 = " + current
                            + "，需要 = " + cost);

                    // 发送节点状态更新（进入等待队列）
                    sendNodeStatsUpdate(taskName);
                    sendResourcePoolStatus(alh);
                }

                /*
                 * 只有队首任务有资格申请资源。
                 */
                String head = waitingQueue.peek();

                if (taskName.equals(head) && current >= cost) {
                    waitingQueue.poll();

                    int afterAcquire = current - cost;

                    setGlobalVariable(alh, GV_AVAILABLE_OPERATORS, afterAcquire);

                    recordStartTime(taskName, cost, afterAcquire);

                    log("[" + taskName + "] FIFO 队首申请成功，占用 " + cost
                            + " 人，剩余可用 = " + afterAcquire
                            + "，等待队列剩余长度 = " + waitingQueue.size());

                    // 发送资源池状态更新
                    sendResourcePoolStatus(alh);

                    return true;
                }

                try {
                    LOCK.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();

                    waitingQueue.remove(taskName);
                    fifoPredecessorMap.remove(taskName);

                    log("[" + taskName + "] 等待资源时被中断，已从 FIFO 等待队列移除");

                    // 发送资源池状态更新
                    sendResourcePoolStatus(alh);

                    return false;
                }
            }
        }
    }

    /*
     * 保留原 tryAcquire 方法，兼容你之前的节点代码。
     * 如果你后面全部改成 runResourceTask()，这个方法可以不用管。
     */
    public static boolean tryAcquire(Object alh, String taskName, int cost) {
        synchronized (LOCK) {
            int current = toInt(getGlobalVariable(alh, GV_AVAILABLE_OPERATORS));
            int total = toInt(getGlobalVariable(alh, GV_TOTAL_OPERATORS));

            if (cost <= 0) {
                recordStartTime(taskName, cost, current);
                return true;
            }

            if (total > 0 && cost > total) {
                log("[" + taskName + "] 申请失败：需要 " + cost
                        + " 人，但资源池总人数只有 " + total + " 人");
                return false;
            }

            log("[" + taskName + "] 尝试申请 " + cost + " 人，当前可用 = " + current);

            if (waitingQueue.isEmpty() && current >= cost) {
                int afterAcquire = current - cost;

                setGlobalVariable(alh, GV_AVAILABLE_OPERATORS, afterAcquire);

                fifoPredecessorMap.remove(taskName);
                recordStartTime(taskName, cost, afterAcquire);

                log("[" + taskName + "] 申请成功，占用 " + cost
                        + " 人，剩余可用 = " + afterAcquire);

                sendResourcePoolStatus(alh);

                return true;
            }

            if (!waitingQueue.contains(taskName)) {
                String previousWaiter = waitingQueue.peekLast();
                if (previousWaiter != null) {
                    fifoPredecessorMap.put(taskName, previousWaiter);
                } else {
                    fifoPredecessorMap.remove(taskName);
                }

                waitingQueue.add(taskName);

                log("[" + taskName + "] 加入 FIFO 等待队列，当前位置 = "
                        + waitingQueue.size());

                sendNodeStatsUpdate(taskName);
                sendResourcePoolStatus(alh);
            }

            String head = waitingQueue.peek();

            if (taskName.equals(head) && current >= cost) {
                waitingQueue.poll();

                int afterAcquire = current - cost;

                setGlobalVariable(alh, GV_AVAILABLE_OPERATORS, afterAcquire);

                recordStartTime(taskName, cost, afterAcquire);

                log("[" + taskName + "] FIFO 队首申请成功，占用 " + cost
                        + " 人，剩余可用 = " + afterAcquire);

                sendResourcePoolStatus(alh);

                return true;
            }

            log("[" + taskName + "] 资源不足或未到队首，当前队首 = "
                    + head + "，当前可用 = " + current + "，需要 = " + cost);

            return false;
        }
    }

    /**
     * 节点真正取得资源时，确定它的业务 startTime / plannedFinishTime。
     *
     * ★ 不读取 System.currentTimeMillis() 来计算业务时间。
     */
    private static void recordStartTime(
            String taskName,
            int cost,
            int afterAcquire) {

        if (startSimTimeMap.containsKey(taskName)) {
            return;
        }

        long now = System.currentTimeMillis();
        startRealMsMap.put(taskName, now);

        Double readySimTime =
                readySimTimeMap.get(taskName);

        if (readySimTime == null) {

            readySimTime =
                    getCurrentThreadLogicalTime();

            if (readySimTime == null) {
                readySimTime = 0.0;
            }

            readySimTimeMap.put(
                    taskName,
                    readySimTime
            );

            requiredPeopleMap.put(
                    taskName,
                    Math.max(0, cost)
            );
        }

        Double duration =
                durationMap.get(taskName);

        if (duration == null || duration < 0) {
            duration = 0.0;
        }

        /*
         * FIFO 逻辑下界：
         * 如果当前任务曾排在另一个等待任务后面，
         * 它不能比前一个等待任务的 startTime 更早开始。
         */
        double lowerBound =
                Math.max(
                        0.0,
                        readySimTime
                );

        String fifoPredecessor =
                fifoPredecessorMap.get(
                        taskName
                );

        if (fifoPredecessor != null) {

            Double predecessorStart =
                    startSimTimeMap.get(
                            fifoPredecessor
                    );

            if (predecessorStart != null) {
                lowerBound =
                        Math.max(
                                lowerBound,
                                predecessorStart
                        );
            }
        }

        double startSimTime =
                findEarliestFeasibleStart(
                        taskName,
                        lowerBound,
                        duration,
                        Math.max(0, cost)
                );

        if (Double.isNaN(startSimTime)
                || Double.isInfinite(startSimTime)) {

            /*
             * 理论上 cost > total 时 acquireBlocking 会提前失败，
             * 因而通常不会走到这里。
             */
            startSimTime = lowerBound;
        }

        double finishSimTime =
                startSimTime
                        + duration;

        double waitSimTime =
                Math.max(
                        0.0,
                        startSimTime
                                - readySimTime
                );

        startSimTimeMap.put(
                taskName,
                startSimTime
        );

        plannedFinishSimTimeMap.put(
                taskName,
                finishSimTime
        );

        waitSimTimeMap.put(
                taskName,
                waitSimTime
        );

        fifoPredecessorMap.remove(
                taskName
        );

        /*
         * 当前线程现在处于这个任务的逻辑 startTime。
         * release() 时再推进到 finishTime。
         */
        setCurrentThreadLogicalTime(
                startSimTime
        );

        log("[" + taskName + "] 逻辑排程："
                + "readyTime = " + fmt(readySimTime) + " 天"
                + "，startTime = " + fmt(startSimTime) + " 天"
                + "，finishTime(计划) = " + fmt(finishSimTime) + " 天"
                + "，waitTime = " + fmt(waitSimTime) + " 天"
                + "，duration = " + fmt(duration) + " 天");

        sendNodeStatsUpdate(taskName);
    }

    /**
     * 从 lowerBound 开始，寻找满足资源容量的最早逻辑开始时间。
     *
     * 已经真正取得资源的任务都会在 startSimTimeMap +
     * plannedFinishSimTimeMap 中留下一个逻辑占用区间。
     *
     * 这里完全不看真实 sleep 花了多久。
     */
    private static double findEarliestFeasibleStart(
            String taskName,
            double lowerBound,
            double duration,
            int cost) {

        lowerBound =
                Math.max(
                        0.0,
                        lowerBound
                );

        duration =
                Math.max(
                        0.0,
                        duration
                );

        cost =
                Math.max(
                        0,
                        cost
                );

        if (cost <= 0) {
            return lowerBound;
        }

        int total =
                currentTotalOperators;

        if (total <= 0) {
            /*
             * 与 acquireBlocking 的实际资源状态保持一致：
             * 总资源为 0 且 cost > 0 时，不存在合法业务排程。
             */
            return Double.NaN;
        }

        if (cost > total) {
            return Double.NaN;
        }

        /*
         * 最早可行开始点一定出现在：
         * 1. lowerBound
         * 2. 某个已排程任务的 finishTime
         *
         * 因为资源可用量只会在 start/finish 事件上发生变化，
         * 而“变得更多”只会发生在 finish 上。
         */
        List<Double> candidates =
                new ArrayList<>();

        candidates.add(
                lowerBound
        );

        for (Map.Entry<String, Double> entry
                : plannedFinishSimTimeMap.entrySet()) {

            if (entry.getKey().equals(taskName)) {
                continue;
            }

            Double finish =
                    entry.getValue();

            if (finish != null
                    && finish + EPSILON
                    >= lowerBound) {

                candidates.add(
                        Math.max(
                                lowerBound,
                                finish
                        )
                );
            }
        }

        Collections.sort(candidates);

        double lastTested =
                Double.NaN;

        for (Double candidateObj : candidates) {

            if (candidateObj == null) {
                continue;
            }

            double candidate =
                    candidateObj;

            if (!Double.isNaN(lastTested)
                    && Math.abs(
                    candidate
                            - lastTested
            ) <= EPSILON) {
                continue;
            }

            lastTested = candidate;

            if (hasResourceCapacityForInterval(
                    taskName,
                    candidate,
                    duration,
                    cost,
                    total)) {

                return candidate;
            }
        }

        /*
         * 所有已排程任务结束之后资源一定全部释放。
         */
        double afterAll =
                lowerBound;

        for (Map.Entry<String, Double> entry
                : plannedFinishSimTimeMap.entrySet()) {

            if (entry.getKey().equals(taskName)) {
                continue;
            }

            Double finish =
                    entry.getValue();

            if (finish != null) {
                afterAll =
                        Math.max(
                                afterAll,
                                finish
                        );
            }
        }

        return afterAll;
    }

    /**
     * 检查 [start, start + duration) 内是否始终有足够资源。
     */
    private static boolean hasResourceCapacityForInterval(
            String ignoreTaskName,
            double start,
            double duration,
            int cost,
            int total) {

        if (cost <= 0) {
            return true;
        }

        if (cost > total) {
            return false;
        }

        /*
         * duration = 0 不占用人·天，
         * 而真实 acquire 已经确保这一刻拿到了资源。
         */
        if (duration <= EPSILON) {
            return true;
        }

        double end =
                start + duration;

        List<Double> points =
                new ArrayList<>();

        points.add(start);
        points.add(end);

        for (String otherTask
                : startSimTimeMap.keySet()) {

            if (otherTask.equals(
                    ignoreTaskName
            )) {
                continue;
            }

            Double otherStart =
                    startSimTimeMap.get(
                            otherTask
                    );

            Double otherFinish =
                    plannedFinishSimTimeMap.get(
                            otherTask
                    );

            if (otherStart == null
                    || otherFinish == null) {
                continue;
            }

            if (otherFinish <= start + EPSILON
                    || otherStart >= end - EPSILON) {
                continue;
            }

            if (otherStart > start + EPSILON
                    && otherStart < end - EPSILON) {
                points.add(otherStart);
            }

            if (otherFinish > start + EPSILON
                    && otherFinish < end - EPSILON) {
                points.add(otherFinish);
            }
        }

        Collections.sort(points);

        for (int i = 0;
             i < points.size() - 1;
             i++) {

            double a = points.get(i);
            double b = points.get(i + 1);

            if (b - a <= EPSILON) {
                continue;
            }

            double probe =
                    (a + b) / 2.0;

            int used =
                    getScheduledResourceUsageAt(
                            ignoreTaskName,
                            probe
                    );

            if (used + cost > total) {
                return false;
            }
        }

        return true;
    }

    private static int getScheduledResourceUsageAt(
            String ignoreTaskName,
            double time) {

        int used = 0;

        for (String otherTask
                : startSimTimeMap.keySet()) {

            if (otherTask.equals(
                    ignoreTaskName
            )) {
                continue;
            }

            Double otherStart =
                    startSimTimeMap.get(
                            otherTask
                    );

            Double otherFinish =
                    plannedFinishSimTimeMap.get(
                            otherTask
                    );

            Integer people =
                    requiredPeopleMap.get(
                            otherTask
                    );

            if (otherStart == null
                    || otherFinish == null
                    || people == null
                    || people <= 0) {
                continue;
            }

            if (time + EPSILON >= otherStart
                    && time < otherFinish - EPSILON) {

                used += people;
            }
        }

        return used;
    }

    /*
     * 释放资源，同时记录 finishTime。
     */
    public static void release(
            Object alh,
            String taskName,
            int cost) {

        synchronized (LOCK) {

            int beforeRelease =
                    toInt(
                            getGlobalVariable(
                                    alh,
                                    GV_AVAILABLE_OPERATORS
                            )
                    );

            int total =
                    toInt(
                            getGlobalVariable(
                                    alh,
                                    GV_TOTAL_OPERATORS
                            )
                    );

            int afterRelease =
                    beforeRelease
                            + Math.max(
                            0,
                            cost
                    );

            if (total > 0
                    && afterRelease > total) {
                afterRelease = total;
            }

            setGlobalVariable(
                    alh,
                    GV_AVAILABLE_OPERATORS,
                    afterRelease
            );

            currentTotalOperators = total;
            currentAvailableOperators = afterRelease;

            Double readySimTime =
                    readySimTimeMap.get(
                            taskName
                    );

            Double startSimTime =
                    startSimTimeMap.get(
                            taskName
                    );

            Double duration =
                    durationMap.get(
                            taskName
                    );

            if (readySimTime == null) {
                readySimTime = 0.0;
            }

            if (duration == null
                    || duration < 0) {
                duration = 0.0;
            }

            /*
             * 正常路径下 recordStartTime() 已经完成逻辑排程。
             * 如果因为兼容旧节点调用导致 startTime 缺失，
             * 这里仍然只用 readyTime 做兜底，绝不读电脑真实时间。
             */
            if (startSimTime == null) {

                startSimTime =
                        readySimTime;

                startSimTimeMap.put(
                        taskName,
                        startSimTime
                );
            }

            Double plannedFinish =
                    plannedFinishSimTimeMap.get(
                            taskName
                    );

            double finishSimTime =
                    plannedFinish != null
                            ? plannedFinish
                            : startSimTime
                            + duration;

            double waitSimTime =
                    Math.max(
                            0.0,
                            startSimTime
                                    - readySimTime
                    );

            double taskTotalTime =
                    finishSimTime
                            - readySimTime;

            plannedFinishSimTimeMap.put(
                    taskName,
                    finishSimTime
            );

            finishSimTimeMap.put(
                    taskName,
                    finishSimTime
            );

            waitSimTimeMap.put(
                    taskName,
                    waitSimTime
            );

            /*
             * 给模型前驱遍历使用：
             * Element ID -> 本轮业务 finishTime。
             */
            String elementId =
                    taskElementIdMap.get(
                            taskName
                    );

            if (elementId != null
                    && !elementId.trim().isEmpty()) {

                finishSimTimeByElementId.put(
                        elementId,
                        finishSimTime
                );
            }

            /*
             * 同一执行线程继续后续普通节点/子流程时，
             * 它的业务时钟推进到当前任务 finishTime。
             */
            setCurrentThreadLogicalTime(
                    finishSimTime
            );

            fifoPredecessorMap.remove(
                    taskName
            );

            log("[" + taskName + "] 执行完成，释放 "
                    + cost
                    + " 人，当前可用 = "
                    + afterRelease);

            log("[" + taskName + "] 业务时间统计："
                    + "readyTime = " + fmt(readySimTime) + " 天"
                    + "，startTime = " + fmt(startSimTime) + " 天"
                    + "，finishTime = " + fmt(finishSimTime) + " 天"
                    + "，等待时间 = " + fmt(waitSimTime) + " 天"
                    + "，执行时间 = " + fmt(duration) + " 天"
                    + "，节点总耗时 = " + fmt(taskTotalTime) + " 天");

            long realElapsed =
                    Math.max(
                            0L,
                            System.currentTimeMillis()
                                    - processStartRealMs
                    );

            log("[" + taskName + "] 诊断：当前电脑真实已运行约 "
                    + realElapsed
                    + " ms（仅播放/调试，不参与业务工期）");

            if (!waitingQueue.isEmpty()) {
                log("[资源池] 当前 FIFO 队首 = "
                        + waitingQueue.peek()
                        + "，等待队列长度 = "
                        + waitingQueue.size());
            }

            Map<String, Object> stats =
                    new LinkedHashMap<>();

            stats.put(
                    "taskName",
                    taskName
            );

            stats.put(
                    "status",
                    "done"
            );

            stats.put(
                    "requiredPeople",
                    requiredPeopleMap.getOrDefault(
                            taskName,
                            0
                    )
            );

            stats.put(
                    "duration",
                    durationMap.getOrDefault(
                            taskName,
                            0.0
                    )
            );

            stats.put(
                    "timeUnit",
                    SIM_TIME_UNIT
            );

            stats.put(
                    "readyTime",
                    fmt(readySimTime)
            );

            stats.put(
                    "startTime",
                    fmt(startSimTime)
            );

            stats.put(
                    "finishTime",
                    fmt(finishSimTime)
            );

            stats.put(
                    "waitTime",
                    fmt(waitSimTime)
            );

            stats.put(
                    "totalTime",
                    fmt(taskTotalTime)
            );

            if (statsSender != null) {
                statsSender.sendNodeStats(
                        taskName,
                        stats
                );
            }

            sendNodeStatsUpdate(
                    taskName
            );

            sendResourcePoolStatus(
                    alh
            );

            LOCK.notifyAll();
        }
    }

    public static int getAvailableOperators(Object alh) {
        synchronized (LOCK) {
            return toInt(getGlobalVariable(alh, GV_AVAILABLE_OPERATORS));
        }
    }

    /**
     * 获取当前方案的总工期，单位：天。
     * 总工期 = 所有已完成 ResourceTask 的最大 finishTime。
     */
    public static double getTotalDurationDays() {
        synchronized (LOCK) {
            return round2(getTotalDurationDaysInternal());
        }
    }

    /**
     * 计算“每天平均资源使用数量”。
     *
     * 第 d 天对应时间区间 [d - 1, d)。
     * 某任务对当天的贡献：
     *   requiredPeople * overlapDays
     *
     * 因为一天区间长度就是 1，所以该和值同时也是“当天平均资源数量”。
     *
     * 返回示例：
     * [
     *   {day:1, dayStart:0.0, dayEnd:1.0, averageResources:12.5, resourcePersonDays:12.5},
     *   {day:2, dayStart:1.0, dayEnd:2.0, averageResources:18.0, resourcePersonDays:18.0}
     * ]
     */
    public static List<Map<String, Object>> getDailyResourceUsage() {
        synchronized (LOCK) {
            return buildDailyResourceUsageInternal();
        }
    }

    /**
     * 获取本次仿真的方案结果汇总。
     *
     * 这个结果可以直接给后续“多方案对比页面”使用。
     */
    public static Map<String, Object> getSimulationSummary() {
        synchronized (LOCK) {
            return buildSimulationSummaryInternal();
        }
    }

    /**
     * 输出总统计。
     *
     * 在流程最后增加一个 Opaque Action：
     * Packages.myplugin.OperatorResourceManager.printSummary();
     */
    public static void printSummary() {
        synchronized (LOCK) {
            log("========== 资源仿真方案统计 ==========");

            if (finishSimTimeMap.isEmpty()) {
                log("[统计] 尚无已完成的 ResourceTask 节点");
                log("====================================");
                return;
            }

            Map<String, Object> summary = buildSimulationSummaryInternal();

            double totalDuration = toDouble(summary.get("totalDuration"));
            double totalPersonDays = toDouble(summary.get("totalPersonDays"));
            double averageResourceUsage = toDouble(summary.get("averageResourceUsage"));
            double peakDailyResourceUsage = toDouble(summary.get("peakDailyResourceUsage"));
            double totalWaitTime = toDouble(summary.get("totalWaitTime"));
            int waitingNodeCount = toInt(summary.get("waitingNodeCount"));

            log("[统计] 流程总工期 = " + fmt(totalDuration) + " 天");
            log("[统计] 总资源工作量 = " + fmt(totalPersonDays) + " 人·天");
            log("[统计] 全流程平均资源使用量 = " + fmt(averageResourceUsage) + " 人");
            log("[统计] 单日最高平均资源使用量 = " + fmt(peakDailyResourceUsage) + " 人");
            log("[统计] 总等待时间 = " + fmt(totalWaitTime) + " 天，发生等待的节点数 = " + waitingNodeCount);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> dailyResources =
                    (List<Map<String, Object>>) summary.get("dailyResources");

            if (dailyResources != null && !dailyResources.isEmpty()) {
                log("---------- 每日平均资源使用量 ----------");
                for (Map<String, Object> day : dailyResources) {
                    log("[第 " + day.get("day") + " 天] 平均资源 = "
                            + fmt(toDouble(day.get("averageResources")))
                            + " 人，资源工作量 = "
                            + fmt(toDouble(day.get("resourcePersonDays")))
                            + " 人·天");
                }
            }

            // 通过现有 node_stats 通道向前端发送汇总。
            // SimulationSyncServer 会把 taskName 作为 nodeName，因此前端收到的 nodeName = __SUMMARY__。
            if (statsSender != null) {
                Map<String, Object> summaryStats = new LinkedHashMap<>(summary);
                summaryStats.put("status", "summary");
                statsSender.sendNodeStats("__SUMMARY__", summaryStats);
            }

            log("---------- 节点明细 ----------");

            for (String taskName : readySimTimeMap.keySet()) {
                Double readyTime = readySimTimeMap.get(taskName);
                Double startTime = startSimTimeMap.get(taskName);
                Double finishTime = finishSimTimeMap.get(taskName);
                Double waitTime = waitSimTimeMap.get(taskName);
                Double duration = durationMap.get(taskName);
                Integer requiredPeople = requiredPeopleMap.get(taskName);

                if (readyTime == null || startTime == null || finishTime == null) {
                    log("[统计] 节点 = " + taskName + "：尚未完整结束，跳过");
                    continue;
                }

                if (waitTime == null) {
                    waitTime = startTime - readyTime;
                }

                if (duration == null) {
                    duration = finishTime - startTime;
                }

                double taskTotalTime = finishTime - readyTime;

                log("[统计] 节点 = " + taskName);
                log("       需要资源 = " + (requiredPeople == null ? 0 : requiredPeople)
                        + "，readyTime = " + fmt(readyTime) + " 天"
                        + "，startTime = " + fmt(startTime) + " 天"
                        + "，finishTime = " + fmt(finishTime) + " 天"
                        + "，等待时间 = " + fmt(waitTime) + " 天"
                        + "，执行时间 = " + fmt(duration) + " 天"
                        + "，节点总耗时 = " + fmt(taskTotalTime) + " 天");
            }

            log("====================================");
        }
    }

    /**
     * 获取所有节点的当前统计信息（供 WebSocket 查询使用）。
     */
    public static Map<String, Map<String, Object>> getAllNodeStats() {
        synchronized (LOCK) {
            Map<String, Map<String, Object>> allStats = new LinkedHashMap<>();

            for (String taskName : requiredPeopleMap.keySet()) {
                Map<String, Object> stats = new LinkedHashMap<>();
                stats.put("taskName", taskName);
                stats.put("requiredPeople", requiredPeopleMap.getOrDefault(taskName, 0));
                stats.put("duration", durationMap.getOrDefault(taskName, 0.0));
                stats.put("timeUnit", SIM_TIME_UNIT);

                Double readyTime = readySimTimeMap.get(taskName);
                Double startTime = startSimTimeMap.get(taskName);
                Double finishTime = finishSimTimeMap.get(taskName);
                Double waitTime = waitSimTimeMap.get(taskName);

                if (readyTime != null) stats.put("readyTime", fmt(readyTime));
                if (startTime != null) stats.put("startTime", fmt(startTime));
                if (finishTime != null) {
                    stats.put("finishTime", fmt(finishTime));
                    if (readyTime != null) {
                        stats.put("totalTime", fmt(finishTime - readyTime));
                    }
                }
                if (waitTime != null) stats.put("waitTime", fmt(waitTime));

                if (finishTime != null) {
                    stats.put("status", "done");
                } else if (startTime != null) {
                    stats.put("status", "running");
                } else {
                    stats.put("status", "waiting");
                }

                allStats.put(taskName, stats);
            }

            return allStats;
        }
    }

    /**
     * 内部：获取最大完成时间，即总工期。
     */
    private static double getTotalDurationDaysInternal() {
        double maxFinishTime = 0.0;

        for (Double finishTime : finishSimTimeMap.values()) {
            if (finishTime != null && finishTime > maxFinishTime) {
                maxFinishTime = finishTime;
            }
        }

        return maxFinishTime;
    }

    /**
     * 内部：按自然仿真日 [0,1)、[1,2)... 统计每日平均资源使用量。
     */
    private static List<Map<String, Object>> buildDailyResourceUsageInternal() {
        List<Map<String, Object>> result = new ArrayList<>();

        double totalDuration = getTotalDurationDaysInternal();
        if (totalDuration <= EPSILON) {
            return result;
        }

        int dayCount = (int) Math.ceil(totalDuration - EPSILON);
        if (dayCount <= 0) {
            dayCount = 1;
        }

        for (int day = 1; day <= dayCount; day++) {
            double dayStart = day - 1.0;
            double dayEnd = day;
            double resourcePersonDays = 0.0;

            for (String taskName : startSimTimeMap.keySet()) {
                Double taskStart = startSimTimeMap.get(taskName);
                Double taskFinish = finishSimTimeMap.get(taskName);
                Integer requiredPeople = requiredPeopleMap.get(taskName);

                // 只统计已经完整结束的 ResourceTask。
                if (taskStart == null || taskFinish == null || requiredPeople == null) {
                    continue;
                }

                if (requiredPeople <= 0 || taskFinish <= taskStart) {
                    continue;
                }

                double overlapStart = Math.max(taskStart, dayStart);
                double overlapEnd = Math.min(taskFinish, dayEnd);
                double overlapDays = Math.max(0.0, overlapEnd - overlapStart);

                if (overlapDays > EPSILON) {
                    resourcePersonDays += requiredPeople * overlapDays;
                }
            }

            Map<String, Object> dayData = new LinkedHashMap<>();
            dayData.put("day", day);
            dayData.put("dayStart", round2(dayStart));
            dayData.put("dayEnd", round2(Math.min(dayEnd, totalDuration)));
            dayData.put("averageResources", round2(resourcePersonDays));
            dayData.put("resourcePersonDays", round2(resourcePersonDays));
            result.add(dayData);
        }

        return result;
    }

    /**
     * 内部：构造一次仿真的完整方案结果。
     */
    private static Map<String, Object> buildSimulationSummaryInternal() {
        Map<String, Object> summary = new LinkedHashMap<>();

        double totalDuration = getTotalDurationDaysInternal();
        double totalPersonDays = 0.0;
        double totalWaitTime = 0.0;
        int waitingNodeCount = 0;

        for (String taskName : finishSimTimeMap.keySet()) {
            Double startTime = startSimTimeMap.get(taskName);
            Double finishTime = finishSimTimeMap.get(taskName);
            Integer requiredPeople = requiredPeopleMap.get(taskName);
            Double waitTime = waitSimTimeMap.get(taskName);

            if (startTime != null && finishTime != null && requiredPeople != null) {
                double actualDuration = Math.max(0.0, finishTime - startTime);
                totalPersonDays += Math.max(0, requiredPeople) * actualDuration;
            }

            if (waitTime != null && waitTime > EPSILON) {
                totalWaitTime += waitTime;
                waitingNodeCount++;
            }
        }

        List<Map<String, Object>> dailyResources = buildDailyResourceUsageInternal();

        double peakDailyResourceUsage = 0.0;
        for (Map<String, Object> dayData : dailyResources) {
            double value = toDouble(dayData.get("averageResources"));
            if (value > peakDailyResourceUsage) {
                peakDailyResourceUsage = value;
            }
        }

        double averageResourceUsage =
                totalDuration > EPSILON
                        ? totalPersonDays / totalDuration
                        : 0.0;

        summary.put("timeUnit", SIM_TIME_UNIT);
        summary.put("durationUnit", "day");
        summary.put("resourceUnit", "person");
        summary.put("totalOperators", currentTotalOperators);
        summary.put("totalDuration", round2(totalDuration));
        summary.put("completedNodes", finishSimTimeMap.size());
        summary.put("totalPersonDays", round2(totalPersonDays));
        summary.put("averageResourceUsage", round2(averageResourceUsage));
        summary.put("peakDailyResourceUsage", round2(peakDailyResourceUsage));
        summary.put("totalWaitTime", round2(totalWaitTime));
        summary.put("waitingNodeCount", waitingNodeCount);
        summary.put("dayCount", dailyResources.size());
        summary.put("dailyResources", dailyResources);

        // 保存每个 ResourceTask 的仿真时间线快照。
        // 多方案对比页面直接使用这部分数据，不再依赖电脑实际运行时间戳。
        List<Map<String, Object>> tasks = new ArrayList<>();
        for (String taskName : requiredPeopleMap.keySet()) {
            Map<String, Object> task = new LinkedHashMap<>();

            Integer requiredPeople = requiredPeopleMap.get(taskName);
            Double duration = durationMap.get(taskName);
            Double readyTime = readySimTimeMap.get(taskName);
            Double startTime = startSimTimeMap.get(taskName);
            Double finishTime = finishSimTimeMap.get(taskName);
            Double waitTime = waitSimTimeMap.get(taskName);

            task.put("taskName", taskName);
            task.put("requiredPeople", requiredPeople == null ? 0 : requiredPeople);
            task.put("duration", round2(duration == null ? 0.0 : duration));

            if (readyTime != null) {
                task.put("readyTime", round2(readyTime));
            }
            if (startTime != null) {
                task.put("startTime", round2(startTime));
            }
            if (finishTime != null) {
                task.put("finishTime", round2(finishTime));
            }
            if (waitTime != null) {
                task.put("waitTime", round2(waitTime));
            }
            if (readyTime != null && finishTime != null) {
                task.put("totalTime", round2(finishTime - readyTime));
            }

            if (finishTime != null) {
                task.put("status", "done");
            } else if (startTime != null) {
                task.put("status", "running");
            } else {
                task.put("status", "waiting");
            }

            tasks.add(task);
        }
        summary.put("tasks", tasks);

        return summary;
    }

    private static double round2(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        return Math.round(value * 100.0) / 100.0;
    }

    // ★ 添加一个缓存：节点名称 -> 节点 ID
    private static final Map<String, String> elementIdCache = new ConcurrentHashMap<>();

    private static ResourceTaskConfig readResourceTaskConfig(
            Element taskElement) {

        if (taskElement == null) {

            log("[资源管理器] ResourceTask 元素为空");

            return null;
        }

        Project project =
                Application.getInstance().getProject();

        if (project == null) {

            log("[资源管理器] 当前没有打开项目");

            return null;
        }

        String taskName =
                getElementName(taskElement);

        if (taskName == null
                || taskName.trim().isEmpty()) {

            taskName = "<未命名ResourceTask>";
        }

        /*
         * 找到 ResourceTask stereotype 定义。
         */
        Stereotype resourceTask =
                StereotypesHelper.getStereotype(
                        project,
                        "ResourceTask"
                );

        if (resourceTask == null) {

            log("[" + taskName
                    + "] 未找到 stereotype：ResourceTask");

            return null;
        }

        /*
         * 直接判断当前 Action 是否应用了 ResourceTask。
         */
        if (!StereotypesHelper.hasStereotype(
                taskElement,
                resourceTask)) {

            log("[" + taskName
                    + "] 没有应用 <<ResourceTask>>，跳过资源控制");

            return null;
        }

        /*
         * 读取 requiredPeople。
         */
        List<?> requiredPeopleValues =
                StereotypesHelper
                        .getStereotypePropertyValue(
                                taskElement,
                                resourceTask,
                                "requiredPeople"
                        );

        /*
         * 读取 duration。
         */
        List<?> durationValues =
                StereotypesHelper
                        .getStereotypePropertyValue(
                                taskElement,
                                resourceTask,
                                "duration"
                        );

        int requiredPeople =
                toInt(
                        firstValue(
                                requiredPeopleValues
                        )
                );

        double duration =
                toDouble(
                        firstValue(
                                durationValues
                        )
                );

        log("[" + taskName
                + "] 直接读取当前 ResourceTask：requiredPeople = "
                + requiredPeople
                + "，duration = "
                + duration);

        return new ResourceTaskConfig(
                requiredPeople,
                duration
        );
    }

    private static Object firstValue(List<?> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }

        return values.get(0);
    }

    private static String getElementName(Element element) {
        try {
            Method method = findMethod(element, "getName");
            Object value = method.invoke(element);

            if (value == null) {
                return "";
            }

            return String.valueOf(value);
        } catch (Exception e) {
            return "";
        }
    }

    private static String normalizeName(String name) {
        if (name == null) {
            return "";
        }

        return name.replaceAll("\\s+", " ").trim();
    }

    /*
     * 根据名称查找节点。
     * 先做精确匹配，再做空白字符归一化匹配。
     * 这样可以降低换行、多个空格导致找不到节点的概率。
     */
    public static Element findElementByName(Element root, String targetName) {
        if (root == null) {
            return null;
        }

        Element exact = findElementByNameExact(root, targetName);

        if (exact != null) {
            return exact;
        }

        return findElementByNameNormalized(root, targetName);
    }

    public static Element findElementByNameExact(Element root, String targetName) {
        if (root == null) {
            return null;
        }

        String name = getElementName(root);

        if (targetName.equals(name)) {
            return root;
        }

        Collection<Element> owned = root.getOwnedElement();

        if (owned != null) {
            for (Element child : owned) {
                Element result = findElementByNameExact(child, targetName);

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    public static Element findElementByNameNormalized(Element root, String targetName) {
        if (root == null) {
            return null;
        }

        String name = getElementName(root);

        if (normalizeName(targetName).equals(normalizeName(name))) {
            return root;
        }

        Collection<Element> owned = root.getOwnedElement();

        if (owned != null) {
            for (Element child : owned) {
                Element result = findElementByNameNormalized(child, targetName);

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    /**
     * 读取节点的 ResourceTask 配置（供 WebSocket 查询使用）
     */
    public static Map<String, Object> getNodeConfig(String taskName) {
        Project project = Application.getInstance().getProject();
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("taskName", taskName);

        if (project == null) {
            config.put("error", "当前没有打开项目");
            return config;
        }

        Element model = project.getPrimaryModel();
        Stereotype resourceTask = StereotypesHelper.getStereotype(project, "ResourceTask");

        if (resourceTask == null) {
            config.put("error", "未找到 stereotype：ResourceTask");
            return config;
        }

        Element taskElement = findElementByName(model, taskName);

        if (taskElement == null) {
            config.put("error", "未找到同名模型元素");
            return config;
        }

        if (!StereotypesHelper.hasStereotype(taskElement, resourceTask)) {
            config.put("error", "没有应用 <<ResourceTask>>");
            return config;
        }

        List<?> requiredPeopleValues = StereotypesHelper.getStereotypePropertyValue(taskElement, resourceTask, "requiredPeople");
        List<?> durationValues = StereotypesHelper.getStereotypePropertyValue(taskElement, resourceTask, "duration");

        int requiredPeople = toInt(firstValue(requiredPeopleValues));
        double duration = toDouble(firstValue(durationValues));

        config.put("requiredPeople", requiredPeople);
        config.put("duration", duration);
        config.put("elementId", taskElement.getID());

        return config;
    }

    /**
     * 更新节点的 ResourceTask 配置
     */
    public static Map<String, Object> updateNodeConfig(String taskName, Integer requiredPeople, Double duration) {
        Project project = Application.getInstance().getProject();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("taskName", taskName);

        if (project == null) {
            result.put("success", false);
            result.put("error", "当前没有打开项目");
            return result;
        }

        Element model = project.getPrimaryModel();
        Stereotype resourceTask = StereotypesHelper.getStereotype(project, "ResourceTask");

        if (resourceTask == null) {
            result.put("success", false);
            result.put("error", "未找到 stereotype：ResourceTask");
            return result;
        }

        Element taskElement = findElementByName(model, taskName);

        if (taskElement == null) {
            result.put("success", false);
            result.put("error", "未找到同名模型元素：" + taskName);
            return result;
        }

        if (!StereotypesHelper.hasStereotype(taskElement, resourceTask)) {
            result.put("success", false);
            result.put("error", "节点没有应用 <<ResourceTask>>");
            return result;
        }

        // ★ 关键修复：使用 Session 事务确保修改被保存
        com.nomagic.magicdraw.openapi.uml.SessionManager sessionManager =
                com.nomagic.magicdraw.openapi.uml.SessionManager.getInstance();

        boolean sessionCreated = false;
        try {
            // 检查是否已有事务
            if (!sessionManager.isSessionCreated()) {
                sessionManager.createSession(project, "Update ResourceTask Config");
                sessionCreated = true;
            }

            // ★ 打印修改前的值
            List<?> currentRequiredPeople = StereotypesHelper.getStereotypePropertyValue(
                    taskElement, resourceTask, "requiredPeople");
            List<?> currentDuration = StereotypesHelper.getStereotypePropertyValue(
                    taskElement, resourceTask, "duration");

            System.out.println("[ResourceTask] 修改前: requiredPeople=" + currentRequiredPeople + ", duration=" + currentDuration);

            // ★ 使用正确的类型设置值
            if (requiredPeople != null && requiredPeople >= 0) {
                // 直接设置 int 值
                StereotypesHelper.setStereotypePropertyValue(
                        taskElement,
                        resourceTask,
                        "requiredPeople",
                        requiredPeople
                );
                System.out.println("[ResourceTask] 设置 requiredPeople=" + requiredPeople);
            }

            if (duration != null && duration >= 0) {
                StereotypesHelper.setStereotypePropertyValue(
                        taskElement,
                        resourceTask,
                        "duration",
                        duration
                );
                System.out.println("[ResourceTask] 设置 duration=" + duration);
            }

            // ★ 验证修改是否生效
            List<?> newRequiredPeople = StereotypesHelper.getStereotypePropertyValue(
                    taskElement, resourceTask, "requiredPeople");
            List<?> newDuration = StereotypesHelper.getStereotypePropertyValue(
                    taskElement, resourceTask, "duration");

            System.out.println("[ResourceTask] 修改后: requiredPeople=" + newRequiredPeople + ", duration=" + newDuration);

            // ★ 提交事务
            if (sessionCreated) {
                sessionManager.closeSession(project);
            }

            result.put("success", true);
            result.put("requiredPeople", requiredPeople);
            result.put("duration", duration);

            // ★ 清除配置缓存
            clearConfigCache(taskName);

            log("[配置修改] " + taskName + "：requiredPeople = " + requiredPeople + "，duration = " + duration);
            log("[配置修改] 验证 - requiredPeople=" + newRequiredPeople + ", duration=" + newDuration);

        } catch (Exception e) {
            // ★ 发生异常时回滚事务
            if (sessionCreated) {
                try {
                    sessionManager.cancelSession(project);
                } catch (Exception ex) {
                    // 忽略
                }
            }
            result.put("success", false);
            result.put("error", "修改失败：" + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 清除指定节点的配置缓存
     */
    public static void clearConfigCache(String taskName) {
        if (taskName != null) {
            configCache.remove(taskName);
            log("[缓存] 已清除节点配置缓存: " + taskName);
        }
    }

    /**
     * 清除所有配置缓存
     */
    public static void clearAllConfigCache() {
        configCache.clear();
        log("[缓存] 已清除所有节点配置缓存");
    }

    /**
     * 获取资源池配置（全局变量）
     */
    public static Map<String, Object> getResourcePoolConfig(Object alh) {
        Map<String, Object> config = new LinkedHashMap<>();

        try {
            int total = toInt(getGlobalVariable(alh, GV_TOTAL_OPERATORS));
            int available = toInt(getGlobalVariable(alh, GV_AVAILABLE_OPERATORS));
            double timeScale = toDouble(getGlobalVariable(alh, GV_TIME_SCALE_MS));

            synchronized (LOCK) {
                currentTotalOperators = total;
                currentAvailableOperators = available;
                if (timeScale > 0) {
                    OperatorResourceManager.timeScaleMs = timeScale;
                }
            }

            config.put("totalOperators", total);
            config.put("availableOperators", available);
            config.put("timeScaleMs", timeScale);
            config.put(
                    "initialized",
                    toBoolean(
                            getGlobalVariable(
                                    alh,
                                    GV_RESOURCE_POOL_INITIALIZED
                            )
                    )
            );
            config.put("success", true);
        } catch (Exception e) {
            config.put("success", false);
            config.put("error", "读取资源池配置失败: " + e.getMessage());
        }

        return config;
    }

    /**
     * 更新资源池配置（全局变量）
     */
    public static Map<String, Object> updateResourcePoolConfig(Object alh, Integer totalOperators, Double timeScaleMs) {
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            if (totalOperators != null && totalOperators >= 0) {
                int oldTotal = toInt(getGlobalVariable(alh, GV_TOTAL_OPERATORS));
                int oldAvailable = toInt(getGlobalVariable(alh, GV_AVAILABLE_OPERATORS));

                // 保留当前已占用资源数量，再根据新的总资源数计算新的可用量。
                int inUse = Math.max(0, oldTotal - oldAvailable);
                int newAvailable = Math.max(0, totalOperators - inUse);

                setGlobalVariable(alh, GV_TOTAL_OPERATORS, totalOperators);
                setGlobalVariable(alh, GV_AVAILABLE_OPERATORS, newAvailable);

                synchronized (LOCK) {
                    currentTotalOperators = totalOperators;
                    currentAvailableOperators = newAvailable;
                }

                result.put("totalOperators", totalOperators);
                result.put("availableOperators", newAvailable);
            }

            if (timeScaleMs != null && timeScaleMs > 0) {
                setGlobalVariable(alh, GV_TIME_SCALE_MS, timeScaleMs);

                // 注意：参数名和静态字段同名，必须显式写类名。
                synchronized (LOCK) {
                    OperatorResourceManager.timeScaleMs = timeScaleMs;
                }

                result.put("timeScaleMs", timeScaleMs);
            }

            result.put("success", true);

            log("[资源池配置] 已更新: totalOperators=" + totalOperators
                    + ", availableOperators=" + result.get("availableOperators")
                    + ", timeScaleMs=" + timeScaleMs);

            sendResourcePoolStatus(alh);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "更新资源池配置失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取资源池配置（供 WebSocket 查询使用，不需要 ALH）
     * 注意：这个方法需要在仿真运行时调用，否则无法获取 ALH
     */
    public static Map<String, Object> getResourcePoolConfig() {
        Map<String, Object> config = new LinkedHashMap<>();

        // 从内存快照中获取当前值。
        synchronized (LOCK) {
            config.put("totalOperators", currentTotalOperators);
            config.put("availableOperators", currentAvailableOperators);
            config.put("timeScaleMs", timeScaleMs);
            config.put("waitingQueueSize", waitingQueue.size());
            config.put("timeUnit", SIM_TIME_UNIT);
            config.put("success", true);
        }

        return config;
    }

    /**
     * 获取节点的所有 ResourceTask 配置属性（不仅仅是 requiredPeople 和 duration）
     */
    public static Map<String, Object> getNodeFullConfig(String taskName) {
        Project project = Application.getInstance().getProject();
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("taskName", taskName);

        if (project == null) {
            config.put("error", "当前没有打开项目");
            return config;
        }

        Element model = project.getPrimaryModel();
        Stereotype resourceTask = StereotypesHelper.getStereotype(project, "ResourceTask");

        if (resourceTask == null) {
            config.put("error", "未找到 stereotype：ResourceTask");
            return config;
        }

        Element taskElement = findElementByName(model, taskName);

        if (taskElement == null) {
            config.put("error", "未找到同名模型元素");
            return config;
        }

        if (!StereotypesHelper.hasStereotype(taskElement, resourceTask)) {
            config.put("error", "没有应用 <<ResourceTask>>");
            return config;
        }

        // ★ 获取所有属性值
        Map<String, Object> allProperties = new LinkedHashMap<>();

        try {
            // 获取 Stereotype 的所有属性
            Collection<?> ownedAttributes = resourceTask.getOwnedAttribute();
            if (ownedAttributes != null) {
                for (Object attr : ownedAttributes) {
                    if (attr instanceof com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property) {
                        com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property prop =
                                (com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property) attr;
                        String propName = prop.getName();
                        if (propName != null && !propName.isEmpty()) {
                            List<?> values = StereotypesHelper.getStereotypePropertyValue(
                                    taskElement, resourceTask, propName);
                            Object value = firstValue(values);
                            allProperties.put(propName, value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log("[ResourceTask] 获取所有属性失败: " + e.getMessage());
        }

        config.put("allProperties", allProperties);
        config.put("elementId", taskElement.getID());
        config.put("elementType", taskElement.getClass().getSimpleName());

        return config;
    }

    /**
     * 获取所有节点的 ResourceTask 配置
     */
    public static Map<String, Map<String, Object>> getAllNodeConfigs() {
        Project project = Application.getInstance().getProject();
        Map<String, Map<String, Object>> allConfigs = new LinkedHashMap<>();

        if (project == null) {
            return allConfigs;
        }

        Element model = project.getPrimaryModel();
        Stereotype resourceTask = StereotypesHelper.getStereotype(project, "ResourceTask");

        if (resourceTask == null) {
            return allConfigs;
        }

        // 递归查找所有应用了 ResourceTask 的元素
        List<Element> taskElements = new ArrayList<>();
        findElementsWithStereotype(model, resourceTask, taskElements);

        for (Element element : taskElements) {
            String name = getElementName(element);
            if (name != null && !name.trim().isEmpty()) {
                Map<String, Object> config = getNodeFullConfig(name);
                allConfigs.put(name, config);
            }
        }

        return allConfigs;
    }

    private static void findElementsWithStereotype(Element root, Stereotype stereotype, List<Element> result) {
        if (root == null) return;

        if (StereotypesHelper.hasStereotype(root, stereotype)) {
            result.add(root);
        }

        Collection<Element> owned = root.getOwnedElement();
        if (owned != null) {
            for (Element child : owned) {
                findElementsWithStereotype(child, stereotype, result);
            }
        }
    }
}
