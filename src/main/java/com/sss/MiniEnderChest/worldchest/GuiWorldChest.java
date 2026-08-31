package com.sss.MiniEnderChest.worldchest;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.WorldSummary;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiWorldChest extends GuiContainer {

    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("pocketenderchestcommand", "textures/gui/world_chest_gui.png");
    private static final ResourceLocation DEFAULT_ICON = new ResourceLocation("pocketenderchestcommand", "textures/gui/default_icon.png");

    private final IconData[] displayIcons = new IconData[3];
    private final List<ResourceLocation> dynamicResources = new ArrayList<>();

    public GuiWorldChest(InventoryPlayer playerInv, IInventory chestInv) {
        super(new ContainerWorldChest(playerInv, chestInv, Minecraft.getMinecraft().player));
        this.xSize = 176;
        this.ySize = 222;
    }

    @Override
    public void initGui() {
        super.initGui();
        clearDynamicTextures();
        loadWorldIcons();
    }

    private void loadWorldIcons() {
        // 1. 获取并排序存档列表
        List<WorldSummary> saveList;
        try {
            saveList = this.mc.getSaveLoader().getSaveList();
            saveList.sort((w1, w2) -> Long.compare(w2.getLastTimePlayed(), w1.getLastTimePlayed()));
        } catch (Exception e) {
            saveList = new ArrayList<>();
        }

        WorldSummary currentWorld = null;
        List<WorldSummary> others = new ArrayList<>();

        // 2. 尝试寻找“当前正在游玩的单人存档”
        String currentFolderName = "";
        if (this.mc.isIntegratedServerRunning() && this.mc.getIntegratedServer() != null) {
            currentFolderName = this.mc.getIntegratedServer().getFolderName();
        }

        // 3. 分离数据
        for (WorldSummary ws : saveList) {
            if (!currentFolderName.isEmpty() && ws.getFileName().equals(currentFolderName)) {
                currentWorld = ws;
            } else {
                others.add(ws);
            }
        }

        // 4. 智能分配位置 (核心修改逻辑)
        if (currentWorld != null) {
            // --- 情况A：在单人游戏中 ---
            // 中间：当前存档
            // 左边：其他存档第1个
            // 右边：其他存档第2个
            displayIcons[1] = createIconData(currentWorld);
            displayIcons[0] = createIconData(others.size() > 0 ? others.get(0) : null);
            displayIcons[2] = createIconData(others.size() > 1 ? others.get(1) : null);
        } else {
            // --- 情况B：在服务器中 (或者当前存档未保存) ---
            // 策略：优先把最好的截图放中间
            // 中间：其他存档第1个 (最近玩的)
            // 左边：其他存档第2个
            // 右边：其他存档第3个
            displayIcons[1] = createIconData(others.size() > 0 ? others.get(0) : null);
            displayIcons[0] = createIconData(others.size() > 1 ? others.get(1) : null);
            displayIcons[2] = createIconData(others.size() > 2 ? others.get(2) : null);
        }
    }

    private IconData createIconData(WorldSummary world) {
        if (world == null) return new IconData(null);
        File iconFile = this.mc.getSaveLoader().getFile(world.getFileName(), "icon.png");

        if (iconFile.exists() && iconFile.isFile()) {
            try {
                BufferedImage image = ImageIO.read(iconFile);
                DynamicTexture texture = new DynamicTexture(image);
                String name = "world_icon_" + world.getFileName().toLowerCase().replaceAll("[^a-z0-9_]", "_");
                ResourceLocation rl = this.mc.getTextureManager().getDynamicTextureLocation(name, texture);
                dynamicResources.add(rl);
                return new IconData(rl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new IconData(null);
    }

    private void clearDynamicTextures() {
        for (ResourceLocation rl : dynamicResources) {
            this.mc.getTextureManager().deleteTexture(rl);
        }
        dynamicResources.clear();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        clearDynamicTextures();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;
        int[] iconX = {16, 56, 97}; // 左, 中, 右 的X坐标
        int[] iconY = {88, 29, 56};  // 左, 中, 右 的Y坐标
        int size = 64;

        GlStateManager.enableBlend();

        renderIcon(guiLeft + iconX[0], guiTop + iconY[0], displayIcons[0], size);
        renderIcon(guiLeft + iconX[2], guiTop + iconY[2], displayIcons[2], size);

        renderIcon(guiLeft + iconX[1], guiTop + iconY[1], displayIcons[1], size);

        this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
        drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0, 0, this.xSize, this.ySize, 176, 222);

        GlStateManager.disableBlend();
    }

    private void renderIcon(int x, int y, IconData data, int size) {
        ResourceLocation tex = (data != null && data.rl != null) ? data.rl : DEFAULT_ICON;
        this.mc.getTextureManager().bindTexture(tex);
        drawModalRectWithCustomSizedTexture(x, y, 0, 0, size, size, 64, 64);
    }

    private static class IconData {
        ResourceLocation rl;
        IconData(ResourceLocation rl) { this.rl = rl; }
    }
}