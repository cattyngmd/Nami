package me.kiriyaga.nami.feature.module.impl.combat;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.core.rotation.model.RotationRequest;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import me.kiriyaga.nami.util.InteractionUtils;
import me.kiriyaga.nami.util.RotationUtils;
import me.kiriyaga.nami.util.entity.DamageUtils;
import me.kiriyaga.nami.util.entity.EntityUtils;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.EndCrystalItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.EntityHitResult;

import java.awt.*;
import java.util.function.Predicate;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.RotationUtils.*;

@RegisterModule
public class AutoCrystalModule extends Module {
    public enum Page {PLACE, BREAK, DAMAGES, RENDER}

    private final EnumSetting<Page> page = addSetting(new EnumSetting<>("Page", Page.PLACE));

    //place
    public final BoolSetting doPlace = addSetting(new BoolSetting("Place", true));
    public final DoubleSetting placeRange = addSetting(new DoubleSetting("Range", 4.0, 1.0, 7.0));
    public final DoubleSetting placeDelay = addSetting(new DoubleSetting("Delay", 0, 0, 20));
    public final BoolSetting placeRotate = addSetting(new BoolSetting("Rotate", true));
    public final BoolSetting placeSwing = addSetting(new BoolSetting("Swing", true));
    public final BoolSetting placeIgnoreItems = addSetting(new BoolSetting("IgnoreItems", true));
    public final BoolSetting placeOnlyCanBreak = addSetting(new BoolSetting("OnlyCanBreak", false));
    public final BoolSetting placeMultitask = addSetting(new BoolSetting("Multitask", false));

    //break
    public final BoolSetting doBreak = addSetting(new BoolSetting("Break", true));
    public final DoubleSetting breakRange = addSetting(new DoubleSetting("Range", 3.0, 1.0, 7.0));
    public final DoubleSetting breakDelay = addSetting(new DoubleSetting("Delay", 0, 0, 20));
    public final BoolSetting breakRotate = addSetting(new BoolSetting("Rotate", true));
    public final BoolSetting breakSwing = addSetting(new BoolSetting("Swing", true));
    public final BoolSetting breakMultitask = addSetting(new BoolSetting("Multitask", true));
    public final IntSetting breakAge = addSetting(new IntSetting("Age", 0, 0, 20));

    //damages
    public final BoolSetting assumeBestArmor = addSetting(new BoolSetting("AssumeBestArmor", true));
    public final BoolSetting noSelfPop = addSetting(new BoolSetting("NoSelfPop", true));
    public final DoubleSetting minDamage = addSetting(new DoubleSetting("MinDamage", 2.0, 0.0, 36.0));
    public final DoubleSetting maxSelfDamage = addSetting(new DoubleSetting("MaxSelfDamage", 4.0, 0.0, 36.0));
    public final DoubleSetting maxFriendDamage = addSetting(new DoubleSetting("MaxFriendDamage", 2.0, 0.0, 36.0));

    //render
    public final BoolSetting render = addSetting(new BoolSetting("Render", true));

    private int breakTimer, placeTimer = 0; // i love it
    private PlaceTarget placeTarget = null;

