package ca.slackinjack.tbc.module.ms;

import ca.slackinjack.tbc.TBC;
import ca.slackinjack.tbc.utils.TBCExternalModulesEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class MinestrikeCommand extends CommandBase {

    private final TBC INSTANCE;
    private final TBCMinestrikeModule MODULE_INSTANCE;
    private final Minestrike MINESTRIKE;
    private final Minecraft MINECRAFT = Minecraft.getMinecraft();
    private final String usageText = "Usage: /ms smoke/gui (health/hitmarker/hotbar/log)";

    public MinestrikeCommand(TBC tbcIn, TBCMinestrikeModule modIn, Minestrike msIn) {
        INSTANCE = tbcIn;
        MODULE_INSTANCE = modIn;
        MINESTRIKE = msIn;
    }

    @Override
    public String getCommandName() {
        return TBCExternalModulesEnum.getCommandNameFor(TBCExternalModulesEnum.MINESTRIKE);
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        switch (args.length) {
            case 1:
                switch (args[0]) {
                    case "smoke":
                        MINESTRIKE.toggleSmoke();
                        MINECRAFT.renderGlobal.loadRenderers();
                        break;
                    default:
                        INSTANCE.getUtilsPublic().addUnformattedChatMessage(this.usageText, 1);
                        break;
                }
            case 2:
                switch (args[0]) {
                    case "gui":
                        switch (args[1]) {
                            case "health":
                                MINESTRIKE.setShouldShowHealthEditor();
                                break;
                            case "hitmarker":
                                MINESTRIKE.setShouldShowHitmarkerEditor();
                                break;
                            case "hotbar":
                                MINESTRIKE.setShouldShowHotbarEditor();
                                break;
                            case "log":
                                MINESTRIKE.setShouldShowLogEditor();
                                break;
                            default:
                                INSTANCE.getUtilsPublic().addUnformattedChatMessage(this.usageText, 1);
                                break;
                        }
                        break;
                    default:
                        INSTANCE.getUtilsPublic().addUnformattedChatMessage(this.usageText, 1);
                        break;
                }
            default:
                INSTANCE.getUtilsPublic().addUnformattedChatMessage(this.usageText, 1);
                break;
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}
