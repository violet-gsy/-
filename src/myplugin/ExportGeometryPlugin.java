package myplugin;

import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.plugins.Plugin;

public class ExportGeometryPlugin extends Plugin {

    @Override
    public void init() {

        try {
            ActionsConfiguratorsManager.getInstance()
                    .addMainMenuConfigurator(new ExportGeometryMenuConfigurator());

        } catch (Exception e) {
            Application.getInstance().getGUILog()
                    .log("[SimSync] 菜单注册失败: " + e.getMessage());
        }
        // 1. 先注册非阻塞的组件
        try {
            SimulationEventBroadcaster.register();

        } catch (Exception e) {
            Application.getInstance().getGUILog()
                    .log("[SimSync] 监听器注册失败: " + e.getMessage());
        }


        // 2. 最后启动 WebSocket 服务器（异步）
        Thread serverThread = new Thread(() -> {
            try {
                SimulationSyncServer server = SimulationSyncServer.getInstance();
                if (!server.isRunning()) {
                    server.start();
                    Application.getInstance().getGUILog()
                            .log("[SimSync] ✅ WebSocket 服务器已启动在端口 8765");
                }
            } catch (Exception e) {
                Application.getInstance().getGUILog()
                        .log("[SimSync] WebSocket 启动失败: " + e.getMessage());
            }
        });
        serverThread.setDaemon(true); // 设置为守护线程
        serverThread.start();
    }

    @Override
    public boolean close() {
        try {
            SimulationSyncServer.getInstance().stop();
        } catch (Exception ignored) {}
        return true;
    }

    @Override
    public boolean isSupported() {
        return true;
    }
}