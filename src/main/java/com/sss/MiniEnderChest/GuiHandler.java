package com.sss.MiniEnderChest; // 修改为你的实际包名

import com.sss.MiniEnderChest.worldchest.ContainerWorldChest;
import com.sss.MiniEnderChest.worldchest.GlobalStorageManager;
import com.sss.MiniEnderChest.worldchest.GuiWorldChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

    public static final int GUI_WORLD_CHEST = 101; // 定义一个唯一的 ID

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GUI_WORLD_CHEST) {
            // 获取之前写好的 InventoryBasic
            InventoryBasic chest = GlobalStorageManager.loadPlayerChest(player.getUniqueID());
            return new ContainerWorldChest(player.inventory, chest, player);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GUI_WORLD_CHEST) {
            InventoryBasic chest = GlobalStorageManager.loadPlayerChest(player.getUniqueID());
            return new GuiWorldChest(player.inventory, chest);
        }
        return null;
    }
}