package filterDTO;

public class AplicacaoFilterDTO {
    private Integer id;
    private String nome;
    private Integer idUsuario;
    private String nomeBanco;
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public Integer getIdUsuario() {
        return idUsuario;
    }
    
    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
    
    public String getNomeBanco() {
        return nomeBanco;
    }
    
    public void setNomeBanco(String nomeBanco) {
        this.nomeBanco = nomeBanco;
    }
}