package me.kiriyaga.essentials.mixininterface;

import net.minecraft.client.MinecraftClient;

public interface IMinecraft {
    MinecraftClient mc = MinecraftClient.getInstance();
}