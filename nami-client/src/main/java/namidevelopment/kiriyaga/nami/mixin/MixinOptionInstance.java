package namidevelopment.kiriyaga.nami.mixin;

import namidevelopment.kiriyaga.nami.mixininterface.ISimpleOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;
import java.util.function.Consumer;

@Mixin(OptionInstance.class)
public abstract class MixinOptionInstance implements ISimpleOption {
    @Shadow Object value;
    @Shadow @Final private Consumer<Object> onValueUpdate;

    @Override
    public void setValue(Object value) {
        if (!Minecraft.getInstance().isRunning()) {
            this.value = value;
        } else {
            if (!Objects.equals(this.value, value)) {
                this.value = value;
                this.onValueUpdate.accept(this.value);
            }
        }
    }
}