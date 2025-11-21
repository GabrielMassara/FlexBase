package model;

public class Endpoint {
    private int id;
    private int idAplicacao;
    private String rota;
    private String query;
    private int metodo;

    public Endpoint() {}

    public Endpoint(int id, int idAplicacao, String rota, String query, int metodo) {
        this.id = id;
        this.idAplicacao = idAplicacao;
        this.rota = rota;
        this.query = query;
        this.metodo = metodo;
    }

    public Endpoint(int idAplicacao, String rota, String query, int metodo) {
        this.idAplicacao = idAplicacao;
        this.rota = rota;
        this.query = query;
        this.metodo = metodo;
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

    @Override
    public String toString() {
        return "Endpoint [id=" + id + ", idAplicacao=" + idAplicacao + ", rota=" + rota + ", metodo=" + metodo + "]";
    }
}