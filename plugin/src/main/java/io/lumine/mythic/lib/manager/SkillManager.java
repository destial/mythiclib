package io.lumine.mythic.lib.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.skill.custom.CustomSkill;
import io.lumine.mythic.lib.skill.custom.condition.Condition;
import io.lumine.mythic.lib.skill.condition.def.*;
import io.lumine.mythic.lib.skill.custom.condition.def.*;
import io.lumine.mythic.lib.skill.custom.condition.def.generic.BooleanCondition;
import io.lumine.mythic.lib.skill.custom.condition.def.generic.CompareCondition;
import io.lumine.mythic.lib.skill.custom.condition.def.generic.InBetweenCondition;
import io.lumine.mythic.lib.skill.custom.condition.def.generic.StringEqualsCondition;
import io.lumine.mythic.lib.skill.custom.mechanic.Mechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.buff.FeedMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.buff.HealMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.buff.ReduceCooldownMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.buff.SaturateMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.buff.stat.AddStatModifierMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.buff.stat.RemoveStatModifierMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.misc.DelayMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.misc.LightningStrikeMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.misc.SkillMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.movement.TeleportMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.movement.VelocityMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.offense.DamageMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.offense.MultiplyDamageMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.offense.PotionMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.raytrace.RayTraceAnyMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.raytrace.RayTraceBlocksMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.raytrace.RayTraceEntitiesMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.shaped.HelixMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.shaped.ProjectileMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.shaped.SlashMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.variable.SetDoubleMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.variable.SetIntegerMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.variable.SetStringMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.variable.SetVectorMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.variable.vector.*;
import io.lumine.mythic.lib.skill.custom.targeter.entity.*;
import io.lumine.mythic.lib.skill.custom.targeter.location.*;
import io.lumine.mythic.lib.skill.mechanic.variable.vector.*;
import io.lumine.mythic.lib.skill.custom.mechanic.visual.ParticleMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.visual.SoundMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.visual.TellMechanic;
import io.lumine.mythic.lib.skill.custom.targeter.EntityTargeter;
import io.lumine.mythic.lib.skill.custom.targeter.LocationTargeter;
import io.lumine.mythic.lib.skill.targeter.entity.*;
import io.lumine.mythic.lib.skill.targeter.location.*;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * The next step for MMO/Mythic abilities is to merge all the
 * different abilities of MMOItems and MMOCore. This will allow
 * us not to implement twice the same skill in the two plugins
 * which will be a gain of time.
 * <p>
 * The second thing is to make MythicLib a database combining:
 * - default MMOItems/MMOCore skills
 * - custom skills made using MythicMobs
 * - custom skills made using SkillAPI
 * - custom skills made using MythicLib (still under development)
 * <p>
 * Then users can "register" any of these base skills inside MMOItems
 * or MMOCore by adding one specific YAML to the "/skill" folder.
 *
 * @author jules
 */
public class SkillManager {
    private final Map<String, Function<ConfigObject, Mechanic>> mechanics = new HashMap<>();
    private final Map<String, Function<ConfigObject, Condition>> conditions = new HashMap<>();
    private final Map<String, Function<ConfigObject, EntityTargeter>> entityTargets = new HashMap<>();
    private final Map<String, Function<ConfigObject, LocationTargeter>> locationTargets = new HashMap<>();

    private final Map<String, CustomSkill> skills = new HashMap<>();

    private boolean registration = true;

