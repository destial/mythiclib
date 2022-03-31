package io.lumine.mythic.lib.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.skill.custom.CustomSkill;
import io.lumine.mythic.lib.skill.custom.condition.Condition;
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
import io.lumine.mythic.lib.skill.custom.mechanic.misc.DispatchCommandMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.misc.LightningStrikeMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.misc.SkillMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.movement.TeleportMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.movement.VelocityMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.offense.DamageMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.offense.MultiplyDamageMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.offense.PotionMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.player.GiveItemMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.player.SudoMechanic;
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
import io.lumine.mythic.lib.skill.custom.mechanic.visual.ParticleMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.visual.SoundMechanic;
import io.lumine.mythic.lib.skill.custom.mechanic.visual.TellMechanic;
import io.lumine.mythic.lib.skill.custom.targeter.EntityTargeter;
import io.lumine.mythic.lib.skill.custom.targeter.LocationTargeter;
import io.lumine.mythic.lib.skill.custom.targeter.entity.*;
import io.lumine.mythic.lib.skill.custom.targeter.location.*;
import io.lumine.mythic.lib.skill.handler.MythicLibSkillHandler;
import io.lumine.mythic.lib.skill.handler.MythicMobsSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillAPISkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.util.RecursiveFolderExplorer;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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

    /**
     * Registered custom skills. In fact they have as much
     * information as a skill handler but it is not yet a
     * skill handler.
     */
    private final Map<String, CustomSkill> customSkills = new HashMap<>();

    /**
     * All registered skill handlers accessible by any external plugins. This uncludes:
     * - custom MM skill handlers
     * - custom SkillAPI skill handlers
     * - custom ML skill handlers
     * - default skill handlers from both MI and MMOCore (found in /skill/handler/def)
     */
    private final Map<String, SkillHandler> handlers = new HashMap<>();

    private final Map<Predicate<ConfigurationSection>, Function<ConfigurationSection, SkillHandler>> skillHandlerTypes = new HashMap<>();

    private boolean registration = true;

    public SkillManager() {

        // Default mechanics
        registerMechanic("skill", config -> new SkillMechanic(config));
        registerMechanic("delay", config -> new DelayMechanic(config));
        registerMechanic("lightning", config -> new LightningStrikeMechanic(config));
        registerMechanic("reduce_cooldown", config -> new ReduceCooldownMechanic(config));
        registerMechanic("dispatch_command", DispatchCommandMechanic::new);

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

        registerMechanic("sudo", config -> new SudoMechanic(config));
        registerMechanic("give_item", config -> new GiveItemMechanic(config));

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

        // Default skill handler types
        registerSkillHandlerType(config -> config.contains("mythiclib-skill-id"), config -> new MythicLibSkillHandler(getSkillOrThrow(config.getString("mythiclib-skill"))));
    }

    /**
     * @param matcher  If a certain skill config redirects to the skill handler
     *                 Example: a config which the following key should be handled
     *                 by {@link io.lumine.mythic.lib.skill.handler.MythicMobsSkillHandler}
     *                 <code>mythic-mobs-skill-id: WarriorStrike</code>
     * @param provider Function that provides the skill handler given the previous config,
     *                 if the config matches
     */
    public void registerSkillHandlerType(Predicate<ConfigurationSection> matcher, Function<ConfigurationSection, SkillHandler> provider) {
        Validate.notNull(matcher);
        Validate.notNull(provider);

        skillHandlerTypes.put(matcher, provider);
    }

    @NotNull
    public SkillHandler<?> loadSkillHandler(Object obj) throws IllegalArgumentException, IllegalStateException {

        // By handler name
        if (obj instanceof String)
            return getHandlerOrThrow(obj.toString());

        // By type of configuration section
        if (obj instanceof ConfigurationSection) {
            ConfigurationSection config = (ConfigurationSection) obj;
            for (Map.Entry<Predicate<ConfigurationSection>, Function<ConfigurationSection, SkillHandler>> type : skillHandlerTypes.entrySet())
                if (type.getKey().test(config))
                    return type.getValue().apply(config);

            throw new IllegalArgumentException("Could not match handler type to config");
        }

        throw new IllegalArgumentException("Provide either a string or configuration section instead of " + obj.getClass().getSimpleName());
    }

    public void registerSkillHandler(SkillHandler<?> handler) {
        Validate.isTrue(!handlers.containsKey(handler.getId()), "A skill handler with the same name already exists");

        handlers.put(handler.getId(), handler);
    }

    @NotNull
    public SkillHandler<?> getHandlerOrThrow(String id) {
        return Objects.requireNonNull(handlers.get(id), "Could not find handler with ID '" + id + "'");
    }

    public void registerCustomSkill(@NotNull CustomSkill skill) {
        Validate.isTrue(!customSkills.containsKey(skill.getId()), "A skill with the same name already exists");

        customSkills.put(skill.getId(), skill);
    }

    @NotNull
    public CustomSkill getSkillOrThrow(String name) {
        return Objects.requireNonNull(customSkills.get(name), "Could not find skill with name '" + name + "'");
    }

    public CustomSkill loadCustomSkill(Object obj) {

        if (obj instanceof String)
            return getSkillOrThrow(obj.toString());

        if (obj instanceof ConfigurationSection) {
            CustomSkill skill = new CustomSkill((ConfigurationSection) obj);
            skill.postLoad();
            return skill;
        }

        throw new IllegalArgumentException("Provide either a string or configuration section");
    }

    public Collection<CustomSkill> getCustomSkills() {
        return customSkills.values();
    }

    /**
     * @return Currently registered skill handlers.
     */
    public Collection<SkillHandler> getHandlers() {
        return handlers.values();
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

    public void initialize(boolean clearBefore) {
        if (clearBefore) {
            for (SkillHandler<?> handler : handlers.values())
                if (handler instanceof Listener)
                    HandlerList.unregisterAll((Listener) handler);

            handlers.clear();
            customSkills.clear();
        } else {
            registration = false;

            // mkdir skill folders
            File skillsFolder = new File(MythicLib.plugin.getDataFolder() + "/skill");
            if (!skillsFolder.exists())
                skillsFolder.mkdir();

            // mkdir skill folders
            File customSkillsFolder = new File(MythicLib.plugin.getDataFolder() + "/skill/custom");
            if (!customSkillsFolder.exists())
                customSkillsFolder.mkdir();

            // MythicMobs skill handler type
            if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null)
                registerSkillHandlerType(config -> config.contains("mythicmobs-skill-id"), config -> new MythicMobsSkillHandler(config));

            // SkillAPI skill handler type
            if (Bukkit.getPluginManager().getPlugin("SkillAPI") != null)
                registerSkillHandlerType(config -> config.contains("skillapi-skill-id"), config -> new SkillAPISkillHandler(config));
        }

        // Load default skills
        try {
            JarFile file = new JarFile(MythicLib.plugin.getJarFile());
            for (Enumeration<JarEntry> enu = file.entries(); enu.hasMoreElements(); ) {
                String name = enu.nextElement().getName().replace("/", ".");
                if (!name.contains("$") && name.endsWith(".class") && name.startsWith("io.lumine.mythic.lib.skill.handler.def.")) {
                    SkillHandler<?> ability = (SkillHandler<?>) Class.forName(name.substring(0, name.length() - 6)).getDeclaredConstructor().newInstance();
                    registerSkillHandler(ability);
                    if (ability instanceof Listener)
                        Bukkit.getPluginManager().registerEvents((Listener) ability, MythicLib.plugin);
                }
            }
            file.close();
        } catch (IOException | InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException exception) {
            exception.printStackTrace();
        }

        // Initialize custom skills
        new RecursiveFolderExplorer(file -> {

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            for (String key : config.getKeys(false))
                try {
                    registerCustomSkill(new CustomSkill(Objects.requireNonNull(config.getConfigurationSection(key), "Config is null")));
                } catch (RuntimeException exception) {
                    MythicLib.plugin.getLogger().log(Level.WARNING, "Could not initialize custom skill '" + key + "' from '" + file.getName() + "': " + exception.getMessage());
                }

        }, MythicLib.plugin, "Could not load custom skills").explore(new File(MythicLib.plugin.getDataFolder() + "/skill/custom"));

        // Post load custom skills and register a skill handler
        for (CustomSkill skill : customSkills.values())
            try {
                skill.postLoad();
                if (skill.isPublic())
                    registerSkillHandler(new MythicLibSkillHandler(skill));
            } catch (RuntimeException exception) {
                MythicLib.plugin.getLogger().log(Level.WARNING, "Could not load skill '" + skill.getId() + "': " + exception.getMessage());
            }

        // Load skills
        RecursiveFolderExplorer explorer = new RecursiveFolderExplorer(file -> {
            try {

                registerSkillHandler(loadSkillHandler(YamlConfiguration.loadConfiguration(file)));

            } catch (RuntimeException exception) {


                boolean oneSuccess = false;

                // Attempt to parse every key I guess
                ConfigurationSection config = YamlConfiguration.loadConfiguration(file);
                for (String key : config.getKeys(false)) {

                    // Get as configuration section
                    ConfigurationSection section = config.getConfigurationSection(key);
                    if (section == null) { continue; }

                    try {

                        // Attempt to load as normal section
                        registerSkillHandler(loadSkillHandler(section));
                        oneSuccess = true;

                    } catch (Throwable ignored) { }
                }

                /*
                 * Apparently users were not getting the correct 'messages' so this
                 * should tell the correct message if no skill was loaded right.
                 */
                if (!oneSuccess) { MythicLib.plugin.getLogger().log(Level.WARNING, "Could not load skill from '" + file.getName() + "': " + exception.getMessage()); }
            }

        }, MythicLib.plugin, "Could not load skills");

        for (File file : new File(MythicLib.plugin.getDataFolder() + "/skill").listFiles())
            if (!file.isDirectory() || !file.getName().equals("custom"))
                explorer.explore(file);
    }
}
