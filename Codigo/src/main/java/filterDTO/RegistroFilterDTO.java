package filterDTO;

public class RegistroFilterDTO {
    private Integer id;
    private String tabela;
    private Integer idAplicacao;
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getTabela() {
        return tabela;
    }
    
    public void setTabela(String tabela) {
        this.tabela = tabela;
    }
    
    public Integer getIdAplicacao() {
        return idAplicacao;
    }
    
    public void setIdAplicacao(Integer idAplicacao) {
        this.idAplicacao = idAplicacao;
    }
}