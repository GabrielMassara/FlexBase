package dao;

import model.Usuario;
import filterDTO.UsuarioFilterDTO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class UsuarioDAO extends DAO {
    public UsuarioDAO() {
        super();
        conectar();
    }

    public boolean inserir(Usuario usuario) throws Exception {
        String query = "INSERT INTO tb_usuarios (nome, sobrenome, email, senha) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getSobrenome());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, toMD5(usuario.getSenha()));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Usuario buscarPorId(int id) {
        String query = "SELECT * FROM tb_usuarios WHERE id = ?";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Usuario(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("sobrenome"),
                    rs.getString("email"),
                    rs.getString("senha")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Usuario buscarPorEmail(String email) {
        String query = "SELECT * FROM tb_usuarios WHERE email = ?";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Usuario(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("sobrenome"),
                    rs.getString("email"),
                    rs.getString("senha")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Usuario> listarTodos() {
        String query = "SELECT * FROM tb_usuarios";
        List<Usuario> usuarios = new ArrayList<>();
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                usuarios.add(new Usuario(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("sobrenome"),
                    rs.getString("email"),
                    rs.getString("senha")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usuarios;
    }

    public List<Usuario> buscarComFiltro(UsuarioFilterDTO filtro) {
        StringBuilder query = new StringBuilder("SELECT * FROM tb_usuarios WHERE 1=1");
        List<Object> parametros = new ArrayList<>();

        if (filtro.getId() != null) {
            query.append(" AND id = ?");
            parametros.add(filtro.getId());
        }
        if (filtro.getNome() != null && !filtro.getNome().isEmpty()) {
            query.append(" AND (nome ILIKE ? OR sobrenome ILIKE ?)");
            parametros.add("%" + filtro.getNome() + "%");
            parametros.add("%" + filtro.getNome() + "%");
        }
        if (filtro.getEmail() != null && !filtro.getEmail().isEmpty()) {
            query.append(" AND email ILIKE ?");
            parametros.add("%" + filtro.getEmail() + "%");
        }

        List<Usuario> usuarios = new ArrayList<>();
        try {
            PreparedStatement stmt = conexao.prepareStatement(query.toString());
            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                usuarios.add(new Usuario(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("sobrenome"),
                    rs.getString("email"),
                    rs.getString("senha")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usuarios;
    }

    public boolean atualizar(Usuario usuario) throws Exception {
        String query = "UPDATE tb_usuarios SET nome = ?, sobrenome = ?, email = ?" +
                      (usuario.getSenha() != null ? ", senha = ?" : "") + 
                      " WHERE id = ?";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getSobrenome());
            stmt.setString(3, usuario.getEmail());
            
            if (usuario.getSenha() != null) {
                stmt.setString(4, toMD5(usuario.getSenha()));
                stmt.setInt(5, usuario.getId());
            } else {
                stmt.setInt(4, usuario.getId());
            }
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean excluir(int id) {
        String query = "DELETE FROM tb_usuarios WHERE id = ?";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Usuario autenticar(String email, String senha) throws Exception {
        String query = "SELECT * FROM tb_usuarios WHERE email = ? AND senha = ?";
        try {
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, toMD5(senha));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Usuario(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("sobrenome"),
                    rs.getString("email"),
                    rs.getString("senha")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}