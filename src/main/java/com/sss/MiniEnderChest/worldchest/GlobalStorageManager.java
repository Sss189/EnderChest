package com.sss.MiniEnderChest.worldchest;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class GlobalStorageManager {

    // 去掉 static final 的初始化，改为只是 static
    private static File saveFile;

    // 新增初始化方法，由主类调用
    public static void init(File configDirectory) {
        // 将数据文件放在传入的文件夹内
        saveFile = new File(configDirectory, "world_chest_data.dat");
    }

    public static InventoryBasic loadPlayerChest(UUID playerUUID) {

        InventoryBasic inventory = new InventoryBasic("container.worldchest", false, 9);

        inventory.addInventoryChangeListener((inv) -> savePlayerChest(playerUUID, inv));
        // 增加安全检查，防止未初始化调用
        if (saveFile == null) {
            System.err.println("[MiniEnderChest] Storage manager not initialized!");
            return inventory;
        }

        if (!saveFile.exists()) {
            return inventory;
        }

        try {
            NBTTagCompound rootTag = CompressedStreamTools.readCompressed(new FileInputStream(saveFile));
            if (rootTag.hasKey(playerUUID.toString())) {
                NBTTagList itemsList = rootTag.getTagList(playerUUID.toString(), Constants.NBT.TAG_COMPOUND);
                for (int i = 0; i < itemsList.tagCount(); i++) {
                    NBTTagCompound itemTag = itemsList.getCompoundTagAt(i);
                    int slot = itemTag.getByte("Slot") & 255;
                    if (slot >= 0 && slot < inventory.getSizeInventory()) {
                        inventory.setInventorySlotContents(slot, new ItemStack(itemTag));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inventory;
    }

    public static void savePlayerChest(UUID playerUUID, IInventory inventory) {
        if (saveFile == null) return; // 安全检查

        try {
            NBTTagCompound rootTag;
            if (saveFile.exists()) {
                rootTag = CompressedStreamTools.readCompressed(new FileInputStream(saveFile));
            } else {
                rootTag = new NBTTagCompound();
            }

            NBTTagList itemsList = new NBTTagList();
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    NBTTagCompound itemTag = new NBTTagCompound();
                    itemTag.setByte("Slot", (byte) i);
                    stack.writeToNBT(itemTag);
                    itemsList.appendTag(itemTag);
                }
            }

            rootTag.setTag(playerUUID.toString(), itemsList);
            CompressedStreamTools.writeCompressed(rootTag, new FileOutputStream(saveFile));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}