package service;

import dao.UsuarioDAO;
import dao.AplicacaoDAO;
import dao.UsuarioAplicacaoDAO;
import dao.KeyDAO;
import model.Usuario;
import model.Aplicacao;
import model.UsuarioAplicacao;
import model.Key;
import responseDTO.UsuarioAplicacaoDTO;
import util.JwtUtil;
import spark.Request;
import spark.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CadastroAplicacaoService {
    private UsuarioDAO usuarioDAO = new UsuarioDAO();
    private AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
    private UsuarioAplicacaoDAO usuarioAplicacaoDAO = new UsuarioAplicacaoDAO();
    private ObjectMapper objectMapper = new ObjectMapper();

    public Object cadastrarUsuarioNaAplicacao(Request request, Response response) {
        try {
            // Extrair ID da aplicação da URL
            String idAplicacaoStr = request.params(":idAplicacao");
            if (idAplicacaoStr == null || idAplicacaoStr.trim().isEmpty()) {
                response.status(400);
                return "{\"success\": false, \"message\": \"ID da aplicação é obrigatório\"}";
            }

            Integer idAplicacao;
            try {
                idAplicacao = Integer.parseInt(idAplicacaoStr);
            } catch (NumberFormatException e) {
                response.status(400);
                return "{\"success\": false, \"message\": \"ID da aplicação inválido\"}";
            }

            // Verificar se a aplicação existe
            Aplicacao aplicacao = aplicacaoDAO.buscarPorId(idAplicacao);
            if (aplicacao == null) {
                response.status(404);
                return "{\"success\": false, \"message\": \"Aplicação não encontrada\"}";
            }

            // Verificar se a aplicação tem key base
            if (aplicacao.getIdKeyBase() == 0) {
                response.status(400);
                return "{\"success\": false, \"message\": \"Aplicação não possui key base configurada\"}";
            }

            // Parse do body da requisição
            @SuppressWarnings("unchecked")
            Map<String, Object> requestBody = objectMapper.readValue(request.body(), Map.class);
            
            String email = (String) requestBody.get("email");
            String senha = (String) requestBody.get("senha");
            Object dadosUsuarioObj = requestBody.get("dados_usuario");

            if (email == null || email.trim().isEmpty() || senha == null || senha.trim().isEmpty()) {
                response.status(400);
                return "{\"success\": false, \"message\": \"Email e senha são obrigatórios\"}";
            }

            // 1. Fazer login na FlexBase (tb_usuarios)
            Usuario usuario = usuarioDAO.buscarPorEmail(email);
            if (usuario == null) {
                response.status(401);
                return "{\"success\": false, \"message\": \"Usuário não encontrado\"}";
            }

            // Verificar senha (assumindo que a senha está em hash)
            if (!verificarSenha(senha, usuario.getSenha())) {
                response.status(401);
                return "{\"success\": false, \"message\": \"Senha incorreta\"}";
            }

            // 2. Verificar se usuário já está cadastrado nesta aplicação
            UsuarioAplicacao usuarioExistente = usuarioAplicacaoDAO.buscarPorUsuarioEAplicacao(usuario.getId(), idAplicacao);
            if (usuarioExistente != null) {
                response.status(409);
                return "{\"success\": false, \"message\": \"Usuário já está cadastrado nesta aplicação\"}";
            }

            // 3. Cadastrar usuário na aplicação
            UsuarioAplicacao novoUsuarioAplicacao = new UsuarioAplicacao(
                usuario.getId(), 
                idAplicacao, 
                aplicacao.getIdKeyBase()
            );

            // Adicionar dados de usuário se fornecidos
            if (dadosUsuarioObj != null) {
                JsonNode dadosUsuario = objectMapper.valueToTree(dadosUsuarioObj);
                novoUsuarioAplicacao.setDadosUsuario(dadosUsuario);
            }

            UsuarioAplicacao usuarioAplicacaoCriado = usuarioAplicacaoDAO.inserir(novoUsuarioAplicacao);
            
            if (usuarioAplicacaoCriado == null) {
                response.status(500);
                return "{\"success\": false, \"message\": \"Erro ao cadastrar usuário na aplicação\"}";
            }

            // 4. Criar resposta com dados do usuário e aplicação
            UsuarioAplicacaoDTO responseDto = new UsuarioAplicacaoDTO();
            responseDto.setId(usuarioAplicacaoCriado.getId());
            responseDto.setIdUsuario(usuarioAplicacaoCriado.getIdUsuario());
            responseDto.setIdAplicacao(usuarioAplicacaoCriado.getIdAplicacao());
            responseDto.setIdKey(usuarioAplicacaoCriado.getIdKey());
            responseDto.setDadosUsuario(usuarioAplicacaoCriado.getDadosUsuario());
            responseDto.setDataVinculo(usuarioAplicacaoCriado.getDataVinculo());
            responseDto.setAtivo(usuarioAplicacaoCriado.getAtivo());
            responseDto.setNomeUsuario(usuario.getNome() + " " + usuario.getSobrenome());
            responseDto.setEmailUsuario(usuario.getEmail());
            responseDto.setNomeAplicacao(aplicacao.getNome());
            responseDto.setCodigoKey(aplicacao.getCodigoKeyBase());

            // 5. Gerar token JWT para a sessão (opcional)
            String token = JwtUtil.generateToken(usuario.getId(), usuario.getEmail());

            response.status(201);
            Map<String, Object> responseMap = new java.util.HashMap<>();
            responseMap.put("success", true);
            responseMap.put("message", "Usuário cadastrado na aplicação com sucesso");
            responseMap.put("data", responseDto);
            responseMap.put("token", token);
            return objectMapper.writeValueAsString(responseMap);

        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return "{\"success\": false, \"message\": \"Erro interno do servidor\"}";
        }
    }

    public Object listarUsuariosDaAplicacao(Request request, Response response) {
        try {
            // Verificar autenticação
            String authHeader = request.headers("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.status(401);
                return "{\"success\": false, \"message\": \"Token de acesso requerido\"}";
            }

            String token = authHeader.substring(7);
            int idUsuarioLogado = JwtUtil.getUserIdFromToken(token);
            if (idUsuarioLogado == -1) {
                // Tentar validar como token de usuário de aplicação
                if (JwtUtil.validateAppUserToken(token) == null) {
                    response.status(401);
                    return "{\"success\": false, \"message\": \"Token inválido\"}";
                }
            }

            String idAplicacaoStr = request.params(":idAplicacao");
            if (idAplicacaoStr == null || idAplicacaoStr.trim().isEmpty()) {
                response.status(400);
                return "{\"success\": false, \"message\": \"ID da aplicação é obrigatório\"}";
            }

            Integer idAplicacao = Integer.parseInt(idAplicacaoStr);
            
            // Verificar se a aplicação existe
            Aplicacao aplicacao = aplicacaoDAO.buscarPorId(idAplicacao);
            if (aplicacao == null) {
                response.status(404);
                return "{\"success\": false, \"message\": \"Aplicação não encontrada\"}";
            }

            // Verificar se o usuário logado é o owner da aplicação (apenas para token de usuário normal)
            if (idUsuarioLogado != -1 && aplicacao.getIdUsuario() != idUsuarioLogado) {
                response.status(403);
                return "{\"success\": false, \"message\": \"Sem permissão para acessar esta aplicação\"}";
            }

            // Listar usuários da aplicação (incluindo inativos para o gerenciamento)
            // Usar um método modificado do DAO que inclui inativos
            List<UsuarioAplicacao> usuariosAplicacao = usuarioAplicacaoDAO.listarTodosPorAplicacao(idAplicacao);
            
            response.status(200);
            Map<String, Object> responseMap = new java.util.HashMap<>();
            responseMap.put("success", true);
            responseMap.put("data", usuariosAplicacao);
            return objectMapper.writeValueAsString(responseMap);

        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return "{\"success\": false, \"message\": \"Erro interno do servidor\"}";
        }
    }

    public Object loginNaAplicacao(Request request, Response response) {
        try {
            // Extrair ID da aplicação da URL
            String idAplicacaoStr = request.params(":idAplicacao");
            if (idAplicacaoStr == null || idAplicacaoStr.trim().isEmpty()) {
                response.status(400);
                return "{\"success\": false, \"message\": \"ID da aplicação é obrigatório\"}";
            }

            Integer idAplicacao;
            try {
                idAplicacao = Integer.parseInt(idAplicacaoStr);
            } catch (NumberFormatException e) {
                response.status(400);
                return "{\"success\": false, \"message\": \"ID da aplicação inválido\"}";
            }

            // Verificar se a aplicação existe
            Aplicacao aplicacao = aplicacaoDAO.buscarPorId(idAplicacao);
            if (aplicacao == null) {
                response.status(404);
                return "{\"success\": false, \"message\": \"Aplicação não encontrada\"}";
            }

            // Parse do body da requisição
            @SuppressWarnings("unchecked")
            Map<String, Object> requestBody = objectMapper.readValue(request.body(), Map.class);
            
            String email = (String) requestBody.get("email");
            String senha = (String) requestBody.get("senha");

            if (email == null || email.trim().isEmpty() || senha == null || senha.trim().isEmpty()) {
                response.status(400);
                return "{\"success\": false, \"message\": \"Email e senha são obrigatórios\"}";
            }

            // 1. Fazer login na FlexBase (tb_usuarios)
            Usuario usuario = usuarioDAO.buscarPorEmail(email);
            if (usuario == null) {
                response.status(401);
                return "{\"success\": false, \"message\": \"Credenciais inválidas\"}";
            }

            // Verificar senha
            if (!verificarSenha(senha, usuario.getSenha())) {
                response.status(401);
                return "{\"success\": false, \"message\": \"Credenciais inválidas\"}";
            }

            // 2. Verificar se usuário possui conta na aplicação
            UsuarioAplicacao usuarioAplicacao = usuarioAplicacaoDAO.buscarPorUsuarioEAplicacao(usuario.getId(), idAplicacao);
            if (usuarioAplicacao == null) {
                response.status(403);
                return "{\"success\": false, \"message\": \"Usuário não possui acesso a esta aplicação\"}";
            }

            if (!usuarioAplicacao.getAtivo()) {
                response.status(403);
                return "{\"success\": false, \"message\": \"Conta de usuário desativada nesta aplicação\"}";
            }

            // 3. Buscar a key específica do usuário
            KeyDAO keyDAO = new KeyDAO();
            Key keyUsuario = keyDAO.buscarPorId(usuarioAplicacao.getIdKey());
            if (keyUsuario == null || !keyUsuario.isAtivo()) {
                response.status(403);
                return "{\"success\": false, \"message\": \"Key de acesso do usuário inválida ou inativa\"}";
            }

            // 4. Gerar token JWT com dados específicos da aplicação e key do usuário
            String token = gerarTokenAplicacao(
                usuarioAplicacao.getId(),
                usuario.getNome() + " " + usuario.getSobrenome(),
                usuario.getEmail(),
                keyUsuario.getCodigo(),
                usuarioAplicacao.getDadosUsuario()
            );

            if (token == null) {
                response.status(500);
                return "{\"success\": false, \"message\": \"Erro ao gerar token de acesso\"}";
            }

            // 6. Preparar resposta de sucesso
            Map<String, Object> userData = new java.util.HashMap<>();
            userData.put("id_usuario_aplicacao", usuarioAplicacao.getId());
            userData.put("nome_usuario", usuario.getNome() + " " + usuario.getSobrenome());
            userData.put("email_usuario", usuario.getEmail());
            userData.put("key_acesso", keyUsuario.getCodigo());
            userData.put("dados_usuario", usuarioAplicacao.getDadosUsuario());
            userData.put("nome_aplicacao", aplicacao.getNome());
            userData.put("data_vinculo", usuarioAplicacao.getDataVinculo());

            Map<String, Object> responseMap = new java.util.HashMap<>();
            responseMap.put("success", true);
            responseMap.put("message", "Login realizado com sucesso");
            responseMap.put("token", token);
            responseMap.put("user", userData);

            response.status(200);
            return objectMapper.writeValueAsString(responseMap);

        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return "{\"success\": false, \"message\": \"Erro interno do servidor\"}";
        }
    }

    private String gerarTokenAplicacao(Integer idUsuarioAplicacao, String nomeUsuario, 
                                     String emailUsuario, String keyAcesso, 
                                     JsonNode dadosUsuario) {
        try {
            long currentTimeMillis = System.currentTimeMillis();
            long expirationTime = currentTimeMillis + (24 * 60 * 60 * 1000); // 24 horas
            
            return com.auth0.jwt.JWT.create()
                    .withIssuer("naviapi")  // Usar o mesmo issuer do JwtUtil
                    .withSubject(idUsuarioAplicacao.toString())
                    .withClaim("nome_usuario", nomeUsuario)
                    .withClaim("email_usuario", emailUsuario)
                    .withClaim("key_acesso", keyAcesso)
                    .withClaim("dados_usuario", dadosUsuario != null ? dadosUsuario.toString() : null)
                    .withClaim("type", "app_user_token") // Identificador para diferenciar do token de aplicação
                    .withExpiresAt(new java.util.Date(expirationTime))
                    .withIssuedAt(new java.util.Date(currentTimeMillis))
                    .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256("naviapi-secret-key-2025")); // Usar a mesma chave do JwtUtil
                    
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object alterarKeyUsuario(Request request, Response response) {
        try {
            // Verificar se o usuário tem permissão (owner da aplicação)
            String authHeader = request.headers("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.status(401);
                return "{\"success\": false, \"message\": \"Token de acesso requerido\"}";
            }

            String token = authHeader.substring(7);
            int idUsuarioLogado = JwtUtil.getUserIdFromToken(token);
            if (idUsuarioLogado == -1) {
                response.status(401);
                return "{\"success\": false, \"message\": \"Token inválido\"}";
            }

            String idAplicacaoStr = request.params(":idAplicacao");
            String idUsuarioAplicacaoStr = request.params(":idUsuarioAplicacao");

            Integer idAplicacao = Integer.parseInt(idAplicacaoStr);
            Integer idUsuarioAplicacao = Integer.parseInt(idUsuarioAplicacaoStr);

            // Verificar se o usuário logado é o owner da aplicação
            Aplicacao aplicacao = aplicacaoDAO.buscarPorId(idAplicacao);
            if (aplicacao == null || aplicacao.getIdUsuario() != idUsuarioLogado) {
                response.status(403);
                return "{\"success\": false, \"message\": \"Sem permissão para gerenciar esta aplicação\"}";
            }

            // Parse do body
            @SuppressWarnings("unchecked")
            Map<String, Object> requestBody = objectMapper.readValue(request.body(), Map.class);
            Integer novaIdKey = (Integer) requestBody.get("id_key");

            if (novaIdKey == null) {
                response.status(400);
                return "{\"success\": false, \"message\": \"ID da nova key é obrigatório\"}";
            }

            // Verificar se a key existe e pertence à aplicação
            dao.KeyDAO keyDAO = new dao.KeyDAO();
            model.Key key = keyDAO.buscarPorId(novaIdKey);
            if (key == null || key.getIdAplicacao() != idAplicacao) {
                response.status(400);
                return "{\"success\": false, \"message\": \"Key inválida ou não pertence a esta aplicação\"}";
            }

            // Alterar a key
            boolean sucesso = usuarioAplicacaoDAO.alterarKey(idUsuarioAplicacao, novaIdKey);
            
            if (sucesso) {
                response.status(200);
                return "{\"success\": true, \"message\": \"Key alterada com sucesso\"}";
            } else {
                response.status(500);
                return "{\"success\": false, \"message\": \"Erro ao alterar key\"}";
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return "{\"success\": false, \"message\": \"Erro interno do servidor\"}";
        }
    }

    public Object removerUsuarioDaAplicacao(Request request, Response response) {
        try {
            // Verificar se o usuário tem permissão (owner da aplicação)
            String authHeader = request.headers("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.status(401);
                return "{\"success\": false, \"message\": \"Token de acesso requerido\"}";
            }

            String token = authHeader.substring(7);
            int idUsuarioLogado = JwtUtil.getUserIdFromToken(token);
            if (idUsuarioLogado == -1) {
                response.status(401);
                return "{\"success\": false, \"message\": \"Token inválido\"}";
            }

            String idAplicacaoStr = request.params(":idAplicacao");
            String idUsuarioAplicacaoStr = request.params(":idUsuarioAplicacao");

            Integer idAplicacao = Integer.parseInt(idAplicacaoStr);
            Integer idUsuarioAplicacao = Integer.parseInt(idUsuarioAplicacaoStr);

            // Verificar se o usuário logado é o owner da aplicação
            Aplicacao aplicacao = aplicacaoDAO.buscarPorId(idAplicacao);
            if (aplicacao == null || aplicacao.getIdUsuario() != idUsuarioLogado) {
                response.status(403);
                return "{\"success\": false, \"message\": \"Sem permissão para gerenciar esta aplicação\"}";
            }

            // Desativar usuário na aplicação
            boolean sucesso = usuarioAplicacaoDAO.desativar(idUsuarioAplicacao);
            
            if (sucesso) {
                response.status(200);
                return "{\"success\": true, \"message\": \"Usuário removido da aplicação com sucesso\"}";
            } else {
                response.status(500);
                return "{\"success\": false, \"message\": \"Erro ao remover usuário\"}";
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return "{\"success\": false, \"message\": \"Erro interno do servidor\"}";
        }
    }

    public Object alterarStatusUsuario(Request request, Response response) {
        try {
            // Verificar se o usuário tem permissão (owner da aplicação)
            String authHeader = request.headers("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.status(401);
                return "{\"success\": false, \"message\": \"Token de acesso requerido\"}";
            }

            String token = authHeader.substring(7);
            int idUsuarioLogado = JwtUtil.getUserIdFromToken(token);
            if (idUsuarioLogado == -1) {
                response.status(401);
                return "{\"success\": false, \"message\": \"Token inválido\"}";
            }

            String idAplicacaoStr = request.params(":idAplicacao");
            String idUsuarioAplicacaoStr = request.params(":idUsuarioAplicacao");

            Integer idAplicacao = Integer.parseInt(idAplicacaoStr);
            Integer idUsuarioAplicacao = Integer.parseInt(idUsuarioAplicacaoStr);

            // Verificar se o usuário logado é o owner da aplicação
            Aplicacao aplicacao = aplicacaoDAO.buscarPorId(idAplicacao);
            if (aplicacao == null || aplicacao.getIdUsuario() != idUsuarioLogado) {
                response.status(403);
                return "{\"success\": false, \"message\": \"Sem permissão para gerenciar esta aplicação\"}";
            }

            // Parse do body
            @SuppressWarnings("unchecked")
            Map<String, Object> requestBody = objectMapper.readValue(request.body(), Map.class);
            Boolean ativo = (Boolean) requestBody.get("ativo");

            if (ativo == null) {
                response.status(400);
                return "{\"success\": false, \"message\": \"Status é obrigatório\"}";
            }

            // Alterar status
            boolean sucesso = usuarioAplicacaoDAO.alterarStatus(idUsuarioAplicacao, ativo);
            
            if (sucesso) {
                String status = ativo ? "ativado" : "desativado";
                response.status(200);
                return "{\"success\": true, \"message\": \"Usuário " + status + " com sucesso\"}";
            } else {
                response.status(500);
                return "{\"success\": false, \"message\": \"Erro ao alterar status do usuário\"}";
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return "{\"success\": false, \"message\": \"Erro interno do servidor\"}";
        }
    }

    private boolean verificarSenha(String senhaFornecida, String senhaArmazenada) {
        try {
            // Converte a senha fornecida para MD5 e compara com a armazenada
            String senhaFornecidaMD5 = dao.DAO.toMD5(senhaFornecida);
            return senhaFornecidaMD5.equals(senhaArmazenada);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}