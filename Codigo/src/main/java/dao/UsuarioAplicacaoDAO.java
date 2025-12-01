package dao;

import model.UsuarioAplicacao;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioAplicacaoDAO extends DAO {
    private ObjectMapper objectMapper = new ObjectMapper();
    
    public UsuarioAplicacaoDAO() {
        super();
        conectar();
    }

    public UsuarioAplicacao buscarPorUsuarioEAplicacao(Integer idUsuario, Integer idAplicacao) {
        String sql = "SELECT * FROM tb_usuario_aplicacao WHERE id_usuario = ? AND id_aplicacao = ?";
        
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idAplicacao);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUsuarioAplicacao(rs);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }

    public UsuarioAplicacao inserir(UsuarioAplicacao usuarioAplicacao) {
        String sql = "INSERT INTO tb_usuario_aplicacao (id_usuario, id_aplicacao, id_key, dados_usuario) " +
                     "VALUES (?, ?, ?, ?::jsonb) RETURNING *";
        
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            
            stmt.setInt(1, usuarioAplicacao.getIdUsuario());
            stmt.setInt(2, usuarioAplicacao.getIdAplicacao());
            stmt.setInt(3, usuarioAplicacao.getIdKey());
            
            if (usuarioAplicacao.getDadosUsuario() != null) {
                stmt.setString(4, usuarioAplicacao.getDadosUsuario().toString());
            } else {
                stmt.setNull(4, Types.OTHER);
            }
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUsuarioAplicacao(rs);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }

    public List<UsuarioAplicacao> listarPorUsuario(Integer idUsuario) {
        String sql = "SELECT * FROM tb_usuario_aplicacao WHERE id_usuario = ? AND ativo = true";
        List<UsuarioAplicacao> lista = new ArrayList<>();
        
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsuario);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                lista.add(mapResultSetToUsuarioAplicacao(rs));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return lista;
    }

    public List<UsuarioAplicacao> listarPorAplicacao(Integer idAplicacao) {
        String sql = "SELECT * FROM tb_usuario_aplicacao WHERE id_aplicacao = ? AND ativo = true";
        List<UsuarioAplicacao> lista = new ArrayList<>();
        
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            
            stmt.setInt(1, idAplicacao);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                lista.add(mapResultSetToUsuarioAplicacao(rs));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return lista;
    }

    public boolean atualizarDados(Integer id, JsonNode dadosUsuario) {
        String sql = "UPDATE tb_usuario_aplicacao SET dados_usuario = ?::jsonb WHERE id = ?";
        
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            
            if (dadosUsuario != null) {
                stmt.setString(1, dadosUsuario.toString());
            } else {
                stmt.setNull(1, Types.OTHER);
            }
            stmt.setInt(2, id);
            
            return stmt.executeUpdate() > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }

    public boolean desativar(Integer id) {
        String sql = "UPDATE tb_usuario_aplicacao SET ativo = false WHERE id = ?";
        
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            return stmt.executeUpdate() > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }

    private UsuarioAplicacao mapResultSetToUsuarioAplicacao(ResultSet rs) throws Exception {
        UsuarioAplicacao usuarioAplicacao = new UsuarioAplicacao();
        
        usuarioAplicacao.setId(rs.getInt("id"));
        usuarioAplicacao.setIdUsuario(rs.getInt("id_usuario"));
        usuarioAplicacao.setIdAplicacao(rs.getInt("id_aplicacao"));
        usuarioAplicacao.setIdKey(rs.getInt("id_key"));
        usuarioAplicacao.setAtivo(rs.getBoolean("ativo"));
        
        // Convert timestamp to string
        Timestamp dataVinculo = rs.getTimestamp("data_vinculo");
        if (dataVinculo != null) {
            usuarioAplicacao.setDataVinculo(dataVinculo.toLocalDateTime().toString());
        }
        
        // Parse JSONB dados_usuario
        String dadosUsuarioJson = rs.getString("dados_usuario");
        if (dadosUsuarioJson != null && !dadosUsuarioJson.trim().isEmpty()) {
            try {
                JsonNode dadosUsuario = objectMapper.readTree(dadosUsuarioJson);
                usuarioAplicacao.setDadosUsuario(dadosUsuario);
            } catch (Exception e) {
                // Se não conseguir fazer parse, deixa como null
                usuarioAplicacao.setDadosUsuario(null);
            }
        }
        
        return usuarioAplicacao;
    }
}