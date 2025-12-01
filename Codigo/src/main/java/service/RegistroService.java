package service;

import dao.RegistroDAO;
import dao.AplicacaoDAO;
import model.Registro;
import model.Aplicacao;
import filterDTO.RegistroFilterDTO;
import responseDTO.RegistroDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class RegistroService {
    
    public Object listar(Request request, Response response) {
        response.type("application/json");
        JsonMapper mapper = JsonMapper.builder().build();
        
        try {
            // Obter informações do usuário logado através do token de autenticação
            Integer idUsuario = request.attribute("userId");
            
            if (idUsuario == null) {
                response.status(401);
                return criarRespostaErro(mapper, "Token de autenticação inválido");
            }
            
            RegistroDAO registroDAO = new RegistroDAO();
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            List<Registro> registros = registroDAO.listarTodos();
            List<RegistroDTO> registrosDTO = new ArrayList<>();
            
            for (Registro registro : registros) {
                Aplicacao aplicacao = aplicacaoDAO.buscarPorId(registro.getIdAplicacao());
                
                // Filtrar registros apenas das aplicações que o usuário pode acessar
                if (aplicacao != null && aplicacao.getIdUsuario() == idUsuario.intValue()) {
                    String nomeAplicacao = aplicacao.getNome();
                
                    registrosDTO.add(new RegistroDTO(
                        registro.getId(),
                        registro.getTabela(),
                        registro.getValor(),
                        registro.getIdAplicacao(),
                        nomeAplicacao
                    ));
                }
            }
            
            return mapper.writeValueAsString(registrosDTO);
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return criarRespostaErro(mapper, "Erro interno do servidor");
        }
    }
    
    public Object buscarPorId(Request request, Response response) {
        response.type("application/json");
        JsonMapper mapper = JsonMapper.builder().build();
        
        try {
            int id = Integer.parseInt(request.params(":id"));
            RegistroDAO registroDAO = new RegistroDAO();
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            Registro registro = registroDAO.buscarPorId(id);
            
            if (registro == null) {
                response.status(404);
                return criarRespostaErro(mapper, "Registro não encontrado");
            }
            
            Aplicacao aplicacao = aplicacaoDAO.buscarPorId(registro.getIdAplicacao());
            String nomeAplicacao = aplicacao != null ? aplicacao.getNome() : "Aplicação não encontrada";
            
            RegistroDTO registroDTO = new RegistroDTO(
                registro.getId(),
                registro.getTabela(),
                registro.getValor(),
                registro.getIdAplicacao(),
                nomeAplicacao
            );
            
            return mapper.writeValueAsString(registroDTO);
        } catch (NumberFormatException e) {
            response.status(400);
            return criarRespostaErro(mapper, "ID inválido");
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return criarRespostaErro(mapper, "Erro interno do servidor");
        }
    }
    
    public Object buscarPorAplicacao(Request request, Response response) {
        response.type("application/json");
        JsonMapper mapper = JsonMapper.builder().build();
        
        try {
            int idAplicacao = Integer.parseInt(request.params(":idAplicacao"));
            RegistroDAO registroDAO = new RegistroDAO();
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            List<Registro> registros = registroDAO.buscarPorAplicacao(idAplicacao);
            List<RegistroDTO> registrosDTO = new ArrayList<>();
            
            Aplicacao aplicacao = aplicacaoDAO.buscarPorId(idAplicacao);
            String nomeAplicacao = aplicacao != null ? aplicacao.getNome() : "Aplicação não encontrada";
            
            for (Registro registro : registros) {
                registrosDTO.add(new RegistroDTO(
                    registro.getId(),
                    registro.getTabela(),
                    registro.getValor(),
                    registro.getIdAplicacao(),
                    nomeAplicacao
                ));
            }
            
            return mapper.writeValueAsString(registrosDTO);
        } catch (NumberFormatException e) {
            response.status(400);
            return criarRespostaErro(mapper, "ID de aplicação inválido");
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return criarRespostaErro(mapper, "Erro interno do servidor");
        }
    }
    
    public Object buscarPorTabela(Request request, Response response) {
        response.type("application/json");
        JsonMapper mapper = JsonMapper.builder().build();
        
        try {
            String tabela = request.params(":tabela");
            RegistroDAO registroDAO = new RegistroDAO();
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            List<Registro> registros = registroDAO.buscarPorTabela(tabela);
            List<RegistroDTO> registrosDTO = new ArrayList<>();
            
            for (Registro registro : registros) {
                Aplicacao aplicacao = aplicacaoDAO.buscarPorId(registro.getIdAplicacao());
                String nomeAplicacao = aplicacao != null ? aplicacao.getNome() : "Aplicação não encontrada";
                
                registrosDTO.add(new RegistroDTO(
                    registro.getId(),
                    registro.getTabela(),
                    registro.getValor(),
                    registro.getIdAplicacao(),
                    nomeAplicacao
                ));
            }
            
            return mapper.writeValueAsString(registrosDTO);
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return criarRespostaErro(mapper, "Erro interno do servidor");
        }
    }
    
    public Object buscarComFiltro(Request request, Response response) {
        response.type("application/json");
        JsonMapper mapper = JsonMapper.builder().build();
        
        try {
            RegistroFilterDTO filtro = null;
            try {
                if (request.body() != null && !request.body().isEmpty()) {
                    filtro = mapper.readValue(request.body(), new TypeReference<RegistroFilterDTO>() {});
                }
            } catch (JsonProcessingException e) {
                response.status(400);
                return criarRespostaErro(mapper, "JSON inválido");
            }
            
            RegistroDAO registroDAO = new RegistroDAO();
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            List<Registro> registros;
            
            if (filtro != null) {
                registros = registroDAO.buscarComFiltro(filtro);
            } else {
                registros = registroDAO.listarTodos();
            }
            
            List<RegistroDTO> registrosDTO = new ArrayList<>();
            for (Registro registro : registros) {
                Aplicacao aplicacao = aplicacaoDAO.buscarPorId(registro.getIdAplicacao());
                String nomeAplicacao = aplicacao != null ? aplicacao.getNome() : "Aplicação não encontrada";
                
                registrosDTO.add(new RegistroDTO(
                    registro.getId(),
                    registro.getTabela(),
                    registro.getValor(),
                    registro.getIdAplicacao(),
                    nomeAplicacao
                ));
            }
            
            return mapper.writeValueAsString(registrosDTO);
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return criarRespostaErro(mapper, "Erro interno do servidor");
        }
    }
    
    public Object inserir(Request request, Response response) {
        response.type("application/json");
        JsonMapper mapper = JsonMapper.builder().build();
        
        try {
            Registro registro = mapper.readValue(request.body(), new TypeReference<Registro>() {});
            
            if (registro.getTabela() == null || registro.getTabela().trim().isEmpty() ||
                registro.getValor() == null ||
                registro.getIdAplicacao() <= 0) {
                response.status(400);
                return criarRespostaErro(mapper, "Tabela, valor e ID da aplicação são obrigatórios");
            }
            
            RegistroDAO registroDAO = new RegistroDAO();
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            
            // Verificar se aplicação existe e obter schema
            Aplicacao aplicacao = aplicacaoDAO.buscarPorId(registro.getIdAplicacao());
            if (aplicacao == null) {
                response.status(400);
                return criarRespostaErro(mapper, "Aplicação não encontrada");
            }
            
            // Validar referências antes de inserir
            String erroValidacao = validarReferencias(registro.getValor(), registro.getIdAplicacao(), mapper);
            if (erroValidacao != null) {
                response.status(400);
                return criarRespostaErro(mapper, erroValidacao);
            }
            
            // Aplicar regras do schema antes de inserir
            com.fasterxml.jackson.databind.JsonNode valorProcessado = aplicarRegrasSchema(registro, aplicacao, registroDAO, mapper);
            registro.setValor(valorProcessado);
            
            if (registroDAO.inserir(registro)) {
                response.status(201);
                return criarRespostaSucesso(mapper, "Registro criado com sucesso");
            } else {
                response.status(500);
                return criarRespostaErro(mapper, "Erro ao criar registro");
            }
        } catch (JsonProcessingException e) {
            response.status(400);
            return criarRespostaErro(mapper, "JSON inválido");
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return criarRespostaErro(mapper, "Erro interno do servidor");
        }
    }
    
    private com.fasterxml.jackson.databind.JsonNode aplicarRegrasSchema(Registro registro, Aplicacao aplicacao, RegistroDAO registroDAO, JsonMapper mapper) throws Exception {
        // Se não há schema, usar valor original
        if (aplicacao.getSchemaBanco() == null) {
            return registro.getValor();
        }
        
        // Parse do schema
        com.fasterxml.jackson.databind.JsonNode schemaBanco = aplicacao.getSchemaBanco();
        
        // Buscar tabela no schema
        com.fasterxml.jackson.databind.JsonNode tabelas = schemaBanco.get("tabelas");
        if (tabelas == null || !tabelas.isArray()) {
            return registro.getValor();
        }
        
        com.fasterxml.jackson.databind.JsonNode tabelaSchema = null;
        for (com.fasterxml.jackson.databind.JsonNode tabela : tabelas) {
            if (registro.getTabela().equals(tabela.get("nome").asText())) {
                tabelaSchema = tabela;
                break;
            }
        }
        
        if (tabelaSchema == null) {
            return registro.getValor();
        }
        
        // Parse do valor do registro
        Map<String, Object> valorMap = mapper.convertValue(registro.getValor(), new TypeReference<Map<String, Object>>() {});
        
        // Aplicar regras dos campos
        com.fasterxml.jackson.databind.JsonNode campos = tabelaSchema.get("campos");
        if (campos != null && campos.isArray()) {
            for (com.fasterxml.jackson.databind.JsonNode campo : campos) {
                String nomeCampo = campo.get("nome").asText();
                String tipoCampo = campo.get("tipo").asText();
                boolean isChavePrimaria = campo.has("chave_primaria") && campo.get("chave_primaria").asBoolean();
                
                // Aplicar auto incremento para campos ID ou chave primária
                if (isChavePrimaria || "id".equals(tipoCampo)) {
                    // Sempre gerar novo ID usando a função do banco
                    long novoId = registroDAO.obterProximoId(registro.getIdAplicacao(), registro.getTabela());
                    valorMap.put(nomeCampo, novoId);
                }
                // Aplicar criptografia se necessário
                else if ("criptografia".equals(tipoCampo)) {
                    Object valor = valorMap.get(nomeCampo);
                    if (valor != null) {
                        String valorStr = valor.toString();
                        if (!valorStr.isEmpty()) {
                            // Aplicar MD5 (usando a mesma lógica do GeradorEndpointsService)
                            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
                            byte[] digest = md.digest(valorStr.getBytes());
                            StringBuilder sb = new StringBuilder();
                            for (byte b : digest) {
                                sb.append(String.format("%02x", b));
                            }
                            valorMap.put(nomeCampo, sb.toString());
                        }
                    }
                }
            }
        }
        
        return mapper.valueToTree(valorMap);
    }
    
    private com.fasterxml.jackson.databind.JsonNode aplicarRegrasSchemaParaAtualizacao(Registro registroNovo, Registro registroExistente, Aplicacao aplicacao, JsonMapper mapper) throws Exception {
        // Se não há schema, usar valor original
        if (aplicacao.getSchemaBanco() == null) {
            return registroNovo.getValor();
        }
        
        // Parse do schema
        com.fasterxml.jackson.databind.JsonNode schemaBanco = aplicacao.getSchemaBanco();
        
        // Buscar tabela no schema
        com.fasterxml.jackson.databind.JsonNode tabelas = schemaBanco.get("tabelas");
        if (tabelas == null || !tabelas.isArray()) {
            return registroNovo.getValor();
        }
        
        com.fasterxml.jackson.databind.JsonNode tabelaSchema = null;
        for (com.fasterxml.jackson.databind.JsonNode tabela : tabelas) {
            if (registroNovo.getTabela().equals(tabela.get("nome").asText())) {
                tabelaSchema = tabela;
                break;
            }
        }
        
        if (tabelaSchema == null) {
            return registroNovo.getValor();
        }
        
        // Parse dos valores
        Map<String, Object> valorNovoMap = mapper.convertValue(registroNovo.getValor(), new TypeReference<Map<String, Object>>() {});
        Map<String, Object> valorExistenteMap = mapper.convertValue(registroExistente.getValor(), new TypeReference<Map<String, Object>>() {});
        
        // Aplicar regras dos campos
        com.fasterxml.jackson.databind.JsonNode campos = tabelaSchema.get("campos");
        if (campos != null && campos.isArray()) {
            for (com.fasterxml.jackson.databind.JsonNode campo : campos) {
                String nomeCampo = campo.get("nome").asText();
                String tipoCampo = campo.get("tipo").asText();
                boolean isChavePrimaria = campo.has("chave_primaria") && campo.get("chave_primaria").asBoolean();
                
                // Preservar campos ID ou chave primária - manter valor existente
                if (isChavePrimaria || "id".equals(tipoCampo)) {
                    Object valorExistente = valorExistenteMap.get(nomeCampo);
                    if (valorExistente != null) {
                        valorNovoMap.put(nomeCampo, valorExistente);
                    }
                }
                // Aplicar criptografia se necessário para campos alterados
                else if ("criptografia".equals(tipoCampo)) {
                    Object valorNovo = valorNovoMap.get(nomeCampo);
                    if (valorNovo != null) {
                        String valorStr = valorNovo.toString();
                        if (!valorStr.isEmpty()) {
                            // Só aplicar MD5 se o valor foi realmente alterado
                            Object valorExistente = valorExistenteMap.get(nomeCampo);
                            if (valorExistente == null || !valorStr.equals(valorExistente.toString())) {
                                java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
                                byte[] digest = md.digest(valorStr.getBytes());
                                StringBuilder sb = new StringBuilder();
                                for (byte b : digest) {
                                    sb.append(String.format("%02x", b));
                                }
                                valorNovoMap.put(nomeCampo, sb.toString());
                            }
                        }
                    }
                }
            }
        }
        
        return mapper.valueToTree(valorNovoMap);
    }
    
    public Object atualizar(Request request, Response response) {
        response.type("application/json");
        JsonMapper mapper = JsonMapper.builder().build();
        
        try {
            int id = Integer.parseInt(request.params(":id"));
            Registro registro = mapper.readValue(request.body(), new TypeReference<Registro>() {});
            registro.setId(id);
            
            if (registro.getTabela() == null || registro.getTabela().trim().isEmpty() ||
                registro.getValor() == null) {
                response.status(400);
                return criarRespostaErro(mapper, "Tabela e valor são obrigatórios");
            }
            
            RegistroDAO registroDAO = new RegistroDAO();
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            
            // Verificar se registro existe
            Registro registroExistente = registroDAO.buscarPorId(id);
            if (registroExistente == null) {
                response.status(404);
                return criarRespostaErro(mapper, "Registro não encontrado");
            }
            
            // Obter aplicação e schema
            Aplicacao aplicacao = aplicacaoDAO.buscarPorId(registro.getIdAplicacao());
            if (aplicacao != null) {
                // Validar referências antes de atualizar
                String erroValidacao = validarReferencias(registro.getValor(), registro.getIdAplicacao(), mapper);
                if (erroValidacao != null) {
                    response.status(400);
                    return criarRespostaErro(mapper, erroValidacao);
                }
                
                // Aplicar regras do schema para preservar IDs e outros campos especiais
                com.fasterxml.jackson.databind.JsonNode valorProcessado = aplicarRegrasSchemaParaAtualizacao(registro, registroExistente, aplicacao, mapper);
                registro.setValor(valorProcessado);
            }
            
            if (registroDAO.atualizar(registro)) {
                return criarRespostaSucesso(mapper, "Registro atualizado com sucesso");
            } else {
                response.status(500);
                return criarRespostaErro(mapper, "Erro ao atualizar registro");
            }
        } catch (NumberFormatException e) {
            response.status(400);
            return criarRespostaErro(mapper, "ID inválido");
        } catch (JsonProcessingException e) {
            response.status(400);
            return criarRespostaErro(mapper, "JSON inválido");
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return criarRespostaErro(mapper, "Erro interno do servidor");
        }
    }
    
    public Object excluir(Request request, Response response) {
        response.type("application/json");
        JsonMapper mapper = JsonMapper.builder().build();
        
        try {
            int id = Integer.parseInt(request.params(":id"));
            RegistroDAO registroDAO = new RegistroDAO();
            
            if (registroDAO.buscarPorId(id) == null) {
                response.status(404);
                return criarRespostaErro(mapper, "Registro não encontrado");
            }
            
            if (registroDAO.excluir(id)) {
                return criarRespostaSucesso(mapper, "Registro excluído com sucesso");
            } else {
                response.status(500);
                return criarRespostaErro(mapper, "Erro ao excluir registro");
            }
        } catch (NumberFormatException e) {
            response.status(400);
            return criarRespostaErro(mapper, "ID inválido");
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return criarRespostaErro(mapper, "Erro interno do servidor");
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
    
    public Object contarPorAplicacao(Request request, Response response) {
        response.type("application/json");
        JsonMapper mapper = JsonMapper.builder().build();
        
        try {
            // Obter informações do usuário logado através do token de autenticação
            Integer idUsuario = request.attribute("userId");
            
            if (idUsuario == null) {
                response.status(401);
                return criarRespostaErro(mapper, "Token de autenticação inválido");
            }
            
            // Obter ID da aplicação via query param ou path param
            String idAplicacaoStr = request.queryParams("id_aplicacao");
            if (idAplicacaoStr == null || idAplicacaoStr.isEmpty()) {
                idAplicacaoStr = request.params(":idAplicacao");
            }
            
            if (idAplicacaoStr == null || idAplicacaoStr.isEmpty()) {
                response.status(400);
                return criarRespostaErro(mapper, "ID da aplicação é obrigatório");
            }
            
            int idAplicacao = Integer.parseInt(idAplicacaoStr);
            
            // Verificar se a aplicação pertence ao usuário
            AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();
            Aplicacao aplicacao = aplicacaoDAO.buscarPorId(idAplicacao);
            
            if (aplicacao == null || aplicacao.getIdUsuario() != idUsuario.intValue()) {
                response.status(403);
                return criarRespostaErro(mapper, "Acesso negado à aplicação");
            }
            
            // Contar registros
            RegistroDAO registroDAO = new RegistroDAO();
            int count = registroDAO.contarPorAplicacao(idAplicacao);
            
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("count", count);
            resposta.put("aplicacao_id", idAplicacao);
            
            return mapper.writeValueAsString(resposta);
        } catch (NumberFormatException e) {
            response.status(400);
            return criarRespostaErro(mapper, "ID de aplicação inválido");
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return criarRespostaErro(mapper, "Erro interno do servidor");
        }
    }
    
    private String validarReferencias(com.fasterxml.jackson.databind.JsonNode valorJson, int idAplicacao, JsonMapper mapper) {
        try {
            if (valorJson == null) {
                return null; // Sem dados para validar
            }
            
            // Verificar campos que podem ser referências (terminam com _id, id_, ou são apenas "id")
            valorJson.fieldNames().forEachRemaining(fieldName -> {
                com.fasterxml.jackson.databind.JsonNode fieldNode = valorJson.get(fieldName);
                
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
            RegistroDAO registroDAO = new RegistroDAO();
            List<Registro> registros = registroDAO.buscarPorAplicacao(idAplicacao);
            
            for (Registro registro : registros) {
                if (registro.getTabela().equals(tabela)) {
                    // Parse do JSON para verificar se tem o ID procurado
                    try {
                        JsonMapper mapper = JsonMapper.builder().build();
                        Map<String, Object> valorMap = mapper.convertValue(registro.getValor(), new TypeReference<Map<String, Object>>() {});
                        
                        // Verificar se existe um campo "id" com o valor procurado
                        Object idObj = valorMap.get("id");
                        if (idObj != null) {
                            int registroId = Integer.parseInt(idObj.toString());
                            if (registroId == id) {
                                return true;
                            }
                        }
                    } catch (Exception e) {
                        // Ignorar erros de parsing e continuar
                    }
                }
            }
            
            return false;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Em caso de erro, assumir que não existe
        }
    }
    
    private String criarRespostaSucesso(JsonMapper mapper, String mensagem) {
        try {
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("success", true);
            resposta.put("message", mensagem);
            return mapper.writeValueAsString(resposta);
        } catch (JsonProcessingException e) {
            return "{\"success\": true, \"message\": \"Operação realizada\"}";
        }
    }
}