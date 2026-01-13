package me.kiriyaga.nami.core.config.model;

import me.kiriyaga.nami.core.config.ConfigMode;

import java.time.LocalDateTime;

public record ConfigMeta(
        String author,
        String clientVersion,
        LocalDateTime createdAt,
        ConfigMode mode
) {}
