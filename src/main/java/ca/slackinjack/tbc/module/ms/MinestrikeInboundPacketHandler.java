package ca.slackinjack.tbc.module.ms;

import ca.slackinjack.tbc.TBC;
import ca.slackinjack.tbc.server.mineplex.Mineplex;
import ca.slackinjack.tbc.server.mineplex.MineplexInboundPacketHandler;
import ca.slackinjack.tbc.utils.packethandler.PacketEnum;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.network.play.server.S1FPacketSetExperience;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.network.play.server.S3BPacketScoreboardObjective;
import net.minecraft.network.play.server.S3CPacketUpdateScore;
import net.minecraft.scoreboard.IScoreObjectiveCriteria.EnumRenderType;

public class MinestrikeInboundPacketHandler extends MineplexInboundPacketHandler {

    private final TBC INSTANCE;
    private final Minestrike MINESTRIKE;
    private final Minecraft MINECRAFT = Minecraft.getMinecraft();
    private final List<Integer> particlesToRemove = new ArrayList();
    //private final List<String> titlesToRemove = new ArrayList();

    public MinestrikeInboundPacketHandler(TBC tbcIn, Minestrike msIn) {
        super(tbcIn, (Mineplex) tbcIn.getUtilsPublic().getCurrentServer());
        INSTANCE = tbcIn;
        MINESTRIKE = msIn;
        this.initParticlesToRemoveList();
    }

