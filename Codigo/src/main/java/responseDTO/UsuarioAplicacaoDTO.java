package responseDTO;

import com.fasterxml.jackson.databind.JsonNode;

public class UsuarioAplicacaoDTO {
    private Integer id;
    private Integer idUsuario;
    private Integer idAplicacao;
    private Integer idKey;
    private JsonNode dadosUsuario;
    private String dataVinculo;
    private Boolean ativo;
    private String nomeUsuario;
    private String emailUsuario;
    private String nomeAplicacao;
    private String codigoKey;

    // Constructors
    public UsuarioAplicacaoDTO() {}

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

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getEmailUsuario() {
        return emailUsuario;
    }

    public void setEmailUsuario(String emailUsuario) {
        this.emailUsuario = emailUsuario;
    }

    public String getNomeAplicacao() {
        return nomeAplicacao;
    }

    public void setNomeAplicacao(String nomeAplicacao) {
        this.nomeAplicacao = nomeAplicacao;
    }

    public String getCodigoKey() {
        return codigoKey;
    }

    public void setCodigoKey(String codigoKey) {
        this.codigoKey = codigoKey;
    }
}