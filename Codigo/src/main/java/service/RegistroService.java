package service;

import dao.RegistroDAO;
import dao.AplicacaoDAO;
import model.Registro;
import model.Aplicacao;
import filterDTO.RegistroFilterDTO;
import responseDTO.RegistroDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class RegistroService {
    
    public Object listar(Request request, Response response) {
        response.type("application/json");
        JsonMapper mapper = JsonMapper.builder().build();
        
        try {
            RegistroDAO registroDAO = new RegistroDAO();
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            List<Registro> registros = registroDAO.listarTodos();
            List<RegistroDTO> registrosDTO = new ArrayList<>();
            
            for (Registro registro : registros) {
                Aplicacao aplicacao = aplicacaoDAO.buscarPorId(registro.getIdAplicacao());
                String nomeAplicacao = aplicacao != null ? aplicacao.getNome() : "Aplicação não encontrada";
                
                registrosDTO.add(new RegistroDTO(
                    registro.getId(),
                    registro.getTabela(),
                    registro.getValor(),
                    registro.getIdAplicacao(),
                    nomeAplicacao
                ));
            }
            
            return mapper.writeValueAsString(registrosDTO);
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
            int id = Integer.parseInt(request.params(":id"));
            RegistroDAO registroDAO = new RegistroDAO();
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            Registro registro = registroDAO.buscarPorId(id);
            
            if (registro == null) {
                response.status(404);
                return criarRespostaErro(mapper, "Registro não encontrado");
            }
            
            Aplicacao aplicacao = aplicacaoDAO.buscarPorId(registro.getIdAplicacao());
            String nomeAplicacao = aplicacao != null ? aplicacao.getNome() : "Aplicação não encontrada";
            
            RegistroDTO registroDTO = new RegistroDTO(
                registro.getId(),
                registro.getTabela(),
                registro.getValor(),
                registro.getIdAplicacao(),
                nomeAplicacao
            );
            
            return mapper.writeValueAsString(registroDTO);
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
            int idAplicacao = Integer.parseInt(request.params(":idAplicacao"));
            RegistroDAO registroDAO = new RegistroDAO();
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            List<Registro> registros = registroDAO.buscarPorAplicacao(idAplicacao);
            List<RegistroDTO> registrosDTO = new ArrayList<>();
            
            Aplicacao aplicacao = aplicacaoDAO.buscarPorId(idAplicacao);
            String nomeAplicacao = aplicacao != null ? aplicacao.getNome() : "Aplicação não encontrada";
            
            for (Registro registro : registros) {
                registrosDTO.add(new RegistroDTO(
                    registro.getId(),
                    registro.getTabela(),
                    registro.getValor(),
                    registro.getIdAplicacao(),
                    nomeAplicacao
                ));
            }
            
            return mapper.writeValueAsString(registrosDTO);
        } catch (NumberFormatException e) {
            response.status(400);
            return criarRespostaErro(mapper, "ID de aplicação inválido");
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return criarRespostaErro(mapper, "Erro interno do servidor");
        }
    }
    
    public Object buscarPorTabela(Request request, Response response) {
        response.type("application/json");
        JsonMapper mapper = JsonMapper.builder().build();
        
        try {
            String tabela = request.params(":tabela");
            RegistroDAO registroDAO = new RegistroDAO();
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            List<Registro> registros = registroDAO.buscarPorTabela(tabela);
            List<RegistroDTO> registrosDTO = new ArrayList<>();
            
            for (Registro registro : registros) {
                Aplicacao aplicacao = aplicacaoDAO.buscarPorId(registro.getIdAplicacao());
                String nomeAplicacao = aplicacao != null ? aplicacao.getNome() : "Aplicação não encontrada";
                
                registrosDTO.add(new RegistroDTO(
                    registro.getId(),
                    registro.getTabela(),
                    registro.getValor(),
                    registro.getIdAplicacao(),
                    nomeAplicacao
                ));
            }
            
            return mapper.writeValueAsString(registrosDTO);
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
            RegistroFilterDTO filtro = null;
            try {
                if (request.body() != null && !request.body().isEmpty()) {
                    filtro = mapper.readValue(request.body(), new TypeReference<RegistroFilterDTO>() {});
                }
            } catch (JsonProcessingException e) {
                response.status(400);
                return criarRespostaErro(mapper, "JSON inválido");
            }
            
            RegistroDAO registroDAO = new RegistroDAO();
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            List<Registro> registros;
            
            if (filtro != null) {
                registros = registroDAO.buscarComFiltro(filtro);
            } else {
                registros = registroDAO.listarTodos();
            }
            
            List<RegistroDTO> registrosDTO = new ArrayList<>();
            for (Registro registro : registros) {
                Aplicacao aplicacao = aplicacaoDAO.buscarPorId(registro.getIdAplicacao());
                String nomeAplicacao = aplicacao != null ? aplicacao.getNome() : "Aplicação não encontrada";
                
                registrosDTO.add(new RegistroDTO(
                    registro.getId(),
                    registro.getTabela(),
                    registro.getValor(),
                    registro.getIdAplicacao(),
                    nomeAplicacao
                ));
            }
            
            return mapper.writeValueAsString(registrosDTO);
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
            Registro registro = mapper.readValue(request.body(), new TypeReference<Registro>() {});
            
            if (registro.getTabela() == null || registro.getTabela().trim().isEmpty() ||
                registro.getValor() == null ||
                registro.getIdAplicacao() <= 0) {
                response.status(400);
                return criarRespostaErro(mapper, "Tabela, valor e ID da aplicação são obrigatórios");
            }
            
            RegistroDAO registroDAO = new RegistroDAO();
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            
            // Verificar se aplicação existe
            if (aplicacaoDAO.buscarPorId(registro.getIdAplicacao()) == null) {
                response.status(400);
                return criarRespostaErro(mapper, "Aplicação não encontrada");
            }
            
            if (registroDAO.inserir(registro)) {
                response.status(201);
                return criarRespostaSucesso(mapper, "Registro criado com sucesso");
            } else {
                response.status(500);
                return criarRespostaErro(mapper, "Erro ao criar registro");
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
            int id = Integer.parseInt(request.params(":id"));
            Registro registro = mapper.readValue(request.body(), new TypeReference<Registro>() {});
            registro.setId(id);
            
            if (registro.getTabela() == null || registro.getTabela().trim().isEmpty() ||
                registro.getValor() == null) {
                response.status(400);
                return criarRespostaErro(mapper, "Tabela e valor são obrigatórios");
            }
            
            RegistroDAO registroDAO = new RegistroDAO();
            
            // Verificar se registro existe
            if (registroDAO.buscarPorId(id) == null) {
                response.status(404);
                return criarRespostaErro(mapper, "Registro não encontrado");
            }
            
            if (registroDAO.atualizar(registro)) {
                return criarRespostaSucesso(mapper, "Registro atualizado com sucesso");
            } else {
                response.status(500);
                return criarRespostaErro(mapper, "Erro ao atualizar registro");
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
            int id = Integer.parseInt(request.params(":id"));
            RegistroDAO registroDAO = new RegistroDAO();
            
            if (registroDAO.buscarPorId(id) == null) {
                response.status(404);
                return criarRespostaErro(mapper, "Registro não encontrado");
            }
            
            if (registroDAO.excluir(id)) {
                return criarRespostaSucesso(mapper, "Registro excluído com sucesso");
            } else {
                response.status(500);
                return criarRespostaErro(mapper, "Erro ao excluir registro");
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