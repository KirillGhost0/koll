package com.endlessbrood.common;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

/** Read-only, works in survival on a dedicated server without cheats. */
public final class EvolutionCommand extends CommandBase {
    @Override
    public String getCommandName() { return "broodstage"; }

    @Override
    public String getCommandUsage(ICommandSender sender) { return "/broodstage"; }

    @Override
    public int getRequiredPermissionLevel() { return 0; }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        EvolutionData data = EvolutionData.get(sender.getEntityWorld());
        sender.addChatMessage(new ChatComponentText(
            "[Endless Brood] Stage: " + data.getStage() +
            " | biomass: " + data.getPoints() +
            " | next stage cost: " + BroodConfig.effectiveEvolutionCost()));
        sender.addChatMessage(new ChatComponentText(
            "[Brood pacing] Combat progress today: " +
            data.getCombatPointsToday(sender.getEntityWorld().getTotalWorldTime()) +
            "/" + BroodConfig.combatPointsPerDay +
            " (passive growth is not capped)."));
        if (BroodConfig.adaptiveDefense) {
            sender.addChatMessage(new ChatComponentText(
                "[Brood resistance] fire: " + percent(data.getResistance(BroodAdaptiveMath.FIRE)) +
                " / blast: " + percent(data.getResistance(BroodAdaptiveMath.EXPLOSIVE)) +
                " / ranged: " + percent(data.getResistance(BroodAdaptiveMath.RANGED)) +
                " / radiation: " + percent(data.getResistance(BroodAdaptiveMath.RADIATION))));
        }
    }

    private static String percent(double value) {
        return (int) Math.round(value * 100.0D) + "%";
    }
}
