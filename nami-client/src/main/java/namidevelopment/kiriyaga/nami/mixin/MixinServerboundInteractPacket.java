package namidevelopment.kiriyaga.nami.mixin;

import namidevelopment.kiriyaga.nami.mixininterface.IPlayerInteractEntityC2SPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static namidevelopment.kiriyaga.nami.Nami.MC;


@Mixin(ServerboundInteractPacket.class)
public abstract class MixinServerboundInteractPacket implements IPlayerInteractEntityC2SPacket {

    @Shadow @Final private int entityId;
    @Shadow @Final private ServerboundInteractPacket.Action action;

    @Override
    public ServerboundInteractPacket.ActionType getType() {
        return this.action.getType();
    }

    @Override
    public Entity getEntity() {
        if (MC.level == null) return null;
        return MC.level.getEntity(entityId);
    }
}
