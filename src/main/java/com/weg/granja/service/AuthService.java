package com.weg.granja.service;

import com.weg.granja.dto.AuthDTO;
import com.weg.granja.exception.NegocioException;
import com.weg.granja.model.Usuario;
import com.weg.granja.repository.UsuarioRepository;
import com.weg.granja.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthDTO.LoginResponse login(AuthDTO.LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.senha()));
        } catch (BadCredentialsException e) {
            throw new NegocioException("Email ou senha invalidos", HttpStatus.UNAUTHORIZED);
        }

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new NegocioException("Email ou senha invalidos", HttpStatus.UNAUTHORIZED));

        String token = jwtService.gerarToken(usuario);
        return new AuthDTO.LoginResponse(token, usuario.getNome(), usuario.getEmail(), usuario.getPapel());
    }

    public AuthDTO.LoginResponse registrar(AuthDTO.RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new NegocioException("Ja existe um usuario com o email " + request.email(), HttpStatus.CONFLICT);
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setPapel(request.papel());

        usuario = usuarioRepository.save(usuario);

        String token = jwtService.gerarToken(usuario);
        return new AuthDTO.LoginResponse(token, usuario.getNome(), usuario.getEmail(), usuario.getPapel());
    }
}