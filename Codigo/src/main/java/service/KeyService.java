package service;

import dao.KeyDAO;
import dao.AplicacaoDAO;
import model.Key;
import model.Aplicacao;
import responseDTO.KeyDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class KeyService {
    
    public Object listarPorAplicacao(Request request, Response response) {
        response.type("application/json");
        JsonMapper mapper = JsonMapper.builder().build();
        
        try {
            // Obter informações do usuário logado através do token de autenticação
            Integer idUsuario = request.attribute("userId");
            
            if (idUsuario == null) {
                response.status(401);
                return criarRespostaErro(mapper, "Token de autenticação inválido");
            }
            
            int idAplicacao = Integer.parseInt(request.params(":idAplicacao"));
            
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            KeyDAO keyDAO = new KeyDAO();
            
            // Verificar se a aplicação existe e se o usuário tem permissão
            Aplicacao aplicacao = aplicacaoDAO.buscarPorId(idAplicacao);
            if (aplicacao == null) {
                response.status(404);
                return criarRespostaErro(mapper, "Aplicação não encontrada");
            }
            
            if (aplicacao.getIdUsuario() != idUsuario.intValue()) {
                response.status(403);
                return criarRespostaErro(mapper, "Você não tem permissão para visualizar as keys desta aplicação");
            }
            
            List<Key> keys = keyDAO.buscarPorAplicacao(idAplicacao);
            List<KeyDTO> keysDTO = new ArrayList<>();
            
            for (Key key : keys) {
                List<Integer> endpoints = keyDAO.buscarEndpointsAssociados(key.getId());
                String dataCriacaoStr = key.getDataCriacao() != null ? key.getDataCriacao().toString() : null;
                keysDTO.add(new KeyDTO(
                    key.getId(),
                    key.getCodigo(),
                    key.getIdAplicacao(),
                    aplicacao.getNome(),
                    key.getNome(),
                    key.getDescricao(),
                    key.isAtivo(),
                    dataCriacaoStr,
                    endpoints
                ));
            }
            
            return mapper.writeValueAsString(keysDTO);
        } catch (NumberFormatException e) {
            response.status(400);
            return criarRespostaErro(mapper, "ID da aplicação inválido");
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return criarRespostaErro(mapper, "Erro interno do servidor");
        }
    }
    
    public Object buscarPorId(Request request, Response response) {
        response.type("application/json");
        JsonMapper mapper = JsonMapper.builder().build();
        
        try {
            // Obter informações do usuário logado através do token de autenticação
            Integer idUsuario = request.attribute("userId");
            
            if (idUsuario == null) {
                response.status(401);
                return criarRespostaErro(mapper, "Token de autenticação inválido");
            }
            
            int id = Integer.parseInt(request.params(":id"));
            KeyDAO keyDAO = new KeyDAO();
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            
            Key key = keyDAO.buscarPorId(id);
            
            if (key == null) {
                response.status(404);
                return criarRespostaErro(mapper, "Key não encontrada");
            }
            
            // Verificar se o usuário tem permissão para visualizar esta key
            Aplicacao aplicacao = aplicacaoDAO.buscarPorId(key.getIdAplicacao());
            if (aplicacao == null || aplicacao.getIdUsuario() != idUsuario.intValue()) {
                response.status(403);
                return criarRespostaErro(mapper, "Você não tem permissão para visualizar esta key");
            }
            
            List<Integer> endpoints = keyDAO.buscarEndpointsAssociados(key.getId());
            String dataCriacaoStr = key.getDataCriacao() != null ? key.getDataCriacao().toString() : null;
            KeyDTO keyDTO = new KeyDTO(
                key.getId(),
                key.getCodigo(),
                key.getIdAplicacao(),
                aplicacao.getNome(),
                key.getNome(),
                key.getDescricao(),
                key.isAtivo(),
                dataCriacaoStr,
                endpoints
            );
            
            return mapper.writeValueAsString(keyDTO);
        } catch (NumberFormatException e) {
            response.status(400);
            return criarRespostaErro(mapper, "ID inválido");
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return criarRespostaErro(mapper, "Erro interno do servidor");
        }
    }
    
    public Object inserir(Request request, Response response) {
        response.type("application/json");
        JsonMapper mapper = JsonMapper.builder().build();
        
        try {
            Map<String, Object> requestData = mapper.readValue(request.body(), new TypeReference<Map<String, Object>>() {});
            
            // Obter ID do usuário logado através do token de autenticação
            Integer idUsuario = request.attribute("userId");
            if (idUsuario == null) {
                response.status(401);
                return criarRespostaErro(mapper, "Token de autenticação inválido");
            }
            
            // Validações básicas
            if (!requestData.containsKey("idAplicacao") || !requestData.containsKey("nome")) {
                response.status(400);
                return criarRespostaErro(mapper, "ID da aplicação e nome são obrigatórios");
            }
            
            int idAplicacao = (Integer) requestData.get("idAplicacao");
            String nome = (String) requestData.get("nome");
            String descricao = (String) requestData.getOrDefault("descricao", "");
            boolean ativo = (Boolean) requestData.getOrDefault("ativo", true);
            
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            KeyDAO keyDAO = new KeyDAO();
            
            // Verificar se a aplicação existe e se o usuário tem permissão
            Aplicacao aplicacao = aplicacaoDAO.buscarPorId(idAplicacao);
            if (aplicacao == null) {
                response.status(404);
                return criarRespostaErro(mapper, "Aplicação não encontrada");
            }
            
            if (aplicacao.getIdUsuario() != idUsuario.intValue()) {
                response.status(403);
                return criarRespostaErro(mapper, "Você não tem permissão para criar keys nesta aplicação");
            }
            
            // Gerar código único para a key
            String codigo = gerarCodigoKey();
            
            Key key = new Key(codigo, idAplicacao, nome, descricao, ativo);
            
            if (keyDAO.inserir(key)) {
                // Processar endpoints associados se fornecidos
                if (requestData.containsKey("endpointsAssociados")) {
                    @SuppressWarnings("unchecked")
                    List<Integer> endpointsIds = (List<Integer>) requestData.get("endpointsAssociados");
                    keyDAO.atualizarEndpointsAssociados(key.getId(), endpointsIds);
                }
                
                response.status(201);
                Map<String, Object> resposta = new HashMap<>();
                resposta.put("success", true);
                resposta.put("message", "Key criada com sucesso");
                
                Map<String, Object> data = new HashMap<>();
                data.put("id", key.getId());
                data.put("codigo", key.getCodigo());
                resposta.put("data", data);
                
                return mapper.writeValueAsString(resposta);
            } else {
                response.status(500);
                return criarRespostaErro(mapper, "Erro ao criar key");
            }
        } catch (JsonProcessingException e) {
            response.status(400);
            return criarRespostaErro(mapper, "JSON inválido");
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return criarRespostaErro(mapper, "Erro interno do servidor");
        }
    }
    
    public Object atualizar(Request request, Response response) {
        response.type("application/json");
        JsonMapper mapper = JsonMapper.builder().build();
        
        try {
            // Obter informações do usuário logado através do token de autenticação
            Integer idUsuario = request.attribute("userId");
            
            if (idUsuario == null) {
                response.status(401);
                return criarRespostaErro(mapper, "Token de autenticação inválido");
            }
            
            int id = Integer.parseInt(request.params(":id"));
            Map<String, Object> requestData = mapper.readValue(request.body(), new TypeReference<Map<String, Object>>() {});
            
            KeyDAO keyDAO = new KeyDAO();
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            
            // Verificar se key existe
            Key keyExistente = keyDAO.buscarPorId(id);
            if (keyExistente == null) {
                response.status(404);
                return criarRespostaErro(mapper, "Key não encontrada");
            }
            
            // Verificar se o usuário tem permissão para atualizar esta key
            Aplicacao aplicacao = aplicacaoDAO.buscarPorId(keyExistente.getIdAplicacao());
            if (aplicacao == null || aplicacao.getIdUsuario() != idUsuario.intValue()) {
                response.status(403);
                return criarRespostaErro(mapper, "Você não tem permissão para atualizar esta key");
            }
            
            // Atualizar campos
            if (requestData.containsKey("nome")) {
                keyExistente.setNome((String) requestData.get("nome"));
            }
            if (requestData.containsKey("descricao")) {
                keyExistente.setDescricao((String) requestData.get("descricao"));
            }
            if (requestData.containsKey("ativo")) {
                keyExistente.setAtivo((Boolean) requestData.get("ativo"));
            }
            
            if (keyDAO.atualizar(keyExistente)) {
                // Atualizar endpoints associados se fornecidos
                if (requestData.containsKey("endpointsAssociados")) {
                    @SuppressWarnings("unchecked")
                    List<Integer> endpointsIds = (List<Integer>) requestData.get("endpointsAssociados");
                    keyDAO.atualizarEndpointsAssociados(keyExistente.getId(), endpointsIds);
                }
                
                return criarRespostaSucesso(mapper, "Key atualizada com sucesso");
            } else {
                response.status(500);
                return criarRespostaErro(mapper, "Erro ao atualizar key");
            }
        } catch (NumberFormatException e) {
            response.status(400);
            return criarRespostaErro(mapper, "ID inválido");
        } catch (JsonProcessingException e) {
            response.status(400);
            return criarRespostaErro(mapper, "JSON inválido");
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return criarRespostaErro(mapper, "Erro interno do servidor");
        }
    }
    
    public Object excluir(Request request, Response response) {
        response.type("application/json");
        JsonMapper mapper = JsonMapper.builder().build();
        
        try {
            // Obter informações do usuário logado através do token de autenticação
            Integer idUsuario = request.attribute("userId");
            
            if (idUsuario == null) {
                response.status(401);
                return criarRespostaErro(mapper, "Token de autenticação inválido");
            }
            
            int id = Integer.parseInt(request.params(":id"));
            KeyDAO keyDAO = new KeyDAO();
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            
            // Verificar se key existe
            Key keyExistente = keyDAO.buscarPorId(id);
            if (keyExistente == null) {
                response.status(404);
                return criarRespostaErro(mapper, "Key não encontrada");
            }
            
            // Verificar se o usuário tem permissão para excluir esta key
            Aplicacao aplicacao = aplicacaoDAO.buscarPorId(keyExistente.getIdAplicacao());
            if (aplicacao == null || aplicacao.getIdUsuario() != idUsuario.intValue()) {
                response.status(403);
                return criarRespostaErro(mapper, "Você não tem permissão para excluir esta key");
            }
            
            // Verificar se é a key base da aplicação
            if (aplicacao.getIdKeyBase() == id) {
                response.status(400);
                return criarRespostaErro(mapper, "Não é possível excluir a key base da aplicação");
            }
            
            if (keyDAO.excluir(id)) {
                return criarRespostaSucesso(mapper, "Key excluída com sucesso");
            } else {
                response.status(500);
                return criarRespostaErro(mapper, "Erro ao excluir key");
            }
        } catch (NumberFormatException e) {
            response.status(400);
            return criarRespostaErro(mapper, "ID inválido");
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return criarRespostaErro(mapper, "Erro interno do servidor");
        }
    }
    
    private String gerarCodigoKey() {
        return "FB_" + System.currentTimeMillis() + "_" + String.format("%06d", (int)(Math.random() * 1000000));
    }
    
    private String criarRespostaErro(JsonMapper mapper, String mensagem) {
        try {
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("success", false);
            resposta.put("message", mensagem);
            return mapper.writeValueAsString(resposta);
        } catch (JsonProcessingException e) {
            return "{\"success\": false, \"message\": \"Erro interno\"}";
        }
    }
    
    private String criarRespostaSucesso(JsonMapper mapper, String mensagem) {
        try {
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("success", true);
            resposta.put("message", mensagem);
            return mapper.writeValueAsString(resposta);
        } catch (JsonProcessingException e) {
            return "{\"success\": true, \"message\": \"Operação realizada\"}";
        }
    }
}