package io.lumine.mythic.lib.api.stat.handler;

import io.lumine.mythic.lib.api.stat.StatInstance;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class MovementSpeedStatHandler extends StatHandler {
    private final boolean moveSpeed;

    /**
     * Move speed and speed malus reduction, which share the same
     * update task, so they are grouped up in the same stat handler.
     *
     * @param config    Root stat handlers config file
     * @param stat      Stat identifier
     * @param moveSpeed Is it move speed?
     */
    public MovementSpeedStatHandler(@NotNull ConfigurationSection config, @NotNull String stat, boolean moveSpeed) {
        super(config, stat);

        this.moveSpeed = moveSpeed;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void runUpdate(@NotNull StatInstance randomInstance) {
        final AttributeInstance attrIns = randomInstance.getMap().getPlayerData().getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        removeModifiers(attrIns);

        // Calculate speed malus reduction (capped at 80%)
        final double coef = 1 - randomInstance.getMap().getStat("SPEED_MALUS_REDUCTION") / 100;

        final StatInstance statIns = randomInstance.getMap().getInstance("MOVEMENT_SPEED");
        final double mmo = statIns.getTotal(mod -> mod.getValue() < 0 ? mod.multiply(coef) : mod);

        /*
         * Calculate the stat base value. Since it can be changed by external
         * plugins, it's better to calculate it once and cache the result.
         *
         * This cannot use the TOTAL stat value otherwise the output of that
         * function depends on the date of execution because of attribute
         * modifiers from other plugins.
         */
        final double base = randomInstance.getMap().getPlayerData().getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();

        /*
         * Only add an attribute modifier if the very final stat
         * value is different from the main one to save calculations.
         */
        if (mmo != base)
            attrIns.addModifier(new AttributeModifier("mythiclib.main", mmo - base, AttributeModifier.Operation.ADD_NUMBER));
    }

    @Override
    public double getFinalValue(@NotNull StatInstance instance) {
        return moveSpeed ? instance.getMap().getPlayerData().getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue() : instance.getMap().getInstance("SPEED_MALUS_REDUCTION").getTotal();
    }

    @Override
    public double getBaseValue(@NotNull StatInstance instance) {
        return super.getBaseValue(instance) + (moveSpeed ? instance.getMap().getPlayerData().getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue() : 0);
    }
}
