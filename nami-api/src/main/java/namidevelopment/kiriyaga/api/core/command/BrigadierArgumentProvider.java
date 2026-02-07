package namidevelopment.kiriyaga.api.core.command;

import com.mojang.brigadier.arguments.ArgumentType;

public interface BrigadierArgumentProvider {
    ArgumentType<?> toArgumentType(boolean last);
}
