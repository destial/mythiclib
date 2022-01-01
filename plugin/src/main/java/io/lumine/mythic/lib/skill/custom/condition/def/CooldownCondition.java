package io.lumine.mythic.lib.skill.custom.condition.def;

import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.custom.condition.Condition;

/**
 * Checks for a player cooldown
 */
public class CooldownCondition extends Condition {
    private final String cooldownPath;

    public CooldownCondition(ConfigObject config) {
        super(config);

        config.validateKeys("path");

        cooldownPath = config.getString("path");
    }

    @Override
    public boolean isMet(SkillMetadata meta) {
        return !meta.getCaster().getData().getCooldownMap().isOnCooldown(cooldownPath);
    }
}