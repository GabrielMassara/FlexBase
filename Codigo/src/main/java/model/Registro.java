package model;

import com.fasterxml.jackson.databind.JsonNode;

public class Registro {
    private int id;
    private String tabela;
    private JsonNode valor;
    private int idAplicacao;

    public Registro() {}

    public Registro(int id, String tabela, JsonNode valor, int idAplicacao) {
        this.id = id;
        this.tabela = tabela;
        this.valor = valor;
        this.idAplicacao = idAplicacao;
    }

    public Registro(String tabela, JsonNode valor, int idAplicacao) {
        this.tabela = tabela;
        this.valor = valor;
        this.idAplicacao = idAplicacao;
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

    @Override
    public String toString() {
        return "Registro [id=" + id + ", tabela=" + tabela + ", idAplicacao=" + idAplicacao + "]";
    }
}