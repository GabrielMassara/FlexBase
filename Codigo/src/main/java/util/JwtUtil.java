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
}