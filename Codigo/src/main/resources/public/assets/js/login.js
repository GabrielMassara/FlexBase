// Elementos do DOM
let loginForm, emailInput, senhaInput, loginBtn, btnText, loadingSpinner, alertContainer;

// Inicializar quando o DOM estiver carregado
document.addEventListener('DOMContentLoaded', function() {
    initializeElements();
    checkIfLoggedIn();
    setupEventListeners();
    emailInput?.focus();
});

function initializeElements() {
    loginForm = document.getElementById('loginForm');
    emailInput = document.getElementById('email');
    senhaInput = document.getElementById('senha');
    loginBtn = document.getElementById('loginBtn');
    btnText = loginBtn?.querySelector('.btn-text');
    loadingSpinner = loginBtn?.querySelector('.loading-spinner');
    alertContainer = document.getElementById('alertContainer');
}

function checkIfLoggedIn() {
    if (ConfigUtils.isAuthenticated()) {
        // Redirecionar para home se já estiver logado
        window.location.href = '../home/index.html';
    }
}

function showAlert(message, type = 'danger') {
    if (!alertContainer) return;
    
    alertContainer.innerHTML = `
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
            <i class="bi ${type === 'success' ? 'bi-check-circle' : 'bi-exclamation-triangle'} me-2"></i>
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
}

function clearAlerts() {
    if (alertContainer) {
        alertContainer.innerHTML = '';
    }
}

function setLoading(loading) {
    if (!loginBtn) return;
    
    if (loading) {
        loginBtn.disabled = true;
        if (btnText) btnText.textContent = 'Entrando...';
        if (loadingSpinner) loadingSpinner.style.display = 'inline-block';
    } else {
        loginBtn.disabled = false;
        if (btnText) btnText.textContent = 'Entrar';
        if (loadingSpinner) loadingSpinner.style.display = 'none';
    }
}

async function login(email, senha) {
    try {
        const response = await fetch(ConfigUtils.getEndpointUrl('LOGIN'), {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, senha })
        });
        
        const data = await response.json();
        
        // Verificar tanto 'sucesso' quanto 'success' para compatibilidade
        if (response.ok && (data.sucesso || data.success)) {
            // Login bem-sucedido - salvar usando as constantes do config
            localStorage.setItem(CONFIG.AUTH.TOKEN_KEY, data.token);
            localStorage.setItem(CONFIG.AUTH.USER_KEY, JSON.stringify(data.usuario));
            
            showAlert('Login realizado com sucesso! Redirecionando...', 'success');
            
            // Redirecionar após 1.5 segundos para home
            setTimeout(() => {
                window.location.href = '../home/index.html';
            }, 1500);
        } else {
            // Erro no login - usar 'mensagem' ou 'message'
            const errorMessage = data.mensagem || data.message || 'Erro ao fazer login. Verifique suas credenciais.';
            showAlert(errorMessage);
        }
    } catch (error) {
        console.error('Erro na requisição:', error);
        showAlert('Erro de conexão. Verifique sua internet e tente novamente.');
    }
}

function setupEventListeners() {
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            clearAlerts();
            
            const email = emailInput.value.trim();
            const senha = senhaInput.value.trim();
            
            // Validações básicas
            if (!email || !senha) {
                showAlert('Por favor, preencha todos os campos.');
                return;
            }
            
            if (!email.includes('@')) {
                showAlert('Por favor, insira um email válido.');
                return;
            }
            
            setLoading(true);
            
            try {
                await login(email, senha);
            } finally {
                setLoading(false);
            }
        });
    }
    
    // Enter para submeter
    document.addEventListener('keypress', (e) => {
        if (e.key === 'Enter' && loginBtn && !loginBtn.disabled) {
            loginForm.dispatchEvent(new Event('submit'));
        }
    });
}