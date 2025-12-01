// Dashboard JavaScript
document.addEventListener('DOMContentLoaded', function() {
    initializeDashboard();
    setupEventListeners();
    loadDashboardData();
});

// Função para obter ID da aplicação da URL
function getApplicationIdFromUrl() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('id');
}

function initializeDashboard() {
    // Configurar tema
    const savedTheme = localStorage.getItem('theme') || 'dark';
    document.documentElement.setAttribute('data-bs-theme', savedTheme);
    updateThemeIcon(savedTheme);
    
    // Verificar se o usuário está logado
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '../login/index.html';
        return;
    }
    
    // Verificar se há ID da aplicação na URL
    const appId = getApplicationIdFromUrl();
    if (!appId) {
        showNotification('ID da aplicação não fornecido na URL. Redirecionando...', 'warning');
        // Redirecionar para home se não houver ID
        setTimeout(() => {
            window.location.href = '../home/index.html';
        }, 2000);
        return;
    }
    
    // Armazenar ID da aplicação selecionada
    localStorage.setItem('selectedApplicationId', appId);
    
    console.log('Dashboard inicializado para aplicação ID:', appId);
}

function setupEventListeners() {
    // Toggle de tema
    const themeToggle = document.getElementById('themeToggle');
    if (themeToggle) {
        themeToggle.addEventListener('click', toggleTheme);
    }
    
    // Logout
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', handleLogout);
    }
    
    // Navegação para home
    const homeBtn = document.querySelector('.btn-glass.btn-secondary');
    if (homeBtn) {
        homeBtn.addEventListener('click', () => {
            window.location.href = '../home/index.html';
        });
    }
    
    // Navegação para o diagrama
    const diagramCard = document.querySelector('.diagram-card');
    if (diagramCard) {
        diagramCard.addEventListener('click', function(e) {
            e.preventDefault();
            navigateToDiagram();
        });
    }
    
    // Navegação para keys
    const keysCard = document.querySelector('.keys-card');
    if (keysCard) {
        keysCard.addEventListener('click', function(e) {
            e.preventDefault();
            navigateToKeys();
        });
    }
    
    // Navegação para banco de dados
    const databaseCard = document.querySelector('.database-card');
    if (databaseCard) {
        databaseCard.addEventListener('click', function(e) {
            e.preventDefault();
            navigateToDatabase();
        });
    }
    
    // Outros cards (desabilitados por enquanto)
    setupDisabledCards();
}

function navigateToDiagram() {
    // Obter ID da aplicação da URL
    const appId = getApplicationIdFromUrl();
    
    if (!appId) {
        showNotification('ID da aplicação não encontrado.', 'error');
        return;
    }
    
    // Redirecionar para o diagrama com o parâmetro app
    window.location.href = `../db/index.html?app=${appId}`;
}

function navigateToKeys() {
    // Obter ID da aplicação da URL
    const appId = getApplicationIdFromUrl();
    
    if (!appId) {
        showNotification('ID da aplicação não encontrado.', 'error');
        return;
    }
    
    // Redirecionar para o gerenciamento de keys
    window.location.href = `../keys/index.html?idAplicacao=${appId}`;
}

function navigateToDatabase() {
    // Obter ID da aplicação da URL
    const appId = getApplicationIdFromUrl();
    
    if (!appId) {
        showNotification('ID da aplicação não encontrado.', 'error');
        return;
    }
    
    // Redirecionar para o banco de dados
    window.location.href = `../database/index.html?id=${appId}`;
}

function setupDisabledCards() {
    const disabledCards = document.querySelectorAll('.action-card.disabled');
    
    disabledCards.forEach(card => {
        card.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            const cardTitle = card.querySelector('.action-card-title').textContent;
            showNotification(`${cardTitle} estará disponível em breve!`, 'info');
        });
    });
}

function loadDashboardData() {
    // Carregar informações do usuário
    loadUserInfo();
    
    // Carregar estatísticas
    loadStats();
    
    // Verificar aplicação selecionada
    checkSelectedApplication();
}

function loadUserInfo() {
    const token = localStorage.getItem('token');
    if (!token) return;
    
    // Aqui você pode fazer uma chamada para buscar dados do usuário
    // Por enquanto, vamos usar dados básicos
    const userInfo = {
        name: 'Usuário',
        email: 'usuario@example.com'
    };
    
    // Atualizar interface se necessário
    console.log('Informações do usuário carregadas:', userInfo);
}

function loadStats() {
    // Carregar estatísticas do dashboard
    const stats = {
        totalApps: 0,
        totalEndpoints: 0,
        totalTables: 0,
        activeUsers: 1
    };
    
    // Atualizar estatísticas na interface
    updateStatsDisplay(stats);
}

function updateStatsDisplay(stats) {
    const statCards = document.querySelectorAll('.stat-card');
    
    if (statCards.length >= 4) {
        statCards[0].querySelector('.stat-number').textContent = stats.totalApps;
        statCards[1].querySelector('.stat-number').textContent = stats.totalEndpoints;
        statCards[2].querySelector('.stat-number').textContent = stats.totalTables;
        statCards[3].querySelector('.stat-number').textContent = stats.activeUsers;
    }
}

