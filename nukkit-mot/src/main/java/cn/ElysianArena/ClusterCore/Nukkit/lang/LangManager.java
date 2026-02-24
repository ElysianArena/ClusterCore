package cn.ElysianArena.ClusterCore.Nukkit.lang;

import cn.ElysianArena.ClusterCore.Nukkit.Main;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 语言管理器 - 支持多语言消息配置
 * Author: NemoCat
 */
public class LangManager {

    private static LangManager instance;
    private Config langConfig;
    private final Map<String, String> defaultMessages = new HashMap<>();

    private LangManager() {
        loadDefaultMessages();
        loadLanguageFile();
    }

    public static LangManager getInstance() {
        if (instance == null) {
            instance = new LangManager();
        }
        return instance;
    }

    private void loadDefaultMessages() {
        defaultMessages.put("plugin.loading", "[ClusterCore] 正在启动...");
        defaultMessages.put("plugin.loaded", "[ClusterCore] Nukkit-MOT 模块已加载");
        defaultMessages.put("plugin.disabling", "[ClusterCore] Nukkit-MOT 模块正在关闭");
        defaultMessages.put("redis.not_enabled", "[ClusterCore] Redis未启用！请在配置中启用Redis。");
        defaultMessages.put("redis.connection_failed", "[ClusterCore] Redis连接失败！");
        defaultMessages.put("redis.connected", "[ClusterCore] Redis连接成功: {host}:{port}");
        defaultMessages.put("redis.registered", "[ClusterCore] 成功注册到Redis");
        defaultMessages.put("config.info", "[ClusterCore] groupId: {groupId}, wdpeId: {wdpeId}");
        defaultMessages.put("commands.registered", "[ClusterCore] 命令已注册: /wdt, /hub");
        defaultMessages.put("hub.player_only", "此命令只能由玩家执行");
        defaultMessages.put("hub.teleport_spawn", "已传送回出生点");
        defaultMessages.put("hub.teleporting_lobby", "正在寻找可用的大厅服务器...");
        defaultMessages.put("hub.no_lobby_found", "无法找到可用的大厅服务器，请稍后再试");
        defaultMessages.put("wdt.player_only", "此命令只能由玩家执行");
        defaultMessages.put("wdt.usage", "用法: /wdt <groupId>");
        defaultMessages.put("wdt.description", "查询并传送到指定分组的可用子服");
        defaultMessages.put("wdt.query_error", "查询服务器信息时发生错误: {error}");
        defaultMessages.put("wdt.connection_failed", "无法连接到群组管理系统");
        defaultMessages.put("wdt.no_servers", "分组 {groupId} 中没有可用的服务器");
        defaultMessages.put("wdt.server_list_header", "=== 分组 {groupId} 可用服务器 ===");
        defaultMessages.put("wdt.server_format", "- {wdpeId} ({current}/{max}) {status}");
        defaultMessages.put("wdt.query_failed", "查询失败: {message}");
        defaultMessages.put("wdt.process_error", "处理服务器信息时发生错误: {error}");
        defaultMessages.put("wdt.exception", "查询服务器信息失败: {error}");
        defaultMessages.put("server.open", "[开放]");
        defaultMessages.put("server.closed", "[关闭]");
        
        // 错误消息
        defaultMessages.put("error.no_target_servers", "没有可用的目标服务器");
        defaultMessages.put("error.transfer_failed", "传送失败");
        defaultMessages.put("error.no_lobby_servers", "没有可用的大厅服务器");
        defaultMessages.put("error.transfer_to_lobby_failed", "传送至大厅失败");
        defaultMessages.put("error.invalid_server_address", "§c传送失败：无效的服务器地址");
        defaultMessages.put("error.invalid_port", "§c传送失败：无效的端口号");
    }

    private void loadLanguageFile() {
        File langFile = new File(Main.getInstance().getDataFolder(), "lang.yml");
        
        if (!langFile.exists()) {
            Main.getInstance().saveResource("lang.yml", false);
        }
        
        langConfig = new Config(langFile, Config.YAML);
        boolean needsSave = false;
        for (Map.Entry<String, String> entry : defaultMessages.entrySet()) {
            if (!langConfig.exists(entry.getKey())) {
                langConfig.set(entry.getKey(), entry.getValue());
                needsSave = true;
            }
        }
        
        if (needsSave) {
            langConfig.save();
        }
    }

    public String getMessage(String key, Object... params) {
        String message = langConfig.getString(key, defaultMessages.getOrDefault(key, key));
        for (int i = 0; i < params.length; i += 2) {
            if (i + 1 < params.length) {
                String placeholder = "{" + params[i] + "}";
                String value = String.valueOf(params[i + 1]);
                message = message.replace(placeholder, value);
            }
        }

        return TextFormat.colorize(message);
    }

    public String getOpMessage(cn.nukkit.Player player, String key, Object... params) {
        if (player != null && player.isOp()) {
            return getMessage(key, params);
        }
        return "";
    }

    public void reload() {
        loadLanguageFile();
    }
}