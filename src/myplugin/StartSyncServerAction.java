package myplugin;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;

import java.awt.event.ActionEvent;

public class StartSyncServerAction extends MDAction {

    public StartSyncServerAction() {
        super("DEMO_START_SYNC_SERVER", "启动/停止 WebSocket 同步服务器", null, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUILog log = Application.getInstance().getGUILog();
        SimulationSyncServer server = SimulationSyncServer.getInstance();

        if (server.isRunning()) {
            try {
                server.stop();
                log.log("[SimSync] WebSocket 服务器已停止");
            } catch (Exception ex) {
                log.log("[SimSync] 停止失败: " + ex.getMessage());
            }
        } else {
            try {
                server.start();
                log.log("[SimSync] WebSocket 服务器已启动，端口 "
                        + SimulationSyncServer.PORT);
            } catch (Exception ex) {
                log.log("[SimSync] 启动失败: " + ex.getMessage());
            }
        }
    }
}