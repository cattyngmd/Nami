package namidevelopment.kiriyaga.api.mixin;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.GuiMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(ChatComponent.class)
public interface DuckChatComponent {

    @Accessor("allMessages")
    List<GuiMessage> getAllMessages();

    @Accessor("trimmedMessages")
    List<GuiMessage.Line> getTrimmedMessages();

    @Invoker("refreshTrimmedMessages")
    void callRefreshTrimmedMessages();
}
