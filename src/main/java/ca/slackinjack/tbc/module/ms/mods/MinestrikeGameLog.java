package ca.slackinjack.tbc.module.ms.mods;

import ca.slackinjack.tbc.TBC;
import ca.slackinjack.tbc.module.ms.Minestrike;
import ca.slackinjack.tbc.module.ms.TBCMinestrikeModule;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class MinestrikeGameLog {

    private final TBC INSTANCE;
    private final TBCMinestrikeModule MODULE_INSTANCE;
    private final Minestrike MINESTRIKE;
    private final Minecraft MINECRAFT = Minecraft.getMinecraft();
    private final List<MinestrikeGameLogEntry> gameLog = new CopyOnWriteArrayList();
    private final int iconWidth = 32;
    private final ResourceLocation skullIconLocation;
    private final ResourceLocation fallIconLocation;
    private final ResourceLocation headshotIconLocation;
    private final int logSpacing = 8;
    private final Timer t = new Timer();

    /*
    Format:
    
    Original:
    Death> Player1 killed by Fire.
    Death> Player1 killed by C4 Explosion.
    Death> Player1 killed by Border Damage.
    Death> Player1 killed by Fall.
    Death> Player1 killed by Player2 with {GUN/KNIFE} (, Headshot)
    
    New:
    [Skull Icon] Player1
    [Skull Icon] Player1
    [Skull Icon] Player1
    [Fall Icon] Player1
    Player1 [Weapon Icon][Headshot Icon] Player2 (+1)
     */
    public MinestrikeGameLog(TBC tbcIn, TBCMinestrikeModule modIn, Minestrike msIn) {
        INSTANCE = tbcIn;
        MODULE_INSTANCE = modIn;
        MINESTRIKE = msIn;
        this.skullIconLocation = new ResourceLocation("tbcminestrikemodule", "textures/log/death_suicide.png");
        this.fallIconLocation = new ResourceLocation("tbcminestrikemodule", "textures/log/death_falling.png");
        this.headshotIconLocation = new ResourceLocation("tbcminestrikemodule", "textures/log/death_headshot.png");

        this.t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (gameLog != null && !gameLog.isEmpty()) {
                    for (MinestrikeGameLogEntry log : gameLog) {
                        log.updateDisplayTimeRemaining();
                    }
                }
            }
        }, 0, 100);
    }

    public void onRenderTickEvent(TickEvent.RenderTickEvent e) {
        this.renderGameLog();
    }

    public void add(String killedPlayerIn, int iconIn, String killerIn, boolean selfKillIn, boolean headshotKillIn, String weaponIn) {
        this.gameLog.add(0, new MinestrikeGameLogEntry(killedPlayerIn, iconIn, killerIn, selfKillIn, headshotKillIn, weaponIn));
    }

    public void clear() {
        this.gameLog.clear();
    }

    private void renderGameLog() {
        if (!MINECRAFT.gameSettings.hideGUI && MINECRAFT.currentScreen == null) {
            ScaledResolution res = new ScaledResolution(MINECRAFT);
            int windowWidth = res.getScaledWidth();
            int backgroundTop = 32;
            int backgroundRight = 256;
            int backgroundBottom = backgroundTop + 156;
            GlStateManager.pushMatrix();
            GlStateManager.translate(((double) windowWidth * (1.0D / INSTANCE.getUtilsPublic().getConfigLoader().getMSHealthIndicatorScale())) - backgroundRight, 0.0D, 0.0D);

            GlStateManager.translate((double) INSTANCE.getUtilsPublic().getConfigLoader().getMSHealthBarsAdjustmentX(), (double) INSTANCE.getUtilsPublic().getConfigLoader().getMSHealthBarsAdjustmentY(), 0.0D);

            for (MinestrikeGameLogEntry l : this.gameLog) {
                if (l.getDisplayTimeRemaining() > 0) {
                    int i = this.gameLog.indexOf(l);
                    int lineYCoord = backgroundBottom - 18 - (i * 16);

                    if (lineYCoord > backgroundTop) {
                        int killerNameXCoord;
                        int iconXCoord;
                        int killedPlayerNameXCoord;
                        int headshotIconXCoord;

                        if (l.getSelfKill()) {
                            if (l.getHeadshotKill()) {
                                killerNameXCoord = 0;
                                killedPlayerNameXCoord = backgroundRight - this.logSpacing - MINECRAFT.fontRendererObj.getStringWidth(l.getKilledPlayer());
                                headshotIconXCoord = killedPlayerNameXCoord - this.logSpacing - 4;
                                iconXCoord = headshotIconXCoord - this.logSpacing - 8;
                            } else {
                                killerNameXCoord = 0;
                                headshotIconXCoord = 0;
                                killedPlayerNameXCoord = backgroundRight - this.logSpacing - MINECRAFT.fontRendererObj.getStringWidth(l.getKilledPlayer());
                                iconXCoord = killedPlayerNameXCoord - this.logSpacing - 4;
                            }
                        } else {
                            if (l.getHeadshotKill()) {
                                killedPlayerNameXCoord = backgroundRight - this.logSpacing - MINECRAFT.fontRendererObj.getStringWidth(l.getKilledPlayer());
                                headshotIconXCoord = killedPlayerNameXCoord - this.logSpacing - 4;
                                iconXCoord = headshotIconXCoord - this.logSpacing - 20;
                                killerNameXCoord = iconXCoord - this.logSpacing - MINECRAFT.fontRendererObj.getStringWidth(l.getKiller()) - 16;
                            } else {
                                headshotIconXCoord = 0;
                                killedPlayerNameXCoord = backgroundRight - this.logSpacing - MINECRAFT.fontRendererObj.getStringWidth(l.getKilledPlayer());
                                iconXCoord = killedPlayerNameXCoord - this.logSpacing - 16;
                                killerNameXCoord = iconXCoord - this.logSpacing - MINECRAFT.fontRendererObj.getStringWidth(l.getKiller()) - 16;
                            }
                        }

                        int cellBackgroundLeft = (l.getSelfKill() ? iconXCoord - 8 : killerNameXCoord) - 4;
                        int cellBackgroundTop = lineYCoord - 4;
                        int cellBackgroundRight = killedPlayerNameXCoord + MINECRAFT.fontRendererObj.getStringWidth(l.getKilledPlayer()) + 4;
                        int cellBackgroundBottom = lineYCoord + 12;
                        int cellBackgroundColor = Integer.MIN_VALUE;
                        if (l.getKiller().length() > 0 && l.getKiller().toLowerCase().equals(MINECRAFT.thePlayer.getGameProfile().getName().toLowerCase())) {
                            cellBackgroundColor = 0x90FF5555;
                        }

                        INSTANCE.getUtilsPublic().drawBoxOnScreen(cellBackgroundLeft, cellBackgroundTop, cellBackgroundRight, cellBackgroundBottom, cellBackgroundColor);

                        //draw names
                        if (!l.getSelfKill()) {
                            MINECRAFT.fontRendererObj.drawStringWithShadow(l.killer, killerNameXCoord, lineYCoord, MINESTRIKE.getScoreboard().getPlayersTeam(l.killer).getRGBValue());
                        }
                        MINECRAFT.fontRendererObj.drawStringWithShadow(l.killedPlayer, killedPlayerNameXCoord, lineYCoord, MINESTRIKE.getScoreboard().getPlayersTeam(l.killedPlayer).getRGBValue());

                        //headshot icon
                        if (l.getHeadshotKill()) {
                            this.drawTexturedModalRect(this.headshotIconLocation, headshotIconXCoord, lineYCoord + 4, this.iconWidth, false);
                        }

                        //icon
                        ResourceLocation customResource;
                        switch (l.getIcon()) {
                            case 0:
                                customResource = this.fallIconLocation;
                                break;
                            case 1:
                                customResource = this.skullIconLocation;
                                break;
                            default:
                                customResource = null;
                                break;
                        }

                        if (customResource != null) {
                            this.drawTexturedModalRect(customResource, iconXCoord, lineYCoord + 4, this.iconWidth, false);
                        } else {
                            if (!l.getWeapon().isEmpty()) {
                                MinestrikeWeaponsEnum theWeapon = null;
                                outer:
                                for (MinestrikeWeaponsEnum e : MinestrikeWeaponsEnum.values()) {
                                    for (String s : e.getName()) {
                                        if (l.getWeapon().toLowerCase().contains(s.toLowerCase())) {
                                            theWeapon = e;
                                            break outer;
                                        }
                                    }
                                }

                                if (theWeapon != null) {
                                    String weaponIconName;
                                    if (theWeapon == MinestrikeWeaponsEnum.KNIFE) {
                                        String knifeNamePrefix = "Knife_";
                                        switch (MINESTRIKE.getScoreboard().getPlayersTeam(l.killer)) {
                                            case RED:
                                                weaponIconName = knifeNamePrefix + "T";
                                                break;
                                            default:
                                                weaponIconName = knifeNamePrefix + "CT";
                                                break;
                                        }

                                    } else {
                                        weaponIconName = theWeapon.getName()[0];
                                    }
                                    this.drawTexturedModalRect(new ResourceLocation("tbcminestrikemodule", "textures/guns/" + weaponIconName + ".png"), iconXCoord, lineYCoord + 4, 256, true);
                                }
                            }
                        }
                    }

                } else {
                    this.gameLog.remove(l);
                }
            }
            GlStateManager.popMatrix();
        }
    }

    private void drawTexturedModalRect(ResourceLocation resourceIn, double x, double y, int imgSize, boolean reflect) {
        GlStateManager.pushMatrix();
        MINECRAFT.getTextureManager().bindTexture(resourceIn);
        float f = 0.00390625F * (256.0F / imgSize);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate(x, y, 0);

        int sizeX;
        int sizeY;

        if (imgSize == 32) {
            sizeX = imgSize / 2;
            sizeY = imgSize / 2;
        } else {
            sizeX = imgSize / 4;
            sizeY = imgSize / 4;
        }

        GlStateManager.translate(-sizeX / 2, -sizeY / 2, 0.0D);

        /*
        double minX = 0;
        double minY = 0;
        double maxX = sizeX;
        double maxY = sizeY;
        double minTex = reflect ? imgSize * f : 0;
        double maxTex = imgSize * f;
        
        INSTANCE.getUtilsPublic().drawBoxOnScreenWithTexture(minX, minY, maxX, maxY, minTex, maxTex);*/
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(7, DefaultVertexFormats.POSITION_TEX);
        wr.pos(0, sizeY, 0).tex((reflect ? imgSize * f : 0), imgSize * f).endVertex();
        wr.pos(sizeX, sizeY, 0).tex((reflect ? 0 : imgSize * f), imgSize * f).endVertex();
        wr.pos(sizeX, 0, 0).tex((reflect ? 0 : imgSize * f), 0).endVertex();
        wr.pos(0, 0, 0).tex((reflect ? imgSize * f : 0), 0).endVertex();
        tess.draw();
        GlStateManager.popMatrix();
    }

    public class MinestrikeGameLogEntry {

        private final String killedPlayer;
        private final int icon;
        // -1 = use gun icon
        // 0 = fall
        // 1 = selfKill
        private final String killer;
        private final boolean selfKill;
        private final boolean headshotKill;
        private final String weapon;

        private int displayTimeRemaining;

        public MinestrikeGameLogEntry(String killedPlayerIn, int iconIn, String killerIn, boolean selfKillIn, boolean headshotKillIn, String weaponIn) {
            this.killedPlayer = killedPlayerIn;
            this.icon = iconIn;
            this.killer = killerIn;
            this.selfKill = selfKillIn;
            this.headshotKill = headshotKillIn;
            this.weapon = weaponIn;
            this.displayTimeRemaining = 14000;
        }

        public String getKilledPlayer() {
            return this.killedPlayer;
        }

        public int getIcon() {
            return this.icon;
        }

        public String getKiller() {
            return this.killer;
        }

        public boolean getSelfKill() {
            return this.selfKill;
        }

        public boolean getHeadshotKill() {
            return this.headshotKill;
        }

        public String getWeapon() {
            return this.weapon;
        }

        public int getDisplayTimeRemaining() {
            return this.displayTimeRemaining;
        }

        public void updateDisplayTimeRemaining() {
            this.displayTimeRemaining -= 100;
        }
    }
}
