package ru.artegoser.enchatticus.badge;

import org.junit.jupiter.api.Test;
import ru.artegoser.enchatticus.config.EnchatticusConfig;
import static org.junit.jupiter.api.Assertions.*;

class BadgeCustomizationPolicyTest {
    @Test void assignedReadOnlyBadgesRemainVisibleWithoutGrantingCustomization() {
        var badge = new EnchatticusConfig.Badge();
        assertTrue(BadgeCustomizationPolicy.visibleInListing(badge, false, () -> true));
        assertFalse(BadgeCustomizationPolicy.permits(badge, () -> true, node -> true));
        assertFalse(BadgeCustomizationPolicy.visibleInListing(badge, false, () -> false));
        badge.showInChat = false;
        badge.showInTab = true;
        assertTrue(BadgeCustomizationPolicy.visibleInListing(badge, false, () -> true));
        badge.showInTab = false;
        assertFalse(BadgeCustomizationPolicy.visibleInListing(badge, false, () -> true));
        assertTrue(BadgeCustomizationPolicy.visibleInListing(badge, true, () -> false));
    }
    @Test void defaultsDenyEvenWithAllPermissions() {
        var badge = new EnchatticusConfig.Badge();
        assertFalse(BadgeCustomizationPolicy.permits(badge, () -> true, node -> true));
        badge.customizable = true;
        assertFalse(BadgeCustomizationPolicy.permits(badge, () -> true, node -> false));
    }
    @Test void requiresBothExistingEligibilityAndPerBadgePermission() {
        var badge = new EnchatticusConfig.Badge(); badge.id = "police"; badge.customizable = true;
        assertTrue(BadgeCustomizationPolicy.permits(badge, () -> true, node -> node.equals("enchatticus.badges.customize.police")));
        assertFalse(BadgeCustomizationPolicy.permits(badge, () -> false, node -> true));
        badge.customizePermission = "custom.police";
        assertTrue(BadgeCustomizationPolicy.permits(badge, () -> true, node -> node.equals("custom.police")));
        badge.showInChat = false;
        assertFalse(BadgeCustomizationPolicy.permits(badge, () -> true, node -> true));
    }
    @Test void callerDefaultPermissionRemainsExplicitAndDeniedWhenUndefined() {
        var badge = new EnchatticusConfig.Badge(); badge.customizable = true;
        assertTrue(BadgeCustomizationPolicy.permits(badge, () -> true, node -> node.equals("example.badge"), "example.badge"));
        assertFalse(BadgeCustomizationPolicy.permits(badge, () -> true, node -> false, "example.badge"));
        assertFalse(BadgeCustomizationPolicy.permits(badge, () -> true, node -> true, ""));
    }
}
