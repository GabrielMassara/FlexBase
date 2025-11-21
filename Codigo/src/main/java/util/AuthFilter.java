package util;

import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;
import com.fasterxml.jackson.databind.json.JsonMapper;
import responseDTO.LoginResponseDTO;

public class AuthFilter {
    

	//FILTRO PARA VALIDAR O TOKEN JWT
    public static Filter authenticate = (Request request, Response response) -> {
        response.type("application/json");
        
        //PEGA O TOKEN DO HEADER AUTHORIZATION
        String authHeader = request.headers("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.status(401);
            JsonMapper mapper = JsonMapper.builder().build();
            LoginResponseDTO errorResponse = new LoginResponseDTO(false, "Token de autenticação não fornecido");
            response.body(mapper.writeValueAsString(errorResponse));
            Spark.halt(401);
            return;
        }
        
        // REMOVE O "Bearer " E PEGA APENAS O TOKEN
        String token = authHeader.substring(7);
        
        // VALIDA O TOKEN
        if (JwtUtil.validateToken(token) == null) {
            response.status(401);
            JsonMapper mapper = JsonMapper.builder().build();
            LoginResponseDTO errorResponse = new LoginResponseDTO(false, "Token inválido ou expirado");
            response.body(mapper.writeValueAsString(errorResponse));
            Spark.halt(401);
            return;
        }
        
        // RETORNA OS DADOS DO USUÁRIO
        int userId = JwtUtil.getUserIdFromToken(token);
        String userEmail = JwtUtil.getEmailFromToken(token);
        boolean isAdmin = JwtUtil.isAdminFromToken(token);
        
        request.attribute("userId", userId);
        request.attribute("userEmail", userEmail);
        request.attribute("isAdmin", isAdmin);
    };
    

    //VERIFICA SE O USUÁRIO É ADM
    public static Filter requireAdmin = (Request request, Response response) -> {
        Boolean isAdmin = request.attribute("isAdmin");
        
        if (isAdmin == null || !isAdmin) {
            response.status(403);
            response.type("application/json");
            JsonMapper mapper = JsonMapper.builder().build();
            LoginResponseDTO errorResponse = new LoginResponseDTO(false, "Acesso negado. Privilégios de administrador necessários");
            response.body(mapper.writeValueAsString(errorResponse));
            Spark.halt(403);
        }
    };
}