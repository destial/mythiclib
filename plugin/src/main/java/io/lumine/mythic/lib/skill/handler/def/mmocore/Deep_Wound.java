package io.lumine.mythic.lib.skill.handler.def.mmocore;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.SkillResult;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class Deep_Wound extends SkillHandler<TargetSkillResult> {
    public Deep_Wound() {
        super();

        registerModifiers("damage", "extra");
    }

    @NotNull
    @Override
    public TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = result.getTarget();
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2, 2);
        target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, target.getHeight() / 2, 0), 32, 0, 0, 0, .7);
        target.getWorld().spawnParticle(Particle.BLOCK_CRACK, target.getLocation().add(0, target.getHeight() / 2, 0), 32, 0, 0, 0, 2,
                Material.REDSTONE_BLOCK.createBlockData());

        double max = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double ratio = (max - target.getHealth()) / max;

        double damage = skillMeta.getModifier("damage") * (1 + skillMeta.getModifier("extra") * ratio / 100);
        skillMeta.attack(target, damage, DamageType.SKILL, DamageType.PHYSICAL);
    }
}
