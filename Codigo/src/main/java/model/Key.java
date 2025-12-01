package model;

import java.time.LocalDateTime;

public class Key {
    private int id;
    private String codigo;
    private int idAplicacao;
    private String nome;
    private String descricao;
    private boolean ativo;
    private LocalDateTime dataCriacao;

    public Key() {}

    public Key(int id, String codigo, int idAplicacao, String nome, String descricao, boolean ativo, LocalDateTime dataCriacao) {
        this.id = id;
        this.codigo = codigo;
        this.idAplicacao = idAplicacao;
        this.nome = nome;
        this.descricao = descricao;
        this.ativo = ativo;
        this.dataCriacao = dataCriacao;
    }

    public Key(String codigo, int idAplicacao, String nome, String descricao, boolean ativo) {
        this.codigo = codigo;
        this.idAplicacao = idAplicacao;
        this.nome = nome;
        this.descricao = descricao;
        this.ativo = ativo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public int getIdAplicacao() {
        return idAplicacao;
    }

    public void setIdAplicacao(int idAplicacao) {
        this.idAplicacao = idAplicacao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    @Override
    public String toString() {
        return "Key [id=" + id + ", codigo=" + codigo + ", idAplicacao=" + idAplicacao + ", nome=" + nome + ", ativo=" + ativo + "]";
    }
}