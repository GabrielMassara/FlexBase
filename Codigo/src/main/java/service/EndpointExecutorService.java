package service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

import dao.EndpointDAO;
import model.Endpoint;
import spark.Request;
import spark.Response;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EndpointExecutorService {
    
    public Object executeEndpoint(Request request, Response response) {
        response.type("application/json");
        JsonMapper mapper = JsonMapper.builder().build();
        
        try {
            // Obter informações já validadas pelo filtro de autenticação
            Integer idAplicacao = request.attribute("idAplicacao");
            
            // Extrair a rota do splat parameter
            String rotaEndpoint = request.splat()[0];
            if (rotaEndpoint == null || rotaEndpoint.trim().isEmpty()) {
                response.status(400);
                return criarRespostaErro(mapper, "Rota do endpoint não fornecida");
            }
            
            // Adicionar "/" no início se não tiver
            if (!rotaEndpoint.startsWith("/")) {
                rotaEndpoint = "/" + rotaEndpoint;
            }
            
            // Buscar endpoint correspondente (validação já foi feita no filtro)
            EndpointDAO endpointDAO = new EndpointDAO();
            List<Endpoint> endpoints = endpointDAO.buscarPorAplicacao(idAplicacao);
            
            Endpoint endpointEncontrado = null;
            Map<String, String> parametrosRota = new HashMap<>();
            
            // Verificar método HTTP
            int metodoHTTP = getMetodoHTTP(request.requestMethod());
            
            for (Endpoint endpoint : endpoints) {
                if (endpoint.getMetodo() == metodoHTTP && rotaCorresponde(endpoint.getRota(), rotaEndpoint, parametrosRota)) {
                    endpointEncontrado = endpoint;
                    break;
                }
            }
            
            if (endpointEncontrado == null) {
                response.status(404);
                return criarRespostaErro(mapper, "Endpoint não encontrado para esta rota e método");
            }
            
            // Executar a query do endpoint
            return executarQuery(endpointEncontrado, request, response, idAplicacao, parametrosRota, mapper);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return criarRespostaErro(mapper, "Erro interno do servidor: " + e.getMessage());
        }
    }
    
    private boolean rotaCorresponde(String rotaEndpoint, String rotaRequisicao, Map<String, String> parametros) {
        // Converter rota do endpoint para regex
        // Exemplo: "/clientes/{id}" -> "/clientes/([^/]+)"
        String regexRota = rotaEndpoint.replaceAll("\\{([^}]+)\\}", "([^/]+)");
        regexRota = "^" + regexRota + "$";
        
        Pattern pattern = Pattern.compile(regexRota);
        Matcher matcher = pattern.matcher(rotaRequisicao);
        
        if (matcher.matches()) {
            // Extrair parâmetros
            Pattern paramPattern = Pattern.compile("\\{([^}]+)\\}");
            Matcher paramMatcher = paramPattern.matcher(rotaEndpoint);
            
            int groupIndex = 1;
            while (paramMatcher.find()) {
                String paramName = paramMatcher.group(1);
                if (groupIndex <= matcher.groupCount()) {
                    parametros.put(paramName, matcher.group(groupIndex));
                    groupIndex++;
                }
            }
            
            return true;
        }
        
        return false;
    }
    
    private int getMetodoHTTP(String metodo) {
        switch (metodo.toUpperCase()) {
            case "GET": return 1;
            case "POST": return 2;
            case "PUT": return 3;
            case "DELETE": return 4;
            default: return 0;
        }
    }
    
    private Object executarQuery(Endpoint endpoint, Request request, Response response, 
                                int idAplicacao, Map<String, String> parametrosRota, JsonMapper mapper) {
        
        try {
            String query = endpoint.getQuery();
            
            // Validar referências antes de executar INSERT/UPDATE
            if (("POST".equalsIgnoreCase(request.requestMethod()) || "PUT".equalsIgnoreCase(request.requestMethod()))) {
                String validationError = validarReferencias(request, idAplicacao);
                if (validationError != null) {
                    response.status(400);
                    return criarRespostaErro(mapper, validationError);
                }
            }
            
            // Substituir placeholders na query
            query = substituirPlaceholders(query, request, idAplicacao, parametrosRota);
            
            // Debug: imprimir query final para depuração
            System.out.println("Query executada: " + query);
            
            // Verificar se ainda há placeholders não substituídos
            if (query.contains("${")) {
                response.status(400);
                return criarRespostaErro(mapper, "Parâmetros obrigatórios não fornecidos na requisição");
            }
            
            // Executar a query
            Connection conexao = obterConexao();
            PreparedStatement stmt = conexao.prepareStatement(query);
            
            boolean isSelect = query.trim().toLowerCase().startsWith("select");
            
            if (isSelect) {
                // Query de SELECT - retornar resultados
                ResultSet rs = stmt.executeQuery();
                return processarResultSet(rs, mapper);
            } else {
                // Query de INSERT/UPDATE/DELETE - retornar número de linhas afetadas
                int linhasAfetadas = stmt.executeUpdate();
                Map<String, Object> resultado = new HashMap<>();
                resultado.put("success", true);
                resultado.put("message", "Operação executada com sucesso");
                resultado.put("linhasAfetadas", linhasAfetadas);
                return mapper.writeValueAsString(resultado);
            }
            
        } catch (SQLException | JsonProcessingException e) {
            e.printStackTrace();
            response.status(500);
            return criarRespostaErro(mapper, "Erro ao executar query: " + e.getMessage());
        }
    }
    
    private String substituirPlaceholders(String query, Request request, int idAplicacao, Map<String, String> parametrosRota) {
        // Substituir ${id_aplicacao}
        query = query.replace("${id_aplicacao}", String.valueOf(idAplicacao));
        
        // Substituir parâmetros da rota (ex: ${id})
        for (Map.Entry<String, String> param : parametrosRota.entrySet()) {
            query = query.replace("${" + param.getKey() + "}", param.getValue());
        }
        
            // Substituir parâmetros do corpo da requisição (para POST/PUT)
        if ("POST".equalsIgnoreCase(request.requestMethod()) || "PUT".equalsIgnoreCase(request.requestMethod())) {
            try {
                String body = request.body();
                if (body != null && !body.trim().isEmpty()) {
                    JsonMapper mapper = JsonMapper.builder().build();
                    JsonNode bodyJson = mapper.readTree(body);
                    
                    // Substituir cada campo do JSON
                    final String[] queryRef = {query};
                    bodyJson.fieldNames().forEachRemaining(fieldName -> {
                        JsonNode fieldNode = bodyJson.get(fieldName);
                        String value;
                        
                        // Tratar diferentes tipos de dados
                        if (fieldNode.isNull()) {
                            value = "NULL";
                        } else if (fieldNode.isNumber()) {
                            value = fieldNode.asText();
                        } else if (fieldNode.isBoolean()) {
                            value = fieldNode.asBoolean() ? "true" : "false";
                        } else {
                            // Para strings, escapar aspas simples e envolver em aspas
                            value = "'" + fieldNode.asText().replace("'", "''") + "'";
                        }
                        
                        // Só substituir se o placeholder existir na query
                        String placeholder = "${" + fieldName + "}";
                        if (queryRef[0].contains(placeholder)) {
                            queryRef[0] = queryRef[0].replace(placeholder, value);
                        }
                    });
                    query = queryRef[0];
                }
            } catch (Exception e) {
                // Se não conseguir parsear o JSON, continuar sem substituir
                e.printStackTrace();
            }
        }        // Substituir query parameters (ex: ?nome=valor)
        final String[] queryRef = {query};
        request.queryParams().forEach(paramName -> {
            String paramValue = request.queryParams(paramName);
            
            // Para query parameters, assumir que valores numéricos não precisam de aspas
            // e strings precisam ser envolvidas em aspas
            String processedValue;
            try {
                // Tentar converter para número
                Double.parseDouble(paramValue);
                processedValue = paramValue; // É número, usar sem aspas
            } catch (NumberFormatException e) {
                // É string, envolver em aspas e escapar aspas simples
                processedValue = "'" + paramValue.replace("'", "''") + "'";
            }
            
            queryRef[0] = queryRef[0].replace("${" + paramName + "}", processedValue);
        });
        
        return queryRef[0];
    }
    
    private Object processarResultSet(ResultSet rs, JsonMapper mapper) throws SQLException, JsonProcessingException {
        java.util.List<Object> resultados = new java.util.ArrayList<>();
        
        while (rs.next()) {
            // Verificar se a query retorna apenas o campo 'valor' (estrutura de registros)
            int columnCount = rs.getMetaData().getColumnCount();
            
            // Se há coluna 'valor', extrair apenas seu conteúdo JSON
            boolean hasValorColumn = false;
            for (int i = 1; i <= columnCount; i++) {
                if ("valor".equals(rs.getMetaData().getColumnLabel(i))) {
                    hasValorColumn = true;
                    break;
                }
            }
            
            if (hasValorColumn) {
                // Extrair apenas o JSON do campo 'valor'
                String valorJson = rs.getString("valor");
                if (valorJson != null) {
                    // Parsear o JSON e adicionar diretamente aos resultados
                    JsonNode valorNode = mapper.readTree(valorJson);
                    resultados.add(valorNode);
                } else {
                    resultados.add(null);
                }
            } else {
                // Para queries que não retornam a estrutura de registros,
                // montar objeto com todas as colunas
                Map<String, Object> linha = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rs.getMetaData().getColumnLabel(i);
                    Object value = rs.getObject(i);
                    linha.put(columnName, value);
                }
                resultados.add(linha);
            }
        }
        
        return mapper.writeValueAsString(resultados);
    }
    
    private Connection obterConexao() throws SQLException {
        // Usar as mesmas configurações da classe DAO
        String driverName = "org.postgresql.Driver";
        String serverName = "localhost";
        String mydatabase = "postgres";
        int porta = 5432;
        String url = "jdbc:postgresql://" + serverName + ":" + porta + "/" + mydatabase + "?currentSchema=db_flexbase";
        String username = "postgres";
        String password = "02022007";
        
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver PostgreSQL não encontrado", e);
        }
        
        return DriverManager.getConnection(url, username, password);
    }
    
    private String validarReferencias(Request request, int idAplicacao) {
        try {
            String body = request.body();
            if (body == null || body.trim().isEmpty()) {
                return null; // Sem dados para validar
            }
            
            JsonMapper mapper = JsonMapper.builder().build();
            JsonNode bodyJson = mapper.readTree(body);
            
            // Verificar campos que podem ser referências (terminam com _id, id_, ou são apenas "id")
            bodyJson.fieldNames().forEachRemaining(fieldName -> {
                JsonNode fieldNode = bodyJson.get(fieldName);
                
                // Verificar se é um campo de referência (ID de outra tabela)
                if (isReferenciaField(fieldName) && !fieldNode.isNull() && fieldNode.isNumber()) {
                    int referenciaId = fieldNode.asInt();
                    String tabelaReferenciada = obterTabelaReferenciada(fieldName);
                    
                    if (tabelaReferenciada != null && !existeRegistro(tabelaReferenciada, referenciaId, idAplicacao)) {
                        throw new RuntimeException("Referência inválida: " + fieldName + " = " + referenciaId + " não existe na tabela " + tabelaReferenciada);
                    }
                }
            });
            
            return null; // Todas as referências são válidas
            
        } catch (RuntimeException e) {
            return e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Em caso de erro, não bloquear
        }
    }
    
    private boolean isReferenciaField(String fieldName) {
        // Considerar campos que são referências:
        // - Terminam com "_id" (ex: cliente_id, produto_id)
        // - Começam com "id_" (ex: id_cliente, id_produto)  
        // - São compostos como "id_x" onde x é inicial da tabela (ex: id_c, id_p)
        // - NÃO incluir campo "id" simples (chave primária da própria tabela)
        
        return !fieldName.equals("id") && 
               (fieldName.endsWith("_id") || 
                fieldName.startsWith("id_") || 
                fieldName.matches("id_[a-zA-Z]+"));
    }
    
    private String obterTabelaReferenciada(String fieldName) {
        // Mapear nome do campo para nome da tabela
        // Convenções possíveis:
        // id_c -> clientes, id_p -> produtos, etc.
        // cliente_id -> clientes, produto_id -> produtos
        // id_cliente -> clientes, id_produto -> produtos
        
        if (fieldName.equals("id_c")) return "clientes";
        if (fieldName.equals("id_p")) return "produtos";
        
        // Para campos como "cliente_id" -> "clientes"
        if (fieldName.endsWith("_id")) {
            String base = fieldName.substring(0, fieldName.length() - 3);
            return pluralizarTabela(base);
        }
        
        // Para campos como "id_cliente" -> "clientes"  
        if (fieldName.startsWith("id_")) {
            String base = fieldName.substring(3);
            return pluralizarTabela(base);
        }
        
        return null; // Não é possível determinar a tabela
    }
    
    private String pluralizarTabela(String nomeBase) {
        // Regras simples de pluralização em português
        if (nomeBase.equals("cliente")) return "clientes";
        if (nomeBase.equals("produto")) return "produtos";
        if (nomeBase.equals("usuario")) return "usuarios";
        if (nomeBase.equals("categoria")) return "categorias";
        
        // Regra geral: adicionar 's' se não termina com 's'
        if (!nomeBase.endsWith("s")) {
            return nomeBase + "s";
        }
        
        return nomeBase;
    }
    
    private boolean existeRegistro(String tabela, int id, int idAplicacao) {
        try {
            Connection conexao = obterConexao();
            String query = "SELECT 1 FROM tb_registros WHERE tabela = ? AND id_aplicacao = ? AND (valor->>'id')::BIGINT = ? LIMIT 1";
            
            PreparedStatement stmt = conexao.prepareStatement(query);
            stmt.setString(1, tabela);
            stmt.setInt(2, idAplicacao);
            stmt.setInt(3, id);
            
            ResultSet rs = stmt.executeQuery();
            boolean existe = rs.next();
            
            rs.close();
            stmt.close();
            conexao.close();
            
            return existe;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Em caso de erro, assumir que não existe
        }
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