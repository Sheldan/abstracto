package dev.sheldan.abstracto.core.models.dto;


import dev.sheldan.abstracto.core.models.SnowFlake;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDto implements SnowFlake {
    private Long id;
}
