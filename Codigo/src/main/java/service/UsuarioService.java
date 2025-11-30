package service;

import dao.UsuarioDAO;
import model.Usuario;
import filterDTO.UsuarioFilterDTO;
import responseDTO.UsuarioDTO;
import responseDTO.LoginResponseDTO;
import util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class UsuarioService {
    
    public Object listar(Request request, Response response) {
        response.type("application/json");
        JsonMapper mapper = JsonMapper.builder().build();
        
        try {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            List<Usuario> usuarios = usuarioDAO.listarTodos();
            List<UsuarioDTO> usuariosDTO = new ArrayList<>();
            
            for (Usuario usuario : usuarios) {
                usuariosDTO.add(new UsuarioDTO(
                    usuario.getId(),
                    usuario.getNome(),
                    usuario.getSobrenome(),
                    usuario.getEmail()
                ));
            }
            
            return mapper.writeValueAsString(usuariosDTO);
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
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            Usuario usuario = usuarioDAO.buscarPorId(id);
            
            if (usuario == null) {
                response.status(404);
                return criarRespostaErro(mapper, "Usuário não encontrado");
            }
            
            UsuarioDTO usuarioDTO = new UsuarioDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getSobrenome(),
                usuario.getEmail()
            );
            
            return mapper.writeValueAsString(usuarioDTO);
        } catch (NumberFormatException e) {
            response.status(400);
            return criarRespostaErro(mapper, "ID inválido");
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
            UsuarioFilterDTO filtro = null;
            try {
                if (request.body() != null && !request.body().isEmpty()) {
                    filtro = mapper.readValue(request.body(), new TypeReference<UsuarioFilterDTO>() {});
                }
            } catch (JsonProcessingException e) {
                response.status(400);
                return criarRespostaErro(mapper, "JSON inválido");
            }
            
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            List<Usuario> usuarios;
            
            if (filtro != null) {
                usuarios = usuarioDAO.buscarComFiltro(filtro);
            } else {
                usuarios = usuarioDAO.listarTodos();
            }
            
            List<UsuarioDTO> usuariosDTO = new ArrayList<>();
            for (Usuario usuario : usuarios) {
                usuariosDTO.add(new UsuarioDTO(
                    usuario.getId(),
                    usuario.getNome(),
                    usuario.getSobrenome(),
                    usuario.getEmail()
                ));
            }
            
            return mapper.writeValueAsString(usuariosDTO);
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
            Usuario usuario = mapper.readValue(request.body(), new TypeReference<Usuario>() {});
            
            if (usuario.getNome() == null || usuario.getNome().trim().isEmpty() ||
                usuario.getSobrenome() == null || usuario.getSobrenome().trim().isEmpty() ||
                usuario.getEmail() == null || usuario.getEmail().trim().isEmpty() ||
                usuario.getSenha() == null || usuario.getSenha().trim().isEmpty()) {
                response.status(400);
                return criarRespostaErro(mapper, "Nome, sobrenome, email e senha são obrigatórios");
            }
            
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            
            // Verificar se email já existe
            if (usuarioDAO.buscarPorEmail(usuario.getEmail()) != null) {
                response.status(400);
                return criarRespostaErro(mapper, "Email já está em uso");
            }
            
            if (usuarioDAO.inserir(usuario)) {
                response.status(201);
                return criarRespostaSucesso(mapper, "Usuário criado com sucesso");
            } else {
                response.status(500);
                return criarRespostaErro(mapper, "Erro ao criar usuário");
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
            Usuario usuario = mapper.readValue(request.body(), new TypeReference<Usuario>() {});
            usuario.setId(id);
            
            if (usuario.getNome() == null || usuario.getNome().trim().isEmpty() ||
                usuario.getSobrenome() == null || usuario.getSobrenome().trim().isEmpty() ||
                usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
                response.status(400);
                return criarRespostaErro(mapper, "Nome, sobrenome e email são obrigatórios");
            }
            
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            
            // Verificar se usuário existe
            if (usuarioDAO.buscarPorId(id) == null) {
                response.status(404);
                return criarRespostaErro(mapper, "Usuário não encontrado");
            }
            
            // Verificar se email já está em uso por outro usuário
            Usuario usuarioExistente = usuarioDAO.buscarPorEmail(usuario.getEmail());
            if (usuarioExistente != null && usuarioExistente.getId() != id) {
                response.status(400);
                return criarRespostaErro(mapper, "Email já está em uso por outro usuário");
            }
            
            if (usuarioDAO.atualizar(usuario)) {
                return criarRespostaSucesso(mapper, "Usuário atualizado com sucesso");
            } else {
                response.status(500);
                return criarRespostaErro(mapper, "Erro ao atualizar usuário");
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
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            
            if (usuarioDAO.buscarPorId(id) == null) {
                response.status(404);
                return criarRespostaErro(mapper, "Usuário não encontrado");
            }
            
            if (usuarioDAO.excluir(id)) {
                return criarRespostaSucesso(mapper, "Usuário excluído com sucesso");
            } else {
                response.status(500);
                return criarRespostaErro(mapper, "Erro ao excluir usuário");
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
    
    public Object login(Request request, Response response) {
        response.type("application/json");
        JsonMapper mapper = JsonMapper.builder().build();
        
        try {
            Map<String, String> loginData = mapper.readValue(request.body(), new TypeReference<Map<String, String>>() {});
            
            String email = loginData.get("email");
            String senha = loginData.get("senha");
            
            if (email == null || email.trim().isEmpty() || senha == null || senha.trim().isEmpty()) {
                response.status(400);
                return criarRespostaErro(mapper, "Email e senha são obrigatórios");
            }
            
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            Usuario usuario = usuarioDAO.autenticar(email, senha);
            
            if (usuario == null) {
                response.status(401);
                return criarRespostaErro(mapper, "Email ou senha inválidos");
            }
            
            // Gerar token JWT
            String token = JwtUtil.generateToken(usuario.getId(), usuario.getEmail());
            
            if (token == null) {
                response.status(500);
                return criarRespostaErro(mapper, "Erro ao gerar token de autenticação");
            }
            
            UsuarioDTO usuarioDTO = new UsuarioDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getSobrenome(),
                usuario.getEmail()
            );
            
            LoginResponseDTO loginResponse = new LoginResponseDTO(true, token, "Login realizado com sucesso", usuarioDTO);
            response.status(200);
            
            return mapper.writeValueAsString(loginResponse);
        } catch (JsonProcessingException e) {
            response.status(400);
            return criarRespostaErro(mapper, "JSON inválido");
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