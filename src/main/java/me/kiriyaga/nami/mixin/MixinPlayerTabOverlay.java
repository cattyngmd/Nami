package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.feature.module.impl.miscellaneous.BetterTabModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.stream.Collectors;

import static me.kiriyaga.nami.Nami.FRIEND_MANAGER;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(PlayerTabOverlay.class)
public abstract class MixinPlayerTabOverlay {

    @Shadow @Final private Minecraft minecraft;

    @Shadow @Final private static Comparator<PlayerInfo> PLAYER_COMPARATOR;

    @Shadow protected abstract Component decorateName(PlayerInfo entry, MutableComponent name);

    private final Set<String> cachedFriends = new HashSet<>();
    private long lastFriendCacheUpdate = 0;
    private final long friendCacheInterval = 1000;

    @Inject(method = "getPlayerInfos", at = @At("HEAD"), cancellable = true)
    private void collectPlayerEntries(CallbackInfoReturnable<List<PlayerInfo>> info) {
        BetterTabModule betterTab = MODULE_MANAGER.getStorage() != null ? MODULE_MANAGER.getStorage().getByClass(BetterTabModule.class) : null;
        if (betterTab == null || !betterTab.isEnabled()) return;
        if (minecraft == null || minecraft.player == null || minecraft.player.connection == null) return;

        Collection<PlayerInfo> allEntries = minecraft.player.connection.getListedOnlinePlayers();

        List<PlayerInfo> result;

        if (betterTab.friendsOnly.get()) {
            long now = System.currentTimeMillis();
            if (now - lastFriendCacheUpdate > friendCacheInterval) {
                cachedFriends.clear();
                FRIEND_MANAGER.getFriends().forEach(friend -> cachedFriends.add(friend.toLowerCase()));
                lastFriendCacheUpdate = now;
            }

            int limit = betterTab.limit.get();
            result = new ArrayList<>(limit);

            for (PlayerInfo entry : allEntries) {
                String name = entry.getProfile().name().toLowerCase();
                if (cachedFriends.contains(name)) {
                    result.add(entry);
                    if (result.size() >= limit) break;
                }
            }

            result.sort(PLAYER_COMPARATOR);

        } else {
            result = allEntries.stream()
                    .limit(betterTab.limit.get())
                    .sorted(PLAYER_COMPARATOR)
                    .collect(Collectors.toList());
        }

        info.setReturnValue(result);
    }

    @Inject(method = "getNameForDisplay", at = @At("HEAD"), cancellable = true)
    private void getPlayerName(PlayerInfo entry, CallbackInfoReturnable<Component> info) {
        BetterTabModule betterTab = MODULE_MANAGER.getStorage() != null ? MODULE_MANAGER.getStorage().getByClass(BetterTabModule.class) : null;
        if (betterTab == null || !betterTab.isEnabled()) return;

        boolean highlightFriends = betterTab.highlighFriends.get();
        String playerName = entry.getProfile().name();
        boolean isFriend = FRIEND_MANAGER.isFriend(playerName);

        if (highlightFriends && isFriend) {
            MutableComponent formattedName = Component.empty();

            if (entry.getTabListDisplayName() != null) {
                for (Component sibling : entry.getTabListDisplayName().getSiblings()) {
                    String str = sibling.getString();
                    if (str.equals(playerName)) {
                        formattedName.append(Component.literal(playerName).withStyle(ChatFormatting.AQUA));
                    } else if (str.equals("] " + playerName)) {
                        formattedName.append(Component.literal("] ").withStyle(ChatFormatting.WHITE))
                                .append(Component.literal(playerName).withStyle(ChatFormatting.AQUA));
                    } else {
                        formattedName.append(sibling);
                    }
                }
            } else {
                formattedName = PlayerTeam.formatNameForTeam(entry.getTeam(), Component.literal(playerName).withStyle(ChatFormatting.AQUA));
            }

            info.setReturnValue(decorateName(entry, formattedName));
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void render(GuiGraphics drawContext, int width, Scoreboard scoreboard, @Nullable Objective objective, CallbackInfo ci) {
        BetterTabModule betterTab = MODULE_MANAGER.getStorage().getByClass(BetterTabModule.class);
        if (betterTab != null && betterTab.isEnabled()) {
            float scale = betterTab.scale.get().floatValue();

            drawContext.pose().pushMatrix();

            float centerX = width / 2f;
            float centerY = 10f;

            drawContext.pose().translate(centerX, centerY);
            drawContext.pose().scale(scale, scale);
            drawContext.pose().translate(-centerX, -centerY);
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void render2(GuiGraphics drawContext, int width, Scoreboard scoreboard, @Nullable Objective objective, CallbackInfo ci) {
        BetterTabModule betterTab = MODULE_MANAGER.getStorage().getByClass(BetterTabModule.class);
        if (betterTab != null && betterTab.isEnabled()) {
            drawContext.pose().popMatrix();
        }
    }
}
