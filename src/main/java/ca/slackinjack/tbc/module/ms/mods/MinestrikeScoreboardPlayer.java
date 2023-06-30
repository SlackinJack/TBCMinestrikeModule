package ca.slackinjack.tbc.module.ms.mods;

import ca.slackinjack.tbc.utils.chat.TextFormattingEnum;
import net.minecraft.util.ResourceLocation;

public class MinestrikeScoreboardPlayer {

    private int kills;
    private int deaths;
    private int hp;
    private int defusesPlants;
    private TextFormattingEnum teamColor;
    // gray = unknown
    private boolean isDead;
    private boolean inServer;
    private ResourceLocation skinResourceLocation;

    public MinestrikeScoreboardPlayer() {
        this.kills = 0;
        this.deaths = 0;
        this.hp = 100;
        this.defusesPlants = 0;
        this.teamColor = TextFormattingEnum.GRAY;
        this.isDead = true;
        this.inServer = true;
        this.skinResourceLocation = null;
    }

    public MinestrikeScoreboardPlayer(ResourceLocation skinResourceLocationIn) {
        this.kills = 0;
        this.deaths = 0;
        this.hp = 100;
        this.defusesPlants = 0;
        this.teamColor = TextFormattingEnum.GRAY;
        this.isDead = true;
        this.inServer = true;
        this.skinResourceLocation = skinResourceLocationIn;
    }

    public void addKill() {
        ++this.kills;
    }

    public void addDeath() {
        ++this.deaths;
        this.isDead = true;
    }

    public void setHP(int hpIn) {
        this.hp = hpIn;
    }

    public void setTeamColor(TextFormattingEnum teamColor) {
        this.teamColor = teamColor;
    }

    public void setIsDead(boolean isDead) {
        this.isDead = isDead;
    }

    public void setInServer(boolean inServer) {
        this.inServer = inServer;
        if (!this.inServer) {
            this.isDead = true;
        }
    }

    public void setSkinResourceLocation(ResourceLocation skinResourceLocationIn) {
        this.skinResourceLocation = skinResourceLocationIn;
    }

    public int getKills() {
        return this.kills;
    }

    public int getDeaths() {
        return this.deaths;
    }

    public int getHP() {
        return this.hp;
    }

    public TextFormattingEnum getTeamColor() {
        return this.teamColor;
    }

    public boolean getIsDead() {
        return this.isDead;
    }

    public boolean getInServer() {
        return this.inServer;
    }

    public ResourceLocation getSkinResourceLocation() {
        return this.skinResourceLocation;
    }

    public int getDefusesOrPlants() {
        return this.defusesPlants;
    }

    public void addDefuseOrPlant() {
        ++this.defusesPlants;
    }
}
