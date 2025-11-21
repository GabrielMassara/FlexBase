package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import filterDTO.ApiFilterDTO;

public class ApiDAO extends DAO {
	public ApiDAO() {
		super();
		conectar();
	}

	public void finalize() {
		close();
	}
	

	public ResultSet entrar(ApiFilterDTO filtro) {
		ResultSet rs = null;
		
		if (filtro == null || filtro.getEmail() == null || filtro.getSenha() == null) {
			return null;
		}
		
		try {
			String senhaMD5 = toMD5(filtro.getSenha());
			
			String sql = "SELECT id, nome, email, administrador, pontos, descricao, foto FROM tb_usuarios WHERE email = ? AND senha = ?";
			PreparedStatement st = conexao.prepareStatement(sql);
			st.setString(1, filtro.getEmail());
			st.setString(2, senhaMD5);
			
			rs = st.executeQuery();
			
		} catch (SQLException e) {
			System.err.println("Erro ao executar consulta de login: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erro ao criptografar senha: " + e.getMessage());
		}
		
		return rs;
	}
}
