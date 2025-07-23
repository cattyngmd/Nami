package me.kiriyaga.nami.util;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import static me.kiriyaga.nami.Nami.MC;
import static net.minecraft.util.Hand.MAIN_HAND;

public class InteractionUtils {

    public static boolean interactWithEntity(Entity entity, Vec3d hitVec, boolean swing) {
        if (MC.player == null || MC.interactionManager == null) return false;

        EntityHitResult hitResult = new EntityHitResult(entity, hitVec);
        ClientPlayerInteractionManager im = MC.interactionManager;

        im.interactEntityAtLocation(MC.player, entity, hitResult, MAIN_HAND);
        im.interactEntity(MC.player, entity, MAIN_HAND);

        if (swing)
            MC.player.swingHand(MAIN_HAND);

        return true;
    }
}
