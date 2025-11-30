package app;

import static spark.Spark.*;

import service.ApiService;
import service.UsuarioService;
import service.AplicacaoService;
import service.EndpointService;
import service.RegistroService;
import service.GeradorEndpointsService;
import service.EndpointExecutorService;
import util.AuthFilter;

public class Aplicacao {

    private static ApiService apiService = new ApiService();
    private static UsuarioService usuarioService = new UsuarioService();
    private static AplicacaoService aplicacaoService = new AplicacaoService();
    private static EndpointService endpointService = new EndpointService();
    private static RegistroService registroService = new RegistroService();
    private static GeradorEndpointsService geradorEndpointsService = new GeradorEndpointsService();
    private static EndpointExecutorService endpointExecutorService = new EndpointExecutorService();    public static void main(String[] args) {  
    	System.out.println(" ________ ___       _______      ___    ___ ________  ________  ________  _______      \n"
    			+ "|\\  _____\\\\  \\     |\\  ___ \\    |\\  \\  /  /|\\   __  \\|\\   __  \\|\\   ____\\|\\  ___ \\     \n"
    			+ "\\ \\  \\__/\\ \\  \\    \\ \\   __/|   \\ \\  \\/  / | \\  \\|\\ /\\ \\  \\|\\  \\ \\  \\___|\\ \\   __/|    \n"
    			+ " \\ \\   __\\\\ \\  \\    \\ \\  \\_|/__  \\ \\    / / \\ \\   __  \\ \\   __  \\ \\_____  \\ \\  \\_|/__  \n"
    			+ "  \\ \\  \\_| \\ \\  \\____\\ \\  \\_|\\ \\  /     \\/   \\ \\  \\|\\  \\ \\  \\ \\  \\|____|\\  \\ \\  \\_|\\ \\ \n"
    			+ "   \\ \\__\\   \\ \\_______\\ \\_______\\/  /\\   \\    \\ \\_______\\ \\__\\ \\__\\____\\_\\  \\ \\_______\\\n"
    			+ "    \\|__|    \\|_______|\\|_______/__/ /\\ __\\    \\|_______|\\|__|\\|__|\\_________\\|_______|\n"
    			+ "                                |__|/ \\|__|                       \\|_________|         \n"
    			+ "                                                                                       \n"
    			+ "                                                                                       ");
    	
        port(80);
        staticFiles.location("/public");
        
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
        });
        
        // ROTAS PÚBLICAS (ANTES DO FILTRO DE AUTENTICAÇÃO)
        post("/api/entrar", (request, response) -> apiService.entrar(request, response));
        post("/api/login", (request, response) -> usuarioService.login(request, response));
        post("/api/usuarios", (request, response) -> usuarioService.inserir(request, response)); // Cadastro público
        
        // FILTRO DE AUTENTICAÇÃO PARA TODAS AS OUTRAS ROTAS /api/*
        // Exceto as rotas públicas já definidas acima
        //before("/api/usuarios", "GET", AuthFilter.authenticate); // Proteger GET de usuários
        //before("/api/usuarios/*", AuthFilter.authenticate); // Proteger rotas específicas de usuário
        before("/api/aplicacoes", AuthFilter.authenticate);
        before("/api/aplicacoes/*", AuthFilter.authenticate);
        before("/api/endpoints", AuthFilter.authenticate);
        before("/api/endpoints/*", AuthFilter.authenticate);
        before("/api/registros", AuthFilter.authenticate);
        before("/api/registros/*", AuthFilter.authenticate);
        
        // === ROTAS USUARIOS (PROTEGIDAS) ===
        get("/api/usuarios", (request, response) -> usuarioService.listar(request, response));
        get("/api/usuarios/:id", (request, response) -> usuarioService.buscarPorId(request, response));
        post("/api/usuarios/buscar", (request, response) -> usuarioService.buscarComFiltro(request, response));
        put("/api/usuarios/:id", (request, response) -> usuarioService.atualizar(request, response));
        delete("/api/usuarios/:id", (request, response) -> usuarioService.excluir(request, response));
        
        // === ROTAS APLICACOES ===
        get("/api/aplicacoes", (request, response) -> aplicacaoService.listar(request, response));
        get("/api/aplicacoes/minhas", (request, response) -> aplicacaoService.buscarPorUsuario(request, response));
        get("/api/aplicacoes/:id", (request, response) -> aplicacaoService.buscarPorId(request, response));
        post("/api/aplicacoes/buscar", (request, response) -> aplicacaoService.buscarComFiltro(request, response));
        post("/api/aplicacoes", (request, response) -> aplicacaoService.inserir(request, response));
        put("/api/aplicacoes/:id", (request, response) -> aplicacaoService.atualizar(request, response));
        delete("/api/aplicacoes/:id", (request, response) -> aplicacaoService.excluir(request, response));
        
        // === ROTAS ENDPOINTS ===
        get("/api/endpoints", (request, response) -> endpointService.listar(request, response));
        get("/api/endpoints/:id", (request, response) -> endpointService.buscarPorId(request, response));
        get("/api/endpoints/aplicacao/:idAplicacao", (request, response) -> endpointService.buscarPorAplicacao(request, response));
        post("/api/endpoints/buscar", (request, response) -> endpointService.buscarComFiltro(request, response));
        post("/api/endpoints", (request, response) -> endpointService.inserir(request, response));
        put("/api/endpoints/:id", (request, response) -> endpointService.atualizar(request, response));
        delete("/api/endpoints/:id", (request, response) -> endpointService.excluir(request, response));
        
        // === ROTAS REGISTROS ===
        get("/api/registros", (request, response) -> registroService.listar(request, response));
        get("/api/registros/count", (request, response) -> registroService.contarPorAplicacao(request, response));
        get("/api/registros/:id", (request, response) -> registroService.buscarPorId(request, response));
        get("/api/registros/aplicacao/:idAplicacao", (request, response) -> registroService.buscarPorAplicacao(request, response));
        get("/api/registros/tabela/:tabela", (request, response) -> registroService.buscarPorTabela(request, response));
        post("/api/registros/buscar", (request, response) -> registroService.buscarComFiltro(request, response));
        post("/api/registros", (request, response) -> registroService.inserir(request, response));
        put("/api/registros/:id", (request, response) -> registroService.atualizar(request, response));
        delete("/api/registros/:id", (request, response) -> registroService.excluir(request, response));
        
        // === ROTA PARA GERAR OS ENDPOINTS ===
        before("/api/generateEndpoints/:idAplicacao", AuthFilter.authenticate);
        post("/api/generateEndpoints/:idAplicacao", (request, response) -> geradorEndpointsService.generateEndpoints(request, response));
        
        // === ROTAS GENÉRICAS PARA EXECUTAR ENDPOINTS DINÂMICOS ===
        before("/api/endpoints/:idAplicacao/*", AuthFilter.authenticate);
        get("/api/endpoints/:idAplicacao/*", (request, response) -> endpointExecutorService.executeEndpoint(request, response));
        post("/api/endpoints/:idAplicacao/*", (request, response) -> endpointExecutorService.executeEndpoint(request, response));
        put("/api/endpoints/:idAplicacao/*", (request, response) -> endpointExecutorService.executeEndpoint(request, response));
        delete("/api/endpoints/:idAplicacao/*", (request, response) -> endpointExecutorService.executeEndpoint(request, response));
        
        // === ROTA OPTIONS PARA CORS ===
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });
    }
}
