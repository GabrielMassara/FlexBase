package service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

import dao.AplicacaoDAO;
import dao.EndpointDAO;
import model.Aplicacao;
import model.Endpoint;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;

public class GeradorEndpointsService {
    
    public Object generateEndpoints(Request request, Response response) {
        response.type("application/json");
        JsonMapper mapper = JsonMapper.builder().build();
        
        try {
            // Obter informações do usuário logado através do token de autenticação
            Integer idUsuario = request.attribute("userId");
            
            if (idUsuario == null) {
                response.status(401);
                return criarRespostaErro(mapper, "Token de autenticação inválido");
            }
            
            // Obter ID da aplicação da URL
            int idAplicacao;
            try {
                idAplicacao = Integer.parseInt(request.params(":idAplicacao"));
            } catch (NumberFormatException e) {
                response.status(400);
                return criarRespostaErro(mapper, "ID da aplicação inválido");
            }
            
            // Verificar se a aplicação existe e pertence ao usuário
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            Aplicacao aplicacao = aplicacaoDAO.buscarPorId(idAplicacao);
            
            if (aplicacao == null) {
                response.status(404);
                return criarRespostaErro(mapper, "Aplicação não encontrada");
            }
            
            if (aplicacao.getIdUsuario() != idUsuario.intValue()) {
                response.status(403);
                return criarRespostaErro(mapper, "Você não tem permissão para modificar esta aplicação");
            }
            
            // Parse do JSON de entrada
            JsonNode estruturaBanco = null;
            try {
                estruturaBanco = mapper.readTree(request.body());
            } catch (JsonProcessingException e) {
                response.status(400);
                return criarRespostaErro(mapper, "JSON inválido");
            }
            
            JsonNode banco = estruturaBanco.get("banco");
            if (banco == null) {
                response.status(400);
                return criarRespostaErro(mapper, "Estrutura 'banco' não encontrada");
            }
            
            JsonNode tabelas = banco.get("tabelas");
            
            if (tabelas == null || !tabelas.isArray()) {
                response.status(400);
                return criarRespostaErro(mapper, "Tabelas não encontradas ou formato inválido");
            }
            
            // Atualizar o schema da aplicação
            aplicacao.setSchemaBanco(banco);
            if (!aplicacaoDAO.atualizar(aplicacao)) {
                response.status(500);
                return criarRespostaErro(mapper, "Erro ao atualizar aplicação");
            }
            
            EndpointDAO endpointDAO = new EndpointDAO();
            int endpointsCriados = 0;
            
            // Gerar endpoints CRUD para cada tabela
            for (JsonNode tabela : tabelas) {
                String nomeTabela = tabela.get("nome").asText();
                JsonNode campos = tabela.get("campos");
                
                // Criar endpoints CRUD
                endpointsCriados += criarEndpointsCRUD(endpointDAO, aplicacao.getId(), nomeTabela, campos);
            }
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("success", true);
            resultado.put("message", "Endpoints gerados com sucesso");
            resultado.put("aplicacao_id", aplicacao.getId());
            resultado.put("aplicacao_nome", aplicacao.getNome());
            resultado.put("endpoints_criados", endpointsCriados);
            resultado.put("total_tabelas", tabelas.size());
            
            return mapper.writeValueAsString(resultado);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return criarRespostaErro(mapper, "Erro interno do servidor: " + e.getMessage());
        }
    }
    
    private int criarEndpointsCRUD(EndpointDAO endpointDAO, int idAplicacao, String nomeTabela, JsonNode campos) {
        int count = 0;
        
        // 1. CREATE (POST) - Inserir registro
        String createQuery = gerarQueryInsert(nomeTabela, campos);
        Endpoint createEndpoint = new Endpoint(idAplicacao, "/" + nomeTabela, createQuery, 2); // POST = 2
        if (endpointDAO.inserir(createEndpoint)) count++;
        
        // 2. READ ALL (GET) - Listar todos os registros
        String readAllQuery = gerarQuerySelectAll(nomeTabela);
        Endpoint readAllEndpoint = new Endpoint(idAplicacao, "/" + nomeTabela, readAllQuery, 1); // GET = 1
        if (endpointDAO.inserir(readAllEndpoint)) count++;
        
        // 3. READ BY ID (GET) - Buscar por ID
        String readByIdQuery = gerarQuerySelectById(nomeTabela, campos);
        Endpoint readByIdEndpoint = new Endpoint(idAplicacao, "/" + nomeTabela + "/{id}", readByIdQuery, 1); // GET = 1
        if (endpointDAO.inserir(readByIdEndpoint)) count++;
        
        // 4. UPDATE (PUT) - Atualizar registro
        String updateQuery = gerarQueryUpdate(nomeTabela, campos);
        Endpoint updateEndpoint = new Endpoint(idAplicacao, "/" + nomeTabela + "/{id}", updateQuery, 3); // PUT = 3
        if (endpointDAO.inserir(updateEndpoint)) count++;
        
        // 5. DELETE (DELETE) - Excluir registro
        String deleteQuery = gerarQueryDelete(nomeTabela, campos);
        Endpoint deleteEndpoint = new Endpoint(idAplicacao, "/" + nomeTabela + "/{id}", deleteQuery, 4); // DELETE = 4
        if (endpointDAO.inserir(deleteEndpoint)) count++;
        
        return count;
    }
    
