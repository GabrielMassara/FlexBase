package util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Date;

public class JwtUtil {
    private static final String SECRET = "naviapi-secret-key-2025";
    private static final String ISSUER = "naviapi";
    private static final long EXPIRATION_TIME = 86400000; // 24 HORAS
    
    private static final Algorithm algorithm = Algorithm.HMAC256(SECRET);
    

    
    //GERA O TOKEN DO USUARIO
    public static String generateToken(int userId, String email) {
        try {
            Date expiresAt = new Date(System.currentTimeMillis() + EXPIRATION_TIME);
            
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(String.valueOf(userId))
                    .withClaim("email", email)
                    .withExpiresAt(expiresAt)
                    .withIssuedAt(new Date())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            System.err.println("Erro ao criar token JWT: " + exception.getMessage());
            return null;
        }
    }
    
    //VALIDA O TOKEN
    public static DecodedJWT validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build();
            
            return verifier.verify(token);
        } catch (JWTVerificationException exception) {
            System.err.println("Token JWT inválido: " + exception.getMessage());
            return null;
        }
    }
    
    //PEGA O ID DO USUARIO PELO TOKEN
    public static int getUserIdFromToken(String token) {
        DecodedJWT decodedJWT = validateToken(token);
        if (decodedJWT != null) {
            try {
                return Integer.parseInt(decodedJWT.getSubject());
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
    
    //PEGA O EMMAIL DO USUARIO
    public static String getEmailFromToken(String token) {
        DecodedJWT decodedJWT = validateToken(token);
        if (decodedJWT != null) {
            return decodedJWT.getClaim("email").asString();
        }
        return null;
    }
    
    //PEGA O ID DO USUARIO LOGADO A PARTIR DA REQUEST
    public static int getUserIdFromRequest(spark.Request request) {
        String authHeader = request.headers("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return -1;
        }
        
        String token = authHeader.substring(7);
        return getUserIdFromToken(token);
    }
    

    
    //VERIFICA SE O TOKEN JA EXPIROU
    public static boolean isTokenExpired(String token) {
        DecodedJWT decodedJWT = validateToken(token);
        if (decodedJWT != null) {
            return decodedJWT.getExpiresAt().before(new Date());
        }
        return true;
    }
    
    // ========== MÉTODOS PARA TOKENS DE APLICAÇÃO ==========
    
    //GERA O TOKEN PARA UMA KEY DE APLICACAO
    public static String generateAppToken(int idUsuario, int idAplicacao, int idKey, String codigoKey) {
        try {
            Date expiresAt = new Date(System.currentTimeMillis() + EXPIRATION_TIME);
            
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(String.valueOf(idUsuario))
                    .withClaim("idAplicacao", idAplicacao)
                    .withClaim("idKey", idKey)
                    .withClaim("codigoKey", codigoKey)
                    .withClaim("type", "app_token") // Identificador do tipo de token
                    .withExpiresAt(expiresAt)
                    .withIssuedAt(new Date())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            System.err.println("Erro ao criar token JWT de aplicação: " + exception.getMessage());
            return null;
        }
    }
    
    //VALIDA E VERIFICA SE É UM TOKEN DE APLICACAO
    public static DecodedJWT validateAppToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .withClaim("type", "app_token")
                    .build();
            
            return verifier.verify(token);
        } catch (JWTVerificationException exception) {
            System.err.println("Token JWT de aplicação inválido: " + exception.getMessage());
            return null;
        }
    }
    
    //PEGA O ID DA APLICACAO DO TOKEN
    public static int getAplicacaoIdFromAppToken(String token) {
        DecodedJWT decodedJWT = validateAppToken(token);
        if (decodedJWT != null) {
            try {
                return decodedJWT.getClaim("idAplicacao").asInt();
            } catch (Exception e) {
                return -1;
            }
        }
        return -1;
    }
    
    //PEGA O ID DA KEY DO TOKEN
    public static int getKeyIdFromAppToken(String token) {
        DecodedJWT decodedJWT = validateAppToken(token);
        if (decodedJWT != null) {
            try {
                return decodedJWT.getClaim("idKey").asInt();
            } catch (Exception e) {
                return -1;
            }
        }
        return -1;
    }
    
    //PEGA O CODIGO DA KEY DO TOKEN
    public static String getKeyCodeFromAppToken(String token) {
        DecodedJWT decodedJWT = validateAppToken(token);
        if (decodedJWT != null) {
            return decodedJWT.getClaim("codigoKey").asString();
        }
        return null;
    }
    
    //PEGA O ID DO USUARIO DO TOKEN DE APLICACAO
    public static int getUserIdFromAppToken(String token) {
        DecodedJWT decodedJWT = validateAppToken(token);
        if (decodedJWT != null) {
            try {
                return Integer.parseInt(decodedJWT.getSubject());
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
    
    // ========== MÉTODOS PARA TOKENS DE USUÁRIO DE APLICAÇÃO ==========
    
    //VALIDA E VERIFICA SE É UM TOKEN DE USUÁRIO DE APLICAÇÃO
    public static DecodedJWT validateAppUserToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .withClaim("type", "app_user_token")
                    .build();
            
            return verifier.verify(token);
        } catch (JWTVerificationException exception) {
            System.err.println("Token JWT de usuário de aplicação inválido: " + exception.getMessage());
            return null;
        }
    }
    
    //PEGA O NOME DO USUARIO DO TOKEN DE USUÁRIO DE APLICAÇÃO
    public static String getNomeUsuarioFromAppUserToken(String token) {
        DecodedJWT decodedJWT = validateAppUserToken(token);
        if (decodedJWT != null) {
            return decodedJWT.getClaim("nome_usuario").asString();
        }
        return null;
    }
    
    //PEGA O EMAIL DO USUARIO DO TOKEN DE USUÁRIO DE APLICAÇÃO
    public static String getEmailUsuarioFromAppUserToken(String token) {
        DecodedJWT decodedJWT = validateAppUserToken(token);
        if (decodedJWT != null) {
            return decodedJWT.getClaim("email_usuario").asString();
        }
        return null;
    }
    
    //PEGA A KEY DE ACESSO DO TOKEN DE USUÁRIO DE APLICAÇÃO
    public static String getKeyAcessoFromAppUserToken(String token) {
        DecodedJWT decodedJWT = validateAppUserToken(token);
        if (decodedJWT != null) {
            return decodedJWT.getClaim("key_acesso").asString();
        }
        return null;
    }
    
    //PEGA OS DADOS DO USUÁRIO DO TOKEN DE USUÁRIO DE APLICAÇÃO
    public static String getDadosUsuarioFromAppUserToken(String token) {
        DecodedJWT decodedJWT = validateAppUserToken(token);
        if (decodedJWT != null) {
            return decodedJWT.getClaim("dados_usuario").asString();
        }
        return null;
    }
    
    // ========== MÉTODOS AUXILIARES ==========
    
    //DETECTA O TIPO DE TOKEN AUTOMATICAMENTE
    public static String detectTokenType(String token) {
        try {
            // Decodifica o token sem validar a assinatura para ler os claims
            DecodedJWT decoded = JWT.decode(token);
            String type = decoded.getClaim("type").asString();
            
            if ("app_token".equals(type)) {
                return "app_token";
            } else if ("app_user_token".equals(type)) {
                return "app_user_token";
            } else {
                // Se não tem claim 'type' ou tem valor diferente, assume token de usuário padrão
                return "user_token";
            }
        } catch (Exception e) {
            return "invalid_token";
        }
    }
    
    //VALIDA QUALQUER TIPO DE TOKEN E RETORNA O TIPO DETECTADO
    public static String validateAnyToken(String token) {
        String tokenType = detectTokenType(token);
        
        switch (tokenType) {
            case "app_token":
                return validateAppToken(token) != null ? "app_token" : null;
            case "app_user_token":
                return validateAppUserToken(token) != null ? "app_user_token" : null;
            case "user_token":
                return validateToken(token) != null ? "user_token" : null;
            default:
                return null;
        }
    }
}