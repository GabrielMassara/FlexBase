package dao;

import model.Endpoint;
import filterDTO.EndpointFilterDTO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class EndpointDAO extends DAO {
    public EndpointDAO() {
        super();
        conectar();
    }

    public boolean inserir(Endpoint endpoint) {
        String query = "INSERT INTO tb_endpoints (id_aplicacao, rota, query, metodo) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setInt(1, endpoint.getIdAplicacao());
            stmt.setString(2, endpoint.getRota());
            stmt.setString(3, endpoint.getQuery());
            stmt.setInt(4, endpoint.getMetodo());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Endpoint buscarPorId(int id) {
        String query = "SELECT * FROM tb_endpoints WHERE id = ?";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Endpoint(
                    rs.getInt("id"),
                    rs.getInt("id_aplicacao"),
                    rs.getString("rota"),
                    rs.getString("query"),
                    rs.getInt("metodo")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Endpoint> listarTodos() {
        String query = "SELECT * FROM tb_endpoints";
        List<Endpoint> endpoints = new ArrayList<>();
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                endpoints.add(new Endpoint(
                    rs.getInt("id"),
                    rs.getInt("id_aplicacao"),
                    rs.getString("rota"),
                    rs.getString("query"),
                    rs.getInt("metodo")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return endpoints;
    }

    public List<Endpoint> buscarPorAplicacao(int idAplicacao) {
        String query = "SELECT * FROM tb_endpoints WHERE id_aplicacao = ?";
        List<Endpoint> endpoints = new ArrayList<>();
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setInt(1, idAplicacao);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                endpoints.add(new Endpoint(
                    rs.getInt("id"),
                    rs.getInt("id_aplicacao"),
                    rs.getString("rota"),
                    rs.getString("query"),
                    rs.getInt("metodo")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return endpoints;
    }

    public List<Endpoint> buscarComFiltro(EndpointFilterDTO filtro) {
        StringBuilder query = new StringBuilder("SELECT * FROM tb_endpoints WHERE 1=1");
        List<Object> parametros = new ArrayList<>();

        if (filtro.getId() != null) {
            query.append(" AND id = ?");
            parametros.add(filtro.getId());
        }
        if (filtro.getIdAplicacao() != null) {
            query.append(" AND id_aplicacao = ?");
            parametros.add(filtro.getIdAplicacao());
        }
        if (filtro.getRota() != null && !filtro.getRota().isEmpty()) {
            query.append(" AND rota ILIKE ?");
            parametros.add("%" + filtro.getRota() + "%");
        }
        if (filtro.getMetodo() != null) {
            query.append(" AND metodo = ?");
            parametros.add(filtro.getMetodo());
        }

        List<Endpoint> endpoints = new ArrayList<>();
        try {
            PreparedStatement stmt = conexao.prepareStatement(query.toString());
            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                endpoints.add(new Endpoint(
                    rs.getInt("id"),
                    rs.getInt("id_aplicacao"),
                    rs.getString("rota"),
                    rs.getString("query"),
                    rs.getInt("metodo")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return endpoints;
    }

    public boolean atualizar(Endpoint endpoint) {
        String query = "UPDATE tb_endpoints SET rota = ?, query = ?, metodo = ? WHERE id = ?";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setString(1, endpoint.getRota());
            stmt.setString(2, endpoint.getQuery());
            stmt.setInt(3, endpoint.getMetodo());
            stmt.setInt(4, endpoint.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean excluir(int id) {
        String query = "DELETE FROM tb_endpoints WHERE id = ?";
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
        String query = "DELETE FROM tb_endpoints WHERE id_aplicacao = ?";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setInt(1, idAplicacao);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}