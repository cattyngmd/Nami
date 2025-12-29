package me.kiriyaga.nami.feature.module.impl.visuals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.kiriyaga.nami.core.executable.model.ExecutableThreadType;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import me.kiriyaga.nami.util.entity.EntityUtils;
import me.kiriyaga.nami.util.NametagFormatter;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL32C;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.List;

import static me.kiriyaga.nami.Nami.*;
import static net.caffeinemc.mods.sodium.client.util.FlawlessFrames.isActive;

@RegisterModule
public class NametagsModule extends Module {

    public final BoolSetting self = addSetting(new BoolSetting("Self", false));
    public final BoolSetting players = addSetting(new BoolSetting("Players", true));
    public final BoolSetting hostiles = addSetting(new BoolSetting("Hostiles", false));
    public final BoolSetting neutrals = addSetting(new BoolSetting("Neutrals", false));
    public final BoolSetting passives = addSetting(new BoolSetting("Passives", false));
    public final BoolSetting items = addSetting(new BoolSetting("Items", false));
    public final BoolSetting tamed = addSetting(new BoolSetting("Tamed", false));
    public final BoolSetting pearls = addSetting(new BoolSetting("Pearls", false));
    public final BoolSetting showItems = addSetting(new BoolSetting("Equipment", true));
    public final BoolSetting showHealth = addSetting(new BoolSetting("Health", false));
    public final BoolSetting showGameMode = addSetting(new BoolSetting("Gamemode", false));
    public final BoolSetting showPing = addSetting(new BoolSetting("Ping", true));
    public final BoolSetting showEntityId = addSetting(new BoolSetting("EntityId", false));
    public final EnumSetting<TextFormat> formatting = addSetting(new EnumSetting<>("Format", TextFormat.NONE));
    public final BoolSetting background = addSetting(new BoolSetting("Background", false));
    public final BoolSetting border = addSetting(new BoolSetting("Border", true));
    public final DoubleSetting borderWidth = addSetting(new DoubleSetting("Width", 0.25, 0.11, 1));

    private final NametagFormatter formatter = new NametagFormatter(this);

    public enum TextFormat {
        NONE, BOLD, ITALIC, BOTH
    }

    private static final Map<UUID, String> uuid = new HashMap<>();

    public NametagsModule() {
        super("Nametags", "Draws nametags above certain entities.", ModuleCategory.of("Render"));
        border.setShowCondition(background::get);
        borderWidth.setShowCondition(() -> background.get() && border.get());
    }

