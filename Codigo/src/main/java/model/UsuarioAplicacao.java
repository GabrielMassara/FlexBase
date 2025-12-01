package model;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

public class UsuarioAplicacao {
    private Integer id;
    private Integer idUsuario;
    private Integer idAplicacao;
    private Integer idKey;
    private JsonNode dadosUsuario;
    private String dataVinculo;
    private Boolean ativo;

    // Constructors
    public UsuarioAplicacao() {}

    public UsuarioAplicacao(Integer idUsuario, Integer idAplicacao, Integer idKey) {
        this.idUsuario = idUsuario;
        this.idAplicacao = idAplicacao;
        this.idKey = idKey;
        this.ativo = true;
    }

    public UsuarioAplicacao(Integer idUsuario, Integer idAplicacao, Integer idKey, JsonNode dadosUsuario) {
        this.idUsuario = idUsuario;
        this.idAplicacao = idAplicacao;
        this.idKey = idKey;
        this.dadosUsuario = dadosUsuario;
        this.ativo = true;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdAplicacao() {
        return idAplicacao;
    }

    public void setIdAplicacao(Integer idAplicacao) {
        this.idAplicacao = idAplicacao;
    }

    public Integer getIdKey() {
        return idKey;
    }

    public void setIdKey(Integer idKey) {
        this.idKey = idKey;
    }

    public JsonNode getDadosUsuario() {
        return dadosUsuario;
    }

    public void setDadosUsuario(JsonNode dadosUsuario) {
        this.dadosUsuario = dadosUsuario;
    }

    public String getDataVinculo() {
        return dataVinculo;
    }

    public void setDataVinculo(String dataVinculo) {
        this.dataVinculo = dataVinculo;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}