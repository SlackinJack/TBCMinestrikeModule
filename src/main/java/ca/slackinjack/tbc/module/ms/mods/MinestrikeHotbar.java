package ca.slackinjack.tbc.module.ms.mods;

import ca.slackinjack.tbc.TBC;
import ca.slackinjack.tbc.module.ms.Minestrike;
import ca.slackinjack.tbc.module.ms.TBCMinestrikeModule;
import ca.slackinjack.tbc.utils.chat.TextFormattingEnum;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class MinestrikeHotbar {

    private final TBC INSTANCE;
    private final TBCMinestrikeModule MODULE_INSTANCE;
    private final Minestrike MINESTRIKE;
    private final Minecraft MINECRAFT = Minecraft.getMinecraft();
    private int currentAmmo;
    private int totalAmmo;
    private final String ammoSeparatorText = "/";
    private final int iconWidth = 256;

    private final String modResourceLocation = "tbcminestrikemodule";
    private final String hotbarResourceLocation = "textures/guns/hotbar/";
    private final String gunsResourceLocation = "textures/guns/";
    private final String imageFileExt = ".png";

    public MinestrikeHotbar(TBC tbcIn, TBCMinestrikeModule modIn, Minestrike msIn) {
        INSTANCE = tbcIn;
        MODULE_INSTANCE = modIn;
        MINESTRIKE = msIn;
    }

    public void onRenderHotbar(RenderGameOverlayEvent.Pre e) {
        if (!MINESTRIKE.getPlayerIsDead()) {
            this.renderHotbarAndAmmo(e.resolution);
        }
    }

    public void updateCurrentAmmo(String currentAmmoIn) {
        this.currentAmmo = Integer.valueOf(currentAmmoIn);
    }

    public void updateTotalAmmo(String totalAmmoIn) {
        this.totalAmmo = Integer.valueOf(totalAmmoIn);
    }

    private void renderHotbarAndAmmo(ScaledResolution resIn) {
        boolean primaryIsPresent = false;
        ItemStack primaryItem = null;
        if (MINECRAFT.thePlayer.inventory.getStackInSlot(0) != null) {
            primaryIsPresent = true;
            primaryItem = MINECRAFT.thePlayer.inventory.getStackInSlot(0);
        }

        boolean secondaryIsPresent = false;
        ItemStack secondaryItem = null;
        if (MINECRAFT.thePlayer.inventory.getStackInSlot(1) != null) {
            secondaryIsPresent = true;
            secondaryItem = MINECRAFT.thePlayer.inventory.getStackInSlot(1);
        }

        boolean tertiaryIsPresent = false;
        ItemStack tertiaryItem = null;
        if (MINECRAFT.thePlayer.inventory.getStackInSlot(2) != null) {
            tertiaryIsPresent = true;
            tertiaryItem = MINECRAFT.thePlayer.inventory.getStackInSlot(2);
        }

        boolean throwableIsPresent = false;
        Map<Integer, ItemStack> throwableItems = new ConcurrentHashMap();
        for (int i = 3; i < 8; i++) {
            if (MINECRAFT.thePlayer.inventory.getStackInSlot(i) != null) {
                throwableIsPresent = true;
                throwableItems.put(i, MINECRAFT.thePlayer.inventory.getStackInSlot(i));
            } else {
                throwableItems.put(i, new ItemStack(Blocks.air));
            }
        }

        boolean objIsPresent = false;
        ItemStack objItem = null;
        if (MINECRAFT.thePlayer.inventory.getStackInSlot(8) != null && MINECRAFT.thePlayer.inventory.getStackInSlot(8).getItem() != Items.compass) {
            objIsPresent = true;
            objItem = MINECRAFT.thePlayer.inventory.getStackInSlot(8);
        }

        int itemSlotHeight = 48;
        int itemSlotWidth = 96;
        int itemSlotsTotal = 0;

        if (primaryIsPresent) {
            itemSlotsTotal += 1;
        }
        if (secondaryIsPresent) {
            itemSlotsTotal += 1;
        }
        if (tertiaryIsPresent) {
            itemSlotsTotal += 1;
        }
        if (throwableIsPresent) {
            itemSlotsTotal += 1;
        }
        if (objIsPresent) {
            itemSlotsTotal += 1;
        }

        int windowHeight = resIn.getScaledHeight();
        int windowWidth = resIn.getScaledWidth();
        int backgroundRight = itemSlotWidth;
        int backgroundBottom = itemSlotsTotal * itemSlotHeight;

        GlStateManager.pushMatrix();
        GlStateManager.scale(INSTANCE.getUtilsPublic().getConfigLoader().getMSHotBarScale(), INSTANCE.getUtilsPublic().getConfigLoader().getMSHotBarScale(), 1.0F);
        GlStateManager.translate((windowWidth * (1.0D / INSTANCE.getUtilsPublic().getConfigLoader().getMSHotBarScale())) - backgroundRight, (windowHeight * (1.0D / INSTANCE.getUtilsPublic().getConfigLoader().getMSHotBarScale())) - backgroundBottom, 0.0D);
        GlStateManager.translate(INSTANCE.getUtilsPublic().getConfigLoader().getMSHotBarAdjustmentX(), INSTANCE.getUtilsPublic().getConfigLoader().getMSHotBarAdjustmentY(), 0.0D);

        int drawnItemSlots = 0;

        if (objIsPresent && objItem != null) {
            String iconName = "";
            if (objItem.getItem() == Items.shears) {
                iconName = "Kit";
            } else if (objItem.getItem() == Items.golden_sword) {
                iconName = "C4";
            }

            //todo compass
            if (!iconName.isEmpty()) {
                drawnItemSlots += 1;
                boolean highlighted = MINECRAFT.thePlayer.inventory.currentItem == 8;

                int x = (itemSlotWidth / 2);
                int y = backgroundBottom - (drawnItemSlots * itemSlotHeight);
                if (highlighted) {
                    this.drawItemStackName(objItem, 0, y);
                }

                this.drawTexturedModalRect(new ResourceLocation(this.modResourceLocation, this.hotbarResourceLocation + iconName + this.imageFileExt), x, y, this.iconWidth, false, highlighted);
                this.drawHotbarNumber("9", backgroundRight, y);
            }
        }

        if (throwableIsPresent) {
            int throwablesDrawn = 0;
            drawnItemSlots += 1;
            for (Map.Entry<Integer, ItemStack> entry : throwableItems.entrySet()) {
                String iconName = "";
                if (entry.getValue().getItem() == Items.potato) {
                    iconName = "Smoke";
                } else if (entry.getValue().getItem() == Items.apple) {
                    iconName = "High";
                } else if (entry.getValue().getItem() == Items.carrot) {
                    iconName = "Flash";
                } else if (entry.getValue().getItem() == Items.cooked_porkchop) {
                    iconName = "Molotov";
                } else if (entry.getValue().getItem() == Items.porkchop) {
                    iconName = "Incendiary";
                } else {
                    //blank
                }

                if (!iconName.isEmpty()) {
                    boolean highlighted = (MINECRAFT.thePlayer.inventory.currentItem == entry.getKey());

                    int x = (itemSlotWidth / 2);
                    int y = backgroundBottom - (drawnItemSlots * itemSlotHeight);
                    if (highlighted) {
                        this.drawItemStackName(entry.getValue(), 0, y);
                    }

                    this.drawTexturedModalRect(new ResourceLocation(this.modResourceLocation, this.hotbarResourceLocation + iconName + this.imageFileExt), (throwablesDrawn * 22) + 16, y, this.iconWidth, true, highlighted);
                    this.drawHotbarNumber(Integer.toString(4 + throwablesDrawn), (throwablesDrawn * 22) + 32, y);
                    ++throwablesDrawn;
                } else {
                    ++throwablesDrawn;
                }
            }
        }

        if (tertiaryIsPresent && tertiaryItem != null) {
            String iconName = "";
            if (tertiaryItem.getItem() == Items.iron_axe) {
                iconName = "Knife_T";
            } else if (tertiaryItem.getItem() == Items.iron_sword) {
                iconName = "Knife_CT";
            } else if (tertiaryItem.getItem() == Items.diamond_sword) {
                iconName = "Bayonet";
            }

            if (!iconName.isEmpty()) {
                drawnItemSlots += 1;
                boolean highlighted = MINECRAFT.thePlayer.inventory.currentItem == 2;

                int x = (itemSlotWidth / 2);
                int y = backgroundBottom - (drawnItemSlots * itemSlotHeight);
                if (highlighted) {
                    this.drawItemStackName(tertiaryItem, 0, y);
                }

                this.drawTexturedModalRect(new ResourceLocation(this.modResourceLocation, this.gunsResourceLocation + iconName + this.imageFileExt), x, y, this.iconWidth, false, highlighted);
                this.drawHotbarNumber("3", backgroundRight, y);
            }
        }

        if (secondaryIsPresent && secondaryItem != null) {
            String iconName = "";

            outer:
            for (MinestrikeWeaponsEnum e : MinestrikeWeaponsEnum.values()) {
                for (ItemStack i : e.getItems()) {
                    if (secondaryItem.getItem() == Items.dye) {
                        if (secondaryItem.getItem() == i.getItem()) {
                            if (i.getItem().getMetadata(i) == secondaryItem.getItem().getMetadata(secondaryItem)) {
                                iconName = e.getName()[0];
                                break outer;
                            }
                        }
                    } else {
                        if (secondaryItem.getItem() == i.getItem()) {
                            iconName = e.getName()[0];
                            break outer;
                        }
                    }
                }
            }
            if (!iconName.isEmpty()) {
                drawnItemSlots += 1;
                boolean highlighted = MINECRAFT.thePlayer.inventory.currentItem == 1;

                int x = (itemSlotWidth / 2);
                int y = backgroundBottom - (drawnItemSlots * itemSlotHeight);
                if (highlighted) {
                    this.drawItemStackName(secondaryItem, 0, y);
                }

                this.drawTexturedModalRect(new ResourceLocation(this.modResourceLocation, this.gunsResourceLocation + iconName + this.imageFileExt), x, y, this.iconWidth, false, highlighted);
                this.drawHotbarNumber("2", backgroundRight, y);
            }
        }

        if (primaryIsPresent && primaryItem != null) {
            String iconName = "";

            outer:
            for (MinestrikeWeaponsEnum e : MinestrikeWeaponsEnum.values()) {
                if (e.getItems() != null) {
                    for (ItemStack i : e.getItems()) {
                        if (primaryItem.getItem() == Items.dye) {
                            if (primaryItem.getItem() == i.getItem()) {
                                if (i.getItem().getMetadata(i) == primaryItem.getItem().getMetadata(primaryItem)) {
                                    iconName = e.getName()[0];
                                    break outer;
                                }
                            }
                        } else {
                            if (primaryItem.getItem() == i.getItem()) {
                                iconName = e.getName()[0];
                                break outer;
                            }
                        }
                    }
                }
            }
            if (!iconName.isEmpty()) {
                drawnItemSlots += 1;
                boolean highlighted = MINECRAFT.thePlayer.inventory.currentItem == 0;

                int x = (itemSlotWidth / 2);
                int y = backgroundBottom - (drawnItemSlots * itemSlotHeight);
                if (highlighted) {
                    this.drawItemStackName(primaryItem, 0, y);
                }

                this.drawTexturedModalRect(new ResourceLocation(this.modResourceLocation, this.gunsResourceLocation + iconName + this.imageFileExt), x, y, this.iconWidth, false, highlighted);

                this.drawHotbarNumber("1", backgroundRight, y);
            }
        }

        if (MINECRAFT.thePlayer.inventory.currentItem >= 0 && MINECRAFT.thePlayer.inventory.currentItem < 2 && !MINECRAFT.thePlayer.capabilities.allowFlying) {
            ItemStack selectedItem = MINECRAFT.thePlayer.inventory.getStackInSlot(MINECRAFT.thePlayer.inventory.currentItem);
            if (selectedItem != null) {
                int criticalAmmo = 0;

                if (selectedItem.getItem() == Items.dye) {
                    outer:
                    for (MinestrikeWeaponsEnum e : MinestrikeWeaponsEnum.values()) {
                        for (ItemStack i : e.getItems()) {
                            if (i.getItem().getMetadata(i) == selectedItem.getItem().getMetadata(selectedItem)) {
                                criticalAmmo = e.getCriticalAmmo();
                                break outer;
                            }
                        }
                    }
                } else {
                    outer:
                    for (MinestrikeWeaponsEnum e : MinestrikeWeaponsEnum.values()) {
                        if (e.getItems() != null) {
                            ItemStack[] currentItems = e.getItems();
                            for (ItemStack i : currentItems) {
                                if (selectedItem.getItem() == i.getItem()) {
                                    criticalAmmo = e.getCriticalAmmo();
                                    break outer;
                                }
                            }
                        }
                    }
                }

                if (criticalAmmo > 0) {
                    String ammoText = this.currentAmmo + this.ammoSeparatorText + this.totalAmmo;
                    int ammoTextLength = MINECRAFT.fontRendererObj.getStringWidth(ammoText);
                    int ammoBackgroundRight = backgroundRight;
                    int ammoBackgroundBottom = backgroundBottom;
                    int textYLocation = ammoBackgroundBottom - 12;

                    int ammoColor = 0xffffff;
                    if (this.currentAmmo <= criticalAmmo) {
                        ammoColor = 0xaa0000;
                    }

                    MINECRAFT.fontRendererObj.drawStringWithShadow(ammoText, ammoBackgroundRight - 8 - ammoTextLength, textYLocation, ammoColor);
                }
            }
        }

        GlStateManager.popMatrix();
    }

    private void drawItemStackName(ItemStack itemstackIn, int x, int y) {
        int endingX = (x + 96);
        String nameToDraw = itemstackIn.getDisplayName();
        GlStateManager.pushMatrix();
        GlStateManager.scale((0.5F / INSTANCE.getUtilsPublic().getConfigLoader().getMSHotBarScale()), (0.5F / INSTANCE.getUtilsPublic().getConfigLoader().getMSHotBarScale()), 1.0F);

        float hotbarScale = (float) INSTANCE.getUtilsPublic().getConfigLoader().getMSHotBarScale();

        MINECRAFT.fontRendererObj.drawStringWithShadow(nameToDraw, (endingX * (2 * hotbarScale)) - MINECRAFT.fontRendererObj.getStringWidth(nameToDraw) - 8, (y + 24) * (2 * hotbarScale), 0xFFFFFF);
        GlStateManager.popMatrix();
    }

    private void drawTexturedModalRect(ResourceLocation resourceIn, double x, double y, int imgSize, boolean quarterSized, boolean hightlighted) {
        GlStateManager.pushMatrix();
        MINECRAFT.getTextureManager().bindTexture(resourceIn);
        float f = 0.00390625F * (256.0F / imgSize);
        if (hightlighted) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            GlStateManager.color(0.45F, 0.45F, 0.45F, 0.45F);
        }

        GlStateManager.translate(x, y, 0);

        double minX = 0.0D;
        double minY = 0.0D;
        double maxX = imgSize / (quarterSized ? 4 : 2);
        double maxY = imgSize / (quarterSized ? 4 : 2);
        double minTex = 0.0D;
        double maxTex = imgSize * f;
        GlStateManager.translate(-maxX / 2, -maxY / 2, 0.0D);

        INSTANCE.getUtilsPublic().drawBoxOnScreenWithTexture(minX, minY, maxX, maxY, minTex, maxTex);
        GlStateManager.popMatrix();
    }

    private void drawHotbarNumber(String stringIn, int x, int y) {
        MINECRAFT.fontRendererObj.drawStringWithShadow(stringIn, x - 8 - MINECRAFT.fontRendererObj.getStringWidth(stringIn), y, TextFormattingEnum.WHITE.getRGBValue());
    }
}