    @Override
    public boolean processPacket(Packet thePacket) {
        PacketEnum packetType = null;

        for (PacketEnum p : PacketEnum.values()) {
            if (p.getPacketClass() == thePacket.getClass()) {
                packetType = p;
                break;
            }
        }

        if (packetType != null) {
            switch (packetType) {
                case S02:
                    String theMessage = INSTANCE.getUtilsPublic().getUnstylizedText(((S02PacketChat) thePacket).getChatComponent().getUnformattedText());
                    if (theMessage.startsWith("Portal> You")) {
                        if (!theMessage.startsWith("Portal> You are currently on server: ")) {
                            MINESTRIKE.onQuitGame();
                            super.processPacket(thePacket);
                        }
                    } else if (theMessage.startsWith("Game Rewards")) {
                        MINESTRIKE.onGameEnd();
                    } else if (MINESTRIKE.getInGame()) {
                        if (((S02PacketChat) thePacket).getType() == 2) {
                            String theFormattedMessage = ((S02PacketChat) thePacket).getChatComponent().getUnformattedText();
                            if (theFormattedMessage.contains("No Ammo")) {
                                MINESTRIKE.getHotbar().updateCurrentAmmo("0");
                                MINESTRIKE.getHotbar().updateTotalAmmo("0");
                                if (INSTANCE.getUtilsPublic().getConfigLoader().getEnableMSHotbar()) {
                                    return false;
                                }
                            }
                            if (theFormattedMessage.contains("Recharge") || theFormattedMessage.contains("Cancel")) {
                                if (INSTANCE.getUtilsPublic().getConfigLoader().getEnableMSHotbar()) {
                                    return false;
                                }
                            }
                            if (theFormattedMessage.contains("Reload\u00A7r")) {
                                MINESTRIKE.getHotbar().updateCurrentAmmo("0");
                                if (INSTANCE.getUtilsPublic().getConfigLoader().getEnableMSHotbar()) {
                                    return false;
                                }
                            }
                            if (theFormattedMessage.contains("\u00A7r / \u00A7e")) {
                                MINESTRIKE.getHotbar().updateCurrentAmmo(theMessage.split(" / ")[0]);
                                MINESTRIKE.getHotbar().updateTotalAmmo(theMessage.split(" / ")[1]);
                                if (INSTANCE.getUtilsPublic().getConfigLoader().getEnableMSHotbar()) {
                                    return false;
                                }
                            }
                        } else {
                            if (theMessage.startsWith("Death> ") || theMessage.startsWith("Join> ") || theMessage.startsWith("Quit> ")) {
                                if (theMessage.startsWith("Death> ")) {
                                    theMessage = theMessage.replace("Death> ", "");
                                    if (theMessage.endsWith(" killed by Fire.")) {
                                        String killedPlayer = (theMessage.split(" killed by "))[0];
                                        MINESTRIKE.getScoreboard().updateKillsDeathsForPlayer(killedPlayer, false, true);
                                        MINESTRIKE.getGameLog().add(killedPlayer, 1, "", true, false, "");
                                    }
                                    if (theMessage.endsWith(" killed by C4 Explosion.")) {
                                        String[] names = theMessage.split(" killed by ");
                                        String killedPlayer = names[0];
                                        MINESTRIKE.getScoreboard().updateKillsDeathsForPlayer(killedPlayer, false, true);
                                        MINESTRIKE.getGameLog().add(killedPlayer, 1, "", true, false, "");
                                    }
                                    if (theMessage.endsWith(" killed by Border Damage.")) {
                                        String[] names = theMessage.split(" killed by ");
                                        String killedPlayer = names[0];
                                        MINESTRIKE.getScoreboard().updateKillsDeathsForPlayer(killedPlayer, false, true);
                                        MINESTRIKE.getGameLog().add(killedPlayer, 1, "", true, false, "");
                                    }

                                    if (theMessage.endsWith(" killed by Fall.")) {
                                        String[] names = theMessage.split(" killed by ");
                                        String killedPlayer = names[0];
                                        MINESTRIKE.getScoreboard().updateKillsDeathsForPlayer(killedPlayer, false, true);
                                        MINESTRIKE.getGameLog().add(killedPlayer, 0, "", true, false, "");
                                    }
                                    if (theMessage.contains(" killed by ") && theMessage.contains(" with ")) {
                                        String playerNames = (theMessage.split(" with "))[0];
                                        String weaponName = (theMessage.split(" with "))[1];
                                        String[] names = playerNames.split(" killed by ");
                                        String killedPlayer = names[0];
                                        String killer = names[1];

                                        if (killer.contains(" + ")) {
                                            killer = killer.split("\\s+")[0];
                                        }

                                        boolean headshotKill = weaponName.contains("Headshot");
                                        if (!killedPlayer.equals(killer)) {
                                            MINESTRIKE.getScoreboard().updateKillsDeathsForPlayer(killedPlayer, false, true);
                                            MINESTRIKE.getScoreboard().updateKillsDeathsForPlayer(killer, true, false);
                                            MINESTRIKE.getGameLog().add(killedPlayer, -1, killer, false, headshotKill, weaponName);
                                        } else {
                                            MINESTRIKE.getScoreboard().updateKillsDeathsForPlayer(killedPlayer, false, true);
                                            MINESTRIKE.getGameLog().add(killedPlayer, -1, "", true, false, weaponName);
                                        }
                                    }

                                    // remove from chat and let gamelog display the kill
                                    return !INSTANCE.getUtilsPublic().getConfigLoader().getMSShouldHideDeathChatMessages();
                                } else if (theMessage.startsWith("Quit> ")) {
                                    String thePlayer = theMessage.replace("Quit> ", "");
                                    if (thePlayer.length() > 0) {
                                        MINESTRIKE.getScoreboard().setPlayerInServer(thePlayer, false);
                                    }
                                } else if (theMessage.startsWith("Join> ")) {
                                    String thePlayer = theMessage.replace("Join> ", "");
                                    if (thePlayer.length() > 0) {
                                        MINESTRIKE.getScoreboard().setPlayerInServer(thePlayer, true);
                                    }
                                }
                            } else if (theMessage.endsWith(" picked up the Bomb!")) {
                                String thePlayer = theMessage.replace(" picked up the Bomb!", "");
                                if (thePlayer.equals("You")) {
                                    MINESTRIKE.getObjMarkers().resetBombLocation();
                                } else {
                                    MINESTRIKE.getObjMarkers().setBombCarrierPlayerName(thePlayer);
                                }
                            } else if (theMessage.endsWith(" has planted the bomb!")) {
                                MINESTRIKE.onBombPlant();
                                String thePlayer = theMessage.replace(" has planted the bomb!", "");
                                MINESTRIKE.getScoreboard().updateDefusesPlantsForPlayer(thePlayer);
                            } else if (theMessage.endsWith(" defused the bomb!")) {
                                String thePlayer = theMessage.replace(" defused the bomb!", "");
                                MINESTRIKE.getScoreboard().updateDefusesPlantsForPlayer(thePlayer);
                            } else if (theMessage.endsWith("0. Open your Inventory to spend it.")) {
                                MINESTRIKE.onRoundStart(0);
                            } else if (theMessage.contains("Bombers") && theMessage.endsWith("Plant the Bomb at Bomb Site")) {
                                MINESTRIKE.onRoundStart(1);
                            } else if (theMessage.endsWith(" won the vote!") && !theMessage.contains("Bombers ") && !theMessage.contains("SWAT ")) {
                                // catch this damn thing
                                return true;
                            } else if (theMessage.contains(" won the ") && (theMessage.contains("Bombers ") || theMessage.contains("SWAT "))) {
                                if (theMessage.endsWith(" game!")) {
                                    MINESTRIKE.onGameEnd();
                                } else if (theMessage.endsWith(" round!")) {
                                    MINESTRIKE.onRoundStart(2);
                                }
                            }
                        }
                    }
                    break;
                case S04:
                    S04PacketEntityEquipment s04Packet = (S04PacketEntityEquipment) thePacket;
                    ItemStack theItem = s04Packet.getItemStack();
                    if (theItem != null && theItem.getItem().equals(Items.golden_sword)) {
                        if (MINECRAFT.theWorld.getEntityByID(s04Packet.getEntityID()) != null && MINECRAFT.theWorld.getEntityByID(s04Packet.getEntityID()) instanceof EntityPlayer) {
                            GameProfile gameProfile = ((EntityPlayer) MINECRAFT.theWorld.getEntityByID(s04Packet.getEntityID())).getGameProfile();
                            if (gameProfile != null) {
                                MINESTRIKE.getObjMarkers().setBombCarrierPlayerName(gameProfile.getName());
                            }
                        }
                    }
                    break;
                case S1D:
                    if (((S1DPacketEntityEffect) thePacket).getEffectId() == 15 && ((S1DPacketEntityEffect) thePacket).getDuration() <= 20 && ((S1DPacketEntityEffect) thePacket).getEntityId() == Minecraft.getMinecraft().thePlayer.getEntityId()) {
                        return false;
                    }
                    break;
                case S1F:
                    if (INSTANCE.getUtilsPublic().getConfigLoader().getEnableMSHitmarkers()) {
                        if (((S1FPacketSetExperience) thePacket).getLevel() >= 1 && !Minecraft.getMinecraft().thePlayer.capabilities.allowFlying) {
                            MINESTRIKE.getHitMarker().setRenderable(true, ((S1FPacketSetExperience) thePacket).getLevel());
                        }
                    }
                    break;
                case S2A:
                    S2APacketParticles s2aPacket = (S2APacketParticles) thePacket;
                    int id = (s2aPacket.getParticleType().getParticleID());
                    if (!this.particlesToRemove.stream().noneMatch((i) -> (id == i))) {
                        return false;
                    }
                    break;
                case S3B:
                    S3BPacketScoreboardObjective s3bPacket = (S3BPacketScoreboardObjective) thePacket;
                    if (s3bPacket.func_179817_d() != null && s3bPacket.func_179817_d() == EnumRenderType.INTEGER) {
                        if (s3bPacket.func_149337_d() != null) {
                            String scoreDisplayName = INSTANCE.getUtilsPublic().getUnstylizedText(s3bPacket.func_149337_d());
                            if (scoreDisplayName.equals("HP")) {
                                return false;
                            }
                        }
                    }
                    break;
                case S3C:
                    S3CPacketUpdateScore s3cPacket = (S3CPacketUpdateScore) thePacket;
                    String playerName = INSTANCE.getUtilsPublic().getUnstylizedText(s3cPacket.getPlayerName());
                    String objName = INSTANCE.getUtilsPublic().getUnstylizedText(s3cPacket.getObjectiveName());
                    S3CPacketUpdateScore.Action scoreAction = s3cPacket.getScoreAction();
                    if (objName.equals("HP") && scoreAction == S3CPacketUpdateScore.Action.CHANGE) {
                        MINESTRIKE.getScoreboard().updateHPForPlayer(playerName, s3cPacket.getScoreValue());
                    }
                    if (objName.equals("HP")) {
                        return false;
                    }
                    break;
            }
        }

        return super.processPacket(thePacket);
    }

    private void initParticlesToRemoveList() {
        this.particlesToRemove.add(0);
        this.particlesToRemove.add(20);
        this.particlesToRemove.add(29);
        this.particlesToRemove.add(34);
    }
}
