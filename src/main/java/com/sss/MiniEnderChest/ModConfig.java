package com.sss.MiniEnderChest;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.ResourceLocation;

public class ModConfig {

    public static final Map<Integer, ResourceLocation> ENDER_CHEST_LOCKS = new HashMap<>();
    public static final Map<Integer, ResourceLocation> WORLD_CHEST_LOCKS = new HashMap<>();

    public static boolean RESTRICT_UNCONFIGURED = false;

    private static final String FOLDER_NAME = "MiniEnderChest";
    private static final String CONFIG_FILE_NAME = "permissions.txt";
    private static File configDirectory;

    public static void init(File rootConfigDir) {
        configDirectory = new File(rootConfigDir, FOLDER_NAME);
        if (!configDirectory.exists()) {
            configDirectory.mkdirs();
        }

        File configFile = new File(configDirectory, CONFIG_FILE_NAME);

        if (!configFile.exists()) {
            try {
                if (configFile.createNewFile()) {
                    writeDefaultConfig(configFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        loadConfigData(configFile);
    }

    public static File getConfigDirectory() {
        return configDirectory;
    }

    private static void writeDefaultConfig(File configFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(configFile), StandardCharsets.UTF_8))) {

            // 写入第一行配置开关
            writer.write("# If true: Commands are DISABLED in dimensions not listed below (Whitelist mode).");
            writer.newLine();
            writer.write("# If false: Commands are ALLOWED in dimensions not listed below (Restriction mode).");
            writer.newLine();
            writer.write("restrict_unconfigured:false");
            writer.newLine();
            writer.newLine();

            writer.write("# -----------------------------------------------------------");
            writer.newLine();
            writer.write("# Format: [Prefix:]DimensionID:ModID:AdvancementID");
            writer.newLine();
            writer.write("# Prefixes: 'w:' for WorldChest, 'e:' (optional) for EnderChest");
            writer.newLine();
            writer.write("# -----------------------------------------------------------");
            writer.newLine();
            writer.write("# Examples:");
            writer.newLine();
            writer.write("# w:0:minecraft:adventure/sleep_in_bed");
            writer.newLine();

        }
    }

    private static void loadConfigData(File configFile) {
        ENDER_CHEST_LOCKS.clear();
        WORLD_CHEST_LOCKS.clear();
        // 重置为默认值，防止读取空文件时状态错误
        RESTRICT_UNCONFIGURED = true;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(configFile), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                // 1. 读取全局开关 (第一行或任意位置)
                if (line.toLowerCase().startsWith("restrict_unconfigured:")) {
                    String boolStr = line.split(":")[1].trim();
                    RESTRICT_UNCONFIGURED = Boolean.parseBoolean(boolStr);
                    continue; // 读取完开关后跳过后续解析
                }

                try {
                    boolean isWorldChest = false;
                    if (line.toLowerCase().startsWith("w:")) {
                        isWorldChest = true;
                        line = line.substring(2);
                    } else if (line.toLowerCase().startsWith("e:")) {
                        line = line.substring(2);
                    }

                    String[] parts = line.split(":", 3);
                    if (parts.length == 3) {
                        int dimensionId = Integer.parseInt(parts[0].trim());
                        String modId = parts[1].trim();
                        String advancementPath = parts[2].trim();
                        ResourceLocation advancementRL = new ResourceLocation(modId, advancementPath);

                        if (isWorldChest) {
                            WORLD_CHEST_LOCKS.put(dimensionId, advancementRL);
                        } else {
                            ENDER_CHEST_LOCKS.put(dimensionId, advancementRL);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[MiniEnderChest] Error parsing config line: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ResourceLocation getEnderChestLock(int dimensionId) {
        return ENDER_CHEST_LOCKS.get(dimensionId);
    }

    public static ResourceLocation getWorldChestLock(int dimensionId) {
        return WORLD_CHEST_LOCKS.get(dimensionId);
    }
}