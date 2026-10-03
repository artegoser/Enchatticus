package ru.artegoser.enchatticus.badge;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import ru.artegoser.enchatticus.config.EnchatticusConfig;

final class BadgeCustomizationPolicy {
    private BadgeCustomizationPolicy() {}
    static boolean visibleInListing(EnchatticusConfig.Badge badge, boolean administrative, BooleanSupplier assigned) {
        return administrative || (badge.showInChat || badge.showInTab) && assigned.getAsBoolean();
    }
    static boolean permits(EnchatticusConfig.Badge badge, BooleanSupplier eligible, Predicate<String> permissions) {
        return permits(badge, eligible, permissions, "enchatticus.badges.customize." + badge.id);
    }
    static boolean permits(EnchatticusConfig.Badge badge, BooleanSupplier eligible, Predicate<String> permissions,
                           String defaultPermission) {
        if (!badge.customizable || !badge.showInChat || !eligible.getAsBoolean()) return false;
        String node = badge.customizePermission.isBlank() ? defaultPermission : badge.customizePermission;
        if (node == null || node.isBlank()) return false;
        return permissions.test(node);
    }
}
