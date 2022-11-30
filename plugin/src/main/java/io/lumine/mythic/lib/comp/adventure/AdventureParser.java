package io.lumine.mythic.lib.comp.adventure;

import io.lumine.mythic.lib.comp.adventure.argument.AdventureArgument;
import io.lumine.mythic.lib.comp.adventure.argument.AdventureArgumentQueue;
import io.lumine.mythic.lib.comp.adventure.tag.AdventureTag;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * mythiclib
 * 30/11/2022
 *
 * @author Roch Blondiaux (Kiwix).
 */
@ApiStatus.NonExtendable
public class AdventureParser {
    // TODO: make a builder

    /* Constants */
    private static final String DEFAULT_TAG_REGEX = "(?i)(?<=<(%s)).*?(?=>)";
    private static final Pattern TAG_REGEX = Pattern.compile("(?i)(?<=<).*?(?=>)");

    private final List<AdventureTag> tags = new ArrayList<>();
    private final Function<String, String> fallBackResolver;

    public AdventureParser(@NotNull Function<String, String> fallBackResolver) {
        // TODO: register all default tags
        this.fallBackResolver = fallBackResolver;
    }

    public AdventureParser() {
        this(s -> "<invalid>");
    }

    public @NotNull String parse(@NotNull final String src) {
        String cpy = src;
        for (AdventureTag tag : tags) {
            cpy = parseTag(cpy, tag, tag.name());
            for (String alias : tag.aliases())
                cpy = parseTag(cpy, tag, alias);
        }
        cpy = removeUnparsedAndUselessTags(cpy);
        return minecraftColorization(cpy);
    }

    private @NotNull String parseTag(@NotNull final String src, @NotNull final AdventureTag tag, @NotNull final String tagIdentifier) {
        final Pattern pattern = Pattern.compile(String.format(DEFAULT_TAG_REGEX, tagIdentifier));
        Matcher matcher = pattern.matcher(src);

        String cpy = src;
        while (matcher.find()) {
            final String rawTag = matcher.group(1);
            final String rawArgs = matcher.group();
            final List<AdventureArgument> args = Arrays.stream(rawArgs.split(":"))
                    .map(AdventureArgument::new)
                    .collect(Collectors.toList());
            final String resolved = tag.resolver().resolve(rawTag, new AdventureArgumentQueue(args));
            final String original = "<%s%s>".formatted(rawTag, rawArgs);
            cpy = cpy.replace(original, Objects.requireNonNullElse(resolved, fallBackResolver.apply(original)));
            matcher = pattern.matcher(cpy);
        }
        return cpy;
    }

    private @NotNull String removeUnparsedAndUselessTags(@NotNull String src) {
        Matcher matcher = TAG_REGEX.matcher(src);
        while (matcher.find()) {
            final String original = "<%s>".formatted(matcher.group());
            src = src.replace(original, fallBackResolver.apply(original));
            matcher = TAG_REGEX.matcher(src);
        }
        return src;
    }

    private @NotNull String minecraftColorization(@NotNull final String src) {
        return ChatColor.translateAlternateColorCodes('&', src);
    }

    public void add(AdventureTag tag) {
        tags.add(tag);
    }

    public void remove(AdventureTag tag) {
        tags.remove(tag);
    }

    public List<AdventureTag> tags() {
        return tags;
    }
}
