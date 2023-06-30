package ca.slackinjack.tbc.module.ms.mods;

import ca.slackinjack.tbc.TBC;
import ca.slackinjack.tbc.module.ms.Minestrike;
import ca.slackinjack.tbc.module.ms.TBCMinestrikeModule;
import java.util.Map;

public class MinestrikeSpectatorMode {

    private final TBC INSTANCE;
    private final TBCMinestrikeModule MODULE_INSTANCE;
    private final Minestrike MINESTRIKE;

    public MinestrikeSpectatorMode(TBC tbcIn, TBCMinestrikeModule modIn, Minestrike msIn) {
        INSTANCE = tbcIn;
        MODULE_INSTANCE = modIn;
        MINESTRIKE = msIn;
    }

    public void addAllTargets() {
        if (!MINESTRIKE.getScoreboard().getPlayerScoresMap().isEmpty()) {
            for (Map.Entry<String, MinestrikeScoreboardPlayer> thePlayer : MINESTRIKE.getScoreboard().getPlayerScoresMap().entrySet()) {
                INSTANCE.getUtilsPublic().addTargetForMinigame(thePlayer.getKey(), thePlayer.getValue().getTeamColor());
            }
        }
    }

    public void removeAllTargets() {
        INSTANCE.getUtilsPublic().removeAllTargetsForMinigame();
    }
}