    public AutoCrystalModule() {
        super("AutoCrystal", "Automatically places and break crystals to kill people, if you are good enough!.", ModuleCategory.of("Combat"), "autocrystal", "ac", "crystalaura");
        doBreak.setShowCondition(() -> page.get() == Page.BREAK);
        breakRange.setShowCondition(() -> doBreak.get() && page.get() == Page.BREAK);
        breakDelay.setShowCondition(() -> doBreak.get() && page.get() == Page.BREAK);
        breakRotate.setShowCondition(() -> doBreak.get() && page.get() == Page.BREAK);
        breakSwing.setShowCondition(() -> doBreak.get() && page.get() == Page.BREAK);
        breakMultitask.setShowCondition(() -> doBreak.get() && page.get() == Page.BREAK);
        breakAge.setShowCondition(() -> doBreak.get() && page.get() == Page.BREAK);

        doPlace.setShowCondition(() -> page.get() == Page.PLACE);
        placeRange.setShowCondition(() -> doPlace.get() && page.get() == Page.PLACE);
        placeDelay.setShowCondition(() -> doPlace.get() && page.get() == Page.PLACE);
        placeRotate.setShowCondition(() -> doPlace.get() && page.get() == Page.PLACE);
        placeSwing.setShowCondition(() -> doPlace.get() && page.get() == Page.PLACE);
        placeIgnoreItems.setShowCondition(() -> doPlace.get() && page.get() == Page.PLACE);
        placeMultitask.setShowCondition(() -> doPlace.get() && page.get() == Page.PLACE);
        placeOnlyCanBreak.setShowCondition(() -> doPlace.get() && page.get() == Page.PLACE);

        noSelfPop.setShowCondition(() ->  page.get() == Page.DAMAGES);
        minDamage.setShowCondition(() -> page.get() == Page.DAMAGES);
        maxSelfDamage.setShowCondition(() -> page.get() == Page.DAMAGES);
        maxFriendDamage.setShowCondition(() -> page.get() == Page.DAMAGES);
        assumeBestArmor.setShowCondition(() -> page.get() == Page.DAMAGES);

        render.setShowCondition(() -> page.get() == Page.RENDER);
    }

