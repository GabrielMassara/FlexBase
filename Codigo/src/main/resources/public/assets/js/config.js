// Configurações centralizadas da aplicação FlexBase
const CONFIG = {
    // URL base da API
    API_BASE_URL: 'http://localhost:80/api',
    
    // Configurações de autenticação
    AUTH: {
        TOKEN_KEY: 'token',
        USER_KEY: 'usuario',
        TOKEN_EXPIRY_HOURS: 24
    },
    
    // Endpoints da API
    ENDPOINTS: {
        LOGIN: '/login',
        USUARIOS: '/usuarios',
        APLICACOES: '/aplicacoes',
        APLICACOES_MINHAS: '/aplicacoes',
        APLICACOES_ID: '/aplicacoes/{id}',
        ENDPOINTS: '/endpoints',
        ENDPOINTS_BY_APP: '/endpoints/aplicacao/{id}',
        REGISTROS: '/registros',
        REGISTROS_COUNT: '/registros/count'
    },
    
    // Configurações da aplicação
    APP: {
        NAME: 'FlexBase',
        DESCRIPTION: 'Sua plataforma Backend as a Service',
        VERSION: '1.0.0'
    },
    
    // Configurações de tema
    THEME: {
        DEFAULT: 'dark',
        STORAGE_KEY: 'flexbase-theme'
    }
};

// Funções utilitárias para configuração
const ConfigUtils = {
    // Obter URL completa do endpoint
    getEndpointUrl: (endpoint) => {
        return CONFIG.API_BASE_URL + (CONFIG.ENDPOINTS[endpoint] || endpoint);
    },
    
    // Obter token de autenticação
    getAuthToken: () => {
        return localStorage.getItem(CONFIG.AUTH.TOKEN_KEY);
    },
    
    // Obter dados do usuário
    getUser: () => {
        const userData = localStorage.getItem(CONFIG.AUTH.USER_KEY);
        return userData ? JSON.parse(userData) : null;
    },
    
    // Verificar se usuário está logado
    isAuthenticated: () => {
        return !!ConfigUtils.getAuthToken();
    },
    
    // Limpar dados de autenticação
    clearAuth: () => {
        localStorage.removeItem(CONFIG.AUTH.TOKEN_KEY);
        localStorage.removeItem(CONFIG.AUTH.USER_KEY);
    },
    
    // Obter headers padrão para requisições
    getAuthHeaders: () => {
        const token = ConfigUtils.getAuthToken();
        return {
            'Content-Type': 'application/json',
            ...(token && { 'Authorization': `Bearer ${token}` })
        };
    }
};

// Exportar para uso global
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { CONFIG, ConfigUtils };
}