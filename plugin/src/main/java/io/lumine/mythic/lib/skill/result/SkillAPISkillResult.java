package io.lumine.mythic.lib.skill.result;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.api.skills.SkillShot;
import com.sucy.skill.api.skills.TargetSkill;
import com.sucy.skill.api.target.TargetHelper;
import io.lumine.mythic.lib.skill.SkillMetadata;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nullable;

public class SkillAPISkillResult implements SkillResult {
    private final PlayerData skillPlayerData;
    private final Skill skill;
    private final int level;

    @Nullable
    private LivingEntity target;

    public SkillAPISkillResult(SkillMetadata skillMeta, Skill skill) {
        this.skillPlayerData = SkillAPI.getPlayerData(skillMeta.getCaster().getPlayer());
        this.skill = skill;
        this.level = (int) skillMeta.getModifier("level");
    }

    public Skill getSkill() {
        return skill;
    }

    public int getLevel() {
        return level;
    }

    @Nullable
    public LivingEntity getTarget() {
        return target;
    }

    @Override
    public boolean isSuccessful(SkillMetadata skillMeta) {

        // Dead players can't cast skills
        if (skillMeta.getCaster().getPlayer().isDead())
            return false;

        // Skill Shots
        if (skill instanceof SkillShot)
            return true;

            // Target Skills
        else if (skill instanceof TargetSkill) {
            target = TargetHelper.getLivingTarget(skillPlayerData.getPlayer(), skill.getRange(level));

            // Must have a target
            if (target == null)
                return false;
        }

        return false;
    }
}
