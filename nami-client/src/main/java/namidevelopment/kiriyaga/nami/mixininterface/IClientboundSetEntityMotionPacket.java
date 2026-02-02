package namidevelopment.kiriyaga.nami.mixininterface;

import net.minecraft.world.phys.Vec3;

public interface IClientboundSetEntityMotionPacket {
    void setMovement(Vec3 vec);
}