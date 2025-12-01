package dao;

import model.Aplicacao;
import filterDTO.AplicacaoFilterDTO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class AplicacaoDAO extends DAO {
    public AplicacaoDAO() {
        super();
        conectar();
    }

    public boolean inserir(Aplicacao aplicacao) {
        String query = "SELECT * FROM fn_create_aplicacao_with_base_key(?, ?, ?, ?, ?::jsonb)";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setString(1, aplicacao.getNome());
            stmt.setString(2, aplicacao.getReadme());
            stmt.setInt(3, aplicacao.getIdUsuario());
            stmt.setString(4, aplicacao.getNomeBanco());
            stmt.setString(5, aplicacao.getSchemaBanco() != null ? aplicacao.getSchemaBanco().toString() : null);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                aplicacao.setId(rs.getInt("id_aplicacao"));
                aplicacao.setIdKeyBase(rs.getInt("id_key_base"));
                aplicacao.setCodigoKeyBase(rs.getString("codigo_key"));
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Aplicacao buscarPorId(int id) {
        String query = "SELECT a.*, k.codigo as codigo_key_base FROM tb_aplicacao a " +
                      "LEFT JOIN tb_keys k ON a.id_key_base = k.id WHERE a.id = ?";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Aplicacao aplicacao = new Aplicacao();
                aplicacao.setId(rs.getInt("id"));
                aplicacao.setNome(rs.getString("nome"));
                aplicacao.setReadme(rs.getString("readme"));
                aplicacao.setIdUsuario(rs.getInt("id_usuario"));
                aplicacao.setNomeBanco(rs.getString("nome_banco"));
                aplicacao.setIdKeyBase(rs.getInt("id_key_base"));
                aplicacao.setCodigoKeyBase(rs.getString("codigo_key_base"));
                
                String schemaJson = rs.getString("schema_banco");
                if (schemaJson != null) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        aplicacao.setSchemaBanco(mapper.readTree(schemaJson));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                return aplicacao;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Aplicacao> listarTodos() {
        String query = "SELECT a.*, k.codigo as codigo_key_base FROM tb_aplicacao a " +
                      "LEFT JOIN tb_keys k ON a.id_key_base = k.id";
        List<Aplicacao> aplicacoes = new ArrayList<>();
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Aplicacao aplicacao = new Aplicacao();
                aplicacao.setId(rs.getInt("id"));
                aplicacao.setNome(rs.getString("nome"));
                aplicacao.setReadme(rs.getString("readme"));
                aplicacao.setIdUsuario(rs.getInt("id_usuario"));
                aplicacao.setNomeBanco(rs.getString("nome_banco"));
                aplicacao.setIdKeyBase(rs.getInt("id_key_base"));
                aplicacao.setCodigoKeyBase(rs.getString("codigo_key_base"));
                
                String schemaJson = rs.getString("schema_banco");
                if (schemaJson != null) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        aplicacao.setSchemaBanco(mapper.readTree(schemaJson));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                aplicacoes.add(aplicacao);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return aplicacoes;
    }

    public List<Aplicacao> buscarPorUsuario(int idUsuario) {
        String query = "SELECT a.*, k.codigo as codigo_key_base FROM tb_aplicacao a " +
                      "LEFT JOIN tb_keys k ON a.id_key_base = k.id WHERE a.id_usuario = ?";
        List<Aplicacao> aplicacoes = new ArrayList<>();
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Aplicacao aplicacao = new Aplicacao();
                aplicacao.setId(rs.getInt("id"));
                aplicacao.setNome(rs.getString("nome"));
                aplicacao.setReadme(rs.getString("readme"));
                aplicacao.setIdUsuario(rs.getInt("id_usuario"));
                aplicacao.setNomeBanco(rs.getString("nome_banco"));
                aplicacao.setIdKeyBase(rs.getInt("id_key_base"));
                aplicacao.setCodigoKeyBase(rs.getString("codigo_key_base"));
                
                String schemaJson = rs.getString("schema_banco");
                if (schemaJson != null) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        aplicacao.setSchemaBanco(mapper.readTree(schemaJson));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                aplicacoes.add(aplicacao);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return aplicacoes;
    }

    public List<Aplicacao> buscarComFiltro(AplicacaoFilterDTO filtro) {
        StringBuilder query = new StringBuilder("SELECT a.*, k.codigo as codigo_key_base FROM tb_aplicacao a " +
                                              "LEFT JOIN tb_keys k ON a.id_key_base = k.id WHERE 1=1");
        List<Object> parametros = new ArrayList<>();

        if (filtro.getId() != null) {
            query.append(" AND a.id = ?");
            parametros.add(filtro.getId());
        }
        if (filtro.getNome() != null && !filtro.getNome().isEmpty()) {
            query.append(" AND a.nome ILIKE ?");
            parametros.add("%" + filtro.getNome() + "%");
        }
        if (filtro.getIdUsuario() != null) {
            query.append(" AND a.id_usuario = ?");
            parametros.add(filtro.getIdUsuario());
        }
        if (filtro.getNomeBanco() != null && !filtro.getNomeBanco().isEmpty()) {
            query.append(" AND a.nome_banco ILIKE ?");
            parametros.add("%" + filtro.getNomeBanco() + "%");
        }

        List<Aplicacao> aplicacoes = new ArrayList<>();
        try {
            PreparedStatement stmt = conexao.prepareStatement(query.toString());
            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Aplicacao aplicacao = new Aplicacao();
                aplicacao.setId(rs.getInt("id"));
                aplicacao.setNome(rs.getString("nome"));
                aplicacao.setReadme(rs.getString("readme"));
                aplicacao.setIdUsuario(rs.getInt("id_usuario"));
                aplicacao.setNomeBanco(rs.getString("nome_banco"));
                aplicacao.setIdKeyBase(rs.getInt("id_key_base"));
                aplicacao.setCodigoKeyBase(rs.getString("codigo_key_base"));
                
                String schemaJson = rs.getString("schema_banco");
                if (schemaJson != null) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        aplicacao.setSchemaBanco(mapper.readTree(schemaJson));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                aplicacoes.add(aplicacao);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return aplicacoes;
    }

    public boolean atualizar(Aplicacao aplicacao) {
        String query = "UPDATE tb_aplicacao SET nome = ?, readme = ?, nome_banco = ?, schema_banco = ?::jsonb WHERE id = ?";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setString(1, aplicacao.getNome());
            stmt.setString(2, aplicacao.getReadme());
            stmt.setString(3, aplicacao.getNomeBanco());
            stmt.setString(4, aplicacao.getSchemaBanco() != null ? aplicacao.getSchemaBanco().toString() : null);
            stmt.setInt(5, aplicacao.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean excluir(int id) {
        String query = "DELETE FROM tb_aplicacao WHERE id = ?";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}