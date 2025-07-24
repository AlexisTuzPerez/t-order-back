package com.torder.orden.dto;

import com.torder.orden.Status;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CambiarStatusRequest {
    
    @NotNull(message = "El nuevo estado es requerido")
    private Status status;
} 