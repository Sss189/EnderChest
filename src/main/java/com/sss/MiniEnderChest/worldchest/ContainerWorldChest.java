package com.sss.MiniEnderChest.worldchest;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerWorldChest extends Container {
    private final IInventory worldChestInventory;
    private final int numRows;

    public ContainerWorldChest(IInventory playerInv, IInventory chestInv, EntityPlayer player) {
        this.worldChestInventory = chestInv;
        this.numRows = chestInv.getSizeInventory() / 9;
        chestInv.openInventory(player);
        int i = (this.numRows - 4) * 18;

        // 1. 添加世界箱子的槽位 (这里假设是 1 行 9 格，和你的 InventoryBasic 9 对应)
        // 如果你的 GUI 设计位置不同，请调整 x, y 坐标
        for (int j = 0; j < this.numRows; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlotToContainer(new Slot(chestInv, k + j * 9, 8 + k * 18, 54 + j * 18));
            }
        }

        // 2. 添加玩家背包
        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlotToContainer(new Slot(playerInv, j1 + l * 9 + 9, 8 + j1 * 18, 194 + l * 18 + i));
            }
        }

        // 3. 添加玩家快捷栏
        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlotToContainer(new Slot(playerInv, i1, 8 + i1 * 18, 252 + i));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return this.worldChestInventory.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        // 标准的 Shift+点击 逻辑
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < this.numRows * 9) {
                if (!this.mergeItemStack(itemstack1, this.numRows * 9, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, this.numRows * 9, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }
        return itemstack;
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        this.worldChestInventory.closeInventory(playerIn);
    }
}