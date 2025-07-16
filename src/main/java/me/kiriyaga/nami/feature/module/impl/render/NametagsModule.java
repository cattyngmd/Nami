package me.kiriyaga.nami.feature.module.impl.render;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import me.kiriyaga.nami.setting.impl.EnumSetting;
import me.kiriyaga.nami.util.NametagFormatter;
import me.kiriyaga.nami.util.render.Layers;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static me.kiriyaga.nami.Nami.*;

public class NametagsModule extends Module {

    public final BoolSetting self = addSetting(new BoolSetting("self", false));
    public final BoolSetting players = addSetting(new BoolSetting("players", true));
    public final BoolSetting hostiles = addSetting(new BoolSetting("hostiles", false));
    public final BoolSetting neutrals = addSetting(new BoolSetting("neutrals", false));
    public final BoolSetting passives = addSetting(new BoolSetting("passives", false));
    public final BoolSetting items = addSetting(new BoolSetting("items", false));
    public final BoolSetting showItems = addSetting(new BoolSetting("equipment", true));
    public final BoolSetting showHealth = addSetting(new BoolSetting("health", false));
    public final BoolSetting showGameMode = addSetting(new BoolSetting("gamemode", false));
    public final BoolSetting showPing = addSetting(new BoolSetting("ping", true));
    public final BoolSetting showEntityId = addSetting(new BoolSetting("entityId", false));
    public final EnumSetting<TextFormat> formatting = addSetting(new EnumSetting<>("format", TextFormat.None));
    public final BoolSetting background = addSetting(new BoolSetting("background", false));
    public final BoolSetting border = addSetting(new BoolSetting("border", true));
    public final DoubleSetting borderWidth = addSetting(new DoubleSetting("border width", 0.25, 0.11, 1));

    private final NametagFormatter formatter = new NametagFormatter(this);

    public enum TextFormat {
        None, Bold, Italic, Both
    }

    public NametagsModule() {
        super("nametags", "Draws nametags above certain entities.", Category.visuals);
    }

    @SubscribeEvent
    public void onRender3d(Render3DEvent event) {
        if (MC.world == null || MC.player == null) return;

        MatrixStack matrices = event.getMatrices();

        if (players.get()) {
            for (PlayerEntity player : ENTITY_MANAGER.getPlayers()) {
                if (player == MC.player) continue;
                renderEntityNametag(player, event.getTickDelta(), matrices, 30, null);
            }
        }

        if (self.get() && !MC.options.getPerspective().isFirstPerson()){
            renderEntityNametag(MC.player, event.getTickDelta(), matrices, 30, null);
        }

        if (hostiles.get()) {
            for (var entity : ENTITY_MANAGER.getHostile()) {
                if (entity.isInvisible()) continue;
                renderEntityNametag(entity, event.getTickDelta(), matrices, 30, null);
            }
        }

        if (neutrals.get()) {
            for (var entity : ENTITY_MANAGER.getNeutral()) {
                if (entity.isInvisible()) continue;
                renderEntityNametag(entity, event.getTickDelta(), matrices, 30, null);
            }
        }

        if (passives.get()) {
            for (var entity : ENTITY_MANAGER.getPassive()) {
                if (entity.isInvisible()) continue;
                renderEntityNametag(entity, event.getTickDelta(), matrices, 30, null);
            }
        }

        if (items.get()) {
            for (var entity : ENTITY_MANAGER.getDroppedItems()) {
                if (entity.isInvisible()) continue;
                renderEntityNametag(entity, event.getTickDelta(), matrices, 30, null);
            }
        }
    }

