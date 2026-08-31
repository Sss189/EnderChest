package com.sss.MiniEnderChest;

import java.util.Collections;
import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class CommandEnderChest extends CommandBase {

    @Override
    public String getName() { return "enderchest"; }

    @Override
    public String getUsage(ICommandSender sender) { return "/enderchest"; }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        // 只需要是玩家实例即可，权限等级由配置控制
        return sender instanceof EntityPlayerMP;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayerMP)) {
            throw new CommandException("commands.generic.playeronly");
        }

        EntityPlayerMP player = (EntityPlayerMP) sender;
        int dimensionId = player.dimension;

        ResourceLocation requiredAdvancementRL = ModConfig.getEnderChestLock(dimensionId);

        // 1. 检查配置规则和全局限制
        if (requiredAdvancementRL == null) {
            if (ModConfig.RESTRICT_UNCONFIGURED) {
                // 未配置且全局限制为 true (白名单模式)，禁止访问
                ITextComponent errorMsg = new TextComponentTranslation("command.unavailable");
                sender.sendMessage(errorMsg);
                return;
            }
        }
        // 2. 如果有配置规则，检查进度
        else {
            Advancement advancement = server.getAdvancementManager().getAdvancement(requiredAdvancementRL);
            boolean isDone = false;

            if (advancement != null) {
                AdvancementProgress progress = player.getAdvancements().getProgress(advancement);
                isDone = progress != null && progress.isDone();
            }

            if (!isDone) {
                // 构建包含进度名称的错误消息
                ITextComponent advancementTitle;

                if (advancement != null && advancement.getDisplay() != null) {
                    advancementTitle = advancement.getDisplay().getTitle();
                } else {
                    advancementTitle = new TextComponentString(requiredAdvancementRL.toString());
                }

                // 使用定制翻译键，传入进度名称作为参数
                ITextComponent errorMsg = new TextComponentTranslation("command.pocketchest.locked", advancementTitle);
                sender.sendMessage(errorMsg);
                return;
            }
        }

        // 3. 所有检查通过，打开末影箱
        InventoryEnderChest enderChest = player.getInventoryEnderChest();
        player.displayGUIChest(enderChest);
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("ec");
    }
}