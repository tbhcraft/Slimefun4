package io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines;

import java.util.Comparator;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import io.github.bakedlibs.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.implementation.items.multiblocks.OreWasher;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;

import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;

/**
 * The {@link ElectricDustWasher} serves as an electrical {@link OreWasher}.
 * 
 * @author TheBusyBiscuit
 * 
 * @see OreWasher
 *
 */
public class ElectricDustWasher extends AContainer {

    private final OreWasher oreWasher = SlimefunItems.ORE_WASHER.getItem(OreWasher.class);
    private final boolean legacyMode;

    @ParametersAreNonnullByDefault
    public ElectricDustWasher(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);

        legacyMode = Slimefun.getCfg().getBoolean("options.legacy-dust-washer");
    }

    private static final int[] border = { 4, 13, 31, 40 };
    private static final int[] inputBorder = { 0, 1, 2, 3, 12, 30, 36, 37, 38, 39 };
    private static final int[] outputBorder = { 5, 6, 7, 8, 14, 32, 41, 42, 43, 44 };
    
    @Override
    public int[] getInputSlots() {
        return new int[] { 9, 10, 11, 18, 19, 20, 21, 27, 28, 29 };
    }

    @Override
    public int[] getOutputSlots() {
        return new int[] { 15, 16,17, 23, 24, 25, 26, 33, 34, 35 };
    }

    @Nonnull
    private Comparator<Integer> compareSlots(@Nonnull DirtyChestMenu menu) {
        return Comparator.comparingInt(slot -> menu.getItemInSlot(slot).getAmount());
    }

    @Override
    protected void constructMenu(BlockMenuPreset preset) {
        for (int i : border) {
            preset.addItem(i, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }

        for (int i : inputBorder) {
            preset.addItem(i, ChestMenuUtils.getInputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        }

        for (int i : outputBorder) {
            preset.addItem(i, ChestMenuUtils.getOutputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        }

        preset.addItem(22, new CustomItemStack(Material.BLACK_STAINED_GLASS_PANE, " "), ChestMenuUtils.getEmptyClickHandler());

        for (int i : getOutputSlots()) {
            preset.addMenuClickHandler(i, ChestMenuUtils.getDefaultOutputHandler());
        }
    }

    @Override
    public ItemStack getProgressBar() {
        return new ItemStack(Material.GOLDEN_SHOVEL);
    }

    @Override
    protected MachineRecipe findNextRecipe(BlockMenu menu) {
        for (int slot : getInputSlots()) {
            ItemStack input = menu.getItemInSlot(slot);
            MachineRecipe recipe = null;
            if (SlimefunUtils.isItemSimilar(input, SlimefunItems.SIFTED_ORE, true, false)) {
                if (!legacyMode && !hasFreeSlot(menu)) {
                    return null;
                }

                recipe = new MachineRecipe(4 / getSpeed(), new ItemStack[] { SlimefunItems.SIFTED_ORE }, new ItemStack[] { oreWasher.getRandomDust() });

                if (!legacyMode || menu.fits(recipe.getOutput()[0], getOutputSlots())) {
                    menu.consumeItem(slot);
                    return recipe;
                }
            } else if (SlimefunUtils.isItemSimilar(input, SlimefunItems.PULVERIZED_ORE, true)) {
                recipe = new MachineRecipe(4 / getSpeed(), new ItemStack[] { SlimefunItems.PULVERIZED_ORE }, new ItemStack[] { SlimefunItems.PURE_ORE_CLUSTER });
            } else if (SlimefunUtils.isItemSimilar(input, new ItemStack(Material.SAND), true)) {
                recipe = new MachineRecipe(4 / getSpeed(), new ItemStack[] { new ItemStack(Material.SAND) }, new ItemStack[] { SlimefunItems.SALT });
            }

            if (recipe != null && menu.fits(recipe.getOutput()[0], getOutputSlots())) {
                menu.consumeItem(slot);
                return recipe;
            }
        }

        return null;
    }

    private boolean hasFreeSlot(BlockMenu menu) {
        for (int slot : getOutputSlots()) {
            ItemStack item = menu.getItemInSlot(slot);

            if (item == null || item.getType() == Material.AIR) {
                return true;
            }
        }

        return false;
    }

    @Override
    public @Nonnull String getMachineIdentifier() {
        return "ELECTRIC_DUST_WASHER";
    }

}