    private void renderEntityNametag(net.minecraft.entity.Entity entity, float tickDelta, MatrixStack matrices, float scale, Color forcedColor) {
        Vec3d pos = new Vec3d(
                MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX()),
                MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY())
                        + (entity.isSneaking() ? entity.getBoundingBox().getLengthY() + 0.0 : entity.getBoundingBox().getLengthY() + 0.3),
                MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ())
        );

        Vec3d camPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        float distance = (float) camPos.distanceTo(pos);

        float dynamicScale = 0.0018f + (scale / 10000.0f) * distance;
        if (distance <= 8.0f) dynamicScale = 0.0245f;


        Text displayName;

        if (entity instanceof PlayerEntity player) {
            displayName = formatter.formatPlayer(player);

            if (showHealth.get()) {
                displayName = Text.literal("")
                        .append(displayName)
                        .append(Text.literal(" "))
                        .append(formatter.getHealthText(player));
            }

            if (showPing.get()) {
                displayName = Text.literal("")
                        .append(displayName)
                        .append(Text.literal(" "))
                        .append(formatter.formatPing(player));
            }

            if (showGameMode.get()) {
                displayName = Text.literal("")
                        .append(displayName)
                        .append(Text.literal(" "))
                        .append(formatter.formatGameMode(player));
            }

            if (showEntityId.get()) {
                displayName = Text.literal("")
                        .append(displayName)
                        .append(Text.literal(" "))
                        .append(formatter.formatEntityId(entity));
            }
        } else if (entity.getClass().getSimpleName().equals("ItemEntity")) {
            displayName = formatter.formatItem((net.minecraft.entity.ItemEntity) entity);
        } else {
            displayName = formatter.formatEntity(entity);
        }

        Text colored = formatter.formatWithColor(displayName, forcedColor, entity);

        RenderUtil.drawText3D(matrices, colored, pos, dynamicScale, background.get(), border.get(), borderWidth.get().floatValue());

        if (showItems.get() && entity instanceof PlayerEntity player) {
            renderPlayerItems(player, matrices, tickDelta, scale);
        }
    }

    private void renderPlayerItems(PlayerEntity player, MatrixStack matrices, float tickDelta, float baseScale) {
        List<ItemStack> items = Arrays.asList(
                player.getMainHandStack(),
                player.getEquippedStack(EquipmentSlot.HEAD),
                player.getEquippedStack(EquipmentSlot.CHEST),
                player.getEquippedStack(EquipmentSlot.LEGS),
                player.getEquippedStack(EquipmentSlot.FEET),
                player.getOffHandStack()
        );

        List<ItemStack> nonEmptyItems = items.stream().filter(stack -> !stack.isEmpty()).toList();
        int itemCount = nonEmptyItems.size();
        if (itemCount == 0) return;

        double interpMinX = MathHelper.lerp(tickDelta, player.lastRenderX, player.getX()) - player.getWidth() / 2.0;
        double interpMinY = MathHelper.lerp(tickDelta, player.lastRenderY, player.getY());
        double interpMinZ = MathHelper.lerp(tickDelta, player.lastRenderZ, player.getZ()) - player.getWidth() / 2.0;

        double interpMaxX = interpMinX + player.getWidth();
        double interpMaxY = interpMinY + player.getHeight();
        double interpMaxZ = interpMinZ + player.getWidth();

        double baseX = (interpMinX + interpMaxX) / 2.0;
        double baseY = interpMaxY + (player.isSneaking() ? 0.0 : 0.3);
        double baseZ = (interpMinZ + interpMaxZ) / 2.0;

        Vec3d camPos = MC.getEntityRenderDispatcher().camera.getPos();
        float yaw = MC.gameRenderer.getCamera().getYaw();
        Vec3d camForward = Vec3d.fromPolar(0, yaw).normalize();
        Vec3d camRight = camForward.crossProduct(new Vec3d(0, 1, 0)).normalize();

        int renderIndex = 0;

        for (ItemStack stack : nonEmptyItems) {
            Vec3d itemPosBase = new Vec3d(baseX, baseY, baseZ);

            float distance = (float) camPos.distanceTo(itemPosBase);

            float dynamicScale = 0.0018f + (baseScale / 10000.0f) * distance;
            if (distance <= 8.0f) dynamicScale = 0.0245f;

            double itemSpacing = dynamicScale * 12.0;
            double verticalOffset = dynamicScale * 10.0;

            double offsetX = (renderIndex - (itemCount - 1) / 2.0) * itemSpacing;

            Vec3d itemPos = itemPosBase.add(camRight.multiply(offsetX)).add(0, verticalOffset, 0);

            RenderUtil.renderItem3D(stack, matrices, itemPos, Layers.getGlobalItem(), dynamicScale);

            renderIndex++;
        }
    }
}