package responseDTO;

import java.util.List;

public class KeyDTO {
    private int id;
    private String codigo;
    private int idAplicacao;
    private String nomeAplicacao;
    private String nome;
    private String descricao;
    private boolean ativo;
    private String dataCriacao;
    private List<Integer> endpointsAssociados;

    public KeyDTO() {}

    public KeyDTO(int id, String codigo, int idAplicacao, String nomeAplicacao, String nome, String descricao, boolean ativo, String dataCriacao, List<Integer> endpointsAssociados) {
        this.id = id;
        this.codigo = codigo;
        this.idAplicacao = idAplicacao;
        this.nomeAplicacao = nomeAplicacao;
        this.nome = nome;
        this.descricao = descricao;
        this.ativo = ativo;
        this.dataCriacao = dataCriacao;
        this.endpointsAssociados = endpointsAssociados;
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

    public String getNomeAplicacao() {
        return nomeAplicacao;
    }

    public void setNomeAplicacao(String nomeAplicacao) {
        this.nomeAplicacao = nomeAplicacao;
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

    public String getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(String dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public List<Integer> getEndpointsAssociados() {
        return endpointsAssociados;
    }

    public void setEndpointsAssociados(List<Integer> endpointsAssociados) {
        this.endpointsAssociados = endpointsAssociados;
    }
}