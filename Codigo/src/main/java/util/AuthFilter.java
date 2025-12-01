package util;

import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;
import com.fasterxml.jackson.databind.json.JsonMapper;
import responseDTO.LoginResponseDTO;
import dao.KeyDAO;
import dao.EndpointDAO;
import model.Key;
import model.Endpoint;
import java.util.List;
import com.auth0.jwt.interfaces.DecodedJWT;

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
        
        // VERIFICAR SE O USERID É VÁLIDO
        if (userId == -1) {
            response.status(401);
            JsonMapper mapper = JsonMapper.builder().build();
            LoginResponseDTO errorResponse = new LoginResponseDTO(false, "Erro ao extrair dados do token");
            response.body(mapper.writeValueAsString(errorResponse));
            Spark.halt(401);
            return;
        }
        
        request.attribute("userId", Integer.valueOf(userId));
        request.attribute("userEmail", userEmail);
    };
    
    // FILTRO PARA VALIDAR TOKEN DE APLICAÇÃO (USADO NOS ENDPOINTS /api/endpoints/:idAplicacao/*)
    public static Filter authenticateAppToken = (Request request, Response response) -> {
        response.type("application/json");
        
        // PEGA O TOKEN DO HEADER AUTHORIZATION
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
        
        // VALIDAR TOKEN DE APLICAÇÃO OU TOKEN DE USUÁRIO DE APLICAÇÃO
        DecodedJWT decodedToken = null;
        String tokenType = JwtUtil.detectTokenType(token);
        boolean isValidToken = false;
        
        if ("app_token".equals(tokenType)) {
            decodedToken = JwtUtil.validateAppToken(token);
            isValidToken = (decodedToken != null);
        } else if ("app_user_token".equals(tokenType)) {
            decodedToken = JwtUtil.validateAppUserToken(token);
            isValidToken = (decodedToken != null);
        }
        
        if (!isValidToken || decodedToken == null) {
            String mensagemErro;
            
            switch (tokenType) {
                case "user_token":
                    mensagemErro = "Token de usuário detectado. Use um token de aplicação (/api/app-token/gerar) ou faça login na aplicação (/api/login/" + request.params(":idAplicacao") + ")";
                    break;
                case "app_user_token":
                    mensagemErro = "Token de usuário de aplicação inválido ou expirado";
                    break;
                case "app_token":
                    mensagemErro = "Token de aplicação inválido ou expirado";
                    break;
                case "invalid_token":
                    mensagemErro = "Token inválido ou malformado";
                    break;
                default:
                    mensagemErro = "Token não suportado para endpoints de aplicação";
                    break;
            }
            
            response.status(401);
            JsonMapper mapper = JsonMapper.builder().build();
            LoginResponseDTO errorResponse = new LoginResponseDTO(false, mensagemErro);
            response.body(mapper.writeValueAsString(errorResponse));
            Spark.halt(401);
            return;
        }
        
        // EXTRAIR DADOS BASEADO NO TIPO DE TOKEN
        int userId = -1;
        int idAplicacao = -1;
        int idKey = -1;
        String codigoKey = null;
        
        if ("app_token".equals(tokenType)) {
            // Token de aplicação direto
            userId = JwtUtil.getUserIdFromAppToken(token);
            idAplicacao = JwtUtil.getAplicacaoIdFromAppToken(token);
            idKey = JwtUtil.getKeyIdFromAppToken(token);
            codigoKey = JwtUtil.getKeyCodeFromAppToken(token);
        } else if ("app_user_token".equals(tokenType)) {
            // Token de usuário de aplicação - precisa buscar a key de acesso
            String keyAcesso = JwtUtil.getKeyAcessoFromAppUserToken(token);
            
            if (keyAcesso != null) {
                KeyDAO keyDAO = new KeyDAO();
                Key key = keyDAO.buscarPorCodigo(keyAcesso);
                
                if (key != null && key.isAtivo()) {
                    userId = Integer.parseInt(decodedToken.getSubject()); // ID do registro tb_usuario_aplicacao
                    idAplicacao = key.getIdAplicacao();
                    idKey = key.getId();
                    codigoKey = key.getCodigo();
                } else {
                    response.status(401);
                    JsonMapper mapper = JsonMapper.builder().build();
                    LoginResponseDTO errorResponse = new LoginResponseDTO(false, "Key de acesso inválida ou inativa");
                    response.body(mapper.writeValueAsString(errorResponse));
                    Spark.halt(401);
                    return;
                }
            }
        }
        
        // VERIFICAR SE OS DADOS SÃO VÁLIDOS
        if (userId == -1 || idAplicacao == -1 || idKey == -1 || codigoKey == null) {
            response.status(401);
            JsonMapper mapper = JsonMapper.builder().build();
            LoginResponseDTO errorResponse = new LoginResponseDTO(false, "Erro ao extrair dados do token");
            response.body(mapper.writeValueAsString(errorResponse));
            Spark.halt(401);
            return;
        }
        
        // VERIFICAR SE A APLICAÇÃO DO TOKEN CORRESPONDE À APLICAÇÃO DA ROTA
        try {
            int idAplicacaoRota = Integer.parseInt(request.params(":idAplicacao"));
            if (idAplicacao != idAplicacaoRota) {
                response.status(403);
                JsonMapper mapper = JsonMapper.builder().build();
                LoginResponseDTO errorResponse = new LoginResponseDTO(false, "Token não autorizado para esta aplicação");
                response.body(mapper.writeValueAsString(errorResponse));
                Spark.halt(403);
                return;
            }
        } catch (NumberFormatException e) {
            response.status(400);
            JsonMapper mapper = JsonMapper.builder().build();
            LoginResponseDTO errorResponse = new LoginResponseDTO(false, "ID da aplicação inválido");
            response.body(mapper.writeValueAsString(errorResponse));
            Spark.halt(400);
            return;
        }
        
        // VALIDAR SE A KEY EXISTE E ESTÁ ATIVA NO BANCO (apenas para app_token, já validado para app_user_token)
        if ("app_token".equals(tokenType)) {
            KeyDAO keyDAO = new KeyDAO();
            Key key = keyDAO.buscarPorId(idKey);
            
            if (key == null || !key.isAtivo() || !key.getCodigo().equals(codigoKey) || key.getIdAplicacao() != idAplicacao) {
                response.status(401);
                JsonMapper mapper = JsonMapper.builder().build();
                LoginResponseDTO errorResponse = new LoginResponseDTO(false, "Key inválida, inativa ou não pertence à aplicação");
                response.body(mapper.writeValueAsString(errorResponse));
                Spark.halt(401);
                return;
            }
        }
        
        // VERIFICAR SE A KEY TEM PERMISSÃO PARA EXECUTAR O ENDPOINT SOLICITADO
        String rotaEndpoint = request.splat()[0];
        if (rotaEndpoint != null && !rotaEndpoint.isEmpty()) {
            // Adicionar "/" no início se não tiver
            if (!rotaEndpoint.startsWith("/")) {
                rotaEndpoint = "/" + rotaEndpoint;
            }
            
            // Buscar endpoint correspondente
            EndpointDAO endpointDAO = new EndpointDAO();
            List<Endpoint> endpoints = endpointDAO.buscarPorAplicacao(idAplicacao);
            
            Endpoint endpointEncontrado = null;
            int metodoHTTP = getMetodoHTTP(request.requestMethod());
            
            for (Endpoint endpoint : endpoints) {
                if (endpoint.getMetodo() == metodoHTTP && rotaCorresponde(endpoint.getRota(), rotaEndpoint)) {
                    endpointEncontrado = endpoint;
                    break;
                }
            }
            
            if (endpointEncontrado == null) {
                response.status(404);
                JsonMapper mapper = JsonMapper.builder().build();
                LoginResponseDTO errorResponse = new LoginResponseDTO(false, "Endpoint não encontrado para esta rota e método");
                response.body(mapper.writeValueAsString(errorResponse));
                Spark.halt(404);
                return;
            }
            
            // VERIFICAR SE A KEY TEM PERMISSÃO PARA EXECUTAR ESTE ENDPOINT (tb_key_endpoint)
            KeyDAO keyDAOPermissao = new KeyDAO();
            List<Integer> endpointsAssociados = keyDAOPermissao.buscarEndpointsAssociados(idKey);
            if (!endpointsAssociados.contains(endpointEncontrado.getId())) {
                response.status(403);
                JsonMapper mapper = JsonMapper.builder().build();
                LoginResponseDTO errorResponse = new LoginResponseDTO(false, "Key não autorizada a executar este endpoint");
                response.body(mapper.writeValueAsString(errorResponse));
                Spark.halt(403);
                return;
            }
        }
        
        // ADICIONAR ATRIBUTOS À REQUISIÇÃO PARA USO POSTERIOR
        request.attribute("userId", Integer.valueOf(userId));
        request.attribute("idAplicacao", Integer.valueOf(idAplicacao));
        request.attribute("idKey", Integer.valueOf(idKey));
        request.attribute("codigoKey", codigoKey);
        request.attribute("tokenType", tokenType);
    };
    
    // MÉTODO AUXILIAR PARA CONVERTER MÉTODO HTTP PARA INTEIRO
    private static int getMetodoHTTP(String metodo) {
        switch (metodo.toUpperCase()) {
            case "GET": return 1;
            case "POST": return 2;
            case "PUT": return 3;
            case "DELETE": return 4;
            default: return 0;
        }
    }
    
    // MÉTODO AUXILIAR PARA VERIFICAR SE UMA ROTA CORRESPONDE (CONSIDERANDO PARÂMETROS)
    private static boolean rotaCorresponde(String rotaEndpoint, String rotaRequisicao) {
        // Converter rota do endpoint para regex
        // Exemplo: "/clientes/{id}" -> "/clientes/([^/]+)"
        String regexRota = rotaEndpoint.replaceAll("\\{[^}]+\\}", "([^/]+)");
        regexRota = "^" + regexRota + "$";
        
        return rotaRequisicao.matches(regexRota);
    }

}