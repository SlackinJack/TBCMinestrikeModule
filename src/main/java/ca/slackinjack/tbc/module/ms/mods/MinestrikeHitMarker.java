package ca.slackinjack.tbc.module.ms.mods;

import ca.slackinjack.tbc.TBC;
import ca.slackinjack.tbc.module.ms.Minestrike;
import ca.slackinjack.tbc.module.ms.TBCMinestrikeModule;
import java.util.Timer;
import java.util.TimerTask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

public class MinestrikeHitMarker {

    private final TBC INSTANCE;
    private final TBCMinestrikeModule MODULE_INSTANCE;
    private final Minestrike MINESTRIKE;
    private final ResourceLocation resourceLocation;
    private final Minecraft MINECRAFT = Minecraft.getMinecraft();
    private boolean renderable;
    private int displayTime;
    private int lastDamage;
    private final Timer t = new Timer();

    public MinestrikeHitMarker(TBC tbcIn, TBCMinestrikeModule modIn, Minestrike msIn) {
        INSTANCE = tbcIn;
        MODULE_INSTANCE = modIn;
        MINESTRIKE = msIn;
        this.resourceLocation = new ResourceLocation("tbcminestrikemodule", "textures/hitmarker.png");

        this.t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (renderable) {
                    if (displayTime > 25) {
                        displayTime -= 25;
                    } else {
                        renderable = false;
                    }
                }
            }
        }, 0, 25);
    }

    public void onRenderTickEvent(TickEvent.RenderTickEvent e) {
        if (this.renderable) {
            this.renderHitMarker();
        }
    }

    public void setRenderable(boolean renderable, int damageIn) {
        this.renderable = renderable;
        this.displayTime = INSTANCE.getUtilsPublic().getConfigLoader().getHitmarkerVisibilityTime();
        this.lastDamage = damageIn;
    }

    private void renderHitMarker() {
        int l1 = (int) (((float) this.displayTime) * 255.0F / 100.0F);

        if (l1 > 255) {
            l1 = 255;
        }

        if (l1 > 8) {
            int color = 0xFFFFFF;
            int alpha = (color + (l1 << 24 & -color));

            ScaledResolution r = new ScaledResolution(MINECRAFT);
            int screenWidth = r.getScaledWidth();
            int screenHeight = r.getScaledHeight();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            this.drawTexturedModalRect((double) ((screenWidth / 2) - 7.5D + (double) INSTANCE.getUtilsPublic().getConfigLoader().getHitmarkerAdjustmentX()), (double) ((screenHeight / 2) - 6.25D + (double) INSTANCE.getUtilsPublic().getConfigLoader().getHitmarkerAdjustmentY()), 32.0D, alpha);
            int damageStringWidth = MINECRAFT.fontRendererObj.getStringWidth(Integer.toString(this.lastDamage));
            int damageXLocation = (screenWidth / 2) - (damageStringWidth / 2);
            int damageYLocation = (screenHeight / 2) + 20;
            MINECRAFT.fontRendererObj.drawStringWithShadow(Integer.toString(this.lastDamage), damageXLocation, damageYLocation, alpha);
            GlStateManager.disableBlend();
        }
    }

    private void drawTexturedModalRect(double x, double y, double size, int color) {
        MINECRAFT.getTextureManager().bindTexture(this.resourceLocation);
        float f = 0.00390625F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);

        double minX = x;
        double minY = y;
        double maxX = x + size;
        double maxY = y + size;
        double minTex = 0.0D;
        double maxTex = size * f;

        INSTANCE.getUtilsPublic().drawBoxOnScreenWithTexture(minX, minY, maxX, maxY, minTex, maxTex);
    }
}
