package com.weg.granja.dto;

import com.weg.granja.model.Papel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AuthDTO {

    public record LoginRequest(
            @NotBlank(message = "Email e obrigatorio")
            @Email(message = "Email invalido")
            String email,

            @NotBlank(message = "Senha e obrigatoria")
            String senha
    ) {}

    public record LoginResponse(
            String token,
            String nome,
            String email,
            Papel papel
    ) {}

    public record RegistroRequest(
            @NotBlank(message = "Nome e obrigatorio")
            String nome,

            @NotBlank(message = "Email e obrigatorio")
            @Email(message = "Email invalido")
            String email,

            @NotBlank(message = "Senha e obrigatoria")
            @Size(min = 6, message = "A senha deve ter no minimo 6 caracteres")
            String senha,

            @NotNull(message = "Papel e obrigatorio")
            Papel papel
    ) {}
}