    @SubscribeEvent
    public void onRender3d(Render3DEvent event) {
        if (MC.level == null || MC.player == null) return;

        int i = 0;

        PoseStack matrices = event.getMatrices();

        if (players.get()) {
            for (Player player : EntityUtils.getOtherPlayers()) {
                i++;
                renderEntityNametag(player, event.getTickDelta(), matrices, 30, null);
            }
        }

        if (self.get() && !MC.options.getCameraType().isFirstPerson()){
            i++;
            renderEntityNametag(MC.player, event.getTickDelta(), matrices, 30, null);
        }

        if (hostiles.get()) {
            for (var entity : EntityUtils.getEntities(EntityUtils.EntityTypeCategory.HOSTILE)) {
                i++;
                renderEntityNametag(entity, event.getTickDelta(), matrices, 30, null);
            }
        }

        if (neutrals.get()) {
            for (var entity : EntityUtils.getEntities(EntityUtils.EntityTypeCategory.NEUTRAL)) {
                i++;
                renderEntityNametag(entity, event.getTickDelta(), matrices, 30, null);
            }
        }

        if (passives.get()) {
            for (var entity : EntityUtils.getEntities(EntityUtils.EntityTypeCategory.PASSIVE)) {
                i++;
                renderEntityNametag(entity, event.getTickDelta(), matrices, 30, null);
            }
        }

        if (items.get()) {
            for (var entity : EntityUtils.getEntities(EntityUtils.EntityTypeCategory.DROPPED_ITEMS)) {
                if (entity instanceof ItemEntity itemEntity) {
                    renderEntityNametag(entity, Component.translatable(itemEntity.getItem().getItem().getDescriptionId()).getString(), event.getTickDelta(), matrices, 30, null);
                }
            }
        }

        if (tamed.get()) {

            for (var entity : EntityUtils.getAllEntities()) {

                @Nullable EntityReference<LivingEntity> owner;

                if (entity instanceof TamableAnimal tameable) {
                    owner = tameable.getOwnerReference();
                } else {
                    continue;
                }

                if (owner == null)
                    return;

                UUID uuid = owner.getUUID();

                String ownerName;

                if (NametagsModule.uuid.containsKey(uuid)) {
                    ownerName = NametagsModule.uuid.get(uuid);
                } else {
                    ownerName = "";

                    EXECUTABLE_MANAGER.getRequestHandler().submit(() -> {

                        if (isActive()) {
                            try {
                                String urlStr = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replace("-", "");
                                URL url = new URL(urlStr);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("GET");
                                connection.setConnectTimeout(5000);
                                connection.setReadTimeout(5000);

                                int status = connection.getResponseCode();

                                if (status == 200) {
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                    StringBuilder responseBuilder = new StringBuilder();
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        responseBuilder.append(line);
                                    }
                                    reader.close();

                                    String response = responseBuilder.toString();

                                    if (response != null && !response.isEmpty()) {
                                        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                                        if (json.has("name")) {
                                            String name = json.get("name").getAsString();
                                            NametagsModule.uuid.put(uuid, name);
                                        } else {
                                            NametagsModule.uuid.put(uuid, "Failed to get name");
                                        }
                                    } else {
                                        NametagsModule.uuid.put(uuid, "Failed to get name");
                                    }
                                } else {
                                    NametagsModule.uuid.put(uuid, "Failed to get name");
                                }

                                connection.disconnect();
                            } catch (Exception e) {
                                NametagsModule.uuid.put(uuid, "Failed to get name");
                            }
                        } else {
                        }
                    }, 0, ExecutableThreadType.ASYNC);
                }

                i++;
                renderEntityNametag(entity, "Owned by " + ownerName, event.getTickDelta(), matrices, 30, null);
            }
    }


