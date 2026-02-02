package namidevelopment.kiriyaga.api.api.config.model;

import namidevelopment.kiriyaga.api.api.config.ConfigMode;

import java.time.LocalDateTime;

public record ConfigMeta(
        String author,
        String clientVersion,
        LocalDateTime createdAt,
        ConfigMode mode
) {}
