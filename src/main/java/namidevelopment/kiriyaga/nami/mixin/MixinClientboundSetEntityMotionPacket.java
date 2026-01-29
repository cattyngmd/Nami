package namidevelopment.kiriyaga.nami.mixin;

import namidevelopment.kiriyaga.nami.mixininterface.IClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientboundSetEntityMotionPacket.class)
public class MixinClientboundSetEntityMotionPacket implements IClientboundSetEntityMotionPacket {

    @Shadow @Final @Mutable
    private Vec3 movement;

    @Override
    public void setMovement(Vec3 vec) {
        this.movement = vec;
    }
}
