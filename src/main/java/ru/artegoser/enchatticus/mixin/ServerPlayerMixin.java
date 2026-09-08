package ru.artegoser.enchatticus.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.artegoser.enchatticus.tab.TabService;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Inject(method = "getTabListDisplayName", at = @At("RETURN"), cancellable = true)
    private void enchatticus$getTabListDisplayName(CallbackInfoReturnable<Component> cir) {
        Component displayName = TabService.displayName((ServerPlayer) (Object) this);
        if (displayName != null) {
            cir.setReturnValue(displayName);
        }
    }
}
