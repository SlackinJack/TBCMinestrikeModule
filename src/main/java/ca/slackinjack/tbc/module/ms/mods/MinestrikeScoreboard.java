package ca.slackinjack.tbc.module.ms.mods;

import ca.slackinjack.tbc.TBC;
import ca.slackinjack.tbc.module.ms.Minestrike;
import ca.slackinjack.tbc.module.ms.TBCMinestrikeModule;
import ca.slackinjack.tbc.utils.chat.TextFormattingEnum;
import com.google.common.collect.ComparisonChain;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MinestrikeScoreboard extends Gui {

    private final TBC INSTANCE;
    private final TBCMinestrikeModule MODULE_INSTANCE;
    private final Minestrike MINESTRIKE;
    private final Minecraft MINECRAFT = Minecraft.getMinecraft();
    private final Map<String, MinestrikeScoreboardPlayer> playerScoresMapPost = new ConcurrentHashMap();
    private final Map<String, MinestrikeScoreboardPlayer> playerScoresMap = new ConcurrentHashMap();
    private final Map<String, NetworkPlayerInfo> playerMap = new ConcurrentHashMap();
    private boolean displayBombClock;
    private boolean renderAfterGameReport;
    private final String lobbyText = "Lobby";
    private final String playersText = "Players";
    private final String kdDiffText = "Diff";
    private final String killsText = "Kills";
    private final String deathsText = "Deaths";
    private final String kdText = "K/D";
    private final String pdText = "P/D";
    private final String pingText = "Ping";
    private final String ctTeamDesc = "Counter";
    private final String tTeamDesc = "Terrorists";
    private final String gameSummaryText = "Game Summary";
    private final String positiveDiffText = "+";
    private final String negativeDiffText = "-";
    private final String kdFormatString = "%.2f";
    private final String pingSuffixText = "ms";
    private final String pingDeadPlayerText = "Dead";
    private final String pingLeftPlayerText = "Left";
    private final String teamsPlayersAlivePrefix = "Alive: ";
    private final String teamsPlayersAliveSeparator = " / ";
    private final String clockFormatString = "%02d";

    public MinestrikeScoreboard(TBC tbcIn, TBCMinestrikeModule modIn, Minestrike msIn) {
        INSTANCE = tbcIn;
        MODULE_INSTANCE = modIn;
        MINESTRIKE = msIn;
    }

    public void onRenderTabList(RenderGameOverlayEvent.Pre e) {
        this.drawCustomScoreboard(e.resolution);
    }

    public void onRenderTickEvent(TickEvent.RenderTickEvent e) {
        this.drawClockScoreboard();
    }

    private void drawCustomScoreboard(ScaledResolution resIn) {
        try {
            int windowWidth = resIn.getScaledWidth();
            int windowHeight = resIn.getScaledHeight();
            int scoreboardLeft = (windowWidth / 2) - 220;
            int scoreboardRight = (windowWidth / 2) + 220;

            int pingXCoord = scoreboardLeft + 132;
            int playerHeadIconXCoord = scoreboardLeft + 172;
            int playerNameXCoord = scoreboardLeft + 182;
            int kdDiffXCoord = scoreboardRight - 148;
            int killsXCoord = scoreboardRight - 122;
            int deathsXCoord = scoreboardRight - 89;
            int kdRatioXCoord = scoreboardRight - 56;
            int pdXCoord = scoreboardRight - 30;
            int teamDescXCoord = ((scoreboardLeft + pingXCoord) / 2);

            int ctTeamSizeTotal = 0;
            int tTeamSizeTotal = 0;
            int ctPlayersAlive = 0;
            int tPlayersAlive = 0;
            int ctPlayersInServer = 0;
            int tPlayersInServer = 0;

            // populate team sizes
            if (!this.renderAfterGameReport) {
                for (Map.Entry<String, MinestrikeScoreboardPlayer> entry : this.playerScoresMap.entrySet()) {
                    MinestrikeScoreboardPlayer si = entry.getValue();

                    switch (si.getTeamColor()) {
                        case AQUA:
                            ++ctTeamSizeTotal;
                            if (si.getInServer()) {
                                ++ctPlayersInServer;
                            }
                            if (!si.getIsDead()) {
                                ++ctPlayersAlive;
                            }
                            break;
                        case RED:
                            ++tTeamSizeTotal;
                            if (si.getInServer()) {
                                ++tPlayersInServer;
                            }
                            if (!si.getIsDead()) {
                                ++tPlayersAlive;
                            }
                            break;
                    }
                }
            } else {
                for (Map.Entry<String, MinestrikeScoreboardPlayer> entry : this.playerScoresMapPost.entrySet()) {
                    switch (entry.getValue().getTeamColor()) {
                        case AQUA:
                            ++ctTeamSizeTotal;
                            break;
                        case RED:
                            ++tTeamSizeTotal;
                            break;
                    }
                }
            }

            int scoreboardTop;
            int scoreboardBottom;

            if (this.renderAfterGameReport) {
                List<NetworkPlayerInfo> list = new CopyOnWriteArrayList(INSTANCE.getUtilsPublic().getPlayerInfoMap());
                if (list.size() > (ctTeamSizeTotal + tTeamSizeTotal + 1)) {
                    scoreboardTop = (windowHeight * 3 / 8) - ((list.size()) * 5);
                    scoreboardBottom = scoreboardTop + 75 + ((list.size()) * 12);
                } else {
                    scoreboardTop = (windowHeight * 3 / 8) - ((ctTeamSizeTotal + tTeamSizeTotal) * 5);
                    scoreboardBottom = scoreboardTop + 75 + ((ctTeamSizeTotal + tTeamSizeTotal) * 12);
                }
            } else {
                scoreboardTop = (windowHeight * 3 / 8) - ((ctTeamSizeTotal + tTeamSizeTotal) * 5);
                scoreboardBottom = scoreboardTop + 75 + ((ctTeamSizeTotal + tTeamSizeTotal) * 12);
            }

            int headerLocation = scoreboardTop + 28;
            int titleLocation = scoreboardTop + 10;
            int ctTitleYCoord = scoreboardTop + 38;
            int tTitleYCoord = ctTitleYCoord + 18 + (ctTeamSizeTotal * 12);

            drawRect(scoreboardLeft, scoreboardTop, scoreboardRight, scoreboardBottom, Integer.MIN_VALUE);

            int kdDiffHeaderWidth = MINECRAFT.fontRendererObj.getStringWidth(this.kdDiffText);
            int killsHeaderWidth = MINECRAFT.fontRendererObj.getStringWidth(this.killsText);
            int deathsHeaderWidth = MINECRAFT.fontRendererObj.getStringWidth(this.deathsText);
            int kdRatioHeaderWidth = MINECRAFT.fontRendererObj.getStringWidth(this.kdText);
            int pdHeaderWidth = MINECRAFT.fontRendererObj.getStringWidth(this.pdText);
            int scoreboardTimeWidth = MINECRAFT.fontRendererObj.getStringWidth(this.getCurrentRoundTimeRemaining());

            if (!MINESTRIKE.getInGame()) {
                if (this.renderAfterGameReport) {
                    drawRect(playerNameXCoord - 22, scoreboardTop, scoreboardRight, scoreboardBottom, Integer.MIN_VALUE);

                    int gameSummaryTextWidth = MINECRAFT.fontRendererObj.getStringWidth(this.gameSummaryText);

                    int gameSummaryTextXLocation = ((playerNameXCoord - 22 + scoreboardRight) / 2) - (gameSummaryTextWidth / 2);
                    //this.mc.fontRendererObj.drawStringWithShadow(mp.getCurrentServerName(), (scoreboardLeft + 8), titleLocation, 0xffffff);
                    MINECRAFT.fontRendererObj.drawStringWithShadow(this.gameSummaryText, gameSummaryTextXLocation, titleLocation, 0xffffff);
                    MINECRAFT.fontRendererObj.drawStringWithShadow(this.lobbyText, (scoreboardLeft + 8), headerLocation, 0xffffff);
                    MINECRAFT.fontRendererObj.drawStringWithShadow(this.playersText, playerNameXCoord, headerLocation, 0xffffff);
                    MINECRAFT.fontRendererObj.drawStringWithShadow(this.kdDiffText, (kdDiffXCoord - (kdDiffHeaderWidth / 2)), headerLocation, 0xffffff);
                    MINECRAFT.fontRendererObj.drawStringWithShadow(this.killsText, (killsXCoord - (killsHeaderWidth / 2)), headerLocation, 0xffffff);
                    MINECRAFT.fontRendererObj.drawStringWithShadow(this.deathsText, (deathsXCoord - (deathsHeaderWidth / 2)), headerLocation, 0xffffff);
                    MINECRAFT.fontRendererObj.drawStringWithShadow(this.kdText, (kdRatioXCoord - (kdRatioHeaderWidth / 2)), headerLocation, 0xffffff);
                    MINECRAFT.fontRendererObj.drawStringWithShadow(this.pdText, (pdXCoord - (pdHeaderWidth / 2)), headerLocation, 0xffffff);
                    int ctPlayersDrawn = 0;
                    int tPlayersDrawn = 0;
                    int firstCTYCoord = 0;
                    int firstTYCoord = 0;

                    // draw lobby players
                    int lobbyPlayersDrawn = 0;
                    for (NetworkPlayerInfo npi : INSTANCE.getUtilsPublic().getPlayerInfoMap()) {
                        if (npi != null && npi.getGameProfile() != null) {
                            int lobbyLineYCoord = ctTitleYCoord + (lobbyPlayersDrawn * 12) + 3;
                            MINECRAFT.fontRendererObj.drawStringWithShadow(npi.getGameProfile().getName(), (scoreboardLeft + 8), lobbyLineYCoord, 0xaaaaaa);
                            ++lobbyPlayersDrawn;
                        }
                    }

                    if (!this.playerScoresMapPost.isEmpty()) {
                        // draw post-game report
                        for (Map.Entry<String, MinestrikeScoreboardPlayer> entry : this.playerScoresMapPost.entrySet()) {
                            MinestrikeScoreboardPlayer thePlayer = entry.getValue();
                            thePlayer.setIsDead(false);
                            int kills = thePlayer.getKills();
                            int deaths = thePlayer.getDeaths();
                            double kd = 0.00D;
                            if (deaths > 0) {
                                kd = Math.round((double) kills / (double) deaths * 100.00) / 100.00;
                            } else if (deaths == 0) {
                                kd = Math.round((double) kills * 100.00) / 100.00;
                            }

                            int kdrColor;
                            int kdDiffColor;
                            String kdDiffString;
                            if (kills >= deaths) {
                                kdrColor = 0x55ff55;
                                kdDiffColor = 0xffffff;
                                kdDiffString = this.positiveDiffText + Integer.toString(kills - deaths);
                            } else {
                                kdrColor = 0xff5555;
                                kdDiffColor = 0xaaaaaa;
                                kdDiffString = this.negativeDiffText + Integer.toString(deaths - kills);
                            }

                            String killsString = Integer.toString(kills);
                            String deathsString = Integer.toString(deaths);
                            String kdRatioString = String.format(this.kdFormatString, kd);
                            String pdString = Integer.toString(thePlayer.getDefusesOrPlants());
                            int kdDiffWidth = MINECRAFT.fontRendererObj.getStringWidth(kdDiffString);
                            int killsWidth = MINECRAFT.fontRendererObj.getStringWidth(killsString);
                            int deathsWidth = MINECRAFT.fontRendererObj.getStringWidth(deathsString);
                            int kdRatioWidth = MINECRAFT.fontRendererObj.getStringWidth(kdRatioString);
                            int pdWidth = MINECRAFT.fontRendererObj.getStringWidth(pdString);
                            int lineYCoord = 0;
                            int teamColour = 0xffffff;

                            switch (thePlayer.getTeamColor()) {
                                case AQUA:
                                    lineYCoord = ctTitleYCoord + (ctPlayersDrawn * 12) + 3;
                                    if (firstCTYCoord == 0) {
                                        firstCTYCoord = lineYCoord;
                                    }
                                    teamColour = 0x55ffff;
                                    ++ctPlayersDrawn;
                                    break;
                                case RED:
                                    lineYCoord = tTitleYCoord + (tPlayersDrawn * 12) + 3;
                                    if (firstTYCoord == 0) {
                                        firstTYCoord = lineYCoord;
                                    }
                                    teamColour = 0xff5555;
                                    ++tPlayersDrawn;
                                    break;
                            }

                            if (thePlayer.getTeamColor() == TextFormattingEnum.AQUA || thePlayer.getTeamColor() == TextFormattingEnum.RED) {
                                MINECRAFT.fontRendererObj.drawStringWithShadow(entry.getKey(), playerNameXCoord, lineYCoord, !thePlayer.getIsDead() ? teamColour : 0xaaaaaa);
                                MINECRAFT.fontRendererObj.drawStringWithShadow(kdRatioString, (kdRatioXCoord - (kdRatioWidth / 2)), lineYCoord, kdrColor);
                                MINECRAFT.fontRendererObj.drawStringWithShadow(kdDiffString, (kdDiffXCoord - (kdDiffWidth / 2)), lineYCoord, kdDiffColor);
                                MINECRAFT.fontRendererObj.drawStringWithShadow(killsString, (killsXCoord - (killsWidth / 2)), lineYCoord, -1);
                                MINECRAFT.fontRendererObj.drawStringWithShadow(deathsString, (deathsXCoord - (deathsWidth / 2)), lineYCoord, -1);
                                MINECRAFT.fontRendererObj.drawStringWithShadow(pdString, (pdXCoord - (pdWidth / 2)), lineYCoord, -1);
                                MINECRAFT.getTextureManager().bindTexture(thePlayer.getSkinResourceLocation());
                            }
                        }
                    }
                }
            } else {
                //this.mc.fontRendererObj.drawStringWithShadow(mp.getCurrentServerName(), (scoreboardLeft + 8), titleLocation, 0xffffff);
                MINECRAFT.fontRendererObj.drawStringWithShadow(this.getCurrentRoundTimeRemaining(), (scoreboardRight - scoreboardTimeWidth - 8), titleLocation, 0xffffff);
                MINECRAFT.fontRendererObj.drawStringWithShadow(this.pingText, pingXCoord, headerLocation, 0xffffff);
                MINECRAFT.fontRendererObj.drawStringWithShadow(this.playersText, playerHeadIconXCoord, headerLocation, 0xffffff);
                MINECRAFT.fontRendererObj.drawStringWithShadow(this.kdDiffText, (kdDiffXCoord - (kdDiffHeaderWidth / 2)), headerLocation, 0xffffff);
                MINECRAFT.fontRendererObj.drawStringWithShadow(this.killsText, (killsXCoord - (killsHeaderWidth / 2)), headerLocation, 0xffffff);
                MINECRAFT.fontRendererObj.drawStringWithShadow(this.deathsText, (deathsXCoord - (deathsHeaderWidth / 2)), headerLocation, 0xffffff);
                MINECRAFT.fontRendererObj.drawStringWithShadow(this.kdText, (kdRatioXCoord - (kdRatioHeaderWidth / 2)), headerLocation, 0xffffff);
                MINECRAFT.fontRendererObj.drawStringWithShadow(this.pdText, (pdXCoord - (pdHeaderWidth / 2)), headerLocation, 0xffffff);
                int ctPlayersDrawn = 0;
                int tPlayersDrawn = 0;
                int firstCTYCoord = 0;
                int firstTYCoord = 0;

                for (Map.Entry<String, NetworkPlayerInfo> entry : this.playerMap.entrySet()) {
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.enableAlpha();
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

                    MinestrikeScoreboardPlayer thePlayer = this.playerScoresMap.get(entry.getKey());
                    TextFormattingEnum playerTeam = thePlayer.getTeamColor();
                    String playerName = entry.getKey();
                    NetworkPlayerInfo npi = entry.getValue();
                    EntityPlayer entityplayer = MINECRAFT.theWorld.getPlayerEntityByName(playerName);
                    int kills = thePlayer.getKills();
                    int deaths = thePlayer.getDeaths();
                    double kd = 0.00D;
                    if (deaths > 0) {
                        kd = Math.round((double) kills / (double) deaths * 100.00) / 100.00;
                    } else if (deaths == 0) {
                        kd = Math.round((double) kills * 100.00) / 100.00;
                    }

                    int kdrColor;
                    int kdDiffColor;
                    String kdDiffString;
                    if (kills >= deaths) {
                        kdrColor = 0x55ff55;
                        kdDiffColor = 0xffffff;
                        kdDiffString = this.positiveDiffText + Integer.toString(kills - deaths);
                    } else {
                        kdrColor = 0xff5555;
                        kdDiffColor = 0xaaaaaa;
                        kdDiffString = this.negativeDiffText + Integer.toString(deaths - kills);
                    }

                    String killsString = Integer.toString(kills);
                    String deathsString = Integer.toString(deaths);
                    String kdRatioString = String.format(this.kdFormatString, kd);
                    String pdString = Integer.toString(thePlayer.getDefusesOrPlants());
                    int kdDiffWidth = MINECRAFT.fontRendererObj.getStringWidth(kdDiffString);
                    int killsWidth = MINECRAFT.fontRendererObj.getStringWidth(killsString);
                    int deathsWidth = MINECRAFT.fontRendererObj.getStringWidth(deathsString);
                    int kdRatioWidth = MINECRAFT.fontRendererObj.getStringWidth(kdRatioString);
                    int pdWidth = MINECRAFT.fontRendererObj.getStringWidth(pdString);
                    int lineYCoord = 0;
                    int teamColour = 0xffffff;

                    switch (playerTeam) {
                        case AQUA:
                            lineYCoord = ctTitleYCoord + (ctPlayersDrawn * 12) + 3;
                            if (firstCTYCoord == 0) {
                                firstCTYCoord = lineYCoord;
                            }
                            teamColour = 0x55ffff;
                            ++ctPlayersDrawn;
                            break;
                        case RED:
                            lineYCoord = tTitleYCoord + (tPlayersDrawn * 12) + 3;
                            if (firstTYCoord == 0) {
                                firstTYCoord = lineYCoord;
                            }
                            teamColour = 0xff5555;
                            ++tPlayersDrawn;
                            break;
                    }

                    if (playerTeam == TextFormattingEnum.AQUA || playerTeam == TextFormattingEnum.RED) {
                        MINECRAFT.fontRendererObj.drawStringWithShadow(playerName, playerNameXCoord, lineYCoord, !thePlayer.getIsDead() ? teamColour : 0xaaaaaa);

                        if (!thePlayer.getIsDead() && npi != null) {
                            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                            int responseTime = npi.getResponseTime();
                            int pingTextColour = 0xffffff;
                            if (responseTime >= 0 && responseTime <= 100) {
                                pingTextColour = 0x55FF55;
                            } else if (responseTime > 100 && responseTime <= 200) {
                                pingTextColour = 0xFFFF55;
                            } else if (responseTime > 200 && responseTime <= 300) {
                                pingTextColour = 0xFFAA00;
                            } else if (responseTime > 300) {
                                pingTextColour = 0xff5555;
                            }

                            MINECRAFT.fontRendererObj.drawStringWithShadow(responseTime + this.pingSuffixText, pingXCoord, lineYCoord, pingTextColour);

                        } else {
                            String pingReplacementText = this.pingDeadPlayerText;

                            if (!thePlayer.getInServer()) {
                                pingReplacementText = this.pingLeftPlayerText;
                            }

                            MINECRAFT.fontRendererObj.drawStringWithShadow(pingReplacementText, pingXCoord, lineYCoord, 0xaaaaaa);
                        }

                        MINECRAFT.fontRendererObj.drawStringWithShadow(kdRatioString, (kdRatioXCoord - (kdRatioWidth / 2)), lineYCoord, kdrColor);
                        MINECRAFT.fontRendererObj.drawStringWithShadow(kdDiffString, (kdDiffXCoord - (kdDiffWidth / 2)), lineYCoord, kdDiffColor);
                        MINECRAFT.fontRendererObj.drawStringWithShadow(killsString, (killsXCoord - (killsWidth / 2)), lineYCoord, -1);
                        MINECRAFT.fontRendererObj.drawStringWithShadow(deathsString, (deathsXCoord - (deathsWidth / 2)), lineYCoord, -1);
                        MINECRAFT.fontRendererObj.drawStringWithShadow(pdString, (pdXCoord - (pdWidth / 2)), lineYCoord, -1);
                        MINECRAFT.getTextureManager().bindTexture(thePlayer.getSkinResourceLocation());

                        INSTANCE.getUtilsPublic().drawTexturedBoxOnScreenSquare(playerHeadIconXCoord, lineYCoord, 8.0F, 8.0F, 8, 8, 64.0F);
                        if (entityplayer != null && entityplayer.isWearing(EnumPlayerModelParts.HAT)) {
                            INSTANCE.getUtilsPublic().drawTexturedBoxOnScreenSquare(playerHeadIconXCoord, lineYCoord, 40.0F, 8.0F, 8, 8, 64.0F);
                        }
                    }
                }

                int ctTeamDescYCoord = firstCTYCoord + (ctPlayersDrawn * 4);
                int tTeamDescYCoord = firstTYCoord + (tPlayersDrawn * 4);
                String ctPlayersAliveText = this.teamsPlayersAlivePrefix + ctPlayersAlive + this.teamsPlayersAliveSeparator + ctPlayersInServer;
                String tPlayersAliveText = this.teamsPlayersAlivePrefix + tPlayersAlive + this.teamsPlayersAliveSeparator + tPlayersInServer;

                int ctTeamDescWidth = MINECRAFT.fontRendererObj.getStringWidth(this.ctTeamDesc);
                int tTeamDescWidth = MINECRAFT.fontRendererObj.getStringWidth(this.tTeamDesc);
                if (ctTeamSizeTotal > 0) {
                    MINECRAFT.fontRendererObj.drawStringWithShadow(this.ctTeamDesc, teamDescXCoord - (ctTeamDescWidth / 2), ctTeamDescYCoord, 0x55ffff);
                    MINECRAFT.fontRendererObj.drawStringWithShadow(this.tTeamDesc, teamDescXCoord - (tTeamDescWidth / 2), ctTeamDescYCoord + 10, 0x55ffff);
                    MINECRAFT.fontRendererObj.drawStringWithShadow(ctPlayersAliveText, teamDescXCoord - (MINECRAFT.fontRendererObj.getStringWidth(ctPlayersAliveText) / 2), ctTeamDescYCoord + 20, 0xffffff);
                }

                if (tTeamSizeTotal > 0) {
                    MINECRAFT.fontRendererObj.drawStringWithShadow(this.tTeamDesc, teamDescXCoord - (tTeamDescWidth / 2), tTeamDescYCoord, 0xff5555);
                    MINECRAFT.fontRendererObj.drawStringWithShadow(tPlayersAliveText, teamDescXCoord - (MINECRAFT.fontRendererObj.getStringWidth(tPlayersAliveText) / 2), tTeamDescYCoord + 10, 0xffffff);
                }
            }

        } catch (Exception e) {
        }
    }

    private void drawClockScoreboard() {
        try {
            if (MINECRAFT.currentScreen == null) {
                ScaledResolution res = new ScaledResolution(MINECRAFT);
                int windowWidth = res.getScaledWidth();
                int clockStringWidth = MINECRAFT.fontRendererObj.getStringWidth(this.getCurrentRoundTimeRemaining());
                int clockXPosition = (windowWidth / 2);
                int clockYPosition = 6;
                int clockBackgroundLeft = (clockXPosition - (clockStringWidth / 2)) - 8;
                int clockBackgroundRight = (clockXPosition + (clockStringWidth / 2)) + 8;
                int clockBackgroundTop = clockYPosition - 6;
                int clockBackgroundBottom = clockYPosition + 14;

                if (MINESTRIKE.getInGame()) {
                    Map<String, NetworkPlayerInfo> currentPlayerMap = new ConcurrentHashMap();
                    for (NetworkPlayerInfo i : INSTANCE.getUtilsPublic().getPlayerInfoMap()) {
                        if (i != null) {
                            String playerName = i.getGameProfile().getName();
                            // current map
                            currentPlayerMap.put(playerName, i);

                            // player map
                            this.playerMap.put(playerName, i);

                            // scores map
                            if (!this.playerScoresMap.containsKey(playerName)) {
                                this.playerScoresMap.put(playerName, new MinestrikeScoreboardPlayer(i.getLocationSkin()));
                            } else {
                                this.playerScoresMap.get(playerName).setSkinResourceLocation(i.getLocationSkin());
                                this.playerScoresMap.get(playerName).setInServer(true);
                            }

                            // update team color in scores map
                            if (i.getPlayerTeam() != null) {
                                String teamColor = INSTANCE.getUtilsPublic().getUnstylizedTextForMinestrike(i.getPlayerTeam().getColorPrefix());
                                for (TextFormattingEnum f : TextFormattingEnum.values()) {
                                    if (teamColor.endsWith(f.getFormatText())) {
                                        if ((f == TextFormattingEnum.AQUA || f == TextFormattingEnum.RED) && this.playerScoresMap.get(playerName) != null) {
                                            this.playerScoresMap.get(playerName).setTeamColor(f);
                                            break;
                                        } else {
                                            this.playerScoresMap.get(playerName).setTeamColor(TextFormattingEnum.GRAY);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // draw small player list
                    int ctPlayersDrawn = 0;
                    int tPlayersDrawn = 0;
                    int headHeightAndWidth = 12;
                    int smallPlayerListBackgroundXPosition = (windowWidth / 2);
                    int smallPlayerListBackgroundYPosition = 0;
                    int smallPlayerListBackgroundLeft = smallPlayerListBackgroundXPosition - 94;
                    int smallPlayerListBackgroundTop = smallPlayerListBackgroundYPosition;
                    int smallPlayerListBackgroundRight = smallPlayerListBackgroundXPosition + 94;
                    int smallPlayerListBackgroundBottom = 36;

                    INSTANCE.getUtilsPublic().drawBoxOnScreen(smallPlayerListBackgroundLeft, smallPlayerListBackgroundTop, smallPlayerListBackgroundRight, smallPlayerListBackgroundBottom, Integer.MIN_VALUE);

                    for (Map.Entry<String, NetworkPlayerInfo> entry : this.playerMap.entrySet()) {
                        String playerName = entry.getKey();
                        MinestrikeScoreboardPlayer thePlayer = this.playerScoresMap.get(entry.getKey());
                        if (thePlayer != null && thePlayer.getInServer()) {
                            NetworkPlayerInfo i = entry.getValue();
                            if (!playerName.equals(MINECRAFT.thePlayer.getName())) {
                                thePlayer.setIsDead(!currentPlayerMap.containsKey(playerName));
                            } else {
                                thePlayer.setIsDead(MINECRAFT.thePlayer.capabilities.allowFlying);
                            }

                            ResourceLocation skinResourceLocation = thePlayer.getSkinResourceLocation();
                            if (skinResourceLocation == null) {
                                skinResourceLocation = i.getLocationSkin();
                            }

                            if (skinResourceLocation != null) {
                                MINECRAFT.getTextureManager().bindTexture(skinResourceLocation);
                                int headXCoord;
                                int headYCoord;
                                EntityPlayer thePlayerEntity = MINECRAFT.theWorld.getPlayerEntityByName(playerName);

                                if (thePlayer.getIsDead()) {
                                    GlStateManager.color(0.2F, 0.2F, 0.2F, 0.35F);
                                } else {
                                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                                }
                                switch (thePlayer.getTeamColor()) {
                                    case AQUA:
                                        headXCoord = (ctPlayersDrawn >= 5 ? (clockBackgroundLeft - 4 - ((ctPlayersDrawn - 5) * 2) - (((ctPlayersDrawn - 5) + 1) * headHeightAndWidth)) : (clockBackgroundLeft - 4 - (ctPlayersDrawn * 2) - ((ctPlayersDrawn + 1) * headHeightAndWidth)));
                                        headYCoord = (ctPlayersDrawn >= 5 ? 8 + headHeightAndWidth : 4);

                                        INSTANCE.getUtilsPublic().drawTexturedBoxOnScreenSquare(headXCoord, headYCoord, 8.0F, 8.0F, 8, headHeightAndWidth, 64.0F);
                                        //Gui.drawScaledCustomSizeModalRect(headXCoord, headYCoord, 8.0F, (float) 8, 8, 8, headHeightAndWidth, headHeightAndWidth, 64.0F, 64.0F);
                                        if (thePlayerEntity != null && thePlayerEntity.isWearing(EnumPlayerModelParts.HAT)) {
                                            INSTANCE.getUtilsPublic().drawTexturedBoxOnScreenSquare(headXCoord, headYCoord, 40.0F, 8.0F, 8, headHeightAndWidth, 64.0F);
                                            //Gui.drawScaledCustomSizeModalRect(headXCoord, headYCoord, 40.0F, (float) 8, 8, 8, headHeightAndWidth, headHeightAndWidth, 64.0F, 64.0F);
                                        }

                                        ++ctPlayersDrawn;
                                        break;

                                    case RED:
                                        headXCoord = (tPlayersDrawn >= 5 ? (clockBackgroundRight + 4 + ((tPlayersDrawn - 5) * 2) + ((tPlayersDrawn - 5) * headHeightAndWidth)) : (clockBackgroundRight + 4 + (tPlayersDrawn * 2) + (tPlayersDrawn * headHeightAndWidth)));
                                        headYCoord = (tPlayersDrawn >= 5 ? 8 + headHeightAndWidth : 4);

                                        INSTANCE.getUtilsPublic().drawTexturedBoxOnScreenSquare(headXCoord, headYCoord, 8.0F, 8.0F, 8, headHeightAndWidth, 64.0F);
                                        //Gui.drawScaledCustomSizeModalRect(headXCoord, headYCoord, 8.0F, (float) 8, 8, 8, headHeightAndWidth, headHeightAndWidth, 64.0F, 64.0F);
                                        if (thePlayerEntity != null && thePlayerEntity.isWearing(EnumPlayerModelParts.HAT)) {
                                            INSTANCE.getUtilsPublic().drawTexturedBoxOnScreenSquare(headXCoord, headYCoord, 40.0F, 8.0F, 8, headHeightAndWidth, 64.0F);
                                            //Gui.drawScaledCustomSizeModalRect(headXCoord, headYCoord, 40.0F, (float) 8, 8, 8, headHeightAndWidth, headHeightAndWidth, 64.0F, 64.0F);
                                        }

                                        ++tPlayersDrawn;
                                        break;

                                    default:
                                        headXCoord = -100;
                                        headYCoord = -100;
                                        break;
                                }

                                if (thePlayer.getIsDead()) {
                                    String deadStringForClock = this.pingDeadPlayerText.substring(0, 1);
                                    MINECRAFT.fontRendererObj.drawStringWithShadow(deadStringForClock, ((headXCoord + (headHeightAndWidth / 2)) - (MINECRAFT.fontRendererObj.getStringWidth(deadStringForClock) / 2)), ((headYCoord + (headHeightAndWidth / 2) - 3)), 0xFF5555);
                                }
                            }
                        }
                    }

                    int theClockColor = 0xFFFFFF;
                    int theClockBackgroundColor = Integer.MIN_VALUE;
                    if (this.displayBombClock) {
                        theClockBackgroundColor = 0x90FF0000;
                        if (MINESTRIKE.getRoundTimeRemaining() >= 30) {
                            theClockColor = 0xFFFF55;
                        } else if (MINESTRIKE.getRoundTimeRemaining() < 30 && MINESTRIKE.getRoundTimeRemaining() >= 15) {
                            theClockColor = 0xFFAA00;
                        } else {
                            theClockColor = 0xFF5555;
                        }
                    }

                    INSTANCE.getUtilsPublic().drawBoxOnScreen(clockBackgroundLeft, clockBackgroundTop, clockBackgroundRight, clockBackgroundBottom, theClockBackgroundColor);
                    if (MINESTRIKE.getRoundTimeRemaining() == 0) {
                        String scoreboardRoundTime = INSTANCE.getUtilsPublic().getTextFromScoreboardAtLine(0);
                        if (!scoreboardRoundTime.contains("Bomb")) {
                            scoreboardRoundTime = scoreboardRoundTime.replace(" Minutes", "m").replace(" Minute", "m").replace(" Seconds", "s");
                            int scoreboardRoundTimeWidth = MINECRAFT.fontRendererObj.getStringWidth(scoreboardRoundTime);
                            MINECRAFT.fontRendererObj.drawStringWithShadow(scoreboardRoundTime, (clockXPosition - (scoreboardRoundTimeWidth / 2)), clockYPosition, theClockColor);
                        }
                    } else {
                        MINECRAFT.fontRendererObj.drawStringWithShadow(this.getCurrentRoundTimeRemaining(), (clockXPosition - (clockStringWidth / 2)), clockYPosition, theClockColor);
                    }
                    int teamScoresYPosition = clockBackgroundBottom + 4;
                    MINECRAFT.fontRendererObj.drawStringWithShadow(this.getTeamScoreForTeam(TextFormattingEnum.AQUA), clockBackgroundLeft + 4, teamScoresYPosition, 0x55ffff);
                    MINECRAFT.fontRendererObj.drawStringWithShadow(this.getTeamScoreForTeam(TextFormattingEnum.RED), clockBackgroundRight - 8 - (MINECRAFT.fontRendererObj.getStringWidth(this.getTeamScoreForTeam(TextFormattingEnum.RED)) / 2), teamScoresYPosition, 0xff5555);
                }
            }
        } catch (Exception e) {
        }
    }

    private String getCurrentRoundTimeRemaining() {
        if (MINESTRIKE.getRoundTimeRemaining() >= 120) {
            return "2:00";
        } else if (MINESTRIKE.getRoundTimeRemaining() < 120 && MINESTRIKE.getRoundTimeRemaining() >= 60) {
            return ("1:" + String.format(this.clockFormatString, (MINESTRIKE.getRoundTimeRemaining() - 60)));
        } else if (MINESTRIKE.getRoundTimeRemaining() > 0) {
            return "0:" + String.format(this.clockFormatString, (MINESTRIKE.getRoundTimeRemaining()));
        } else {
            return "0:00";
        }
    }

    private String getTeamScoreForTeam(TextFormattingEnum e) {
        switch (e) {
            case AQUA:
                return INSTANCE.getUtilsPublic().getFullStringFromScoreboardWithQuery("SWAT").replace("SWAT ", "");
            case RED:
                return INSTANCE.getUtilsPublic().getFullStringFromScoreboardWithQuery("Bombers").replace("Bombers ", "");
        }
        return "";
    }

    public void updateKillsDeathsForPlayer(String playerName, boolean addKill, boolean addDeath) {
        boolean foundPreviousPlayer = false;
        for (Entry<String, MinestrikeScoreboardPlayer> entry : this.playerScoresMap.entrySet()) {
            if (entry.getKey().equals(playerName)) {
                if (addKill) {
                    entry.getValue().addKill();
                }
                if (addDeath) {
                    entry.getValue().addDeath();
                    entry.getValue().setIsDead(addDeath);
                }
                foundPreviousPlayer = true;
                break;
            }
        }

        if (!foundPreviousPlayer) {
            MinestrikeScoreboardPlayer newPlayer = new MinestrikeScoreboardPlayer();
            if (addKill) {
                newPlayer.addKill();
            }
            if (addDeath) {
                newPlayer.addDeath();
                newPlayer.setIsDead(addDeath);
            }
            this.playerScoresMap.put(playerName, newPlayer);
        }
    }

    public void updateDefusesPlantsForPlayer(String playerName) {
        boolean foundPreviousPlayer = false;
        for (Entry<String, MinestrikeScoreboardPlayer> entry : this.playerScoresMap.entrySet()) {
            if (entry.getKey().equals(playerName)) {
                entry.getValue().addDefuseOrPlant();
                foundPreviousPlayer = true;
                break;
            }
        }

        if (!foundPreviousPlayer) {
            MinestrikeScoreboardPlayer newPlayer = new MinestrikeScoreboardPlayer();
            newPlayer.addDefuseOrPlant();
            this.playerScoresMap.put(playerName, newPlayer);
        }
    }

    public void updateHPForPlayer(String playerName, int hpIn) {
        boolean foundPreviousPlayer = false;
        for (Map.Entry<String, MinestrikeScoreboardPlayer> entry : this.playerScoresMap.entrySet()) {
            if (entry.getKey().equals(playerName)) {
                entry.getValue().setHP(hpIn);
                foundPreviousPlayer = true;
                break;
            }
        }

        if (!foundPreviousPlayer) {
            MinestrikeScoreboardPlayer newPlayer = new MinestrikeScoreboardPlayer();
            newPlayer.setHP(hpIn);
            this.playerScoresMap.put(playerName, newPlayer);
        }
    }

    public void setPlayerInServer(String playerName, boolean inServer) {
        boolean foundPreviousPlayer = false;
        for (Entry<String, MinestrikeScoreboardPlayer> entry : this.playerScoresMap.entrySet()) {
            if (entry.getKey().equals(playerName)) {
                entry.getValue().setInServer(inServer);
                foundPreviousPlayer = true;
                break;
            }
        }

        if (!foundPreviousPlayer) {
            MinestrikeScoreboardPlayer newPlayer = new MinestrikeScoreboardPlayer();
            newPlayer.setInServer(inServer);
            newPlayer.setTeamColor(TextFormattingEnum.GRAY);
            this.playerScoresMap.put(playerName, newPlayer);
        }
    }

    public void renderBombClock() {
        MINESTRIKE.setRoundTimeRemaining(45);
        this.displayBombClock = true;
    }

    public void resetRound(int timeMode) {
        //0 - start of round
        //1 - start of game
        //2 - spectator
        //3 - update left players only
        if (timeMode != 3) {
            this.displayBombClock = false;

            if (this.playerScoresMap.containsKey(MINECRAFT.thePlayer.getName()) && this.playerScoresMap.get(MINECRAFT.thePlayer.getName()).getTeamColor() != TextFormattingEnum.GRAY) {
                this.playerScoresMap.get(MINECRAFT.thePlayer.getName()).setIsDead(false);
                this.playerScoresMap.get(MINECRAFT.thePlayer.getName()).setInServer(true);
            }
        }

        if (timeMode == 3) {
            //if (timeMode == 0 || timeMode == 3) {
            for (Map.Entry<String, MinestrikeScoreboardPlayer> entry : this.playerScoresMap.entrySet()) {
                entry.getValue().setHP(100);
                if (entry.getValue().getIsDead()) {
                    entry.getValue().setInServer(false);
                }
            }
        }
    }

    public void resetGame() {
        this.displayBombClock = false;
        MINESTRIKE.setRoundTimeRemaining(0);
        this.playerMap.clear();
        this.playerScoresMap.clear();
    }

    public void resetAfterGameReport() {
        if (!INSTANCE.getUtilsPublic().checkScoreboardHasString("Kit") && !INSTANCE.getUtilsPublic().checkScoreboardHasString("Players")) {
            this.playerScoresMapPost.clear();
        }
    }

    public void prepareAfterGameReport() {
        if (MINESTRIKE.getInGame()) {
            this.playerScoresMapPost.clear();

            for (Entry<String, MinestrikeScoreboardPlayer> e : this.playerScoresMap.entrySet()) {
                this.playerScoresMapPost.put(e.getKey(), e.getValue());
                this.playerScoresMapPost.get(e.getKey()).setIsDead(false);
            }
        }
    }

    public void setAfterGameReportRenderable(boolean renderable) {
        this.renderAfterGameReport = renderable;
    }

    public boolean getAfterGameReportRenderable() {
        return this.renderAfterGameReport;
    }

    public void initForMPSServer() {
        this.resetGame();
        this.resetRound(1);
    }

    public Map<String, MinestrikeScoreboardPlayer> getPlayerScoresMap() {
        return this.playerScoresMap;
    }
    
    public boolean getPlayerScoresMapPostIsEmpty() {
        return this.playerScoresMapPost.isEmpty();
    }

    public boolean getPlayerIsSameTeam(String playerName) {
        if (MINECRAFT.thePlayer == null || MINECRAFT.thePlayer.getGameProfile() == null || this.playerScoresMap.isEmpty()) {
            return false;
        }

        String ourName = MINECRAFT.thePlayer.getGameProfile().getName();

        if (this.playerScoresMap.get(ourName) != null && this.playerScoresMap.get(ourName).getIsDead()) {
            return true;
        } else if (this.playerScoresMap.get(ourName) != null && this.playerScoresMap.get(playerName) != null) {
            return this.playerScoresMap.get(ourName).getTeamColor() == this.playerScoresMap.get(playerName).getTeamColor();
        }

        return false;
    }

    public TextFormattingEnum getCurrentTeam() {
        if (MINECRAFT.thePlayer == null || MINECRAFT.thePlayer.getGameProfile() == null || this.playerScoresMap.isEmpty()) {
            return TextFormattingEnum.GRAY;
        }

        String ourName = MINECRAFT.thePlayer.getGameProfile().getName();
        if (this.playerScoresMap.get(ourName) != null) {
            return this.playerScoresMap.get(ourName).getTeamColor();
        }

        return TextFormattingEnum.GRAY;
    }

    public TextFormattingEnum getPlayersTeam(String playerName) {
        if (this.playerScoresMap.isEmpty()) {
            return TextFormattingEnum.GRAY;
        }
        if (this.playerScoresMap.get(playerName) != null) {
            return this.playerScoresMap.get(playerName).getTeamColor();
        }

        return TextFormattingEnum.GRAY;
    }

    @SideOnly(Side.CLIENT)
    static class PlayerComparator implements Comparator<NetworkPlayerInfo> {

        private PlayerComparator() {
        }

        @Override
        public int compare(NetworkPlayerInfo p_compare_1_, NetworkPlayerInfo p_compare_2_) {
            ScorePlayerTeam scoreplayerteam = p_compare_1_.getPlayerTeam();
            ScorePlayerTeam scoreplayerteam1 = p_compare_2_.getPlayerTeam();
            return ComparisonChain.start().compareTrueFirst(p_compare_1_.getGameType() != WorldSettings.GameType.SPECTATOR, p_compare_2_.getGameType() != WorldSettings.GameType.SPECTATOR).compare(scoreplayerteam != null ? scoreplayerteam.getRegisteredName() : "", scoreplayerteam1 != null ? scoreplayerteam1.getRegisteredName() : "").compare(p_compare_1_.getGameProfile().getName(), p_compare_2_.getGameProfile().getName()).result();
        }
    }

    public void resetAllHP() {
        for (Map.Entry<String, MinestrikeScoreboardPlayer> entry : this.playerScoresMap.entrySet()) {
            entry.getValue().setHP(100);
        }
    }
}
