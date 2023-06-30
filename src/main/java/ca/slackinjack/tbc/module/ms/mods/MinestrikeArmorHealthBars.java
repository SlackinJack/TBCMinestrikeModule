package ca.slackinjack.tbc.module.ms.mods;

import ca.slackinjack.tbc.TBC;
import ca.slackinjack.tbc.module.ms.Minestrike;
import ca.slackinjack.tbc.module.ms.TBCMinestrikeModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class MinestrikeArmorHealthBars {

    private final TBC INSTANCE;
    private final TBCMinestrikeModule MODULE_INSTANCE;
    private final Minestrike MINESTRIKE;
    private final Minecraft mc = Minecraft.getMinecraft();

    public MinestrikeArmorHealthBars(TBC tbcIn, TBCMinestrikeModule modIn, Minestrike MINESTRIKEIn) {
        INSTANCE = tbcIn;
        MODULE_INSTANCE = modIn;
        MINESTRIKE = MINESTRIKEIn;
    }

    public void onRenderHealth(RenderGameOverlayEvent.Pre e) {
        if (!this.mc.thePlayer.capabilities.allowFlying) {
            this.renderHealthBar(e.resolution);
        }
    }

    private void renderHealthBar(ScaledResolution resIn) {
        GlStateManager.pushMatrix();

        int windowWidth = resIn.getScaledWidth();
        int backgroundLeft = 0;
        int backgroundTop = 0;
        int backgroundRight = 256;
        int backgroundBottom = 32;

        GlStateManager.scale(INSTANCE.getUtilsPublic().getConfigLoader().getMSHealthIndicatorScale(), INSTANCE.getUtilsPublic().getConfigLoader().getMSHealthIndicatorScale(), 1.0D);
        GlStateManager.translate((windowWidth * (1.0D / INSTANCE.getUtilsPublic().getConfigLoader().getMSHealthIndicatorScale())) - backgroundRight, 0.0D, 0.0D);
        GlStateManager.translate(INSTANCE.getUtilsPublic().getConfigLoader().getMSHealthBarsAdjustmentX(), INSTANCE.getUtilsPublic().getConfigLoader().getMSHealthBarsAdjustmentY(), 0.0D);

        INSTANCE.getUtilsPublic().drawBoxOnScreen(backgroundLeft, backgroundTop, backgroundRight, backgroundBottom, Integer.MIN_VALUE);

        double playerArmorFrac = 0.0D;
        double playerHealth = this.mc.thePlayer.getHealth() / this.mc.thePlayer.getMaxHealth();
        double playerHealthFrac = Math.round(playerHealth * 100);

        if (this.mc.thePlayer != null && this.mc.thePlayer.inventory != null) {
            if (this.mc.thePlayer.inventory.armorItemInSlot(2) != null && this.mc.thePlayer.inventory.armorItemInSlot(2).hasDisplayName()) {
                if (this.mc.thePlayer.inventory.armorItemInSlot(2).getDisplayName().equals("Kevlar")) {
                    playerArmorFrac = playerArmorFrac + 0.7;
                }
            }
            if (this.mc.thePlayer.inventory.armorItemInSlot(3) != null && this.mc.thePlayer.inventory.armorItemInSlot(3).hasDisplayName()) {
                if (this.mc.thePlayer.inventory.armorItemInSlot(3).getDisplayName().equals("Helmet")) {
                    playerArmorFrac = playerArmorFrac + 0.3;
                }
            }
        }

        this.mc.fontRendererObj.drawStringWithShadow(Double.toString(playerArmorFrac * 100).split("\\.")[0], 136, 12, 0xffffff);
        this.mc.fontRendererObj.drawStringWithShadow(Double.toString(playerHealthFrac).split("\\.")[0], 8, 12, 0xffffff);

        int backgroundTop2 = backgroundTop;
        int backgroundBottom2 = backgroundBottom;
        int backgroundLeft2 = backgroundLeft;
        int backgroundRight2 = backgroundRight;
        int barTop = backgroundTop2 + 8;
        int barBottom = backgroundBottom2 - 8;
        int armorBarLeft = backgroundLeft2 + 160;
        int armorBarRightMax = backgroundRight - 8;
        int playerArmorBar = new Double(playerArmorFrac * (armorBarRightMax - armorBarLeft)).intValue();
        int armorBarRight = armorBarLeft + playerArmorBar;
        int healthBarLeft = backgroundLeft + 32;
        int healthBarRightMax = backgroundRight2 - 136;
        int playerHealthBar = new Double(playerHealth * (healthBarRightMax - healthBarLeft)).intValue();
        int healthBarRight = healthBarLeft + playerHealthBar;

        INSTANCE.getUtilsPublic().drawBoxOnScreen(armorBarLeft, barTop, armorBarRightMax, barBottom, Integer.MIN_VALUE);
        INSTANCE.getUtilsPublic().drawBoxOnScreen(healthBarLeft, barTop, healthBarRightMax, barBottom, Integer.MIN_VALUE);
        INSTANCE.getUtilsPublic().drawBoxOnScreen(armorBarLeft, barTop, armorBarRight, barBottom, 0x905555FF);
        INSTANCE.getUtilsPublic().drawBoxOnScreen(healthBarLeft, barTop, healthBarRight, barBottom, 0x90FF5555);

        GlStateManager.popMatrix();
    }
}
