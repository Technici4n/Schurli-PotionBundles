package com.github.minecraftschurlimods.potionbundles;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;

import java.util.List;

public class LingeringPotionBundle extends AbstractThrowablePotionBundle {
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).addPotionTooltip(tooltip::add, 0.25F, context.tickRate());
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    protected void playThrowSound(Level world, Player player) {
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LINGERING_POTION_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
    }
}
