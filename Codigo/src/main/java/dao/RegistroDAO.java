package dao;

import model.Registro;
import filterDTO.RegistroFilterDTO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class RegistroDAO extends DAO {
    public RegistroDAO() {
        super();
        conectar();
    }

    public boolean inserir(Registro registro) {
        String query = "INSERT INTO tb_registros (tabela, valor, id_aplicacao) VALUES (?, ?::jsonb, ?)";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setString(1, registro.getTabela());
            stmt.setString(2, registro.getValor() != null ? registro.getValor().toString() : null);
            stmt.setInt(3, registro.getIdAplicacao());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Registro buscarPorId(int id) {
        String query = "SELECT * FROM tb_registros WHERE id = ?";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Registro registro = new Registro();
                registro.setId(rs.getInt("id"));
                registro.setTabela(rs.getString("tabela"));
                registro.setIdAplicacao(rs.getInt("id_aplicacao"));
                
                String valorJson = rs.getString("valor");
                if (valorJson != null) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        registro.setValor(mapper.readTree(valorJson));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                return registro;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Registro> listarTodos() {
        String query = "SELECT * FROM tb_registros";
        List<Registro> registros = new ArrayList<>();
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Registro registro = new Registro();
                registro.setId(rs.getInt("id"));
                registro.setTabela(rs.getString("tabela"));
                registro.setIdAplicacao(rs.getInt("id_aplicacao"));
                
                String valorJson = rs.getString("valor");
                if (valorJson != null) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        registro.setValor(mapper.readTree(valorJson));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                registros.add(registro);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return registros;
    }

    public List<Registro> buscarPorAplicacao(int idAplicacao) {
        String query = "SELECT * FROM tb_registros WHERE id_aplicacao = ?";
        List<Registro> registros = new ArrayList<>();
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setInt(1, idAplicacao);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Registro registro = new Registro();
                registro.setId(rs.getInt("id"));
                registro.setTabela(rs.getString("tabela"));
                registro.setIdAplicacao(rs.getInt("id_aplicacao"));
                
                String valorJson = rs.getString("valor");
                if (valorJson != null) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        registro.setValor(mapper.readTree(valorJson));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                registros.add(registro);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return registros;
    }

    public List<Registro> buscarPorTabela(String tabela) {
        String query = "SELECT * FROM tb_registros WHERE tabela = ?";
        List<Registro> registros = new ArrayList<>();
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setString(1, tabela);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Registro registro = new Registro();
                registro.setId(rs.getInt("id"));
                registro.setTabela(rs.getString("tabela"));
                registro.setIdAplicacao(rs.getInt("id_aplicacao"));
                
                String valorJson = rs.getString("valor");
                if (valorJson != null) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        registro.setValor(mapper.readTree(valorJson));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                registros.add(registro);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return registros;
    }

    public List<Registro> buscarComFiltro(RegistroFilterDTO filtro) {
        StringBuilder query = new StringBuilder("SELECT * FROM tb_registros WHERE 1=1");
        List<Object> parametros = new ArrayList<>();

        if (filtro.getId() != null) {
            query.append(" AND id = ?");
            parametros.add(filtro.getId());
        }
        if (filtro.getTabela() != null && !filtro.getTabela().isEmpty()) {
            query.append(" AND tabela = ?");
            parametros.add(filtro.getTabela());
        }
        if (filtro.getIdAplicacao() != null) {
            query.append(" AND id_aplicacao = ?");
            parametros.add(filtro.getIdAplicacao());
        }

        List<Registro> registros = new ArrayList<>();
        try {
            PreparedStatement stmt = conexao.prepareStatement(query.toString());
            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Registro registro = new Registro();
                registro.setId(rs.getInt("id"));
                registro.setTabela(rs.getString("tabela"));
                registro.setIdAplicacao(rs.getInt("id_aplicacao"));
                
                String valorJson = rs.getString("valor");
                if (valorJson != null) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        registro.setValor(mapper.readTree(valorJson));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                registros.add(registro);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return registros;
    }

    public boolean atualizar(Registro registro) {
        String query = "UPDATE tb_registros SET tabela = ?, valor = ?::jsonb WHERE id = ?";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setString(1, registro.getTabela());
            stmt.setString(2, registro.getValor() != null ? registro.getValor().toString() : null);
            stmt.setInt(3, registro.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean excluir(int id) {
        String query = "DELETE FROM tb_registros WHERE id = ?";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean excluirPorAplicacao(int idAplicacao) {
        String query = "DELETE FROM tb_registros WHERE id_aplicacao = ?";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setInt(1, idAplicacao);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public int contarPorAplicacao(int idAplicacao) {
        String query = "SELECT COUNT(*) FROM tb_registros WHERE id_aplicacao = ?";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setInt(1, idAplicacao);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}