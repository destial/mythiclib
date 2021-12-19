package io.lumine.mythic.lib.skill.mechanic.visual;

import io.lumine.mythic.lib.util.ConfigObject;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.skill.mechanic.type.TargetMechanic;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@MechanicMetadata
public class TellMechanic extends TargetMechanic {
    private final String message;

    public TellMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("format");

        message = config.getString("format");
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        Validate.isTrue(target instanceof Player, "Can only send messages to players");

        // Apply placeholders
        String formatted = meta.parseString(message);

        // Send message
        target.sendMessage(formatted);
    }
}
