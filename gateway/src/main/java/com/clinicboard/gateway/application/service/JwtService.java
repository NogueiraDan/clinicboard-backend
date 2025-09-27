package com.clinicboard.gateway.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * Serviço responsável pela validação e extração de dados dos tokens JWT.
 * 
 * Este serviço é o coração da autenticação no Gateway, validando tokens
 * emitidos pelo User Service e extraindo informações necessárias para
 * enriquecer requisições downstream.
 * 
 * Características:
 * - Validação criptográfica com HMAC256
 * - Verificação de issuer para segurança
 * - Extração de claims customizados
 * - Programação reativa com Mono<>
 */
@Service
public class JwtService {

    // Chave secreta compartilhada com o User Service (deve ser a mesma!)
    private final String secret;

    /**
     * Construtor que injeta a chave secreta do JWT.
     * 
     * IMPORTANTE: Esta chave DEVE ser idêntica à usada no User Service
     * para que a validação funcione corretamente.
     * 
     * @param secret Chave secreta configurada em application.properties
     */
    public JwtService(@Value("${api.security.token.secret}") String secret) {
        this.secret = secret;
    }

    /**
     * Valida um token JWT e retorna apenas o subject (ID do usuário).
     * 
     * Método simplificado usado para validações básicas onde só
     * precisamos saber se o token é válido e o ID do usuário.
     * 
     * Processo de validação:
     * 1. Configura algoritmo HMAC256 com chave secreta
     * 2. Verifica assinatura criptográfica do token
     * 3. Valida issuer (deve ser "clinicboard-backend")
     * 4. Verifica se token não expirou
     * 5. Retorna subject (user ID) se tudo válido
     * 
     * @param token JWT completo (sem "Bearer " prefix)
     * @return Mono<String> - ID do usuário ou erro se inválido
     */
    public Mono<String> validateToken(String token) {
        try {
            // Configura algoritmo de validação (HMAC256 + chave secreta)
            Algorithm algorithm = Algorithm.HMAC256(secret);

            // Cria validador com regras específicas
            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .withIssuer("clinicboard-backend") // Deve ser emitido pelo nosso sistema
                    .build()
                    .verify(token); // Executa a validação completa

            // Se chegou aqui, token é válido - retorna o subject (user ID)
            return Mono.just(decodedJWT.getSubject());

        } catch (JWTVerificationException exception) {
            // Token inválido, expirado, assinatura incorreta, etc.
            return Mono.error(new RuntimeException("Invalid JWT token: " + exception.getMessage()));
        }
    }

    /**
     * Valida JWT e extrai TODOS os claims necessários para o Gateway.
     * 
     * Este é o método principal usado pelo AuthenticationGatewayFilter
     * para obter informações completas do usuário sem consultar o User Service.
     * 
     * Claims extraídos:
     * - subject: ID único do usuário (UUID)
     * - email: Email do usuário (para logs e headers)
     * - role: Função do usuário (ADMIN, PROFESSIONAL, USER)
     * - name: Nome completo do usuário
     * - contact: Telefone/contato do usuário
     * 
     * Processo:
     * 1. Valida token (assinatura, issuer, expiração)
     * 2. Extrai todos os claims customizados
     * 3. Encapsula em objeto JwtClaims
     * 4. Retorna de forma reativa
     * 
     * @param token JWT completo para validação e extração
     * @return Mono<JwtClaims> - Objeto com todos os dados do usuário ou erro
     */
    public Mono<JwtClaims> validateTokenAndExtractClaims(String token) {
        try {
            // Configura algoritmo de validação (mesmo do método anterior)
            Algorithm algorithm = Algorithm.HMAC256(secret);

            // Executa validação completa do token
            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .withIssuer("clinicboard-backend") // Segurança: só aceita nossos tokens
                    .build()
                    .verify(token); // Valida assinatura + expiração + issuer

            // Extrai claims do JWT validado
            // IMPORTANTE: Estes claims devem estar presentes no JWT gerado pelo User
            // Service
            String userId = decodedJWT.getSubject(); // Subject = ID do usuário
            String email = decodedJWT.getClaim("email").asString(); // Email customizado
            String role = decodedJWT.getClaim("role").asString(); // Role customizado
            String name = decodedJWT.getClaim("name").asString(); // Nome customizado
            String contact = decodedJWT.getClaim("contact").asString(); // Contato customizado

            // Encapsula todos os dados em objeto imutável
            JwtClaims claims = new JwtClaims(userId, email, role, name, contact);

            // Retorna de forma reativa para integração com Spring WebFlux
            return Mono.just(claims);

        } catch (JWTVerificationException exception) {
            // Qualquer problema na validação (token inválido, expirado, claims ausentes,
            // etc.)
            return Mono.error(new RuntimeException("Invalid JWT token: " + exception.getMessage()));
        }
    }

    /**
     * Classe imutável que encapsula todos os claims extraídos do JWT.
     * 
     * Esta classe serve como um Data Transfer Object (DTO) que carrega
     * informações do usuário entre o Gateway e os serviços downstream.
     * 
     * Características:
     * - Imutável (campos final, sem setters)
     * - Encapsula dados do usuário
     * - Facilita transporte de informações
     * - Type-safe (evita erros de tipo)
     * 
     * Uso típico:
     * JwtClaims claims = jwtService.validateTokenAndExtractClaims(token);
     * String userId = claims.getUserId();
     * String role = claims.getRole();
     */
    public static class JwtClaims {
        // Todos os campos são final para garantir imutabilidade
        private final String userId; // ID único do usuário (UUID)
        private final String email; // Email para identificação
        private final String role; // Papel/função no sistema
        private final String name; // Nome completo do usuário
        private final String contact; // Telefone/contato

        /**
         * Construtor que inicializa todos os campos do claims.
         * 
         * @param userId  ID único do usuário (subject do JWT)
         * @param email   Email do usuário
         * @param role    Função do usuário (ADMIN, PROFESSIONAL, USER)
         * @param name    Nome completo do usuário
         * @param contact Telefone/contato do usuário
         */
        public JwtClaims(String userId, String email, String role, String name, String contact) {
            this.userId = userId;
            this.email = email;
            this.role = role;
            this.name = name;
            this.contact = contact;
        }

        // Getters para acessar os dados extraídos do JWT

        /**
         * @return ID único do usuário (UUID) - usado como identificador principal
         */
        public String getUserId() {
            return userId;
        }

        /**
         * @return Email do usuário - usado para logs e identificação
         */
        public String getEmail() {
            return email;
        }

        /**
         * @return Role/função do usuário - usado para autorização downstream
         */
        public String getRole() {
            return role;
        }

        /**
         * @return Nome completo do usuário - usado em headers e logs
         */
        public String getName() {
            return name;
        }

        /**
         * @return Contato do usuário - informação adicional se necessária
         */
        public String getContact() {
            return contact;
        }

        /**
         * Representação em string para debugging e logs.
         * 
         * @return String formatada com informações principais do usuário
         */
        @Override
        public String toString() {
            return String.format("JwtClaims{userId='%s', email='%s', role='%s'}",
                    userId, email, role);
        }
    }
}