    public SkillManager() {

        // Default mechanics
        registerMechanic("skill", config -> new SkillMechanic(config));
        registerMechanic("delay", config -> new DelayMechanic(config));
        registerMechanic("lightning", config -> new LightningStrikeMechanic(config));
        registerMechanic("reduce_cooldown", config -> new ReduceCooldownMechanic(config));

        registerMechanic("velocity", config -> new VelocityMechanic(config));
        registerMechanic("teleport", config -> new TeleportMechanic(config));

        registerMechanic("tell", config -> new TellMechanic(config));
        registerMechanic("particle", config -> new ParticleMechanic(config));
        registerMechanic("sound", config -> new SoundMechanic(config));

        registerMechanic("raytrace", config -> new RayTraceAnyMechanic(config));
        registerMechanic("raytrace_entities", config -> new RayTraceEntitiesMechanic(config));
        registerMechanic("raytrace_blocks", config -> new RayTraceBlocksMechanic(config));

        registerMechanic("projectile", config -> new ProjectileMechanic(config));
        registerMechanic("slash", config -> new SlashMechanic(config));
        registerMechanic("helix", config -> new HelixMechanic(config));

        registerMechanic("damage", config -> new DamageMechanic(config));
        registerMechanic("multiply_damage", config -> new MultiplyDamageMechanic(config));
        registerMechanic("potion", config -> new PotionMechanic(config));

        registerMechanic("heal", config -> new HealMechanic(config));
        registerMechanic("feed", config -> new FeedMechanic(config));
        registerMechanic("saturate", config -> new SaturateMechanic(config));
        registerMechanic("addstat", config -> new AddStatModifierMechanic(config));
        registerMechanic("removestat", config -> new RemoveStatModifierMechanic(config));

        registerMechanic("set_double", config -> new SetDoubleMechanic(config));
        registerMechanic("set_integer", config -> new SetIntegerMechanic(config));
        registerMechanic("set_string", config -> new SetStringMechanic(config));
        registerMechanic("set_vector", config -> new SetVectorMechanic(config));

        registerMechanic("save_vector", config -> new SaveVectorMechanic(config));
        registerMechanic("add_vector", config -> new AddVectorMechanic(config));
        registerMechanic("subtract_vector", config -> new SubtractVectorMechanic(config));
        registerMechanic("dot_product", config -> new DotProductMechanic(config));
        registerMechanic("cross_product", config -> new CrossProductMechanic(config));
        registerMechanic("hadamard_product", config -> new HadamardProductMechanic(config));
        registerMechanic("multiply_vector", config -> new MultiplyVectorMechanic(config));
        registerMechanic("normalize_vector", config -> new NormalizeVectorMechanic(config));

        // Default targeters
        registerEntityTargeter("target", config -> new TargetTargeter());
        registerEntityTargeter("caster", config -> new CasterTargeter());
        registerEntityTargeter("nearby_entities", config -> new NearbyEntitiesTargeter(config));
        registerEntityTargeter("nearest_entity", config -> new NearestEntityTargeter(config));
        registerEntityTargeter("variable", config -> new VariableEntityTargeter(config));
        registerEntityTargeter("cone", config -> new ConeTargeter(config));

        registerLocationTargeter("target", config -> new TargetEntityLocationTargeter(config));
        registerLocationTargeter("caster", config -> new CasterLocationTargeter(config));
        registerLocationTargeter("target_location", config -> new TargetLocationTargeter());
        registerLocationTargeter("source_location", config -> new SourceLocationTargeter());
        registerLocationTargeter("looking_at", config -> new LookingAtTargeter(config));
        registerLocationTargeter("circle", config -> new CircleLocationTargeter(config));
        registerLocationTargeter("variable", config -> new VariableLocationTargeter(config));
        registerLocationTargeter("custom", config -> new CustomLocationTargeter(config));

        // Default conditions
        registerCondition("boolean", config -> new BooleanCondition(config));
        registerCondition("string_equals", config -> new StringEqualsCondition(config));
        registerCondition("compare", config -> new CompareCondition(config));
        registerCondition("in_between", config -> new InBetweenCondition(config));

        registerCondition("time", config -> new TimeCondition(config));
        registerCondition("cuboid", config -> new CuboidCondition(config));
        registerCondition("cooldown", config -> new CooldownCondition(config));
        registerCondition("on_fire", config -> new OnFireCondition(config));
        registerCondition("is_living", config -> new IsLivingCondition(config));
        registerCondition("can_target", config -> new CanTargetCondition(config));
        registerCondition("has_damage_type", config -> new HasDamageTypeCondition(config));
    }

    public void registerSkill(CustomSkill skill) {
        Validate.isTrue(!skills.containsKey(skill.getId()), "A skill with the same name already exists");

        skills.put(skill.getId(), skill);
    }

    @NotNull
    public CustomSkill getSkillOrThrow(String name) {
        return Objects.requireNonNull(skills.get(name), "Could not find skill with name '" + name + "'");
    }

    public CustomSkill loadSkill(Object obj) {

        if (obj instanceof String)
            return getSkillOrThrow(obj.toString());

        if (obj instanceof ConfigurationSection) {
            CustomSkill skill = new CustomSkill((ConfigurationSection) obj);
            skill.postLoad();
            return skill;
        }

        throw new IllegalArgumentException("Please provide a string or configuration section");
    }

