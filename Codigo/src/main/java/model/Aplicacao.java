package model;

import com.fasterxml.jackson.databind.JsonNode;

public class Aplicacao {
    private int id;
    private String nome;
    private String readme;
    private int idUsuario;
    private String nomeBanco;
    private JsonNode schemaBanco;
    private int idKeyBase;
    private String codigoKeyBase;

    public Aplicacao() {}

    public Aplicacao(int id, String nome, String readme, int idUsuario, String nomeBanco, JsonNode schemaBanco) {
        this.id = id;
        this.nome = nome;
        this.readme = readme;
        this.idUsuario = idUsuario;
        this.nomeBanco = nomeBanco;
        this.schemaBanco = schemaBanco;
    }

    public Aplicacao(String nome, String readme, int idUsuario, String nomeBanco, JsonNode schemaBanco) {
        this.nome = nome;
        this.readme = readme;
        this.idUsuario = idUsuario;
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

    public int getIdKeyBase() {
        return idKeyBase;
    }

    public void setIdKeyBase(int idKeyBase) {
        this.idKeyBase = idKeyBase;
    }

    public String getCodigoKeyBase() {
        return codigoKeyBase;
    }

    public void setCodigoKeyBase(String codigoKeyBase) {
        this.codigoKeyBase = codigoKeyBase;
    }

    @Override
    public String toString() {
        return "Aplicacao [id=" + id + ", nome=" + nome + ", idUsuario=" + idUsuario + ", nomeBanco=" + nomeBanco + ", idKeyBase=" + idKeyBase + "]";
    }
}