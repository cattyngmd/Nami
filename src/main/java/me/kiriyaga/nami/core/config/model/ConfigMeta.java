package me.kiriyaga.nami.core.config.model;

import java.time.LocalDateTime;

public record ConfigMeta(
        String author,
        String clientVersion,
        LocalDateTime createdAt
) {}
