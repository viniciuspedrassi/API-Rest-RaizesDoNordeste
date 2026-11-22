package com.raizesDoNordeste.raizesDoNordeste.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable());

        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.authorizeHttpRequests(this::configurarRotas);

        http.exceptionHandling(this::configurarErros);

        http.oauth2ResourceServer(this::configurarJwt);

        return http.build();
    }

    private void configurarErros(ExceptionHandlingConfigurer<HttpSecurity> excecoes) {

        excecoes.authenticationEntryPoint(this::tratarNaoAutenticado);

        excecoes.accessDeniedHandler(this::tratarAcessoNegado);
    }

    // TRATAMENTO - Retorna HTTP 401 quando a requisição não possui autenticação válida.
    private void tratarNaoAutenticado(
            HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.core.AuthenticationException excecao)
            throws java.io.IOException {

        response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Autenticação necessária para acessar este recurso."
        );
    }

    // TRATAMENTO - Retorna HTTP 403 quando o usuário não possui permissão.
    private void tratarAcessoNegado(
            HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.access.AccessDeniedException excecao)
            throws java.io.IOException {

        response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Usuário sem permissão para acessar este recurso."
        );
    }
    private void configurarRotas(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry rotas) {

// REGRA DE NEGÓCIO - Permite que o Spring Boot apresente respostas de erro.
        rotas.requestMatchers("/error").permitAll();

// REGRA DE NEGÓCIO - Permite o login sem autenticação.
        rotas.requestMatchers("/auth/login").permitAll();

// REGRA DE NEGÓCIO - Permite consultar a documentação da API sem autenticação.
        rotas.requestMatchers(
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs",
                "/v3/api-docs/**"
        ).permitAll();

// REGRA DE NEGÓCIO - Todos os funcionários autenticados podem consultar produtos.
        rotas.requestMatchers(HttpMethod.GET, "/produtos", "/produtos/**")
                .authenticated();

// REGRA DE NEGÓCIO - Somente administradores e gerentes podem cadastrar produtos.
        rotas.requestMatchers(HttpMethod.POST, "/produtos")
                .hasAnyRole("ADMINISTRADOR", "GERENTE");

// REGRA DE NEGÓCIO - Somente administradores e gerentes podem alterar produtos.
        rotas.requestMatchers(HttpMethod.PUT, "/produtos/**")
                .hasAnyRole("ADMINISTRADOR", "GERENTE");

// REGRA DE NEGÓCIO - Consultas ao estoque exigem perfil autorizado.
        rotas.requestMatchers(HttpMethod.GET, "/estoques")
                .hasAnyRole("ADMINISTRADOR", "GERENTE", "ATENDENTE");

// REGRA DE NEGÓCIO - Movimentações manuais exigem autorização gerencial.
        rotas.requestMatchers(
                        HttpMethod.POST,
                        "/estoques/entradas",
                        "/estoques/saidas"
                )
                .hasAnyRole("ADMINISTRADOR", "GERENTE");

// REGRA DE NEGÓCIO - Funcionários autorizados podem registrar pedidos.
        rotas.requestMatchers(HttpMethod.POST, "/pedidos")
                .hasAnyRole("ADMINISTRADOR", "GERENTE", "ATENDENTE");

// REGRA DE NEGÓCIO - Funcionários autorizados podem simular pagamentos.
        rotas.requestMatchers(HttpMethod.POST, "/pagamentos/simular")
                .hasAnyRole("ADMINISTRADOR", "GERENTE", "ATENDENTE");

// REGRA DE NEGÓCIO - Funcionários autenticados podem consultar unidades.
        rotas.requestMatchers(HttpMethod.GET, "/unidades", "/unidades/**")
                .authenticated();

// REGRA DE NEGÓCIO - Somente administradores podem cadastrar ou alterar unidades.
        rotas.requestMatchers(HttpMethod.POST, "/unidades")
                .hasRole("ADMINISTRADOR");

        rotas.requestMatchers(HttpMethod.PUT, "/unidades/**")
                .hasRole("ADMINISTRADOR");

// REGRA DE NEGÓCIO - Somente administradores podem cadastrar funcionários.
        rotas.requestMatchers(HttpMethod.POST, "/usuarios")
                .hasRole("ADMINISTRADOR");

// REGRA DE NEGÓCIO - Permite consultar pedidos aos perfis autorizados.
        rotas.requestMatchers(HttpMethod.GET, "/pedidos")
                .hasAnyRole("ADMINISTRADOR", "GERENTE", "ATENDENTE", "COZINHA");

// REGRA DE NEGÓCIO - Permite atualizar o status operacional dos pedidos somente aos perfis de cozinha e gestão.
        rotas.requestMatchers(HttpMethod.PATCH, "/pedidos/*/status")
                .hasAnyRole("COZINHA", "GERENTE", "ADMINISTRADOR");

// REGRA DE NEGÓCIO - Permite o cadastro de clientes sem autenticação.
        rotas.requestMatchers(HttpMethod.POST, "/clientes")
                .permitAll();

// REGRA DE NEGÓCIO - Restringe o registro do consentimento a funcionários autorizados.
        rotas.requestMatchers(HttpMethod.PATCH, "/clientes/*/consentimento")
                .hasAnyRole("ADMINISTRADOR", "GERENTE", "ATENDENTE");


// REGRA DE NEGÓCIO - Bloqueia operações não autorizadas explicitamente.
        rotas.anyRequest().denyAll();
    }

    private void configurarJwt(OAuth2ResourceServerConfigurer<HttpSecurity> oauth2) {

        oauth2.authenticationEntryPoint(this::tratarNaoAutenticado);

        oauth2.accessDeniedHandler(this::tratarAcessoNegado);

        oauth2.jwt(jwt ->
                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
        );
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter conversor =
                new JwtGrantedAuthoritiesConverter();

// REGRA DE NEGÓCIO - Utiliza o perfil presente no JWT para definir permissões.
        conversor.setAuthoritiesClaimName("perfil");
        conversor.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter autenticacao =
                new JwtAuthenticationConverter();

        autenticacao.setJwtGrantedAuthoritiesConverter(conversor);

        return autenticacao;
    }

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${app.jwt.secret}") String segredoBase64) {

        byte[] bytes = Base64.getDecoder().decode(segredoBase64);

        // VALIDAÇÃO - Exige uma chave de pelo menos 256 bits.
        if (bytes.length < 32) {

            // TRATAMENTO - Impede iniciar a aplicação com chave insuficiente.
            throw new IllegalArgumentException(
                    "A chave JWT deve possuir pelo menos 32 bytes."
            );
        }

        SecretKey chave = new SecretKeySpec(bytes, "HmacSHA256");

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(chave)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

// REGRA DE NEGÓCIO - Aceita somente tokens com o emissor esperado.
        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer("raizes-do-nordeste")
        );

        return decoder;
    }

    @Bean
    public JwtEncoder jwtEncoder(
            @Value("${app.jwt.secret}") String segredoBase64) {

        byte[] bytes = Base64.getDecoder().decode(segredoBase64);

        // VALIDAÇÃO - Exige uma chave com pelo menos 256 bits.
        if (bytes.length < 32) {

            // TRATAMENTO - Rejeita uma chave JWT insuficiente.
            throw new IllegalArgumentException(
                    "A chave JWT deve possuir pelo menos 32 bytes."
            );
        }

        SecretKey chave = new SecretKeySpec(bytes, "HmacSHA256");

        return NimbusJwtEncoder.withSecretKey(chave).build();
    }
}
