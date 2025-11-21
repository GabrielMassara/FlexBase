package responseDTO;

public class LoginResponseDTO {
    private boolean sucesso;
    private String token;
    private String mensagem;
    private UsuarioDTO usuario;
    
    public LoginResponseDTO() {}
    
    public LoginResponseDTO(boolean sucesso, String mensagem) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
    }
    
    public LoginResponseDTO(boolean sucesso, String token, String mensagem, UsuarioDTO usuario) {
        this.sucesso = sucesso;
        this.token = token;
        this.mensagem = mensagem;
        this.usuario = usuario;
    }
    
    public boolean isSucesso() {
        return sucesso;
    }
    
    public void setSucesso(boolean sucesso) {
        this.sucesso = sucesso;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getMensagem() {
        return mensagem;
    }
    
    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
    
    public UsuarioDTO getUsuario() {
        return usuario;
    }
    
    public void setUsuario(UsuarioDTO usuario) {
        this.usuario = usuario;
    }
}