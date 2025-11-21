package filterDTO;

public class EndpointFilterDTO {
    private Integer id;
    private Integer idAplicacao;
    private String rota;
    private Integer metodo;
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getIdAplicacao() {
        return idAplicacao;
    }
    
    public void setIdAplicacao(Integer idAplicacao) {
        this.idAplicacao = idAplicacao;
    }
    
    public String getRota() {
        return rota;
    }
    
    public void setRota(String rota) {
        this.rota = rota;
    }
    
    public Integer getMetodo() {
        return metodo;
    }
    
    public void setMetodo(Integer metodo) {
        this.metodo = metodo;
    }
}