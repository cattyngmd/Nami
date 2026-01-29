package me.kiriyaga.nami.api.config.model;

import me.kiriyaga.nami.api.config.ConfigMode;

import java.time.LocalDateTime;

public record ConfigMeta(
        String author,
        String clientVersion,
        LocalDateTime createdAt,
        ConfigMode mode
) {}