function checkSelectedApplication() {
    const appId = getApplicationIdFromUrl();
    const diagramCard = document.querySelector('.diagram-card');
    const keysCard = document.querySelector('.keys-card');
    const databaseCard = document.querySelector('.database-card');
    
    if (appId) {
        // Habilitar cards se há ID da aplicação
        if (diagramCard) {
            diagramCard.classList.remove('disabled');
            diagramCard.querySelector('.action-card-status').textContent = 'Disponível';
            diagramCard.querySelector('.action-card-status').className = 'action-card-status status-available';
        }
        
        if (keysCard) {
            keysCard.classList.remove('disabled');
            keysCard.querySelector('.action-card-status').textContent = 'Disponível';
            keysCard.querySelector('.action-card-status').className = 'action-card-status status-available';
        }
        
        if (databaseCard) {
            databaseCard.classList.remove('disabled');
            databaseCard.querySelector('.action-card-status').textContent = 'Disponível';
            databaseCard.querySelector('.action-card-status').className = 'action-card-status status-available';
        }
        
        // Carregar informações da aplicação
        loadApplicationInfo(appId);
    } else {
        // Desabilitar se não há aplicação
        if (diagramCard) {
            diagramCard.classList.add('disabled');
            diagramCard.querySelector('.action-card-status').textContent = 'ID não fornecido';
            diagramCard.querySelector('.action-card-status').className = 'action-card-status status-disabled';
        }
        
        if (keysCard) {
            keysCard.classList.add('disabled');
            keysCard.querySelector('.action-card-status').textContent = 'ID não fornecido';
            keysCard.querySelector('.action-card-status').className = 'action-card-status status-disabled';
        }
        
        if (databaseCard) {
            databaseCard.classList.add('disabled');
            databaseCard.querySelector('.action-card-status').textContent = 'ID não fornecido';
            databaseCard.querySelector('.action-card-status').className = 'action-card-status status-disabled';
        }
    }
}

function toggleTheme() {
    const currentTheme = document.documentElement.getAttribute('data-bs-theme');
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
    
    document.documentElement.setAttribute('data-bs-theme', newTheme);
    localStorage.setItem('theme', newTheme);
    updateThemeIcon(newTheme);
}

function updateThemeIcon(theme) {
    const themeIcon = document.querySelector('#themeToggle i');
    if (themeIcon) {
        themeIcon.className = theme === 'dark' ? 'bi bi-sun' : 'bi bi-moon';
    }
}

function handleLogout() {
    if (confirm('Tem certeza que deseja sair?')) {
        localStorage.removeItem('token');
        localStorage.removeItem('selectedApplicationId');
        localStorage.removeItem('userInfo');
        window.location.href = '../login/index.html';
    }
}

function showNotification(message, type = 'info') {
    // Criar notificação toast
    const toastContainer = document.querySelector('.toast-container') || createToastContainer();
    
    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-bg-${type} border-0`;
    toast.setAttribute('role', 'alert');
    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">
                ${message}
            </div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    `;
    
    toastContainer.appendChild(toast);
    
    const bsToast = new bootstrap.Toast(toast);
    bsToast.show();
    
    // Remover toast após ele ser ocultado
    toast.addEventListener('hidden.bs.toast', () => {
        toast.remove();
    });
}

function createToastContainer() {
    const container = document.createElement('div');
    container.className = 'toast-container position-fixed top-0 end-0 p-3';
    container.style.zIndex = '1200';
    document.body.appendChild(container);
    return container;
}

// Função para navegação rápida
function navigateToModule(moduleName) {
    const routes = {
        'api': '../new/index.html', // Por enquanto vai para nova aplicação
        'diagram': '../db/index.html',
        'database': '#', // Em breve
        'keys': '#', // Em breve
        'users': '#' // Em breve
    };
    
    const route = routes[moduleName];
    if (route && route !== '#') {
        window.location.href = route;
    } else {
        showNotification(`Módulo ${moduleName} estará disponível em breve!`, 'info');
    }
}

// Função para carregar informações da aplicação
async function loadApplicationInfo(appId) {
    try {
        const token = localStorage.getItem('token');
        if (!token) return;
        
        const response = await fetch(`${CONFIG.API_BASE_URL}/aplicacoes/${appId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        
        if (response.ok) {
            const app = await response.json();
            updateDashboardWithAppInfo(app);
        } else {
            console.error('Erro ao carregar informações da aplicação:', response.status);
        }
    } catch (error) {
        console.error('Erro na requisição:', error);
    }
}

// Função para atualizar dashboard com informações da aplicação
function updateDashboardWithAppInfo(app) {
    // Atualizar título da página
    const pageTitle = document.querySelector('.page-title');
    if (pageTitle) {
        pageTitle.textContent = `Dashboard - ${app.nome}`;
    }
    
    // Atualizar subtítulo
    const pageSubtitle = document.querySelector('.page-subtitle');
    if (pageSubtitle) {
        pageSubtitle.textContent = `Gerencie todos os aspectos da aplicação ${app.nome}`;
    }
    
    console.log('Dashboard atualizado com informações da aplicação:', app);
}

// Atualizar dados periodicamente (opcional)
setInterval(() => {
    loadStats();
    checkSelectedApplication();
}, 30000); // A cada 30 segundos