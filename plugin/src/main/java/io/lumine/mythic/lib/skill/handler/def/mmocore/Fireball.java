package io.lumine.mythic.lib.skill.handler.def.mmocore;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.MMORayTraceResult;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.version.VersionSound;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class Fireball extends SkillHandler<SimpleSkillResult> {
    public Fireball() {
        super();

        registerModifiers("damage", "ignite", "ratio");
    }

    @NotNull
    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();

        caster.getWorld().playSound(caster.getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 1, 1);
        new BukkitRunnable() {
            int j = 0;
            final Vector vec = caster.getPlayer().getEyeLocation().getDirection();
            final Location loc = caster.getPlayer().getLocation().add(0, 1.3, 0);

            public void run() {
                if (j++ > 40) {
                    cancel();
                    return;
                }

                loc.add(vec);

                if (j % 3 == 0)
                    loc.getWorld().playSound(loc, Sound.BLOCK_FIRE_AMBIENT, 2, 1);
                loc.getWorld().spawnParticle(Particle.FLAME, loc, 4, .02, .02, .02, 0);
                loc.getWorld().spawnParticle(Particle.LAVA, loc, 0);

                for (Entity target : UtilityMethods.getNearbyChunkEntities(loc))
                    if (MythicLib.plugin.getVersion().getWrapper().isInBoundingBox(target, loc) && UtilityMethods.canTarget(caster, target)) {
                        loc.getWorld().spawnParticle(Particle.LAVA, loc, 8);
                        loc.getWorld().spawnParticle(Particle.FLAME, loc, 32, 0, 0, 0, .1);
                        loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_HURT, 2, 1);
                        target.setFireTicks((int) (target.getFireTicks() + skillMeta.getModifier("ignite") * 20));
                        double damage = skillMeta.getModifier("damage");
                        new AttackMetadata(new DamageMetadata(damage, DamageType.SKILL, DamageType.PROJECTILE, DamageType.MAGIC), skillMeta.getCaster()).damage((LivingEntity) target);

                        new BukkitRunnable() {
                            int i = 0;

                            @Override
                            public void run() {
                                if (i++ > 2) {
                                    cancel();
                                    return;
                                }

                                double range = 2.5 * (1 + random.nextDouble());
                                Vector dir = randomDirection();
                                loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_HURT, 2, 1.5f);

                                MMORayTraceResult result = MythicLib.plugin.getVersion().getWrapper().rayTrace(loc, dir, range, entity -> UtilityMethods.canTarget(caster, entity));
                                if (result.hasHit())
                                    new AttackMetadata(new DamageMetadata(damage, DamageType.SKILL, DamageType.PROJECTILE, DamageType.MAGIC), skillMeta.getCaster()).damage(result.getHit());
                                result.draw(loc.clone(), dir, 8, tick -> tick.getWorld().spawnParticle(Particle.FLAME, tick, 0));

                            }
                        }.runTaskTimer(MythicLib.plugin, 3, 3);

                        cancel();
                        return;
                    }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }

    private Vector randomDirection() {
        double x = random.nextDouble() - .5, y = (random.nextDouble() - .2) / 2, z = random.nextDouble() - .5;
        Vector dir = new Vector(x, y, z);
        return dir.lengthSquared() == 0 ? new Vector(1, 0, 0) : dir.normalize();
    }
}
