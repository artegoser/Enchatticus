package ru.artegoser.enchatticus.chat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.Map;

final class LegacyComponentFormatter {
    private LegacyComponentFormatter() {
    }

    static Component format(String template, Map<String, Value> values) {
        MutableComponent root = Component.empty();
        Parser parser = new Parser(root, Style.EMPTY, values);
        parser.parse(template == null ? "" : template, true);
        return root;
    }

    record Value(String text, boolean allowFormatting) {
        static Value plain(String text) {
            return new Value(text == null ? "" : text, false);
        }

        static Value formatted(String text) {
            return new Value(text == null ? "" : text, true);
        }
    }

    private static final class Parser {
        private final MutableComponent root;
        private final Map<String, Value> values;
        private Style style;
        private final StringBuilder text = new StringBuilder();

        private Parser(MutableComponent root, Style initialStyle, Map<String, Value> values) {
            this.root = root;
            this.style = initialStyle;
            this.values = values;
        }

        private void parse(String input, boolean placeholders) {
            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);

                if ((c == '&' || c == '\u00a7') && i + 1 < input.length()) {
                    if (c == '&' && input.charAt(i + 1) == '&') {
                        text.append('&');
                        i++;
                        continue;
                    }

                    if (input.charAt(i + 1) == '#' && i + 7 < input.length()) {
                        String hex = input.substring(i + 2, i + 8);
                        try {
                            int rgb = Integer.parseInt(hex, 16);
                            flush();
                            style = Style.EMPTY.withColor(rgb);
                            i += 7;
                            continue;
                        } catch (NumberFormatException ignored) {
                        }
                    }

                    ChatFormatting formatting = ChatFormatting.getByCode(input.charAt(i + 1));
                    if (formatting != null) {
                        flush();
                        if (formatting == ChatFormatting.RESET) {
                            style = Style.EMPTY;
                        } else {
                            style = style.applyLegacyFormat(formatting);
                        }
                        i++;
                        continue;
                    }
                }

                if (placeholders && c == '{') {
                    int end = input.indexOf('}', i + 1);
                    if (end > i + 1) {
                        String key = input.substring(i + 1, end);
                        Value value = values.get(key);
                        if (value != null) {
                            flush();
                            if (value.allowFormatting()) {
                                parse(value.text(), false);
                            } else if (!value.text().isEmpty()) {
                                root.append(Component.literal(value.text()).setStyle(style));
                            }
                            i = end;
                            continue;
                        }
                    }
                }

                text.append(c);
            }
            flush();
        }

        private void flush() {
            if (text.isEmpty()) {
                return;
            }
            root.append(Component.literal(text.toString()).setStyle(style));
            text.setLength(0);
        }
    }
}
