package io.lumine.mythic.lib.skill.targeter;

import io.lumine.mythic.lib.skill.SkillMetadata;
import org.bukkit.entity.Entity;

import java.util.List;

@FunctionalInterface
public interface EntityTargeter {

    public List<Entity> findTargets(SkillMetadata meta);
}