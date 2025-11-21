package responseDTO;

public class EndpointDTO {
    private int id;
    private int idAplicacao;
    private String nomeAplicacao;
    private String rota;
    private String query;
    private int metodo;
    private String metodoNome;
    
    public EndpointDTO() {}
    
    public EndpointDTO(int id, int idAplicacao, String nomeAplicacao, String rota, String query, int metodo, String metodoNome) {
        this.id = id;
        this.idAplicacao = idAplicacao;
        this.nomeAplicacao = nomeAplicacao;
        this.rota = rota;
        this.query = query;
        this.metodo = metodo;
        this.metodoNome = metodoNome;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
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
    
    public String getRota() {
        return rota;
    }
    
    public void setRota(String rota) {
        this.rota = rota;
    }
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public int getMetodo() {
        return metodo;
    }
    
    public void setMetodo(int metodo) {
        this.metodo = metodo;
    }
    
    public String getMetodoNome() {
        return metodoNome;
    }
    
    public void setMetodoNome(String metodoNome) {
        this.metodoNome = metodoNome;
    }
}