    @Override
    public void onDisable() {
        breakTimer = 0;
        placeTimer = 0;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPreTickEvent(PreTickEvent event) {
        if (MC.player == null) return;

        if (doPlace.get()) {
            if (placeTimer > 0) {
                placeTimer--;
                return;
            }
            doPlace();
        }

        if (doBreak.get()) {
            if (breakTimer > 0) {
                breakTimer--;
                return;
            }
            doBreak();
        }
    }
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRender(Render3DEvent event) {
        if (MC.level == null || MC.player == null || placeTarget == null || !render.get()) return;

        BlockPos pos = placeTarget.pos;
        AABB box = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        Color color = MODULE_MANAGER.getStorage().getByClass(ColorModule.class).getStyledGlobalColor();

        RenderUtil.drawBoxLines(box, color, true, true, 1.5f);
    }

    private void doBreak() {
        BreakTarget target = bestCrystal();
        if (target == null) return;

        if (!breakMultitask.get() && MC.player.isUsingItem()) return;

        if (breakRotate.get()) {
            Vec3 pos = getClosestPointToEye(MC.player.getEyePosition(), target.crystal.getBoundingBox());
            float yaw = (float) getYawToVec(MC.player, pos);
            float pitch = (float) getPitchToVec(MC.player, pos);

            ROTATION_MANAGER.getRequestHandler().submit(new RotationRequest(AutoCrystalModule.class.getName(), 9, yaw, pitch));
        }

        if (!canBreak(target.crystal)) return;

        MC.gameMode.attack(MC.player, target.crystal);

        if (breakSwing.get())
            MC.player.swing(InteractionHand.MAIN_HAND);

        breakTimer = breakDelay.get().intValue();
    }

    private BreakTarget bestCrystal() {
        BreakTarget best = null;

        for (Entity e : EntityUtils.getEntities(EntityUtils.EntityTypeCategory.END_CRYSTALS, 10)) {
            if (!(e instanceof EndCrystal crystal)) continue;

            if (e.tickCount < breakAge.get()) continue;

            //   if (MC.player.distanceToSqr(crystal) > 10 * 10) continue;

            Vec3 pos = getClosestPointToEye(MC.player.getEyePosition(), crystal.getBoundingBox());
            float yaw = (float) getYawToVec(MC.player, pos);
            float pitch = (float) getPitchToVec(MC.player, pos);
            EntityHitResult perfect = raycastTarget(MC.player, crystal, breakRange.get(), yaw, pitch);
            boolean insideBox = crystal.getBoundingBox().contains(MC.player.getEyePosition());

            if (!insideBox && perfect == null) continue;



            float totalDamage = damageOthers(crystal);
            if (totalDamage <= -0.9f)
                continue;

            if (best == null || totalDamage > best.totalDamage)
                best = new BreakTarget(crystal, totalDamage);
        }
        return best;
    }

    private float damageOthers(EndCrystal crystal) {
        Vec3 pos = crystal.position();
        float totalDamage = 0f;

        float selfDamage = DamageUtils.crystalDamage(MC.player, MC.player.position(), MC.player.getBoundingBox(), pos, DamageUtils.BLOCK_CHECK, assumeBestArmor.get());

        if (selfDamage > maxSelfDamage.get() || (noSelfPop.get() && selfDamage > (MC.player.getHealth() + MC.player.getAbsorptionAmount())))
            return -1f;


        for (Entity e : EntityUtils.getEntities(EntityUtils.EntityTypeCategory.PLAYERS, 15)) {
            if (!(e instanceof LivingEntity living)) continue;

            float dmg = DamageUtils.crystalDamage(living, living.position(), living.getBoundingBox(), pos, DamageUtils.BLOCK_CHECK, assumeBestArmor.get());

            if (e == MC.player)
                continue;

            if (FRIEND_MANAGER.isFriend(e.getName().getString())) {
                if (dmg > maxFriendDamage.get())
                    return -1f;
            } else {
                totalDamage += dmg;
            }
        }

        this.setDisplayInfo(String.format("%.2f", totalDamage));
        return totalDamage;
    }

    private boolean canBreak(EndCrystal crystal) {
        Vec3 eyePos = MC.player.getEyePosition(1.0f);
        Vec3 hitVec = getClosestPointToEye(eyePos, crystal.getBoundingBox());

        float idealYaw = (float) getYawToVec(MC.player, hitVec);
        float idealPitch = (float) getPitchToVec(MC.player, hitVec);

        EntityHitResult distanceCheck = raycastTarget(MC.player, crystal, breakRange.get(), idealYaw, idealPitch);

        boolean insideBox = crystal.getBoundingBox().contains(eyePos);
        if (!insideBox && distanceCheck == null)
            return false;

        if (!breakRotate.get())
            return true;

      //  ROTATION_MANAGER.getRequestHandler().submit(new RotationRequest(AutoCrystalModule.class.getName(), 5, idealYaw, idealPitch));

        EntityHitResult serverCheck = raycastTarget(MC.player, crystal, breakRange.get(), ROTATION_MANAGER.getStateHandler().getServerYaw(), ROTATION_MANAGER.getStateHandler().getServerPitch());

        return serverCheck != null || insideBox;
    }

    private void doPlace() {
        if (!placeMultitask.get() && MC.player.isUsingItem()) return;

        placeTarget = findBestPlace();
        if (placeTarget == null) return;
        int crystalSlot = findHotbarItem(stack -> stack.getItem() instanceof EndCrystalItem);
        if (crystalSlot == -1) return;
        InteractionUtils.interactBlockAt(placeTarget.pos, crystalSlot, placeRange.get(), placeRotate.get(), false, false, placeSwing.get(), AutoCrystalModule.class.getName() + "_PLACE");

        placeTimer = placeDelay.get().intValue();
    }

    private PlaceTarget findBestPlace() {
        PlaceTarget best = null;
        BlockPos playerPos = MC.player.blockPosition();
        int r = (int) Math.ceil(placeRange.get());

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    BlockPos basePos = pos.below();

                    BlockState baseState = MC.level.getBlockState(basePos);
                    if (!baseState.is(Blocks.OBSIDIAN) && !baseState.is(Blocks.BEDROCK)) continue;
                    if (!MC.level.getBlockState(pos).isAir()) continue;
                    if (!MC.level.getBlockState(pos.above()).isAir()) continue;

                    if (!canPlaceAt(pos)) continue;

                    Vec3 crystalVec = Vec3.atCenterOf(pos).add(0, 1, 0);
                    float totalDamage = calculatePlaceDamage(crystalVec);
                    if (totalDamage <= -0.9f) continue;

                    if (best == null || totalDamage > best.totalDamage)
                        best = new PlaceTarget(pos, totalDamage);
                }
            }
        }

        if (best != null) {
            PlaceTarget ret = new PlaceTarget(best.pos.below(), best.totalDamage);
            return ret;
        }
        return best; // always null here
    }

    private boolean canPlaceAt(BlockPos pos) {
        BlockPos base = pos.below();

        BlockState baseState = MC.level.getBlockState(base);
        if (!baseState.is(Blocks.OBSIDIAN) && !baseState.is(Blocks.BEDROCK)) return false;

        if (!MC.level.getBlockState(pos).isAir()) return false;
        if (!MC.level.getBlockState(pos.above()).isAir()) return false;

        Vec3 eyePos = MC.player.getEyePosition();

        AABB blockBox = new AABB(pos);
        AABB placeBox = new AABB(pos.above());
        Vec3 point = RotationUtils.getClosestPointToEye(eyePos, blockBox);
        float yaw = (float) getYawToVec(MC.player, point);
        float pitch = (float) getPitchToVec(MC.player, point);

        if (RotationUtils.raycastAABBFromPlayer(MC.player, blockBox, placeRange.get(), yaw, pitch) == null) {
            return false;
        }

        double centerX = pos.getX() + 0.5;
        double centerZ = pos.getZ() + 0.5;
        double bottomY = pos.getY() + 1;

        double minX = centerX - 1.00;
        double minY = bottomY;
        double minZ = centerZ - 1.00;
        double maxX = centerX + 1.00;
        double maxY = bottomY + 2.00;
        double maxZ = centerZ + 1.00;

        AABB crystalBox = new AABB(minX, minY, minZ, maxX, maxY, maxZ);

        for (Entity e : MC.level.getEntities(null, placeBox)) { // todo: check if we can place into crystals
            if (placeIgnoreItems.get() && e instanceof ItemEntity) continue;
            return false;
        }

        if (placeOnlyCanBreak.get()) { // todo: this shit is incorrect for some reason, prob because of hitbox size

            Vec3 hitVec = getClosestPointToEye(eyePos, crystalBox);
            float idealYaw = (float) getYawToVec(MC.player, hitVec);
            float idealPitch = (float) getPitchToVec(MC.player, hitVec);

            EntityHitResult distanceCheck = raycastAABBFromPlayer(MC.player, crystalBox, breakRange.get(), idealYaw, idealPitch);

            boolean insideBox = crystalBox.contains(eyePos);

            if (!insideBox && distanceCheck == null)
                return false;
        }

        return true;
    }


    private float calculatePlaceDamage(Vec3 crystalPos) {
        float totalDamage = 0f;

        float self = DamageUtils.crystalDamage(MC.player, MC.player.position(), MC.player.getBoundingBox(), crystalPos, DamageUtils.BLOCK_CHECK, assumeBestArmor.get());

        if (self > maxSelfDamage.get() || (noSelfPop.get() && self > MC.player.getHealth() + MC.player.getAbsorptionAmount()))
            return -1f;

        for (Entity e : EntityUtils.getEntities(EntityUtils.EntityTypeCategory.PLAYERS, 15)) {
            if (!(e instanceof LivingEntity living)) continue;
            if (e == MC.player) continue;

            float dmg = DamageUtils.crystalDamage(living, living.position(), living.getBoundingBox(), crystalPos, DamageUtils.BLOCK_CHECK, assumeBestArmor.get());

            if (FRIEND_MANAGER.isFriend(e.getName().getString())) {
                if (dmg > maxFriendDamage.get())
                    return -1f;
                else
                    continue;
            }

            totalDamage += dmg;
        }

        if (totalDamage < minDamage.get())
            return -1f;

        return totalDamage;
    }

    private int findHotbarItem(Predicate<ItemStack> predicate) {
        if (MC.player == null)
            return -1;

/*        if (predicate.test(MC.player.getInventory().getSelectedItem()))
            return MC.player.getInventory().getSelectedSlot();*/

        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = MC.player.getInventory().getItem(slot);
            if (stack.isEmpty())
                continue;

            if (predicate.test(stack))
                return slot;
        }

        return -1;
    }

    private record PlaceTarget(BlockPos pos, float totalDamage) {}
    private record BreakTarget(EndCrystal crystal, float totalDamage) {}
}
