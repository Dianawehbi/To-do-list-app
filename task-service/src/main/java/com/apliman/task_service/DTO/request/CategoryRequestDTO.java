package com.apliman.task_service.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CategoryRequestDTO {

    @NotBlank
    @Size(max = 60)
    private String name;

    @NotBlank
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "color must be a hex value like #A1B2C3")
    private String color;

    private Boolean active;

}