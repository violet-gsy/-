package myplugin;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class ConfigLoader {

    private static final String CONFIG_FILE_NAME = "simsync-config.properties";
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 8766;

    private static Properties properties = null;

    /**
     * 获取插件 JAR 所在目录
     * 支持两种场景：
     * 1. 插件目录结构: plugins/myplugin/myplugin.jar
     * 2. 传统结构: plugins/myplugin.jar
     */
    private static String getPluginDir() {
        try {
            URL url = ConfigLoader.class.getProtectionDomain().getCodeSource().getLocation();
            String path = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8.name());
            File jarFile = new File(path);

            if (jarFile.isFile()) {
                // 返回 JAR 文件所在目录
                // 例如: D:/MagicDraw/plugins/myplugin/ 或 D:/MagicDraw/plugins/
                return jarFile.getParent();
            } else {
                // 开发环境，类在 classes 目录
                return new File(".").getAbsolutePath();
            }
        } catch (Throwable t) {
            return ".";
        }
    }

    private static synchronized void loadConfig() {
        if (properties != null) {
            return;
        }

        properties = new Properties();

        // 获取插件 JAR 所在目录
        String pluginDir = getPluginDir();

        // 按优先级搜索
        String[] searchPaths = {
                // 1. 插件 JAR 同级目录（最优先）
                pluginDir + File.separator + CONFIG_FILE_NAME,
                // 2. 如果是 plugins/myplugin/ 结构，再往上找 plugins/ 目录
                new File(pluginDir).getParent() + File.separator + CONFIG_FILE_NAME,
                // 3. 用户目录
                System.getProperty("user.home") + File.separator + CONFIG_FILE_NAME,
                // 4. 当前工作目录
                "." + File.separator + CONFIG_FILE_NAME,
        };

        boolean loaded = false;
        for (String path : searchPaths) {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                try (InputStream is = new FileInputStream(file)) {
                    properties.load(is);
                    loaded = true;
                    System.out.println("[SimSync] ✅ 加载配置文件: " + file.getAbsolutePath());
                    break;
                } catch (IOException e) {
                    System.out.println("[SimSync] 读取配置文件失败: " + path + " - " + e.getMessage());
                }
            }
        }

        if (!loaded) {
            System.out.println("[SimSync] ⚠️ 未找到外部配置文件，使用默认配置: host=" + DEFAULT_HOST + ", port=" + DEFAULT_PORT);
            System.out.println("[SimSync] 请在以下位置之一放置配置文件:");
            for (String path : searchPaths) {
                System.out.println("[SimSync]   - " + path);
            }
        }
    }

    public static String getHost() {
        loadConfig();
        String host = properties.getProperty("server.host");
        if (host == null || host.trim().isEmpty()) {
            return DEFAULT_HOST;
        }
        return host.trim();
    }

    public static int getPort() {
        loadConfig();
        String portStr = properties.getProperty("server.port");
        if (portStr == null || portStr.trim().isEmpty()) {
            return DEFAULT_PORT;
        }
        try {
            int port = Integer.parseInt(portStr.trim());
            if (port > 0 && port < 65536) {
                return port;
            }
        } catch (NumberFormatException e) {
            System.out.println("[SimSync] 端口配置无效: " + portStr + "，使用默认端口 " + DEFAULT_PORT);
        }
        return DEFAULT_PORT;
    }

    public static String getAddressString() {
        return getHost() + ":" + getPort();
    }
}