package service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;

import dao.KeyDAO;
import dao.UsuarioAplicacaoDAO;
import filterDTO.ApiFilterDTO;
import model.Key;
import model.UsuarioAplicacao;
import responseDTO.LoginResponseDTO;
import util.JwtUtil;
import spark.Request;
import spark.Response;

public class AppTokenService {
    
    public Object gerarToken(Request request, Response response) {
        response.type("application/json");
        JsonMapper mapper = JsonMapper.builder().build();
        
        try {
            // LER OS DADOS DA REQUISIÇÃO
            ApiFilterDTO filtro = null;
            try {
                filtro = mapper.readValue(request.body(), new TypeReference<ApiFilterDTO>() {});
            } catch (Exception e) {
                return criarRespostaErro(mapper, "Formato JSON inválido", response, 400);
            }
            
            // VERIFICAR SE OS CAMPOS OBRIGATÓRIOS FORAM ENVIADOS
            if (filtro == null || filtro.getCodigoKey() == null || filtro.getCodigoKey().trim().isEmpty()) {
                return criarRespostaErro(mapper, "Código da key é obrigatório", response, 400);
            }
            
            // BUSCAR A KEY PELO CÓDIGO
            KeyDAO keyDAO = new KeyDAO();
            Key key = keyDAO.buscarPorCodigo(filtro.getCodigoKey().trim());
            
            if (key == null) {
                return criarRespostaErro(mapper, "Key não encontrada", response, 404);
            }
            
            if (!key.isAtivo()) {
                return criarRespostaErro(mapper, "Key inativa", response, 403);
            }
            
            // SE PRECISAR VALIDAR USUÁRIO ESPECÍFICO (OPCIONAL)
            Integer idUsuarioValidacao = null;
            if (filtro.getIdUsuario() != null) {
                idUsuarioValidacao = filtro.getIdUsuario();
                
                // VERIFICAR SE O USUÁRIO TEM PERMISSÃO PARA USAR ESTA KEY
                UsuarioAplicacaoDAO usuarioAplicacaoDAO = new UsuarioAplicacaoDAO();
                UsuarioAplicacao usuarioAplicacao = usuarioAplicacaoDAO.buscarPorUsuarioEAplicacao(
                    idUsuarioValidacao, key.getIdAplicacao()
                );
                
                if (usuarioAplicacao == null || !usuarioAplicacao.getAtivo() || 
                    usuarioAplicacao.getIdKey() != key.getId()) {
                    return criarRespostaErro(mapper, "Usuário não autorizado a usar esta key", response, 403);
                }
            } else {
                // SE NÃO FOI ESPECIFICADO USUÁRIO, USAR ID GENÉRICO (0)
                // ISSO PERMITE USO DA KEY SEM VINCULAR A UM USUÁRIO ESPECÍFICO
                idUsuarioValidacao = 0;
            }
            
            // GERAR O TOKEN DE APLICAÇÃO
            String token = JwtUtil.generateAppToken(
                idUsuarioValidacao, 
                key.getIdAplicacao(), 
                key.getId(), 
                key.getCodigo()
            );
            
            if (token != null) {
                // CRIAR RESPOSTA DE SUCESSO
                LoginResponseDTO tokenResponse = new LoginResponseDTO(
                    true, 
                    "Token de aplicação gerado com sucesso"
                );
                tokenResponse.setToken(token);
                response.status(200);
                return mapper.writeValueAsString(tokenResponse);
            } else {
                return criarRespostaErro(mapper, "Erro interno ao gerar token", response, 500);
            }
            
        } catch (Exception e) {
            System.err.println("Erro ao gerar token de aplicação: " + e.getMessage());
            e.printStackTrace();
            return criarRespostaErro(mapper, "Erro interno do servidor", response, 500);
        }
    }
    
