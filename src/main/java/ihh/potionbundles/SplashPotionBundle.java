package ihh.potionbundles;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Minecraftschurli
 * @version 2021-07-26
 */
public class SplashPotionBundle extends AbstractThrowablePotionBundle {

    @Override
    public void appendHoverText(@Nonnull final ItemStack stack, @Nullable final Level world, @Nonnull final List<Component> tooltip, @Nonnull final TooltipFlag flag) {
        PotionUtils.addPotionTooltip(stack, tooltip, 1.0F);
        super.appendHoverText(stack, world, tooltip, flag);
    }

    @Override
    protected boolean isEnabled() {
        return Config.SERVER.allowSplashPotion.get();
    }

    @Override
    protected void playThrowSound(final Level world, final Player player) {
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
    }
}