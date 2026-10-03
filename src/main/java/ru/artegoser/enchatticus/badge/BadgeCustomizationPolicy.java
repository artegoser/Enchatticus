package ru.artegoser.enchatticus.badge;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import ru.artegoser.enchatticus.config.EnchatticusConfig;

final class BadgeCustomizationPolicy {
    private BadgeCustomizationPolicy() {}
    static boolean permits(EnchatticusConfig.Badge badge, BooleanSupplier eligible, Predicate<String> permissions) {
        if (!badge.customizable || !badge.showInChat || !eligible.getAsBoolean()) return false;
        String node = badge.customizePermission.isBlank() ? "cosmeticum.badges.customize." + badge.id : badge.customizePermission;
        return permissions.test(node);
    }
}
