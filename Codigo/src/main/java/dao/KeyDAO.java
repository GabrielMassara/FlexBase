package dao;

import model.Key;
import filterDTO.KeyFilterDTO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;

public class KeyDAO extends DAO {
    public KeyDAO() {
        super();
        conectar();
    }

    public boolean inserir(Key key) {
        String query = "INSERT INTO tb_keys (codigo, id_aplicacao, nome, descricao, ativo) VALUES (?, ?, ?, ?, ?) RETURNING id";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setString(1, key.getCodigo());
            stmt.setInt(2, key.getIdAplicacao());
            stmt.setString(3, key.getNome());
            stmt.setString(4, key.getDescricao());
            stmt.setBoolean(5, key.isAtivo());
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                key.setId(rs.getInt("id"));
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Key buscarPorId(int id) {
        String query = "SELECT * FROM tb_keys WHERE id = ?";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Key key = new Key();
                key.setId(rs.getInt("id"));
                key.setCodigo(rs.getString("codigo"));
                key.setIdAplicacao(rs.getInt("id_aplicacao"));
                key.setNome(rs.getString("nome"));
                key.setDescricao(rs.getString("descricao"));
                key.setAtivo(rs.getBoolean("ativo"));
                
                Timestamp timestamp = rs.getTimestamp("data_criacao");
                if (timestamp != null) {
                    key.setDataCriacao(timestamp.toLocalDateTime());
                }
                
                return key;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Key buscarPorCodigo(String codigo) {
        String query = "SELECT * FROM tb_keys WHERE codigo = ?";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setString(1, codigo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Key key = new Key();
                key.setId(rs.getInt("id"));
                key.setCodigo(rs.getString("codigo"));
                key.setIdAplicacao(rs.getInt("id_aplicacao"));
                key.setNome(rs.getString("nome"));
                key.setDescricao(rs.getString("descricao"));
                key.setAtivo(rs.getBoolean("ativo"));
                
                Timestamp timestamp = rs.getTimestamp("data_criacao");
                if (timestamp != null) {
                    key.setDataCriacao(timestamp.toLocalDateTime());
                }
                
                return key;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Key> buscarPorAplicacao(int idAplicacao) {
        String query = "SELECT * FROM tb_keys WHERE id_aplicacao = ? ORDER BY data_criacao DESC";
        List<Key> keys = new ArrayList<>();
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setInt(1, idAplicacao);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Key key = new Key();
                key.setId(rs.getInt("id"));
                key.setCodigo(rs.getString("codigo"));
                key.setIdAplicacao(rs.getInt("id_aplicacao"));
                key.setNome(rs.getString("nome"));
                key.setDescricao(rs.getString("descricao"));
                key.setAtivo(rs.getBoolean("ativo"));
                
                Timestamp timestamp = rs.getTimestamp("data_criacao");
                if (timestamp != null) {
                    key.setDataCriacao(timestamp.toLocalDateTime());
                }
                
                keys.add(key);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return keys;
    }

    public List<Key> buscarComFiltro(KeyFilterDTO filtro) {
        StringBuilder query = new StringBuilder("SELECT * FROM tb_keys WHERE 1=1");
        List<Object> parametros = new ArrayList<>();

        if (filtro.getId() != null) {
            query.append(" AND id = ?");
            parametros.add(filtro.getId());
        }
        if (filtro.getCodigo() != null && !filtro.getCodigo().isEmpty()) {
            query.append(" AND codigo ILIKE ?");
            parametros.add("%" + filtro.getCodigo() + "%");
        }
        if (filtro.getIdAplicacao() != null) {
            query.append(" AND id_aplicacao = ?");
            parametros.add(filtro.getIdAplicacao());
        }
        if (filtro.getNome() != null && !filtro.getNome().isEmpty()) {
            query.append(" AND nome ILIKE ?");
            parametros.add("%" + filtro.getNome() + "%");
        }
        if (filtro.getAtivo() != null) {
            query.append(" AND ativo = ?");
            parametros.add(filtro.getAtivo());
        }

        query.append(" ORDER BY data_criacao DESC");

        List<Key> keys = new ArrayList<>();
        try {
            PreparedStatement stmt = conexao.prepareStatement(query.toString());
            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Key key = new Key();
                key.setId(rs.getInt("id"));
                key.setCodigo(rs.getString("codigo"));
                key.setIdAplicacao(rs.getInt("id_aplicacao"));
                key.setNome(rs.getString("nome"));
                key.setDescricao(rs.getString("descricao"));
                key.setAtivo(rs.getBoolean("ativo"));
                
                Timestamp timestamp = rs.getTimestamp("data_criacao");
                if (timestamp != null) {
                    key.setDataCriacao(timestamp.toLocalDateTime());
                }
                
                keys.add(key);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return keys;
    }

    public boolean atualizar(Key key) {
        String query = "UPDATE tb_keys SET nome = ?, descricao = ?, ativo = ? WHERE id = ?";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setString(1, key.getNome());
            stmt.setString(2, key.getDescricao());
            stmt.setBoolean(3, key.isAtivo());
            stmt.setInt(4, key.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean excluir(int id) {
        String query = "DELETE FROM tb_keys WHERE id = ?";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Integer> buscarEndpointsAssociados(int idKey) {
        String query = "SELECT id_endpoint FROM tb_key_endpoint WHERE id_key = ?";
        List<Integer> endpoints = new ArrayList<>();
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setInt(1, idKey);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                endpoints.add(rs.getInt("id_endpoint"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return endpoints;
    }

    public boolean associarEndpoint(int idKey, int idEndpoint) {
        String query = "INSERT INTO tb_key_endpoint (id_key, id_endpoint) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setInt(1, idKey);
            stmt.setInt(2, idEndpoint);
            return stmt.executeUpdate() >= 0; // ON CONFLICT pode retornar 0
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean desassociarEndpoint(int idKey, int idEndpoint) {
        String query = "DELETE FROM tb_key_endpoint WHERE id_key = ? AND id_endpoint = ?";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setInt(1, idKey);
            stmt.setInt(2, idEndpoint);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean atualizarEndpointsAssociados(int idKey, List<Integer> endpointsIds) {
        try {
            // Primeiro remove todas as associações existentes
            String deleteQuery = "DELETE FROM tb_key_endpoint WHERE id_key = ?";
            PreparedStatement deleteStmt = conexao.prepareStatement(deleteQuery);
            deleteStmt.setInt(1, idKey);
            deleteStmt.executeUpdate();

            // Depois adiciona as novas associações
            if (endpointsIds != null && !endpointsIds.isEmpty()) {
                String insertQuery = "INSERT INTO tb_key_endpoint (id_key, id_endpoint) VALUES (?, ?)";
                PreparedStatement insertStmt = conexao.prepareStatement(insertQuery);
                
                for (Integer endpointId : endpointsIds) {
                    insertStmt.setInt(1, idKey);
                    insertStmt.setInt(2, endpointId);
                    insertStmt.addBatch();
                }
                
                insertStmt.executeBatch();
            }
            
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}