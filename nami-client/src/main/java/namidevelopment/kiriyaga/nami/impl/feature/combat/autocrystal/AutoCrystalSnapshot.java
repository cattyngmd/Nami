package namidevelopment.kiriyaga.nami.impl.feature.combat.autocrystal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record AutoCrystalSnapshot(long tickId, int selfId, Vec3 eyePos, BlockPos playerBlockPos, double placeRange, double breakRange, double minDamage, boolean assumeBestArmor, TargetData[] targets, BlockPos[] candidatePos) {
    public record TargetData(
            int id,
            Vec3 pos,
            AABB box,
            float armor,
            float toughness,
            int resistanceAmp, //-1 if none is present
            byte armorMask, // 1 helmet, 2 chestplate , 4 leggins ,8 boots
            int prot,
            int blastProt,
            float health,
            float absorption
    ) {

    }
}
