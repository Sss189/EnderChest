package com.sss.MiniEnderChest;


import com.sss.MiniEnderChest.worldchest.CommandWorldChest;
import com.sss.MiniEnderChest.worldchest.GlobalStorageManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;

/**
 * 模组主类。
 * 负责初始化配置和注册命令。
 */
@Mod(modid = main.MODID, name = main.NAME)
public class main {
    // 完整的 Mod ID
    public static final String MODID = "pocketenderchestcommand";
    public static final String NAME = "Pocket Ender Chest Command";


    // 模组实例
    @Mod.Instance(MODID)
    public static main instance;

    /**
     * FML预初始化事件处理，用于加载配置。
     */
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // 1. 初始化模组配置 (创建 config/MiniEnderChest/ 文件夹和 permissions.txt)
        ModConfig.init(event.getModConfigurationDirectory());
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        // 2. 初始化世界箱子数据管理器 (将 world_chest_data.dat 放入新文件夹) <-- 关键修正
        GlobalStorageManager.init(ModConfig.getConfigDirectory());
    }

    /**
     * FML服务器启动事件处理，用于注册命令。
     */
    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandEnderChest());
        event.registerServerCommand(new CommandWorldChest());
    }
}