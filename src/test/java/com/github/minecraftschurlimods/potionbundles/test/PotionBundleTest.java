package com.github.minecraftschurlimods.potionbundles.test;

import com.github.minecraftschurlimods.potionbundles.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@PrefixGameTestTemplate(false)
@GameTestHolder(PotionBundles.MODID)
public class PotionBundleTest {
    @GameTest(template = "empty_3x3")
    public static void testPotionBundle(GameTestHelper helper) {
        PotionBundleString string = PotionBundleString.fromItem(Items.STRING);
        PotionBundle potionBundle = PotionBundles.POTION_BUNDLE.get();
        int maxUses = potionBundle.getMaxUses();
        MobEffectInstance customEffect = new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20);
        ItemStack bundle = potionBundle.createStack(string, Potions.WATER, List.of(customEffect), null);
        assertUses(bundle, maxUses, "Uses don't use items max uses");
        Player player = setupPlayer(helper, bundle);
        int duration = potionBundle.getUseDuration(bundle, player);
        GameTestSequence sequence = helper.startSequence();
        for (int i = 0; i < maxUses; i++) {
            int index = i + 1;
            sequence.thenExecute(() -> player.setItemInHand(InteractionHand.MAIN_HAND, bundle.use(helper.getLevel(), player, InteractionHand.MAIN_HAND).getObject()))
                    .thenExecuteAfter(duration + 1, () -> {
                        player.stopUsingItem();
                        assertBundleUsed(player, index, maxUses, string, bundle);
                        assertItemsInInventory(player.getInventory(), new ItemStack(Items.GLASS_BOTTLE), index, "Did not return glass bottle");
                        MobEffectInstance effect = player.getEffect(customEffect.getEffect());
                        if (effect == null || effect.getDuration() + 1 != customEffect.getDuration()) {
                            fail("Did not apply effect");
                        }
                    });
        }
        sequence.thenSucceed();
    }

    @GameTest(template = "empty_3x3")
    public static void testSplashPotionBundle(GameTestHelper helper) {
        AbstractThrowablePotionBundle potionBundle = PotionBundles.SPLASH_POTION_BUNDLE.get();
        testThrownPotionBundle(helper, potionBundle);
    }

    @GameTest(template = "empty_3x3")
    public static void testLingeringPotionBundle(GameTestHelper helper) {
        AbstractThrowablePotionBundle potionBundle = PotionBundles.LINGERING_POTION_BUNDLE.get();
        testThrownPotionBundle(helper, potionBundle);
    }

    private static void testThrownPotionBundle(GameTestHelper helper, AbstractThrowablePotionBundle potionBundle) {
        PotionBundleString string = PotionBundleString.fromItem(Items.STRING);
        MobEffectInstance customEffect = new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20);
        int maxUses = potionBundle.getMaxUses();
        ItemStack bundle = potionBundle.createStack(string, Potions.WATER, List.of(customEffect), null);
        assertUses(bundle, maxUses, "Uses don't use items max uses");
        Player player = setupPlayer(helper, bundle);
        GameTestSequence sequence = helper.startSequence();
        for (int i = 0; i < maxUses; i++) {
            int index = i + 1;
            sequence.thenExecute(() -> player.setItemInHand(InteractionHand.MAIN_HAND, bundle.use(helper.getLevel(), player, InteractionHand.MAIN_HAND).getObject()))
                    .thenExecute(() -> {
                        assertBundleUsed(player, index, maxUses, string, bundle);
                        List<ThrownPotion> potions = helper.getEntities(EntityType.POTION, new BlockPos(1, 1, 1), 2);
                        if (potions.isEmpty()) {
                            fail("Did not throw potion");
                        }
                        if (potions.size() > 1) {
                            fail("Threw too many potions");
                        }
                        ThrownPotion thrownPotion = potions.getFirst();
                        ItemStack thrownPotionItem = thrownPotion.getItem();
                        PotionContents potionContents = thrownPotionItem.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                        List<MobEffectInstance> mobEffects = new ArrayList<>();
                        potionContents.forEachEffect(mobEffects::add);
                        if (mobEffects.size() != 1 || !mobEffects.getFirst().equals(customEffect)) {
                            fail("Wrong potion thrown");
                        }
                        if (potionBundle == PotionBundles.SPLASH_POTION_BUNDLE.get()) {
                            if (!thrownPotionItem.is(Items.SPLASH_POTION)) {
                                helper.fail("Wrong potion type thrown");
                            }
                        }
                        if (potionBundle == PotionBundles.LINGERING_POTION_BUNDLE.get()) {
                            if (!thrownPotionItem.is(Items.LINGERING_POTION)) {
                                helper.fail("Wrong potion type thrown");
                            }
                        }
                        thrownPotion.discard();
                    });
        }
        sequence.thenSucceed();
    }

    private static void assertBundleUsed(Player player, int index, int maxUses, PotionBundleString string, ItemStack bundle) {
        ItemStack inHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        assertNotEmpty(inHand, "Bundle should not be empty");
        if (index == maxUses) {
            assertSameItem(inHand, string.toItemStack(), "Bundle should be empty and returned the string");
        } else {
            assertSameItem(inHand, bundle, "Wrong item in hand");
            assertUses(inHand, maxUses - index, "Uses not decremented");
        }
    }

    private static void assertSameItem(ItemStack stack, ItemStack expectes, String message) {
        if (!ItemStack.isSameItem(stack, expectes)) {
            fail(message);
        }
    }

    private static void assertNotEmpty(ItemStack stack, String message) {
        if (stack.isEmpty()) {
            fail(message);
        }
    }

    private static void assertUses(ItemStack bundle, int expectedUses, String message) {
        if (PotionBundleUtils.getUses(bundle) != expectedUses) {
            fail(message);
        }
    }

    private static void assertItemsInInventory(Inventory inventory, ItemStack item, int count, String message) {
        int slot = inventory.findSlotMatchingItem(item);
        if (slot == -1 || inventory.getItem(slot).getCount() != count) {
            fail(message);
        }
    }

    @Contract("_ -> fail")
    private static void fail(String message) {
        throw new GameTestAssertException(message);
    }

    private static @NotNull Player setupPlayer(GameTestHelper helper, ItemStack bundle) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(helper.absoluteVec(Vec3.ZERO));
        helper.getLevel().addFreshEntity(player);
        player.setItemInHand(InteractionHand.MAIN_HAND, bundle);
        return player;
    }
}