    public Collection<CustomSkill> getSkills() {
        return skills.values();
    }

    public void registerCondition(String name, Function<ConfigObject, Condition> condition) {
        Validate.isTrue(registration, "Condition registration is disabled");
        Validate.isTrue(!conditions.containsKey(name), "A condition with the same name already exists");
        Validate.notNull(condition, "Function cannot be null");

        conditions.put(name, condition);
    }

    @NotNull
    public Condition loadCondition(ConfigObject config) {
        Validate.isTrue(config.contains("type"), "Cannot find condition type");
        String key = config.getString("type");

        Function<ConfigObject, Condition> supplier = conditions.get(key);
        if (supplier != null)
            return supplier.apply(config);
        throw new IllegalArgumentException("Could not match condition to '" + key + "'");
    }

    public void registerMechanic(String name, Function<ConfigObject, Mechanic> mechanic) {
        Validate.isTrue(registration, "Mechanic registration is disabled");
        Validate.isTrue(!mechanics.containsKey(name), "A mechanic with the same name already exists");
        Validate.notNull(mechanic, "Function cannot be null");

        mechanics.put(name, mechanic);
    }

    @NotNull
    public Mechanic loadMechanic(ConfigObject config) {
        Validate.isTrue(config.contains("type"), "Cannot find mechanic type");
        String key = config.getString("type");

        Function<ConfigObject, Mechanic> supplier = mechanics.get(key);
        if (supplier != null)
            return supplier.apply(config);
        throw new IllegalArgumentException("Could not match mechanic to '" + key + "'");
    }

    public void registerEntityTargeter(String name, Function<ConfigObject, EntityTargeter> entityTarget) {
        Validate.isTrue(registration, "Targeter registration is disabled");
        Validate.isTrue(!entityTargets.containsKey(name), "A targeter with the same name already exists");
        Validate.notNull(entityTarget, "Function cannot be null");

        entityTargets.put(name, entityTarget);
    }

    @NotNull
    public EntityTargeter loadEntityTargeter(ConfigObject config) {
        Validate.isTrue(config.contains("type"), "Cannot find targeter type");
        String key = config.getString("type");

        Function<ConfigObject, EntityTargeter> supplier = entityTargets.get(key);
        if (supplier != null)
            return supplier.apply(config);
        throw new IllegalArgumentException("Could not match targeter to '" + key + "'");
    }

    public void registerLocationTargeter(String name, Function<ConfigObject, LocationTargeter> locationTarget) {
        Validate.isTrue(registration, "Targeter registration is disabled");
        Validate.isTrue(!locationTargets.containsKey(name), "A targeter with the same name already exists");
        Validate.notNull(locationTarget, "Function cannot be null");

        locationTargets.put(name, locationTarget);
    }

    @NotNull
    public LocationTargeter loadLocationTargeter(ConfigObject config) {
        Validate.isTrue(config.contains("type"), "Cannot find targeter type");
        String key = config.getString("type");

        Function<ConfigObject, LocationTargeter> supplier = locationTargets.get(key);
        if (supplier != null)
            return supplier.apply(config);
        throw new IllegalArgumentException("Could not match targeter to '" + key + "'");
    }

    public void loadLocalSkills() {
        if (registration)
            registration = false;
        else
            skills.clear();

        File rootFile = new File(MythicLib.plugin.getDataFolder() + "/skill");
        loadSkills(rootFile);

        // Post load all skills
        for (CustomSkill skill : skills.values())
            try {
                skill.postLoad();
            } catch (RuntimeException exception) {
                MythicLib.plugin.getLogger().log(Level.WARNING, "Could not load skill '" + skill.getId() + "': " + exception.getMessage());
            }
    }

    private void loadSkills(File file) {

        // Read subfiles recursively
        if (file.isDirectory())
            for (File subfile : file.listFiles())
                loadSkills(subfile);

            // Finally load skill when it's not a directory
        else {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            for (String key : config.getKeys(false))
                try {
                    CustomSkill skill = new CustomSkill(config.getConfigurationSection(key));
                    registerSkill(skill);
                } catch (RuntimeException exception) {
                    MythicLib.plugin.getLogger().log(Level.WARNING, "Could not initialize skill '" + key + "' from '" + file.getName() + "': " + exception.getMessage());
                }
        }
    }
}