    private String gerarQueryInsert(String nomeTabela, JsonNode campos) {
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO tb_registros (tabela, valor, id_aplicacao) VALUES ('");
        query.append(nomeTabela);
        query.append("', jsonb_build_object(");
        
        boolean primeiro = true;
        for (JsonNode campo : campos) {
            if (!primeiro) query.append(", ");
            
            String nomeCampo = campo.get("nome").asText();
            String tipoCampo = campo.get("tipo").asText();
            boolean isPrimaryKey = campo.has("chave_primaria") && campo.get("chave_primaria").asBoolean();
            
            query.append("'").append(nomeCampo).append("', ");
            
            if (isPrimaryKey || "id".equals(tipoCampo)) {
                query.append("fn_next_id(${id_aplicacao}, '").append(nomeTabela).append("')");
            } else if ("criptografia".equals(tipoCampo)) {
                query.append("MD5(${").append(nomeCampo).append("})");
            } else {
                query.append("${").append(nomeCampo).append("}");
            }
            
            primeiro = false;
        }
        
        query.append("), ${id_aplicacao})");
        
        return query.toString();
    }
    
    private String gerarQuerySelectAll(String nomeTabela) {
        return "SELECT id, (valor->>'id')::BIGINT as id_logico, valor FROM tb_registros WHERE tabela = '" + 
               nomeTabela + "' AND id_aplicacao = ${id_aplicacao} ORDER BY (valor->>'id')::BIGINT";
    }
    
    private String gerarQuerySelectById(String nomeTabela, JsonNode campos) {
        // Encontrar qual é o campo chave primária
        String campoPrimario = "id"; // padrão
        for (JsonNode campo : campos) {
            if ((campo.has("chave_primaria") && campo.get("chave_primaria").asBoolean()) ||
                "id".equals(campo.get("tipo").asText())) {
                campoPrimario = campo.get("nome").asText();
                break;
            }
        }
        
        return "SELECT id, (valor->>'" + campoPrimario + "')::BIGINT as id_logico, valor FROM tb_registros WHERE tabela = '" + 
               nomeTabela + "' AND id_aplicacao = ${id_aplicacao} AND (valor->>'" + campoPrimario + "')::BIGINT = ${id}";
    }
    
    private String gerarQueryUpdate(String nomeTabela, JsonNode campos) {
        StringBuilder query = new StringBuilder();
        query.append("UPDATE tb_registros SET valor = jsonb_build_object(");
        
        // Primeiro, encontrar qual é o campo chave primária
        String campoPrimario = "id"; // padrão
        for (JsonNode campo : campos) {
            if ((campo.has("chave_primaria") && campo.get("chave_primaria").asBoolean()) ||
                "id".equals(campo.get("tipo").asText())) {
                campoPrimario = campo.get("nome").asText();
                break;
            }
        }
        
        boolean primeiro = true;
        for (JsonNode campo : campos) {
            if (!primeiro) query.append(", ");
            
            String nomeCampo = campo.get("nome").asText();
            String tipoCampo = campo.get("tipo").asText();
            boolean isPrimaryKey = campo.has("chave_primaria") && campo.get("chave_primaria").asBoolean();
            
            query.append("'").append(nomeCampo).append("', ");
            
            if (isPrimaryKey || "id".equals(tipoCampo)) {
                // Para campos ID, manter o valor existente
                query.append("(valor->>'").append(nomeCampo).append("')::BIGINT");
            } else if ("criptografia".equals(tipoCampo)) {
                query.append("CASE WHEN ${").append(nomeCampo).append("} IS NOT NULL THEN MD5(${")
                     .append(nomeCampo).append("}) ELSE valor->>'").append(nomeCampo).append("' END");
            } else {
                query.append("COALESCE(${").append(nomeCampo).append("}, valor->>'").append(nomeCampo).append("')");
            }
            
            primeiro = false;
        }
        
        query.append(") WHERE tabela = '").append(nomeTabela)
             .append("' AND id_aplicacao = ${id_aplicacao} AND (valor->>'").append(campoPrimario).append("')::BIGINT = ${id}");
        
        return query.toString();
    }
    
    private String gerarQueryDelete(String nomeTabela, JsonNode campos) {
        // Encontrar qual é o campo chave primária
        String campoPrimario = "id"; // padrão
        for (JsonNode campo : campos) {
            if ((campo.has("chave_primaria") && campo.get("chave_primaria").asBoolean()) ||
                "id".equals(campo.get("tipo").asText())) {
                campoPrimario = campo.get("nome").asText();
                break;
            }
        }
        
        return "DELETE FROM tb_registros WHERE tabela = '" + nomeTabela + 
               "' AND id_aplicacao = ${id_aplicacao} AND (valor->>'" + campoPrimario + "')::BIGINT = ${id}";
    }
    
    private String criarRespostaErro(JsonMapper mapper, String mensagem) {
        try {
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("success", false);
            resposta.put("message", mensagem);
            return mapper.writeValueAsString(resposta);
        } catch (JsonProcessingException e) {
            return "{\"success\": false, \"message\": \"Erro interno\"}";
        }
    }
}
