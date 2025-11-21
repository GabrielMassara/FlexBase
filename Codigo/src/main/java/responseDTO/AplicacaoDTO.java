package responseDTO;

import com.fasterxml.jackson.databind.JsonNode;

public class AplicacaoDTO {
    private int id;
    private String nome;
    private String readme;
    private int idUsuario;
    private String nomeUsuario;
    private String nomeBanco;
    private JsonNode schemaBanco;
    
    public AplicacaoDTO() {}
    
    public AplicacaoDTO(int id, String nome, String readme, int idUsuario, String nomeUsuario, String nomeBanco, JsonNode schemaBanco) {
        this.id = id;
        this.nome = nome;
        this.readme = readme;
        this.idUsuario = idUsuario;
        this.nomeUsuario = nomeUsuario;
        this.nomeBanco = nomeBanco;
        this.schemaBanco = schemaBanco;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getReadme() {
        return readme;
    }
    
    public void setReadme(String readme) {
        this.readme = readme;
    }
    
    public int getIdUsuario() {
        return idUsuario;
    }
    
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }
    
    public String getNomeUsuario() {
        return nomeUsuario;
    }
    
    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }
    
    public String getNomeBanco() {
        return nomeBanco;
    }
    
    public void setNomeBanco(String nomeBanco) {
        this.nomeBanco = nomeBanco;
    }
    
    public JsonNode getSchemaBanco() {
        return schemaBanco;
    }
    
    public void setSchemaBanco(JsonNode schemaBanco) {
        this.schemaBanco = schemaBanco;
    }
}