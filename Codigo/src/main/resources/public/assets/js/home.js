// JavaScript para a home page
let userProjects = [];

// Inicializar quando o DOM estiver carregado
document.addEventListener('DOMContentLoaded', function() {
    initializeHomePage();
});

async function initializeHomePage() {
    // Verificar se está logado
    if (!ConfigUtils.isAuthenticated()) {
        window.location.href = '../login/index.html';
        return;
    }
    
    // Configurar navbar
    setupNavbar();
    
    // Carregar projetos do usuário
    await loadUserProjects();
    
    // Configurar event listeners
    setupEventListeners();
}

function setupNavbar() {
    const user = ConfigUtils.getUser();
    if (user) {
        // Atualizar informações do usuário na navbar se necessário
        console.log('Usuário logado:', user.nome);
    }
}

function setupEventListeners() {
    // Botão criar projeto
    const createProjectBtn = document.getElementById('createProjectBtn');
    if (createProjectBtn) {
        createProjectBtn.addEventListener('click', handleCreateProject);
    }
    
    // Toggle tema
    const themeToggle = document.getElementById('themeToggle');
    if (themeToggle) {
        themeToggle.addEventListener('click', toggleTheme);
    }
    
    // Logout
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', handleLogout);
    }
}

async function loadUserProjects() {
    const loadingContainer = document.getElementById('loadingContainer');
    const projectsContainer = document.getElementById('projectsContainer');
    
    try {
        // Mostrar loading
        if (loadingContainer) loadingContainer.style.display = 'block';
        if (projectsContainer) projectsContainer.style.display = 'none';
        
        // Fazer requisição para buscar projetos
        const response = await fetch(ConfigUtils.getEndpointUrl('APLICACOES_MINHAS'), {
            method: 'GET',
            headers: ConfigUtils.getAuthHeaders()
        });
        
        if (response.status === 401) {
            // Token inválido, redirecionar para login
            ConfigUtils.clearAuth();
            window.location.href = '../login/index.html';
            return;
        }
        
        if (!response.ok) {
            throw new Error(`Erro na requisição: ${response.status}`);
        }
        
        const data = await response.json();
        userProjects = Array.isArray(data) ? data : (data.data || []);
        
        // Renderizar projetos
        renderProjects();
        
    } catch (error) {
        console.error('Erro ao carregar projetos:', error);
        showError('Erro ao carregar seus projetos. Tente novamente.');
    } finally {
        // Ocultar loading
        if (loadingContainer) loadingContainer.style.display = 'none';
        if (projectsContainer) projectsContainer.style.display = 'block';
    }
}

function renderProjects() {
    const projectsGrid = document.getElementById('projectsGrid');
    const emptyState = document.getElementById('emptyState');
    
    if (!projectsGrid) return;
    
    if (userProjects.length === 0) {
        // Mostrar estado vazio
        projectsGrid.style.display = 'none';
        if (emptyState) emptyState.style.display = 'block';
        return;
    }
    
    // Ocultar estado vazio
    if (emptyState) emptyState.style.display = 'none';
    projectsGrid.style.display = 'grid';
    
    // Renderizar cards dos projetos
    projectsGrid.innerHTML = userProjects.map(project => createProjectCard(project)).join('');
}

function createProjectCard(project) {
    const description = project.readme || 'Nenhuma descrição disponível para este projeto.';
    const database = project.nomeBanco || 'Não especificado';
    
    return `
        <div class="project-card">
            <div class="project-card-header">
                <h3 class="project-name">${escapeHtml(project.nome)}</h3>
                <span class="project-id">#${project.id}</span>
            </div>
            
            <p class="project-description">${escapeHtml(description)}</p>
            
            <div class="project-meta">
                <div class="project-database">
                    <i class="bi bi-database"></i>
                    <span>${escapeHtml(database)}</span>
                </div>
            </div>
            
            <div class="project-actions">
                <a href="#" class="btn-project-action btn-primary" onclick="viewProject(${project.id})">
                    <i class="bi bi-eye me-1"></i>Visualizar
                </a>
                <a href="#" class="btn-project-action" onclick="editProject(${project.id})">
                    <i class="bi bi-pencil me-1"></i>Editar
                </a>
                <a href="#" class="btn-project-action" onclick="manageEndpoints(${project.id})">
                    <i class="bi bi-list-ul me-1"></i>APIs
                </a>
            </div>
        </div>
    `;
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function handleCreateProject() {
    // Por enquanto apenas um alert
    alert('Funcionalidade de criar projeto em desenvolvimento!');
}

function viewProject(projectId) {
    const project = userProjects.find(p => p.id === projectId);
    if (project) {
        alert(`Visualizando projeto: ${project.nome}\\nID: ${projectId}`);
    }
}

function editProject(projectId) {
    const project = userProjects.find(p => p.id === projectId);
    if (project) {
        alert(`Editando projeto: ${project.nome}\\nID: ${projectId}`);
    }
}

function manageEndpoints(projectId) {
    const project = userProjects.find(p => p.id === projectId);
    if (project) {
        alert(`Gerenciando APIs do projeto: ${project.nome}\\nID: ${projectId}`);
    }
}

function toggleTheme() {
    const html = document.documentElement;
    const currentTheme = html.getAttribute('data-bs-theme');
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
    
    html.setAttribute('data-bs-theme', newTheme);
    localStorage.setItem(CONFIG.THEME.STORAGE_KEY, newTheme);
    
    // Atualizar ícone do botão
    const themeIcon = document.querySelector('#themeToggle i');
    if (themeIcon) {
        themeIcon.className = newTheme === 'dark' ? 'bi bi-sun' : 'bi bi-moon';
    }
}

function handleLogout() {
    if (confirm('Tem certeza que deseja sair?')) {
        ConfigUtils.clearAuth();
        window.location.href = '../../index.html';
    }
}

function showError(message) {
    // Criar ou atualizar container de erro
    let errorContainer = document.getElementById('errorContainer');
    if (!errorContainer) {
        errorContainer = document.createElement('div');
        errorContainer.id = 'errorContainer';
        errorContainer.className = 'alert alert-danger alert-dismissible fade show';
        errorContainer.style.position = 'fixed';
        errorContainer.style.top = '100px';
        errorContainer.style.right = '20px';
        errorContainer.style.zIndex = '1050';
        errorContainer.style.maxWidth = '400px';
        document.body.appendChild(errorContainer);
    }
    
    errorContainer.innerHTML = `
        <i class="bi bi-exclamation-triangle me-2"></i>
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    // Auto-remover após 5 segundos
    setTimeout(() => {
        if (errorContainer) {
            errorContainer.remove();
        }
    }, 5000);
}