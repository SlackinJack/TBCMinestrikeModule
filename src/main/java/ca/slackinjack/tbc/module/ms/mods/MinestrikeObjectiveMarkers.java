package ca.slackinjack.tbc.module.ms.mods;

import ca.slackinjack.tbc.TBC;
import ca.slackinjack.tbc.events.RenderEvent;
import ca.slackinjack.tbc.events.RenderTileEntityEvent;
import ca.slackinjack.tbc.module.ms.Minestrike;
import ca.slackinjack.tbc.module.ms.TBCMinestrikeModule;
import ca.slackinjack.tbc.utils.chat.TextFormattingEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDaylightDetector;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;

public class MinestrikeObjectiveMarkers {

    private final TBC INSTANCE;
    private final TBCMinestrikeModule MODULE_INSTANCE;
    private final Minestrike MINESTRIKE;
    private final Minecraft MINECRAFT = Minecraft.getMinecraft();
    private final ResourceLocation bombMarkerResourceLocation;
    private final ResourceLocation defendMarkerResourceLocation;
    private final ResourceLocation defuseMarkerResourceLocation;
    private final ResourceLocation killMarkerResourceLocation;
    private Object entityToTrack;
    private String bombCarrierPlayerName = "";

    private int bombMarkerDisplayMode = 0;
    // 0 = bomb
    // 1 = defend
    // 2 = defuse
    // todo 3 = kill

    public MinestrikeObjectiveMarkers(TBC tbcIn, TBCMinestrikeModule modIn, Minestrike MINESTRIKEIn) {
        INSTANCE = tbcIn;
        MODULE_INSTANCE = modIn;
        MINESTRIKE = MINESTRIKEIn;
        this.bombMarkerResourceLocation = new ResourceLocation("tbcminestrikemodule", "textures/waypoint_bomb.png");
        this.defendMarkerResourceLocation = new ResourceLocation("tbcminestrikemodule", "textures/waypoint_defend.png");
        this.defuseMarkerResourceLocation = new ResourceLocation("tbcminestrikemodule", "textures/waypoint_defuse.png");
        this.killMarkerResourceLocation = new ResourceLocation("tbcminestrikemodule", "textures/waypoint_kill.png");
    }

    public void onRenderWorldLastEvent(RenderWorldLastEvent e) {
        this.renderFloatingBombMarker();
    }

    public void onRenderEvent(RenderEvent e) {
        EntityItem theItem = (EntityItem) e.theEntity;
        if (theItem != null && theItem.getEntityItem() != null && theItem.getEntityItem().getItem() == Items.golden_sword) {
            this.updateBombEntityAsEntityItem(theItem);
        }
    }

    public void onRenderTileEntityEvent(RenderTileEntityEvent e) {
        TileEntityDaylightDetector theTE = (TileEntityDaylightDetector) e.theTileEntity;
        this.updateBombEntityAsTileEntity(theTE);
    }

    public void onRenderPlayerEvent(RenderPlayerEvent.Pre e) {
        if (MINESTRIKE.getScoreboard().getCurrentTeam() == TextFormattingEnum.RED) {
            if (e.entityPlayer != null && e.entityPlayer.getHeldItem() != null) {
                if (e.entityPlayer.getHeldItem().getItem() == Items.golden_sword) {
                    this.entityToTrack = e.entityPlayer;
                    this.updateBombEntityDisplayMode(0);
                    this.bombCarrierPlayerName = "";
                }
            }

            if (!this.bombCarrierPlayerName.isEmpty()) {
                EntityPlayer ep = MINECRAFT.theWorld.getPlayerEntityByName(this.bombCarrierPlayerName);
                if (ep != null) {
                    this.entityToTrack = ep;
                    this.updateBombEntityDisplayMode(0);
                    this.bombCarrierPlayerName = "";
                }
            }
        }
    }

    private void updateBombEntityAsTileEntity(TileEntity teIn) {
        this.entityToTrack = teIn;
        this.updateBombEntityDisplayMode(2);
        this.bombCarrierPlayerName = "";
    }

    private void updateBombEntityAsEntityItem(EntityItem itemIn) {
        this.entityToTrack = itemIn;
        this.updateBombEntityDisplayMode(1);
        this.bombCarrierPlayerName = "";
    }

    private void updateBombEntityDisplayMode(int i) {
        // 0 = player
        // 1 = item
        // 2 = planted

        switch (MINESTRIKE.getScoreboard().getCurrentTeam()) {
            case AQUA:
                switch (i) {
                    case 0:
                        this.bombMarkerDisplayMode = 3;
                        break;
                    case 1:
                        this.bombMarkerDisplayMode = 1;
                        break;
                    case 2:
                        this.bombMarkerDisplayMode = 2;
                        break;
                    default:
                        this.bombMarkerDisplayMode = 0;
                        break;
                }
                break;
            case RED:
                switch (i) {
                    case 0:
                        this.bombMarkerDisplayMode = 1;
                        break;
                    case 1:
                        this.bombMarkerDisplayMode = 0;
                        break;
                    case 2:
                        this.bombMarkerDisplayMode = 1;
                        break;
                    default:
                        this.bombMarkerDisplayMode = 0;
                        break;
                }
                break;
            default:
                this.bombMarkerDisplayMode = 0;
                break;
        }
    }