        if (pearls.get()) {
            for (var entity : EntityUtils.getAllEntities()) {
                if (!(entity instanceof ThrownEnderpearl pearl)) continue;
                if (pearl.getOwner() == null) continue;
                i++;
                renderEntityNametag(pearl, pearl.getOwner().getName().getString(), event.getTickDelta(), matrices, 30, null);
            }
        }
        this.setDisplayInfo(String.valueOf(i));
    }

    private void renderEntityNametag(Entity entity, float tickDelta, PoseStack matrices, float scale, Color forcedColor) {
        renderEntityNametag(entity, entity.getName().getString(), tickDelta, matrices, scale, forcedColor);
    }

    private void renderEntityNametag(Entity entity, String name, float tickDelta, PoseStack matrices, float scale, Color forcedColor) {
        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();

        double baseHeightOffset = entity.isShiftKeyDown() ? entity.getBoundingBox().getYsize() : entity.getBoundingBox().getYsize() + 0.3;

        double interpX = Mth.lerp(tickDelta, entity.xOld, entity.getX());
        double interpY = Mth.lerp(tickDelta, entity.yOld, entity.getY());
        double interpZ = Mth.lerp(tickDelta, entity.zOld, entity.getZ());

        float distance = (float) camPos.distanceTo(new Vec3(interpX, interpY, interpZ));
        double distanceYOffset = distance * 0.02;

        Vec3 pos = new Vec3(
                interpX,
                interpY + baseHeightOffset + distanceYOffset,
                interpZ
        );

        float dynamicScale = 0.0018f + (scale / 10000.0f) * distance;
        if (distance <= 8.0f) dynamicScale = 0.0245f;

        Component displayName;

        if (entity instanceof Player player) {
            displayName = formatter.formatPlayer(player);

            if (showHealth.get()) {
                displayName = Component.literal("").append(displayName).append(Component.literal(" ")).append(formatter.getHealthText(player));
            }
            if (showPing.get()) {
                displayName = Component.literal("").append(displayName).append(Component.literal(" ")).append(formatter.formatPing(player));
            }
            if (showGameMode.get()) {
                displayName = Component.literal("").append(displayName).append(Component.literal(" ")).append(formatter.formatGameMode(player));
            }
            if (showEntityId.get()) {
                displayName = Component.literal("").append(displayName).append(Component.literal(" ")).append(formatter.formatEntityId(entity));
            }
        } else if (name != null) {
            displayName = Component.literal(name);
        } else {
            displayName = formatter.formatEntity(entity);
        }

        Component colored = formatter.formatWithColor(displayName, forcedColor, entity);

        RenderUtil.drawText3D(matrices, colored, pos, dynamicScale, background.get(), border.get(), borderWidth.get().floatValue());

        if (showItems.get() && entity instanceof Player player) {
            renderPlayerItems(player, matrices, tickDelta, scale);
        }
    }

    private void renderPlayerItems(Player player, PoseStack matrices, float tickDelta, float baseScale) {
        List<ItemStack> items = Arrays.asList(
                player.getMainHandItem(),
                player.getItemBySlot(EquipmentSlot.HEAD),
                player.getItemBySlot(EquipmentSlot.CHEST),
                player.getItemBySlot(EquipmentSlot.LEGS),
                player.getItemBySlot(EquipmentSlot.FEET),
                player.getOffhandItem()
        );

        List<ItemStack> nonEmptyItems = items.stream().filter(stack -> !stack.isEmpty()).toList();
        int itemCount = nonEmptyItems.size();
        if (itemCount == 0) return;

        double interpMinX = Mth.lerp(tickDelta, player.xOld, player.getX()) - player.getBbWidth() / 2.0;
        double interpMinY = Mth.lerp(tickDelta, player.yOld, player.getY());
        double interpMinZ = Mth.lerp(tickDelta, player.zOld, player.getZ()) - player.getBbWidth() / 2.0;

        double interpMaxX = interpMinX + player.getBbWidth();
        double interpMaxY = interpMinY + player.getBbHeight();
        double interpMaxZ = interpMinZ + player.getBbWidth();

        double baseX = (interpMinX + interpMaxX) / 2.0;
        double baseY = interpMaxY + (player.isShiftKeyDown() ? 0.0 : 0.3);
        double baseZ = (interpMinZ + interpMaxZ) / 2.0;

        Vec3 camPos = MC.getEntityRenderDispatcher().camera.position();
        Camera camera = MC.gameRenderer.getMainCamera();
        float pitch = camera.xRot();
        float yaw = camera.yRot();

        Vec3 lookDir = Vec3.directionFromRotation(pitch, yaw).normalize().reverse();
        Vec3 camRight = lookDir.cross(new Vec3(0, 1, 0)).normalize();

        int renderIndex = 0;
        for (ItemStack stack : nonEmptyItems) {
            renderItemWithDepthIsolation(stack, matrices, baseX, baseY, baseZ, renderIndex, itemCount, camPos, camRight, lookDir, baseScale);
            renderIndex++;
        }
    }

    private void renderItemWithDepthIsolation(
            ItemStack stack,
            PoseStack matrices,
            double baseX, double baseY, double baseZ,
            int renderIndex, int itemCount,
            Vec3 camPos, Vec3 camRight, Vec3 lookDir,
            float baseScale
    ) {
        Vec3 itemPosBase = new Vec3(baseX, baseY, baseZ);
        float distance = (float) camPos.distanceTo(itemPosBase);
        float dynamicScale = 0.0018f + (baseScale / 10000.0f) * distance;
        if (distance <= 8.0f) dynamicScale = 0.0245f;

        double itemSpacing = dynamicScale * 12.0;

        double verticalOffset = dynamicScale * 10.0 + distance * 0.02;

        double offsetX = (renderIndex - (itemCount - 1) / 2.0) * itemSpacing;

        Vec3 itemPos = itemPosBase.add(camRight.scale(offsetX)).add(0, verticalOffset, 0);

        //GL32C.glDisable(GL32C.GL_DEPTH_TEST);
        //GL32C.glDepthMask(false);
        //GL32C.glDepthFunc(GL32C.GL_ALWAYS);
        GL32C.glDepthRange(1.0, 0.1);

        RenderUtil.renderItem3D(stack, matrices, itemPos, dynamicScale, lookDir);
        
        //GL32C.glDepthFunc(GL32C.GL_LEQUAL);
        //GL32C.glDepthMask(true);
        //GL32C.glEnable(GL32C.GL_DEPTH_TEST);
        GL32C.glDepthRange(0.0, 1.0);
    }
}