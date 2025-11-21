package service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import dao.ApiDAO;
import filterDTO.ApiFilterDTO;
import responseDTO.LoginResponseDTO;
import responseDTO.UsuarioDTO;
import util.JwtUtil;
import spark.Request;
import spark.Response;

import java.sql.ResultSet;

public class ApiService {
	public Object entrar(Request request, Response response) {
		response.type("application/json");
		
		// LE OS FILTROS DO CORPO DA REQUISICAO EM JSON E CONVERTE PARA A CLASSE ApiFilterDTO
		JsonMapper mapper = JsonMapper.builder().build();
		ApiFilterDTO filtro = null;
		try {
			filtro = mapper.readValue(request.body(), new TypeReference<ApiFilterDTO>() {});
		} catch (JsonMappingException e) {
			return criarRespostaErro(mapper, "Formato JSON inválido", response);
		} catch (JsonProcessingException e) {
			return criarRespostaErro(mapper, "Erro ao processar JSON", response);
		}
		
		// VERIFICA SE OS CAMPOS PARA LOGIN FORAM ENVIADOS
		if (filtro == null || filtro.getEmail() == null || filtro.getSenha() == null) {
			return criarRespostaErro(mapper, "Email e senha são obrigatórios", response);
		}
		
		// EXECUTA A REQUISICAO PARA VALIDAR LOGIN
		try {
			ApiDAO apiDAO = new ApiDAO();
			ResultSet rs = apiDAO.entrar(filtro);
			
			if (rs != null && rs.next()) {
				//SE TIVER DADOS NA RESPOSTA É PQ O LOGIN FOI VALIDADO
				int userId = rs.getInt("id");
				String nome = rs.getString("nome");
				String email = rs.getString("email");
				boolean administrador = rs.getBoolean("administrador");
				int pontos = rs.getInt("pontos");
				String descricao = rs.getString("descricao");
				String foto = rs.getString("foto");
				
				// GERAR TOKEN JWT
				String token = JwtUtil.generateToken(userId, email, administrador);
				
				if (token != null) {
					// CRIA A RESPOSTA DO ENDPOINT
					UsuarioDTO usuarioDTO = new UsuarioDTO(userId, nome, email, administrador, pontos, descricao, foto);
					LoginResponseDTO loginResponse = new LoginResponseDTO(true, token, "Login realizado com sucesso", usuarioDTO);
					
					response.status(200);
					return mapper.writeValueAsString(loginResponse);
				} else {
					return criarRespostaErro(mapper, "Erro interno ao gerar token", response);
				}
			} else {
				// LOGIN INVALIDO
				response.status(401);
				LoginResponseDTO loginResponse = new LoginResponseDTO(false, "Email ou senha inválidos");
				return mapper.writeValueAsString(loginResponse);
			}
			
		} catch (Exception e) {
			System.err.println("Erro no login: " + e.getMessage());
			return criarRespostaErro(mapper, "Erro interno do servidor", response);
		}
	}
	
	private String criarRespostaErro(JsonMapper mapper, String mensagem, Response response) {
		try {
			response.status(400);
			LoginResponseDTO loginResponse = new LoginResponseDTO(false, mensagem);
			return mapper.writeValueAsString(loginResponse);
		} catch (JsonProcessingException e) {
			response.status(500);
			return "{\"sucesso\": false, \"mensagem\": \"Erro interno do servidor\"}";
		}
	}
}
