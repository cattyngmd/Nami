package me.kiriyaga.nami.feature.module.impl.client.cape;

import net.minecraft.util.Identifier;

public enum CapeType {
    LOST_CONNECTION(Identifier.of("nami", "textures/cape/nami1.png"));

    private final Identifier texture;

    CapeType(Identifier texture) {
        this.texture = texture;
    }

    public Identifier getTexture() {
        return texture;
    }
}