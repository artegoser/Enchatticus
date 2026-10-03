package ru.artegoser.enchatticus.badge;

/** Chat badge editing contract; TAB visibility remains owned by Enchatticus. */
public record BadgeDefinition(String id, String text, String hover, String group,
                              String permission, String clickCommand, String clickAction, boolean showInChat) {
    public void validate() {
        if (id == null || !id.matches("[a-zA-Z0-9_.-]{1,64}")) throw new IllegalArgumentException("Badge ID must contain 1–64 letters, digits, _, . or -");
        bounded(text, 512); bounded(hover, 2048); bounded(group, 128);
        bounded(permission, 256); bounded(clickCommand, 512);
        if (!java.util.Set.of("suggest", "run", "copy").contains(clickAction)) throw new IllegalArgumentException("Click action must be suggest, run or copy");
    }
    private static void bounded(String value, int maximum) {
        if (value == null || value.length() > maximum) throw new IllegalArgumentException("Badge field exceeds " + maximum + " characters");
    }
}
