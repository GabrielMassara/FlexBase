package service;

import dao.EndpointDAO;
import dao.AplicacaoDAO;
import model.Endpoint;
import model.Aplicacao;
import filterDTO.EndpointFilterDTO;
import responseDTO.EndpointDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class EndpointService {
    
    private String[] metodosHttp = {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"};
    
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
            
            EndpointDAO endpointDAO = new EndpointDAO();
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            List<Endpoint> endpoints = endpointDAO.listarTodos();
            List<EndpointDTO> endpointsDTO = new ArrayList<>();
            
            for (Endpoint endpoint : endpoints) {
                Aplicacao aplicacao = aplicacaoDAO.buscarPorId(endpoint.getIdAplicacao());
                
                // Filtrar endpoints apenas das aplicações que o usuário pode acessar
                if (aplicacao != null && aplicacao.getIdUsuario() == idUsuario.intValue()) {
                    String nomeAplicacao = aplicacao.getNome();
                    String metodoNome = getMetodoNome(endpoint.getMetodo());
                
                    endpointsDTO.add(new EndpointDTO(
                        endpoint.getId(),
                        endpoint.getIdAplicacao(),
                        nomeAplicacao,
                        endpoint.getRota(),
                        endpoint.getQuery(),
                        endpoint.getMetodo(),
                        metodoNome
                    ));
                }
            }
            
            return mapper.writeValueAsString(endpointsDTO);
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
            EndpointDAO endpointDAO = new EndpointDAO();
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            Endpoint endpoint = endpointDAO.buscarPorId(id);
            
            if (endpoint == null) {
                response.status(404);
                return criarRespostaErro(mapper, "Endpoint não encontrado");
            }
            
            Aplicacao aplicacao = aplicacaoDAO.buscarPorId(endpoint.getIdAplicacao());
            if (aplicacao == null) {
                response.status(404);
                return criarRespostaErro(mapper, "Aplicação não encontrada");
            }
            
            // Verificar se o usuário tem permissão para visualizar este endpoint
            if (aplicacao.getIdUsuario() != idUsuario.intValue()) {
                response.status(403);
                return criarRespostaErro(mapper, "Você não tem permissão para visualizar este endpoint");
            }
            
            String nomeAplicacao = aplicacao.getNome();
            String metodoNome = getMetodoNome(endpoint.getMetodo());
            
            EndpointDTO endpointDTO = new EndpointDTO(
                endpoint.getId(),
                endpoint.getIdAplicacao(),
                nomeAplicacao,
                endpoint.getRota(),
                endpoint.getQuery(),
                endpoint.getMetodo(),
                metodoNome
            );
            
            return mapper.writeValueAsString(endpointDTO);
        } catch (NumberFormatException e) {
            response.status(400);
            return criarRespostaErro(mapper, "ID inválido");
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return criarRespostaErro(mapper, "Erro interno do servidor");
        }
    }
    
    public Object buscarPorAplicacao(Request request, Response response) {
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
            EndpointDAO endpointDAO = new EndpointDAO();
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            
            Aplicacao aplicacao = aplicacaoDAO.buscarPorId(idAplicacao);
            if (aplicacao == null) {
                response.status(404);
                return criarRespostaErro(mapper, "Aplicação não encontrada");
            }
            
            // Verificar se o usuário tem permissão para visualizar endpoints desta aplicação
            if (aplicacao.getIdUsuario() != idUsuario.intValue()) {
                response.status(403);
                return criarRespostaErro(mapper, "Você não tem permissão para visualizar endpoints desta aplicação");
            }
            
            List<Endpoint> endpoints = endpointDAO.buscarPorAplicacao(idAplicacao);
            List<EndpointDTO> endpointsDTO = new ArrayList<>();
            String nomeAplicacao = aplicacao.getNome();
            
            for (Endpoint endpoint : endpoints) {
                String metodoNome = getMetodoNome(endpoint.getMetodo());
                
                endpointsDTO.add(new EndpointDTO(
                    endpoint.getId(),
                    endpoint.getIdAplicacao(),
                    nomeAplicacao,
                    endpoint.getRota(),
                    endpoint.getQuery(),
                    endpoint.getMetodo(),
                    metodoNome
                ));
            }
            
            return mapper.writeValueAsString(endpointsDTO);
        } catch (NumberFormatException e) {
            response.status(400);
            return criarRespostaErro(mapper, "ID de aplicação inválido");
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
            EndpointFilterDTO filtro = null;
            try {
                if (request.body() != null && !request.body().isEmpty()) {
                    filtro = mapper.readValue(request.body(), new TypeReference<EndpointFilterDTO>() {});
                }
            } catch (JsonProcessingException e) {
                response.status(400);
                return criarRespostaErro(mapper, "JSON inválido");
            }
            
            EndpointDAO endpointDAO = new EndpointDAO();
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            List<Endpoint> endpoints;
            
            if (filtro != null) {
                endpoints = endpointDAO.buscarComFiltro(filtro);
            } else {
                endpoints = endpointDAO.listarTodos();
            }
            
            List<EndpointDTO> endpointsDTO = new ArrayList<>();
            for (Endpoint endpoint : endpoints) {
                Aplicacao aplicacao = aplicacaoDAO.buscarPorId(endpoint.getIdAplicacao());
                String nomeAplicacao = aplicacao != null ? aplicacao.getNome() : "Aplicação não encontrada";
                String metodoNome = getMetodoNome(endpoint.getMetodo());
                
                endpointsDTO.add(new EndpointDTO(
                    endpoint.getId(),
                    endpoint.getIdAplicacao(),
                    nomeAplicacao,
                    endpoint.getRota(),
                    endpoint.getQuery(),
                    endpoint.getMetodo(),
                    metodoNome
                ));
            }
            
            return mapper.writeValueAsString(endpointsDTO);
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
            // Obter informações do usuário logado através do token de autenticação
            Integer idUsuario = request.attribute("userId");
            
            if (idUsuario == null) {
                response.status(401);
                return criarRespostaErro(mapper, "Token de autenticação inválido");
            }
            
            Endpoint endpoint = mapper.readValue(request.body(), new TypeReference<Endpoint>() {});
            
            if (endpoint.getIdAplicacao() <= 0 ||
                endpoint.getRota() == null || endpoint.getRota().trim().isEmpty() ||
                endpoint.getQuery() == null || endpoint.getQuery().trim().isEmpty() ||
                endpoint.getMetodo() < 0 || endpoint.getMetodo() >= metodosHttp.length) {
                response.status(400);
                return criarRespostaErro(mapper, "ID da aplicação, rota, query e método são obrigatórios e válidos");
            }
            
            EndpointDAO endpointDAO = new EndpointDAO();
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            
            // Verificar se aplicação existe
            Aplicacao aplicacao = aplicacaoDAO.buscarPorId(endpoint.getIdAplicacao());
            if (aplicacao == null) {
                response.status(400);
                return criarRespostaErro(mapper, "Aplicação não encontrada");
            }
            
            // Verificar se o usuário tem permissão para criar endpoint nesta aplicação
            if (aplicacao.getIdUsuario() != idUsuario.intValue()) {
                response.status(403);
                return criarRespostaErro(mapper, "Você não tem permissão para criar endpoints nesta aplicação");
            }
            
            if (endpointDAO.inserir(endpoint)) {
                response.status(201);
                return criarRespostaSucesso(mapper, "Endpoint criado com sucesso");
            } else {
                response.status(500);
                return criarRespostaErro(mapper, "Erro ao criar endpoint");
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
            Endpoint endpoint = mapper.readValue(request.body(), new TypeReference<Endpoint>() {});
            endpoint.setId(id);
            
            if (endpoint.getRota() == null || endpoint.getRota().trim().isEmpty() ||
                endpoint.getQuery() == null || endpoint.getQuery().trim().isEmpty() ||
                endpoint.getMetodo() < 0 || endpoint.getMetodo() >= metodosHttp.length) {
                response.status(400);
                return criarRespostaErro(mapper, "Rota, query e método são obrigatórios e válidos");
            }
            
            EndpointDAO endpointDAO = new EndpointDAO();
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            
            // Verificar se endpoint existe
            Endpoint endpointExistente = endpointDAO.buscarPorId(id);
            if (endpointExistente == null) {
                response.status(404);
                return criarRespostaErro(mapper, "Endpoint não encontrado");
            }
            
            // Verificar se a aplicação existe e se o usuário tem permissão
            Aplicacao aplicacao = aplicacaoDAO.buscarPorId(endpointExistente.getIdAplicacao());
            if (aplicacao == null) {
                response.status(404);
                return criarRespostaErro(mapper, "Aplicação não encontrada");
            }
            
            // Verificar se o usuário tem permissão para atualizar este endpoint
            if (aplicacao.getIdUsuario() != idUsuario.intValue()) {
                response.status(403);
                return criarRespostaErro(mapper, "Você não tem permissão para atualizar este endpoint");
            }
            
            // Manter o ID da aplicação original
            endpoint.setIdAplicacao(endpointExistente.getIdAplicacao());
            
            if (endpointDAO.atualizar(endpoint)) {
                return criarRespostaSucesso(mapper, "Endpoint atualizado com sucesso");
            } else {
                response.status(500);
                return criarRespostaErro(mapper, "Erro ao atualizar endpoint");
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
            EndpointDAO endpointDAO = new EndpointDAO();
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            
            // Verificar se endpoint existe
            Endpoint endpointExistente = endpointDAO.buscarPorId(id);
            if (endpointExistente == null) {
                response.status(404);
                return criarRespostaErro(mapper, "Endpoint não encontrado");
            }
            
            // Verificar se a aplicação existe e se o usuário tem permissão
            Aplicacao aplicacao = aplicacaoDAO.buscarPorId(endpointExistente.getIdAplicacao());
            if (aplicacao == null) {
                response.status(404);
                return criarRespostaErro(mapper, "Aplicação não encontrada");
            }
            
            // Verificar se o usuário tem permissão para excluir este endpoint
            if (aplicacao.getIdUsuario() != idUsuario.intValue()) {
                response.status(403);
                return criarRespostaErro(mapper, "Você não tem permissão para excluir este endpoint");
            }
            
            if (endpointDAO.excluir(id)) {
                return criarRespostaSucesso(mapper, "Endpoint excluído com sucesso");
            } else {
                response.status(500);
                return criarRespostaErro(mapper, "Erro ao excluir endpoint");
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
    
    private String getMetodoNome(int metodo) {
        if (metodo >= 0 && metodo < metodosHttp.length) {
            return metodosHttp[metodo];
        }
        return "DESCONHECIDO";
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