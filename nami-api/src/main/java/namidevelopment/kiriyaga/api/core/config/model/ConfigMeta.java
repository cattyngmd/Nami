package namidevelopment.kiriyaga.api.core.config.model;

import namidevelopment.kiriyaga.api.core.config.ConfigMode;

import java.time.LocalDateTime;

public record ConfigMeta(
        String author,
        String clientVersion,
        LocalDateTime createdAt,
        ConfigMode mode
) {}
