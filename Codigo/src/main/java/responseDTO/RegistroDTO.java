package responseDTO;

import com.fasterxml.jackson.databind.JsonNode;

public class RegistroDTO {
    private int id;
    private String tabela;
    private JsonNode valor;
    private int idAplicacao;
    private String nomeAplicacao;
    
    public RegistroDTO() {}
    
    public RegistroDTO(int id, String tabela, JsonNode valor, int idAplicacao, String nomeAplicacao) {
        this.id = id;
        this.tabela = tabela;
        this.valor = valor;
        this.idAplicacao = idAplicacao;
        this.nomeAplicacao = nomeAplicacao;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTabela() {
        return tabela;
    }
    
    public void setTabela(String tabela) {
        this.tabela = tabela;
    }
    
    public JsonNode getValor() {
        return valor;
    }
    
    public void setValor(JsonNode valor) {
        this.valor = valor;
    }
    
    public int getIdAplicacao() {
        return idAplicacao;
    }
    
    public void setIdAplicacao(int idAplicacao) {
        this.idAplicacao = idAplicacao;
    }
    
    public String getNomeAplicacao() {
        return nomeAplicacao;
    }
    
    public void setNomeAplicacao(String nomeAplicacao) {
        this.nomeAplicacao = nomeAplicacao;
    }
}