    public Object validarToken(Request request, Response response) {
        response.type("application/json");
        JsonMapper mapper = JsonMapper.builder().build();
        
        try {
            // PEGAR TOKEN DO HEADER AUTHORIZATION
            String authHeader = request.headers("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return criarRespostaErro(mapper, "Token não fornecido", response, 401);
            }
            
            String token = authHeader.substring(7);
            
            // VALIDAR O TOKEN
            if (JwtUtil.validateAppToken(token) == null) {
                return criarRespostaErro(mapper, "Token inválido ou expirado", response, 401);
            }
            
            // EXTRAIR INFORMAÇÕES DO TOKEN
            int idUsuario = JwtUtil.getUserIdFromAppToken(token);
            int idAplicacao = JwtUtil.getAplicacaoIdFromAppToken(token);
            int idKey = JwtUtil.getKeyIdFromAppToken(token);
            String codigoKey = JwtUtil.getKeyCodeFromAppToken(token);
            
            // VALIDAR SE A KEY AINDA EXISTE E ESTÁ ATIVA
            KeyDAO keyDAO = new KeyDAO();
            Key key = keyDAO.buscarPorId(idKey);
            
            if (key == null || !key.isAtivo() || !key.getCodigo().equals(codigoKey) || 
                key.getIdAplicacao() != idAplicacao) {
                return criarRespostaErro(mapper, "Token inválido: key não existe, está inativa ou foi alterada", response, 401);
            }
            
            // CRIAR RESPOSTA COM INFORMAÇÕES DO TOKEN
            java.util.Map<String, Object> tokenInfo = new java.util.HashMap<>();
            tokenInfo.put("valid", true);
            tokenInfo.put("idUsuario", idUsuario);
            tokenInfo.put("idAplicacao", idAplicacao);
            tokenInfo.put("idKey", idKey);
            tokenInfo.put("codigoKey", codigoKey);
            tokenInfo.put("nomeKey", key.getNome());
            tokenInfo.put("descricaoKey", key.getDescricao());
            
            response.status(200);
            return mapper.writeValueAsString(tokenInfo);
            
        } catch (Exception e) {
            System.err.println("Erro ao validar token de aplicação: " + e.getMessage());
            e.printStackTrace();
            return criarRespostaErro(mapper, "Erro interno do servidor", response, 500);
        }
    }
    
    public Object detectarTipoToken(Request request, Response response) {
        response.type("application/json");
        JsonMapper mapper = JsonMapper.builder().build();
        
        try {
            // PEGAR TOKEN DO HEADER AUTHORIZATION
            String authHeader = request.headers("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return criarRespostaErro(mapper, "Token não fornecido", response, 400);
            }
            
            String token = authHeader.substring(7);
            
            // DETECTAR TIPO DO TOKEN
            String tipoDetectado = JwtUtil.detectTokenType(token);
            String tipoValidado = JwtUtil.validateAnyToken(token);
            
            // CRIAR RESPOSTA COM INFORMAÇÕES DO TOKEN
            java.util.Map<String, Object> tokenInfo = new java.util.HashMap<>();
            tokenInfo.put("tipoDetectado", tipoDetectado);
            tokenInfo.put("tokenValido", tipoValidado != null);
            tokenInfo.put("tipoValidado", tipoValidado);
            
            if ("invalid_token".equals(tipoDetectado)) {
                tokenInfo.put("mensagem", "Token inválido ou malformado");
            } else if (tipoValidado == null) {
                tokenInfo.put("mensagem", "Token do tipo '" + tipoDetectado + "' detectado, mas inválido ou expirado");
            } else {
                tokenInfo.put("mensagem", "Token válido do tipo: " + tipoValidado);
                
                // Adicionar informações específicas por tipo
                switch (tipoValidado) {
                    case "app_token":
                        tokenInfo.put("idUsuario", JwtUtil.getUserIdFromAppToken(token));
                        tokenInfo.put("idAplicacao", JwtUtil.getAplicacaoIdFromAppToken(token));
                        tokenInfo.put("idKey", JwtUtil.getKeyIdFromAppToken(token));
                        tokenInfo.put("codigoKey", JwtUtil.getKeyCodeFromAppToken(token));
                        break;
                    case "user_token":
                        tokenInfo.put("idUsuario", JwtUtil.getUserIdFromToken(token));
                        tokenInfo.put("email", JwtUtil.getEmailFromToken(token));
                        break;
                    case "app_user_token":
                        tokenInfo.put("nomeUsuario", JwtUtil.getNomeUsuarioFromAppUserToken(token));
                        tokenInfo.put("emailUsuario", JwtUtil.getEmailUsuarioFromAppUserToken(token));
                        tokenInfo.put("keyAcesso", JwtUtil.getKeyAcessoFromAppUserToken(token));
                        break;
                }
            }
            
            response.status(200);
            return mapper.writeValueAsString(tokenInfo);
            
        } catch (Exception e) {
            System.err.println("Erro ao detectar tipo de token: " + e.getMessage());
            e.printStackTrace();
            return criarRespostaErro(mapper, "Erro interno do servidor", response, 500);
        }
    }
    
    private String criarRespostaErro(JsonMapper mapper, String mensagem, Response response, int statusCode) {
        try {
            response.status(statusCode);
            LoginResponseDTO errorResponse = new LoginResponseDTO(false, mensagem);
            return mapper.writeValueAsString(errorResponse);
        } catch (JsonProcessingException e) {
            response.status(500);
            return "{\"sucesso\": false, \"mensagem\": \"Erro interno do servidor\"}";
        }
    }
}