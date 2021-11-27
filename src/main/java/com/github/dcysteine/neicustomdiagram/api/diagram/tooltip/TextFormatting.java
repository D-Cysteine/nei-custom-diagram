package com.github.dcysteine.neicustomdiagram.api.diagram.tooltip;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSortedSet;
import net.minecraft.util.EnumChatFormatting;

@AutoValue
public abstract class TextFormatting {
    public static TextFormatting create(boolean small, EnumChatFormatting... formatting) {
        return builder().setSmall(small).addAllFormatting(formatting).build();
    }

    public static TextFormatting create(EnumChatFormatting... formatting) {
        return create( false, formatting);
    }

    public abstract boolean small();

    /**
     * We use a sorted set here (with natural ordering) to ensure that colour codes are iterated
     * over before formatting codes. This ordering is necessary to get formatting to apply
     * correctly.
     *
     * @see <a href="https://minecraft.fandom.com/wiki/Formatting_codes">
     *     https://minecraft.fandom.com/wiki/Formatting_codes</a>
     */
    public abstract ImmutableSortedSet<EnumChatFormatting> formatting();

    public String format(String text) {
        StringBuilder builder = new StringBuilder();

        formatting().forEach(builder::append);
        builder.append(text);

        return builder.toString();
    }

    public static Builder builder() {
        return new AutoValue_TextFormatting.Builder()
                .setSmall(false);
    }

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setSmall(boolean small);
        public abstract Builder setFormatting(Iterable<EnumChatFormatting> formatting);
        public abstract ImmutableSortedSet.Builder<EnumChatFormatting> formattingBuilder();

        public Builder addFormatting(EnumChatFormatting formatting) {
            formattingBuilder().add(formatting);
            return this;
        }

        public Builder addAllFormatting(EnumChatFormatting... formatting) {
            formattingBuilder().add(formatting);
            return this;
        }

        public abstract TextFormatting build();
    }
}
