package com.clinicboard.gateway.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import com.clinicboard.gateway.model.UserInfo;

import java.time.Duration;

/**
 * Serviço responsável pelo cache de informações de usuários no Redis.
 * 
 * Este serviço implementa uma estratégia de cache para otimizar a performance
 * do Gateway, evitando revalidação desnecessária de JWTs para o mesmo usuário.
 * 
 * Arquitetura:
 * - Cache Key: "user_info:{hash_do_token}"
 * - TTL: Configurável via application.properties
 * - Invalidação automática: Por expiração ou manual
 */
@Service
public class RedisService {

    // Template reativo do Redis para operações assíncronas
    private final ReactiveRedisTemplate<String, UserInfo> redisTemplate;

    // Time To Live (TTL) em segundos - tempo que os dados ficam no cache
    private final long userInfoTtl;

    /**
     * Construtor que injeta dependências do Redis e configuração de TTL.
     * 
     * @param redisTemplate Template reativo para operações Redis
     * @param userInfoTtl   TTL em segundos (padrão: 3600s = 1 hora)
     */
    public RedisService(
            ReactiveRedisTemplate<String, UserInfo> redisTemplate,
            @Value("${app.cache.user-info.ttl:3600}") long userInfoTtl) {
        this.redisTemplate = redisTemplate;
        this.userInfoTtl = userInfoTtl;
    }

    /**
     * Busca informações do usuário no cache usando o token JWT como chave.
     * 
     * Fluxo:
     * 1. Gera chave de cache a partir do hash do token
     * 2. Busca no Redis de forma assíncrona
     * 3. Filtra apenas dados não expirados
     * 4. Log de sucesso/erro para observabilidade
     * 
     * @param token JWT do usuário
     * @return Mono<UserInfo> - Dados do usuário se encontrados e válidos, empty()
     *         se não
     */
    public Mono<UserInfo> getUserFromCache(String token) {
        // Gera chave única baseada no hash do token (segurança + performance)
        String cacheKey = generateCacheKey(token);

        return redisTemplate.opsForValue()
                .get(cacheKey) // Busca assíncrona no Redis
                // Filtra apenas se os dados ainda estão válidos (não expirados)
                .filter(userInfo -> !userInfo.isExpired(userInfoTtl))
                // Log para debugging - mostra quando cache foi usado
                .doOnNext(userInfo -> System.out.println("✅ Cache HIT para usuário: " + userInfo.getEmail()))
                // Log de erros de conexão com Redis
                .doOnError(error -> System.err.println("❌ Erro ao acessar cache: " + error.getMessage()));
    }

    /**
     * Armazena informações do usuário no cache com TTL configurado.
     * 
     * Fluxo:
     * 1. Gera chave de cache
     * 2. Armazena no Redis com TTL automático
     * 3. Log de confirmação
     * 4. Retorna o UserInfo para continuar o fluxo
     * 
     * @param token    JWT do usuário
     * @param userInfo Dados do usuário para cachear
     * @return Mono<UserInfo> - Mesmos dados passados, para encadeamento
     */
    public Mono<UserInfo> cacheUser(String token, UserInfo userInfo) {
        // Gera chave única para este usuário/token
        String cacheKey = generateCacheKey(token);

        return redisTemplate.opsForValue()
                // Armazena com TTL - Redis remove automaticamente após expirar
                .set(cacheKey, userInfo, Duration.ofSeconds(userInfoTtl))
                // Log para confirmar que dados foram armazenados
                .doOnSuccess(success -> System.out.println("💾 Cache STORE para usuário: " + userInfo.getEmail()))
                // Log de erros de conexão/armazenamento
                .doOnError(error -> System.err.println("❌ Erro ao armazenar cache: " + error.getMessage()))
                // Retorna o UserInfo original para continuar o fluxo reativo
                .thenReturn(userInfo);
    }

    /**
     * Remove manualmente os dados do usuário do cache.
     * 
     * Útil para:
     * - Logout do usuário
     * - Alteração de permissões
     * - Invalidação de token comprometido
     * 
     * @param token JWT do usuário para invalidar
     * @return Mono<Boolean> - true se removido com sucesso, false se não existia
     */
    public Mono<Boolean> invalidateUserCache(String token) {
        // Gera a mesma chave usada para armazenar
        String cacheKey = generateCacheKey(token);

        return redisTemplate.delete(cacheKey)
                // Converte número de itens deletados para boolean
                .map(deletedCount -> deletedCount > 0)
                // Log para confirmar invalidação
                .doOnNext(deleted -> System.out.println("🗑️ Cache INVALIDATED: " + deleted));
    }

    /**
     * Gera chave única para o cache baseada no hash do token.
     * 
     * Estratégia de segurança:
     * - Não armazena o token JWT completo no Redis
     * - Usa hash do token como identificador único
     * - Prefixo "user_info:" para organização e evitar colisões
     * 
     * Exemplo: "user_info:123456789" onde 123456789 é token.hashCode()
     * 
     * @param token JWT completo
     * @return String chave única para o Redis
     */
    private String generateCacheKey(String token) {
        // Usa hash do token para evitar armazenar token completo (segurança)
        // + prefixo para organizar namespace no Redis
        return "user_info:" + token.hashCode();
    }
}