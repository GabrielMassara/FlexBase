package service;

import dao.AplicacaoDAO;
import dao.UsuarioDAO;
import model.Aplicacao;
import model.Usuario;
import filterDTO.AplicacaoFilterDTO;
import responseDTO.AplicacaoDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class AplicacaoService {
    
    public Object listar(Request request, Response response) {
        response.type("application/json");
        JsonMapper mapper = JsonMapper.builder().build();
        
        try {
            // Obter informações do usuário logado através do token de autenticação
            Integer idUsuario = request.attribute("userId");
            
            if (idUsuario == null) {
                response.status(401);
                return criarRespostaErro(mapper, "Token de autenticação inválido");
            }
            
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            
            // Listar apenas as aplicações do usuário logado
            List<Aplicacao> aplicacoes = aplicacaoDAO.buscarPorUsuario(idUsuario);
            
            List<AplicacaoDTO> aplicacoesDTO = new ArrayList<>();
            
            for (Aplicacao aplicacao : aplicacoes) {
                Usuario usuario = usuarioDAO.buscarPorId(aplicacao.getIdUsuario());
                String nomeUsuario = usuario != null ? usuario.getNome() + " " + usuario.getSobrenome() : "Usuário não encontrado";
                
                aplicacoesDTO.add(new AplicacaoDTO(
                    aplicacao.getId(),
                    aplicacao.getNome(),
                    aplicacao.getReadme(),
                    aplicacao.getIdUsuario(),
                    nomeUsuario,
                    aplicacao.getNomeBanco(),
                    aplicacao.getSchemaBanco()
                ));
            }
            
            return mapper.writeValueAsString(aplicacoesDTO);
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
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            Aplicacao aplicacao = aplicacaoDAO.buscarPorId(id);
            
            if (aplicacao == null) {
                response.status(404);
                return criarRespostaErro(mapper, "Aplicação não encontrada");
            }
            
            // Verificar se o usuário tem permissão para visualizar esta aplicação
            if (aplicacao.getIdUsuario() != idUsuario.intValue()) {
                response.status(403);
                return criarRespostaErro(mapper, "Você não tem permissão para visualizar esta aplicação");
            }
            
            Usuario usuario = usuarioDAO.buscarPorId(aplicacao.getIdUsuario());
            String nomeUsuario = usuario != null ? usuario.getNome() + " " + usuario.getSobrenome() : "Usuário não encontrado";
            
            AplicacaoDTO aplicacaoDTO = new AplicacaoDTO(
                aplicacao.getId(),
                aplicacao.getNome(),
                aplicacao.getReadme(),
                aplicacao.getIdUsuario(),
                nomeUsuario,
                aplicacao.getNomeBanco(),
                aplicacao.getSchemaBanco()
            );
            
            return mapper.writeValueAsString(aplicacaoDTO);
        } catch (NumberFormatException e) {
            response.status(400);
            return criarRespostaErro(mapper, "ID inválido");
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return criarRespostaErro(mapper, "Erro interno do servidor");
        }
    }
    
    public Object buscarPorUsuario(Request request, Response response) {
        response.type("application/json");
        JsonMapper mapper = JsonMapper.builder().build();
        
        try {
            // Obter ID do usuário logado através do token de autenticação
            Integer idUsuario = request.attribute("userId");
            if (idUsuario == null) {
                response.status(401);
                return criarRespostaErro(mapper, "Token de autenticação inválido");
            }
            
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            List<Aplicacao> aplicacoes = aplicacaoDAO.buscarPorUsuario(idUsuario);
            List<AplicacaoDTO> aplicacoesDTO = new ArrayList<>();
            
            Usuario usuario = usuarioDAO.buscarPorId(idUsuario);
            String nomeUsuario = usuario != null ? usuario.getNome() + " " + usuario.getSobrenome() : "Usuário não encontrado";
            
            for (Aplicacao aplicacao : aplicacoes) {
                aplicacoesDTO.add(new AplicacaoDTO(
                    aplicacao.getId(),
                    aplicacao.getNome(),
                    aplicacao.getReadme(),
                    aplicacao.getIdUsuario(),
                    nomeUsuario,
                    aplicacao.getNomeBanco(),
                    aplicacao.getSchemaBanco()
                ));
            }
            
            return mapper.writeValueAsString(aplicacoesDTO);
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return criarRespostaErro(mapper, "Erro interno do servidor");
        }
    }
    
    public Object buscarComFiltro(Request request, Response response) {
        response.type("application/json");
        JsonMapper mapper = JsonMapper.builder().build();
        
        try {
            // Obter informações do usuário logado através do token de autenticação
            Integer idUsuario = request.attribute("userId");
            
            if (idUsuario == null) {
                response.status(401);
                return criarRespostaErro(mapper, "Token de autenticação inválido");
            }
            
            AplicacaoFilterDTO filtro = null;
            try {
                if (request.body() != null && !request.body().isEmpty()) {
                    filtro = mapper.readValue(request.body(), new TypeReference<AplicacaoFilterDTO>() {});
                }
            } catch (JsonProcessingException e) {
                response.status(400);
                return criarRespostaErro(mapper, "JSON inválido");
            }
            
            // Forçar filtro pelo usuário logado
            if (filtro == null) {
                filtro = new AplicacaoFilterDTO();
            }
            
            // Usuário só pode ver suas próprias aplicações
            filtro.setIdUsuario(idUsuario);
            
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            List<Aplicacao> aplicacoes;
            
            aplicacoes = aplicacaoDAO.buscarComFiltro(filtro);
            
            List<AplicacaoDTO> aplicacoesDTO = new ArrayList<>();
            for (Aplicacao aplicacao : aplicacoes) {
                Usuario usuario = usuarioDAO.buscarPorId(aplicacao.getIdUsuario());
                String nomeUsuario = usuario != null ? usuario.getNome() + " " + usuario.getSobrenome() : "Usuário não encontrado";
                
                aplicacoesDTO.add(new AplicacaoDTO(
                    aplicacao.getId(),
                    aplicacao.getNome(),
                    aplicacao.getReadme(),
                    aplicacao.getIdUsuario(),
                    nomeUsuario,
                    aplicacao.getNomeBanco(),
                    aplicacao.getSchemaBanco()
                ));
            }
            
            return mapper.writeValueAsString(aplicacoesDTO);
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
            Aplicacao aplicacao = mapper.readValue(request.body(), new TypeReference<Aplicacao>() {});
            
            // Obter ID do usuário logado através do token de autenticação
            Integer idUsuario = request.attribute("userId");
            if (idUsuario == null) {
                response.status(401);
                return criarRespostaErro(mapper, "Token de autenticação inválido");
            }
            
            // Definir o ID do usuário logado na aplicação
            aplicacao.setIdUsuario(idUsuario);
            
            if (aplicacao.getNome() == null || aplicacao.getNome().trim().isEmpty()) {
                response.status(400);
                return criarRespostaErro(mapper, "Nome é obrigatório");
            }
            
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            
            // Verificar se usuário existe (redundante, mas mantém a validação)
            if (usuarioDAO.buscarPorId(aplicacao.getIdUsuario()) == null) {
                response.status(400);
                return criarRespostaErro(mapper, "Usuário não encontrado");
            }
            
            if (aplicacaoDAO.inserir(aplicacao)) {
                response.status(201);
                return criarRespostaSucesso(mapper, "Aplicação criada com sucesso");
            } else {
                response.status(500);
                return criarRespostaErro(mapper, "Erro ao criar aplicação");
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
            Aplicacao aplicacao = mapper.readValue(request.body(), new TypeReference<Aplicacao>() {});
            aplicacao.setId(id);
            
            if (aplicacao.getNome() == null || aplicacao.getNome().trim().isEmpty()) {
                response.status(400);
                return criarRespostaErro(mapper, "Nome é obrigatório");
            }
            
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            
            // Verificar se aplicação existe
            Aplicacao aplicacaoExistente = aplicacaoDAO.buscarPorId(id);
            if (aplicacaoExistente == null) {
                response.status(404);
                return criarRespostaErro(mapper, "Aplicação não encontrada");
            }
            
            // Verificar se o usuário tem permissão para atualizar esta aplicação
            if (aplicacaoExistente.getIdUsuario() != idUsuario.intValue()) {
                response.status(403);
                return criarRespostaErro(mapper, "Você não tem permissão para atualizar esta aplicação");
            }
            
            // Manter o ID do usuário original da aplicação
            aplicacao.setIdUsuario(aplicacaoExistente.getIdUsuario());
            
            if (aplicacaoDAO.atualizar(aplicacao)) {
                return criarRespostaSucesso(mapper, "Aplicação atualizada com sucesso");
            } else {
                response.status(500);
                return criarRespostaErro(mapper, "Erro ao atualizar aplicação");
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
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            
            // Verificar se aplicação existe
            Aplicacao aplicacaoExistente = aplicacaoDAO.buscarPorId(id);
            if (aplicacaoExistente == null) {
                response.status(404);
                return criarRespostaErro(mapper, "Aplicação não encontrada");
            }
            
            // Verificar se o usuário tem permissão para excluir esta aplicação
            if (aplicacaoExistente.getIdUsuario() != idUsuario.intValue()) {
                response.status(403);
                return criarRespostaErro(mapper, "Você não tem permissão para excluir esta aplicação");
            }
            
            if (aplicacaoDAO.excluir(id)) {
                return criarRespostaSucesso(mapper, "Aplicação excluída com sucesso");
            } else {
                response.status(500);
                return criarRespostaErro(mapper, "Erro ao excluir aplicação");
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