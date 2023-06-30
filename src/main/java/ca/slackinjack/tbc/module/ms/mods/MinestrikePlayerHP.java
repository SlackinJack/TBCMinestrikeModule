package ca.slackinjack.tbc.module.ms.mods;

import ca.slackinjack.tbc.TBC;
import ca.slackinjack.tbc.module.ms.Minestrike;
import ca.slackinjack.tbc.module.ms.TBCMinestrikeModule;
import ca.slackinjack.tbc.utils.chat.TextFormattingEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderPlayerEvent;
import org.lwjgl.opengl.GL11;

public class MinestrikePlayerHP {

    private final TBC INSTANCE;
    private final TBCMinestrikeModule MODULE_INSTANCE;
    private final Minestrike MINESTRIKE;
    private final Minecraft MINECRAFT = Minecraft.getMinecraft();
    private final String hpText = " HP";

    public MinestrikePlayerHP(TBC tbcIn, TBCMinestrikeModule modIn, Minestrike msIn) {
        INSTANCE = tbcIn;
        MODULE_INSTANCE = modIn;
        MINESTRIKE = msIn;
    }

    public void onRenderLivingEvent(RenderPlayerEvent.Pre e) {
        if (e.entityPlayer != null && e.entityPlayer.getGameProfile() != null) {
            if (e.entityPlayer != MINECRAFT.thePlayer && !(INSTANCE.getUtilsPublic().getAreActualPlayerNamesEqual(e.entityPlayer, MINECRAFT.thePlayer))) {
                String playerName = e.entityPlayer.getGameProfile().getName();

                if (MINESTRIKE.getScoreboard().getPlayerScoresMap() != null && !MINESTRIKE.getScoreboard().getPlayerScoresMap().isEmpty() && MINESTRIKE.getScoreboard().getPlayerScoresMap().containsKey(playerName)) {
                    int playerHP = MINESTRIKE.getScoreboard().getPlayerScoresMap().get(playerName).getHP();
                    boolean depthTest = !MINESTRIKE.getScoreboard().getPlayerIsSameTeam(playerName);
                    this.renderHPBar(e.entityPlayer, playerHP, e.x, e.y, e.z, depthTest);
                }
            }
        }
    }

    // draws HP as a solid progressive bar
    private void renderHPBar(Entity entityIn, int healthIn, double x, double y, double z, boolean depthTest) {
        if (depthTest && !MINECRAFT.thePlayer.canEntityBeSeen(entityIn)) {
            return;
        }

        String str = Integer.toString(healthIn) + this.hpText;
        float f = 1.6F;
        float f1 = 0.016666668F * f;
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y + entityIn.height + 0.5F, (float) z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-MINECRAFT.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(MINECRAFT.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-f1, -f1, f1);
        GlStateManager.disableLighting();

        if (depthTest) {
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
        } else {
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
        }

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();

        double i = -22.0D;
        int i2 = -11;

        double j = 16.0D;
        double j2 = MINECRAFT.fontRendererObj.getStringWidth(str) / 2.0D;
        double k = -16.0D + (healthIn * (8.0D / 25.0D));
        
        // -16 <-> -8 <-> 0 <-> 8 <-> 16
        
        // 0 -> 32
        //0-20 20-40 40-60 60-100
        //  6.4   12.8  19.2  32
        // -9.4   -3.2  3.2   16

        int hpColor = TextFormattingEnum.WHITE.getRGBValue();
        if (k >= 3.2) {
            hpColor = TextFormattingEnum.GREEN.getRGBValue();
        } else if (k >= -3.2 && k < 3.2) {
            hpColor = TextFormattingEnum.YELLOW.getRGBValue();
        } else if (k >= -9.6 && k < -3.2) {
            hpColor = TextFormattingEnum.RED.getRGBValue();
        } else if (k >= -16 && k < -9.6) {
            hpColor = TextFormattingEnum.DARK_RED.getRGBValue();
        }

        hpColor = INSTANCE.getUtilsPublic().addAlphaToColor(hpColor, 1.0F);

        double minX = -j;
        double minY = -1 + i;
        double maxX = j;
        double maxY = 8 + i;
        INSTANCE.getUtilsPublic().drawBoxOnScreen(minX, minY, maxX, maxY, 0x40000000);

        double minX2 = -j;
        double minY2 = -1 + i;
        double maxX2 = k;
        double maxY2 = 8 + i;
        INSTANCE.getUtilsPublic().drawBoxOnScreen(minX2, minY2, maxX2, maxY2, hpColor);

        double minX3 = -j2 - 1;
        double minY3 = -1 + i2;
        double maxX3 = j2 + 1;
        double maxY3 = 8 + i2;
        INSTANCE.getUtilsPublic().drawBoxOnScreen(minX3, minY3, maxX3, maxY3, 0x40000000);

        GlStateManager.enableTexture2D();
        MINECRAFT.fontRendererObj.drawString(str, -MINECRAFT.fontRendererObj.getStringWidth(str) / 2, i2, 553648127);
        MINECRAFT.fontRendererObj.drawString(str, -MINECRAFT.fontRendererObj.getStringWidth(str) / 2, i2, -1);
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }
}
