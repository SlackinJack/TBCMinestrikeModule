package ca.slackinjack.tbc.module.ms;

import ca.slackinjack.tbc.module.ms.mods.MinestrikeScoreboard;
import ca.slackinjack.tbc.module.ms.mods.MinestrikeObjectiveMarkers;
import ca.slackinjack.tbc.module.ms.mods.MinestrikeArmorHealthBars;
import ca.slackinjack.tbc.module.ms.mods.MinestrikeSpectatorMode;
import ca.slackinjack.tbc.module.ms.mods.MinestrikeHotbar;
import ca.slackinjack.tbc.module.ms.mods.MinestrikeHitMarker;
import ca.slackinjack.tbc.module.ms.mods.MinestrikePlayerHP;
import ca.slackinjack.tbc.module.ms.mods.MinestrikeGameLog;
import ca.slackinjack.tbc.TBC;
import ca.slackinjack.tbc.events.RenderBlockEvent;
import ca.slackinjack.tbc.events.RenderEvent;
import ca.slackinjack.tbc.events.RenderNameTagEvent;
import ca.slackinjack.tbc.events.RenderScoreboardEvent;
import ca.slackinjack.tbc.events.RenderTileEntityEvent;
import ca.slackinjack.tbc.server.Minigame;
import java.util.Timer;
import java.util.TimerTask;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityDaylightDetector;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Minestrike extends Minigame {

    private final TBCMinestrikeModule MODULE_INSTANCE;
    private final MinestrikeArmorHealthBars msAHB;
    private final MinestrikeHitMarker msHM;
    private final MinestrikeHotbar msHB;
    private final MinestrikeScoreboard msSB;
    private final MinestrikeSpectatorMode msSM;
    private final MinestrikePlayerHP msHP;
    private final MinestrikeObjectiveMarkers msOM;
    private final MinestrikeGameLog msGL;
    private final Timer roundTimer = new Timer();

    private boolean shouldShowHealthEditor;
    private boolean shouldShowHitmarkerEditor;
    private boolean shouldShowHotbarEditor;
    private boolean shouldShowLogEditor;

    private boolean disableSmoke;
    private int roundTimeRemaining;

    public Minestrike(TBC tbcIn, TBCMinestrikeModule modIn, boolean setupForMPS) {
        super(tbcIn);
        MODULE_INSTANCE = modIn;

        this.msAHB = new MinestrikeArmorHealthBars(INSTANCE, MODULE_INSTANCE, this);
        this.msHM = new MinestrikeHitMarker(INSTANCE, MODULE_INSTANCE, this);
        this.msSB = new MinestrikeScoreboard(INSTANCE, MODULE_INSTANCE, this);
        this.msHB = new MinestrikeHotbar(INSTANCE, MODULE_INSTANCE, this);
        this.msSM = new MinestrikeSpectatorMode(INSTANCE, MODULE_INSTANCE, this);
        this.msHP = new MinestrikePlayerHP(INSTANCE, MODULE_INSTANCE, this);
        this.msOM = new MinestrikeObjectiveMarkers(INSTANCE, MODULE_INSTANCE, this);
        this.msGL = new MinestrikeGameLog(INSTANCE, MODULE_INSTANCE, this);
        this.roundTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (getInGame()) {
                    switch (roundTimeRemaining) {
                        case 120:
                            msSB.resetAllHP();
                            msOM.resetBombLocation();
                            msSB.resetRound(3);
                            break;
                    }

                    if (roundTimeRemaining > 0) {
                        --roundTimeRemaining;
                    }
                }
            }
        }, 0, 1000);

        if (setupForMPS) {
            this.getScoreboard().initForMPSServer();
        }
    }

    @Override
    public void onRenderEvent(RenderEvent e) {
        if (e.theEntity instanceof EntityItem) {
            if (((EntityItem) e.theEntity).getEntityItem() != null && ((EntityItem) e.theEntity).getEntityItem().getItem() == Items.dye) {
                ItemStack theItem = ((EntityItem) e.theEntity).getEntityItem();
                ItemStack redDyeItem = new ItemStack(Items.dye, 1, EnumDyeColor.RED.getDyeDamage());
                if (redDyeItem.getItem().getMetadata(redDyeItem) == theItem.getItem().getMetadata(theItem)) {
                    e.setCanceled(true);
                }
            }
            this.msOM.onRenderEvent(e);
        }
    }

    @Override
    public void onRenderBlockEvent(RenderBlockEvent e) {
        if (this.disableSmoke && e.theBlockState.getBlock() == Blocks.portal) {
            e.setCanceled(this.disableSmoke);
        }
    }

    @Override
    public void onRenderTileEntityEvent(RenderTileEntityEvent e) {
        if (e.theTileEntity instanceof TileEntityDaylightDetector) {
            this.msOM.onRenderTileEntityEvent(e);
        }
    }

    @Override
    public void onRenderGameOverlayEvent(RenderGameOverlayEvent.Pre e) {
        if (this.msSB.getAfterGameReportRenderable() == this.getInGame()) {
            this.msSB.setAfterGameReportRenderable(!this.getInGame() && !this.msSB.getPlayerScoresMapPostIsEmpty());
        }

        if (e.type == ElementType.PLAYER_LIST) {
            e.setCanceled(true);
            this.msSB.onRenderTabList(e);
        } else {
            if (this.getInGame()) {
                switch (e.type) {
                    case AIR:
                        e.setCanceled(true);
                        break;
                    case BOSSHEALTH:
                        e.setCanceled(true);
                        break;
                    case EXPERIENCE:
                        if (INSTANCE.getUtilsPublic().getConfigLoader().getEnableMSHitmarkers()) {
                            e.setCanceled(true);
                        }
                        break;
                    case FOOD:
                        e.setCanceled(true);
                        break;
                    case HOTBAR:
                        if (INSTANCE.getUtilsPublic().getConfigLoader().getEnableMSHotbar() && !this.getPlayerIsDead()) {
                            e.setCanceled(true);
                            this.msHB.onRenderHotbar(e);
                        }
                        break;
                    case ARMOR:
                        if (INSTANCE.getUtilsPublic().getConfigLoader().getEnableMSHealthIndicators()) {
                            e.setCanceled(true);
                        }
                        break;
                    case HEALTH:
                        if (INSTANCE.getUtilsPublic().getConfigLoader().getEnableMSHealthIndicators()) {
                            e.setCanceled(true);
                            this.msAHB.onRenderHealth(e);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void onRenderWorldLastEvent(RenderWorldLastEvent e) {
        this.msOM.onRenderWorldLastEvent(e);
    }

    @Override
    public void onRenderTickEvent(TickEvent.RenderTickEvent e) {
        if (MINECRAFT.theWorld != null && this.getInGame()) {
            this.msHM.onRenderTickEvent(e);
            this.msSB.onRenderTickEvent(e);
            this.msGL.onRenderTickEvent(e);

            MINECRAFT.gameSettings.heldItemTooltips = false;
        }
    }

    @Override
    public void onClientTickEvent(TickEvent.ClientTickEvent e) {
        if (this.getPlayerIsDead()) {
            if (this.getInGame() && INSTANCE.getUtilsPublic().getConfigLoader().getEnableMSAutoTarget()) {
                this.msSM.addAllTargets();
            } else {
                this.msSM.removeAllTargets();
            }
        } else {
            this.msSM.removeAllTargets();
        }

        if (this.shouldShowHealthEditor) {
            MINECRAFT.displayGuiScreen(INSTANCE.getUtilsPublic().new UtilsEditGui("minestrike", "Health Bar Coordinates", INSTANCE.getUtilsPublic().getConfigLoader().getCoordinatesForKey("Health Bar Coordinates")));
        }

        if (this.shouldShowHitmarkerEditor) {
            MINECRAFT.displayGuiScreen(INSTANCE.getUtilsPublic().new UtilsEditGui("minestrike", "Hitmarker Coordinates", INSTANCE.getUtilsPublic().getConfigLoader().getCoordinatesForKey("Hitmarker Coordinates")));
        }

        if (this.shouldShowHotbarEditor) {
            MINECRAFT.displayGuiScreen(INSTANCE.getUtilsPublic().new UtilsEditGui("minestrike", "Hotbar Coordinates", INSTANCE.getUtilsPublic().getConfigLoader().getCoordinatesForKey("Hotbar Coordinates")));
        }

        if (this.shouldShowLogEditor) {
            MINECRAFT.displayGuiScreen(INSTANCE.getUtilsPublic().new UtilsEditGui("minestrike", "Game Log Coordinates", INSTANCE.getUtilsPublic().getConfigLoader().getCoordinatesForKey("Game Log Coordinates")));
        }
    }

    @Override
    public boolean onRenderNameTagEvent(RenderNameTagEvent e) {
        return this.getCanDisplayNameForPlayer(e.theEntity);
    }

    @Override
    public void onRenderScoreboardEvent(RenderScoreboardEvent e) {
        e.setCanceled(!this.getCanDisplayVanillaScoreboard());
    }

    @Override
    public void onRenderPlayerEvent(RenderPlayerEvent.Pre e) {
        if (this.getInGame()) {
            this.msHP.onRenderLivingEvent(e);
            this.msOM.onRenderPlayerEvent(e);
        }
    }

    public void onRoundStart(int extraTimeMode) {
        //0 - start of round
        //1 - start of game
        //2 - spectator
        switch (extraTimeMode) {
            case 0:
                this.roundTimeRemaining = 120;
                break;
            case 1:
                this.msSB.resetAfterGameReport();
                this.roundTimeRemaining = 128;
                break;
            case 2:
                this.roundTimeRemaining = 126;
                break;
            case 3:
                break;
        }
        this.msSB.resetRound(extraTimeMode);
    }

    public void onBombPlant() {
        this.msSB.renderBombClock();
    }

    public void onGameEnd() {
        this.msSB.prepareAfterGameReport();
        this.msSB.resetGame();
    }

    public void onQuitGame() {
        this.msSB.resetAfterGameReport();
        this.msSB.resetGame();
    }

    public boolean getPlayerIsDead() {
        return MINECRAFT.thePlayer.capabilities.allowFlying;
    }

    public boolean getInGame() {
        return INSTANCE.getUtilsPublic().checkScoreboardHasString("SWAT");
    }

    public boolean getCanDisplayNameForPlayer(EntityLivingBase thePlayer) {
        if (MINECRAFT.thePlayer.capabilities.allowFlying || !this.getInGame()) {
            return true;
        }

        return this.msSB.getPlayerIsSameTeam(thePlayer.getName());
    }

    public boolean getCanDisplayVanillaScoreboard() {
        if (MINECRAFT.thePlayer != null && MINECRAFT.theWorld != null) {
            if (this.getInGame()) {
                return false;
            }
        }
        return true;
    }

    public boolean getBombIsPlanted() {
        return INSTANCE.getUtilsPublic().checkScoreboardHasString("Active");
    }

    public void setRoundTimeRemaining(int timeIn) {
        this.roundTimeRemaining = timeIn;
    }

    public int getRoundTimeRemaining() {
        return this.roundTimeRemaining;
    }

    public void toggleSmoke() {
        this.disableSmoke = !this.disableSmoke;
    }

    public MinestrikeArmorHealthBars getArmorHealthBars() {
        return this.msAHB;
    }

    public MinestrikeHotbar getHotbar() {
        return this.msHB;
    }

    public MinestrikeHitMarker getHitMarker() {
        return this.msHM;
    }

    public MinestrikeScoreboard getScoreboard() {
        return this.msSB;
    }

    public MinestrikeSpectatorMode getSpecMode() {
        return this.msSM;
    }

    public MinestrikePlayerHP getPlayerHP() {
        return this.msHP;
    }

    public MinestrikeObjectiveMarkers getObjMarkers() {
        return this.msOM;
    }

    public MinestrikeGameLog getGameLog() {
        return this.msGL;
    }

    public void setShouldShowHealthEditor() {
        this.shouldShowHealthEditor = true;
    }

    public void setShouldShowHitmarkerEditor() {
        this.shouldShowHitmarkerEditor = true;
    }

    public void setShouldShowHotbarEditor() {
        this.shouldShowHotbarEditor = true;
    }

    public void setShouldShowLogEditor() {
        this.shouldShowLogEditor = true;
    }
}
