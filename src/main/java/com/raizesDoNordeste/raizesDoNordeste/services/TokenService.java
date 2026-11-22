package com.raizesDoNordeste.raizesDoNordeste.services;

import com.raizesDoNordeste.raizesDoNordeste.entities.Usuario;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;

    public TokenService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String gerarToken(Usuario usuario) {

        Instant agora = Instant.now();

// REGRA DE NEGÓCIO - O token identifica o funcionário e expira após uma hora.
        JwtClaimsSet dados = JwtClaimsSet.builder()
                .issuer("raizes-do-nordeste")
                .subject(usuario.getId().toString())
                .issuedAt(agora)
                .expiresAt(agora.plus(1, ChronoUnit.HOURS))
                .claim("perfil", usuario.getPerfil().name())
                .build();

        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(cabecalho, dados)
        ).getTokenValue();
    }
}