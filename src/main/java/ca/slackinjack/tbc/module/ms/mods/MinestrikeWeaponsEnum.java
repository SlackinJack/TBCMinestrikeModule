package ca.slackinjack.tbc.module.ms.mods;

import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

public enum MinestrikeWeaponsEnum {
    GLOCK(new String[]{"Glock"}, 20, 5, new ItemStack[]{new ItemStack(Items.dye, 1, EnumDyeColor.PINK.getDyeDamage()), new ItemStack(Items.stone_hoe)}),
    P2000(new String[]{"P2000"}, 13, 4, new ItemStack[]{new ItemStack(Items.dye, 1, EnumDyeColor.CYAN.getDyeDamage()), new ItemStack(Items.wooden_hoe)}),
    P250(new String[]{"P250"}, 13, 4, new ItemStack[]{new ItemStack(Items.dye, 1, EnumDyeColor.BROWN.getDyeDamage()), new ItemStack(Items.diamond_hoe)}),
    CZ75(new String[]{"CZ75"}, 12, 4, new ItemStack[]{new ItemStack(Items.brick), new ItemStack(Items.iron_hoe)}),
    DE(new String[]{"Desert", "Golden"}, 7, 3, new ItemStack[]{new ItemStack(Items.glowstone_dust), new ItemStack(Items.nether_wart), new ItemStack(Items.golden_hoe)}),
    NOVA(new String[]{"Nova"}, 8, 3, new ItemStack[]{new ItemStack(Items.dye, 1, EnumDyeColor.ORANGE.getDyeDamage()), new ItemStack(Items.golden_axe)}),
    XM(new String[]{"XM1014"}, 7, 3, new ItemStack[]{new ItemStack(Items.diamond), new ItemStack(Items.leather), new ItemStack(Items.diamond_axe)}),
    BIZON(new String[]{"PP-Bizon"}, 64, 5, new ItemStack[]{new ItemStack(Items.dye, 1, EnumDyeColor.BLUE.getDyeDamage()), new ItemStack(Items.wooden_axe)}),
    P90(new String[]{"P90"}, 50, 5, new ItemStack[]{new ItemStack(Items.dye, 1, EnumDyeColor.BLACK.getDyeDamage()), new ItemStack(Items.stone_axe)}),
    GALIL(new String[]{"Galil"}, 35, 5, new ItemStack[]{new ItemStack(Items.dye, 1, EnumDyeColor.LIME.getDyeDamage()), new ItemStack(Items.stone_pickaxe)}),
    FAMAS(new String[]{"FAMAS"}, 25, 5, new ItemStack[]{new ItemStack(Items.clay_ball), new ItemStack(Items.wooden_pickaxe)}),
    AK(new String[]{"AK-47", "Guardian"}, 30, 5, new ItemStack[]{new ItemStack(Items.dye, 1, EnumDyeColor.SILVER.getDyeDamage()), new ItemStack(Items.prismarine_shard), new ItemStack(Items.wooden_shovel)}),
    M4(new String[]{"M4A4", "Enderman"}, 30, 5, new ItemStack[]{new ItemStack(Items.dye, 1, EnumDyeColor.YELLOW.getDyeDamage()), new ItemStack(Items.coal), new ItemStack(Items.stone_shovel)}),
    SG(new String[]{"SG553"}, 30, 5, new ItemStack[]{new ItemStack(Items.dye, 1, EnumDyeColor.PURPLE.getDyeDamage()), new ItemStack(Items.iron_pickaxe)}),
    AUG(new String[]{"AUG"}, 30, 5, new ItemStack[]{new ItemStack(Items.blaze_rod), new ItemStack(Items.golden_pickaxe)}),
    SSG(new String[]{"SSG"}, 10, 4, new ItemStack[]{new ItemStack(Items.dye, 1, EnumDyeColor.LIGHT_BLUE.getDyeDamage()), new ItemStack(Items.diamond_pickaxe)}),
    AWP(new String[]{"AWP"}, 10, 4, new ItemStack[]{new ItemStack(Items.gunpowder), new ItemStack(Items.golden_shovel)}),
    // below are for Game logs only
    HIGH(new String[]{"High"}, 0, 0, null), //frag
    KNIFE(new String[]{"Knife", "Sword"}, 0, 0, null), //Knife_T / Knife_CT
    BAYONET(new String[]{"Bayonet"}, 0, 0, null);

    private final String[] name;
    private final int maxAmmo;
    private final int criticalAmmo;
    private final ItemStack[] items;

    private MinestrikeWeaponsEnum(String[] nameIn, int maxAmmoIn, int criticalAmmoIn, ItemStack[] itemsIn) {
        this.name = nameIn;
        this.maxAmmo = maxAmmoIn;
        this.criticalAmmo = criticalAmmoIn;
        this.items = itemsIn;
    }

    public String[] getName() {
        return this.name;
    }

    public int getMaxAmmo() {
        return this.maxAmmo;
    }

    public int getCriticalAmmo() {
        return this.criticalAmmo;
    }

    public ItemStack[] getItems() {
        return this.items;
    }
}
