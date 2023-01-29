package com.github.dcysteine.neicustomdiagram.util;

import com.github.dcysteine.neicustomdiagram.main.config.ConfigOptions;
import com.google.common.base.Strings;
import net.minecraft.util.EnumChatFormatting;

import java.util.Stack;

public class NbtUtil {
    // Static class.
    private NbtUtil() {}

    public static String prettyPrintNbt(String nbt) {
        return new NbtPrettyPrinter().prettyPrint(nbt);
    }

    // Thought I could get away with just a simple string reader,
    // but maybe I should have just traversed the NBT tag tree instead...
    private static class NbtPrettyPrinter {
        private static final EnumChatFormatting BRACE_COLOUR = EnumChatFormatting.DARK_AQUA;
        private static final EnumChatFormatting BRACKET_COLOUR = EnumChatFormatting.DARK_GREEN;
        private static final EnumChatFormatting COLON_COLOUR = EnumChatFormatting.DARK_BLUE;
        private static final EnumChatFormatting QUOTE_COLOUR = EnumChatFormatting.DARK_RED;
        private static final EnumChatFormatting KEY_COLOUR = EnumChatFormatting.DARK_PURPLE;
        private static final EnumChatFormatting VALUE_COLOUR = EnumChatFormatting.BLACK;
        private static final EnumChatFormatting LIST_COLOUR = EnumChatFormatting.DARK_GRAY;
        private static final EnumChatFormatting RESET = EnumChatFormatting.RESET;
        private int indent = 0;
        private Stack<EnumChatFormatting> newlineFormatting = new Stack<>();
        private boolean isNewLine = true;
        private StringBuilder builder;

        public String prettyPrint(String nbt) {
            builder = new StringBuilder();
            char[] chars = nbt.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                switch (c) {
                    case ':':
                        // ':' should never be the first character on a line, but just in case...
                        applyIndent();
                        setFormatting(COLON_COLOUR);
                        builder.append(c);
                        if (!ConfigOptions.NBT_VIEWER_NEWLINE_VALUES.get()
                                || chars[i + 1] == '[' || chars[i + 1] == '{') {
                            builder.append(' ');
                        } else {
                            // Put the value on a separate line, to save horizontal space.
                            indent++;
                            newLine();
                            applyIndent();
                            indent--;
                        }
                        setFormatting(VALUE_COLOUR);
                        break;

                    case '"':
                        // '"' should never be the first character on a line, but just in case...
                        applyIndent();
                        // NBT strings can contain formatting characters, so reset.
                        setFormatting(RESET);
                        setFormatting(QUOTE_COLOUR);
                        builder.append(c);
                        setFormatting(VALUE_COLOUR);
                        break;

                    case ',':
                        newLine();
                        break;

                    case '[':
                        newlineFormatting.push(LIST_COLOUR);
                        applyIndent();
                        setFormatting(BRACKET_COLOUR);
                        builder.append(c);
                        indent++;
                        newLine();
                        break;

                    case '{':
                        newlineFormatting.push(KEY_COLOUR);
                        applyIndent();
                        setFormatting(BRACE_COLOUR);
                        builder.append(c);
                        indent++;
                        newLine();
                        break;

                    case ']':
                        newlineFormatting.pop();
                        indent--;
                        newLine();
                        applyIndent();
                        setFormatting(BRACKET_COLOUR);
                        builder.append(c);
                        newLine();
                        break;

                    case '}':
                        newlineFormatting.pop();
                        indent--;
                        newLine();
                        applyIndent();
                        setFormatting(BRACE_COLOUR);
                        builder.append(c);
                        newLine();
                        break;

                    default:
                        applyIndent();
                        builder.append(c);
                        break;
                }
            }
            return builder.toString();
        }

        private void newLine() {
            if (isNewLine) {
                return;
            }

            builder.append('\n');
            if (!newlineFormatting.empty()) {
                setFormatting(newlineFormatting.peek());
            }
            isNewLine = true;
        }

        private void applyIndent() {
            if (!isNewLine) {
                return;
            }

            builder.append(Strings.repeat("  ", indent));
            isNewLine = false;
        }

        private void setFormatting(EnumChatFormatting formatting) {
            builder.append(formatting);
        }
    }
}
