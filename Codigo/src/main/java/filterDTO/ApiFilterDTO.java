package filterDTO;

public class ApiFilterDTO {
	private String email;
	private String senha;
	private String token;
	private String codigoKey;
	private Integer idUsuario;
	
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getSenha() {
		return senha;
	}
	public void setSenha(String senha) {
		this.senha = senha;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getCodigoKey() {
		return codigoKey;
	}
	public void setCodigoKey(String codigoKey) {
		this.codigoKey = codigoKey;
	}
	public Integer getIdUsuario() {
		return idUsuario;
	}
	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}
}
