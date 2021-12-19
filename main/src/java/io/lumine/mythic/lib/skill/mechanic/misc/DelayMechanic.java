package io.lumine.mythic.lib.skill.mechanic.misc;

import io.lumine.mythic.lib.util.ConfigObject;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.skill.MechanicQueue;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.mechanic.Mechanic;
import io.lumine.mythic.lib.skill.mechanic.MechanicMetadata;

/**
 * Used to add delay inside of a skill before casting
 * the rest of the mechanic list. This is quite a special
 * mechanic, see {@link MechanicQueue} for more info
 */
@MechanicMetadata
public class DelayMechanic extends Mechanic {
    private final DoubleFormula delay;

    public DelayMechanic(ConfigObject config) {
        config.validateKeys("amount");

        delay = new DoubleFormula(config.getString("amount"));
    }

    /**
     * @return Delay before next mechanic is cast in ticks
     */
    public long getDelay(SkillMetadata meta) {
        return (long) delay.evaluate(meta);
    }

    @Override
    public void cast(SkillMetadata meta) {
        throw new RuntimeException("Cannot run this mechanic");
    }
}
