package ca.slackinjack.tbc.module.ms;

import ca.slackinjack.tbc.TBC;
import ca.slackinjack.tbc.server.Minigame;
import ca.slackinjack.tbc.server.MinigameModuleBase;
import ca.slackinjack.tbc.utils.packethandler.InboundPacketHandlerBase;
import net.minecraft.command.CommandBase;
import net.minecraftforge.fml.common.Mod;

@Mod(modid = TBCMinestrikeModule.MODID, version = TBCMinestrikeModule.VERSION, dependencies = TBCMinestrikeModule.DEPENDENCIES, acceptedMinecraftVersions = TBCMinestrikeModule.MCVERSION)
public class TBCMinestrikeModule implements MinigameModuleBase {
    
    public static final String MODID = "/";
    public static final String VERSION = "1.0.1";
    public static final String DEPENDENCIES = "required-after:.@[3.0.0,)";
    public static final String MCVERSION = "1.8.9";

    private static TBCMinestrikeModule MODULE_INSTANCE;
    private MinestrikeInboundPacketHandler packetHandler;
    private MinestrikeCommand command;
    private Minestrike MINESTRIKE;

    public TBCMinestrikeModule() {
        MODULE_INSTANCE = this;
    }

    @Override
    public MinigameModuleBase getModuleInstance() {
        return MODULE_INSTANCE;
    }

    @Override
    public Minigame getModuleMinigame(int dataIn) {
        if (MINESTRIKE == null) {
            MINESTRIKE = new Minestrike(TBC.getTBC(), MODULE_INSTANCE, (dataIn == 1));
        }
        return MINESTRIKE;
    }

    @Override
    public InboundPacketHandlerBase getInboundPacketHandler() {
        if (this.packetHandler == null) {
            this.packetHandler = new MinestrikeInboundPacketHandler(TBC.getTBC(), MINESTRIKE);
        }

        return this.packetHandler;
    }

    @Override
    public CommandBase getCommand() {
        if (this.command == null) {
            this.command = new MinestrikeCommand(TBC.getTBC(), this, MINESTRIKE);
        }
        return this.command;
    }
}