    public void setBombCarrierPlayerName(String playerNameIn) {
        this.bombCarrierPlayerName = playerNameIn;
    }

    public void resetBombLocation() {
        this.bombCarrierPlayerName = "";
        this.entityToTrack = null;
    }

    private void renderFloatingBombMarker() {
        BlockPos blockposToUse = this.updateTrackedEntity();

        if (blockposToUse != null && this.getCanRenderBomb() && MINESTRIKE.getInGame()) {
            GlStateManager.pushMatrix();
            GlStateManager.blendFunc(770, 771);
            GlStateManager.enableBlend();
            GlStateManager.disableDepth();
            GlStateManager.enableAlpha();
            GlStateManager.depthMask(false);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.25F);

            switch (this.bombMarkerDisplayMode) {
                case 1:
                    MINECRAFT.getTextureManager().bindTexture(this.defendMarkerResourceLocation);
                    break;
                case 2:
                    MINECRAFT.getTextureManager().bindTexture(this.defuseMarkerResourceLocation);
                    break;
                case 3:
                    MINECRAFT.getTextureManager().bindTexture(this.killMarkerResourceLocation);
                    break;
                default:
                    MINECRAFT.getTextureManager().bindTexture(this.bombMarkerResourceLocation);
                    break;
            }

            double actualX = blockposToUse.getX();
            double actualY = blockposToUse.getY();
            double actualZ = blockposToUse.getZ();

            double x = (actualX - MINECRAFT.thePlayer.posX);
            //double y = (actualY - MINECRAFT.thePlayer.posY);
            double z = (actualZ - MINECRAFT.thePlayer.posZ);

            float scale = 0.016666668F * 0.125F * ((float) MINECRAFT.thePlayer.getDistance(actualX, actualY, actualZ));
            float f = 0.00390625F * 8.0F;

            double y1 = MINECRAFT.thePlayer.posY;
            double yOffset;

            if (y1 > actualY) {
                yOffset = 0.15D * (actualY - y1);
            } else {
                yOffset = 1.75D * (y1 - actualY);
            }

            GlStateManager.translate((float) x, (float) ((-yOffset) + 4), (float) z);
            GlStateManager.scale(-scale, -scale, scale);
            GlStateManager.rotate(MINECRAFT.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-MINECRAFT.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);

            double bombMarkerImgWidth = 64.0D;
            double minX = -bombMarkerImgWidth / 2.0D;
            double minY = -bombMarkerImgWidth / 2.0D;
            double maxX = bombMarkerImgWidth / 2.0D;
            double maxY = bombMarkerImgWidth / 2.0D;
            double minTex = 0.0D;
            double maxTex = (bombMarkerImgWidth / 2.0D) * f;

            INSTANCE.getUtilsPublic().drawBoxOnScreenWithTexture(minX, minY, maxX, maxY, minTex, maxTex);

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableAlpha();
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    private boolean getCanRenderBomb() {
        switch (MINESTRIKE.getScoreboard().getCurrentTeam()) {
            case AQUA:
                return INSTANCE.getUtilsPublic().getConfigLoader().getEnableMSBombFinderCT();
            case RED:
                return INSTANCE.getUtilsPublic().getConfigLoader().getEnableMSBombFinderT();
            default:
                return true;
        }
    }

    private BlockPos updateTrackedEntity() {
        if (this.entityToTrack != null) {
            if (this.entityToTrack instanceof EntityPlayer) {
                if (MINECRAFT.theWorld.getPlayerEntityByName(((EntityPlayer) this.entityToTrack).getName()) != null) {
                    return ((EntityPlayer) this.entityToTrack).getPosition();
                } else {
                    this.entityToTrack = null;
                }
            } else if (this.entityToTrack instanceof EntityItem) {
                if (MINECRAFT.theWorld.loadedEntityList.contains((EntityItem) this.entityToTrack)) {
                    return ((EntityItem) this.entityToTrack).getPosition();
                } else {
                    this.entityToTrack = null;
                }
            } else if (this.entityToTrack instanceof TileEntity) {
                if (MINECRAFT.theWorld.loadedTileEntityList.contains((TileEntity) this.entityToTrack)) {
                    return ((TileEntity) this.entityToTrack).getPos();
                } else {
                    this.entityToTrack = null;
                }
            } else {
                this.entityToTrack = null;
                return null;
            }
        }

        return null;
    }
}
