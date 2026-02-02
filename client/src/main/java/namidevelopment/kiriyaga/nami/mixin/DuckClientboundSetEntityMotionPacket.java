package namidevelopment.kiriyaga.nami.mixin;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientboundSetEntityMotionPacket.class)
public interface DuckClientboundSetEntityMotionPacket {

    @Accessor("movement")
    Vec3 getMovement();

    @Invoker("<init>")
    static ClientboundSetEntityMotionPacket create(int entityId, Vec3 velocity) {
        throw new UnsupportedOperationException();
    }
}
