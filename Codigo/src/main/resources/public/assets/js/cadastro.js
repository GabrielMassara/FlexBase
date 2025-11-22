// Elementos do DOM
let cadastroForm, nomeInput, sobrenomeInput, emailInput, senhaInput, confirmarSenhaInput;
let cadastroBtn, btnText, loadingSpinner, alertContainer, passwordRequirements;

// Inicializar quando o DOM estiver carregado
document.addEventListener('DOMContentLoaded', function() {
    initializeElements();
    checkIfLoggedIn();
    setupEventListeners();
    nomeInput?.focus();
});

function initializeElements() {
    cadastroForm = document.getElementById('cadastroForm');
    nomeInput = document.getElementById('nome');
    sobrenomeInput = document.getElementById('sobrenome');
    emailInput = document.getElementById('email');
    senhaInput = document.getElementById('senha');
    confirmarSenhaInput = document.getElementById('confirmarSenha');
    cadastroBtn = document.getElementById('cadastroBtn');
    btnText = cadastroBtn?.querySelector('.btn-text');
    loadingSpinner = cadastroBtn?.querySelector('.loading-spinner');
    alertContainer = document.getElementById('alertContainer');
    passwordRequirements = document.getElementById('passwordRequirements');
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
    if (!cadastroBtn) return;
    
    if (loading) {
        cadastroBtn.disabled = true;
        if (btnText) btnText.textContent = 'Criando conta...';
        if (loadingSpinner) loadingSpinner.style.display = 'inline-block';
    } else {
        cadastroBtn.disabled = false;
        if (btnText) btnText.textContent = 'Criar Conta';
        if (loadingSpinner) loadingSpinner.style.display = 'none';
    }
}

function validatePassword() {
    if (!senhaInput || !confirmarSenhaInput || !passwordRequirements) return;
    
    const senha = senhaInput.value;
    const confirmarSenha = confirmarSenhaInput.value;
    
    // Mostrar requisitos quando começar a digitar a senha
    if (senha.length > 0) {
        passwordRequirements.style.display = 'block';
    } else {
        passwordRequirements.style.display = 'none';
        return;
    }
    
    // Validar comprimento
    const reqLength = document.getElementById('req-length');
    if (reqLength) {
        if (senha.length >= 6) {
            reqLength.classList.add('valid');
            reqLength.innerHTML = '<i class="bi bi-check"></i> Mínimo de 6 caracteres';
            senhaInput.classList.remove('is-invalid');
            senhaInput.classList.add('is-valid');
        } else {
            reqLength.classList.remove('valid');
            reqLength.innerHTML = '<i class="bi bi-x"></i> Mínimo de 6 caracteres';
            senhaInput.classList.remove('is-valid');
            senhaInput.classList.add('is-invalid');
        }
    }
    
    // Validar confirmação de senha
    const reqMatch = document.getElementById('req-match');
    if (reqMatch) {
        if (confirmarSenha.length > 0) {
            if (senha === confirmarSenha && senha.length >= 6) {
                reqMatch.classList.add('valid');
                reqMatch.innerHTML = '<i class="bi bi-check"></i> As senhas coincidem';
                confirmarSenhaInput.classList.remove('is-invalid');
                confirmarSenhaInput.classList.add('is-valid');
            } else {
                reqMatch.classList.remove('valid');
                reqMatch.innerHTML = '<i class="bi bi-x"></i> As senhas devem coincidir';
                confirmarSenhaInput.classList.remove('is-valid');
                confirmarSenhaInput.classList.add('is-invalid');
            }
        } else {
            confirmarSenhaInput.classList.remove('is-valid', 'is-invalid');
        }
    }
}

async function cadastrar(nome, sobrenome, email, senha) {
    try {
        const response = await fetch(ConfigUtils.getEndpointUrl('USUARIOS'), {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ nome, sobrenome, email, senha })
        });
        
        const data = await response.json();
        
        if (response.ok) {
            // Cadastro bem-sucedido
            showAlert('Conta criada com sucesso! Redirecionando para o login...', 'success');
            
            // Redirecionar após 2 segundos
            setTimeout(() => {
                window.location.href = '../login/index.html';
            }, 2000);
        } else {
            // Erro no cadastro - usar 'mensagem' ou 'message'
            const errorMessage = data.mensagem || data.message || 'Erro ao criar conta. Tente novamente.';
            showAlert(errorMessage);
        }
    } catch (error) {
        console.error('Erro na requisição:', error);
        showAlert('Erro de conexão. Verifique sua internet e tente novamente.');
    }
}

function setupEventListeners() {
    // Validação de senha em tempo real
    if (senhaInput) {
        senhaInput.addEventListener('input', validatePassword);
    }
    if (confirmarSenhaInput) {
        confirmarSenhaInput.addEventListener('input', validatePassword);
    }
    
    if (cadastroForm) {
        cadastroForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            clearAlerts();
            
            const nome = nomeInput.value.trim();
            const sobrenome = sobrenomeInput.value.trim();
            const email = emailInput.value.trim();
            const senha = senhaInput.value.trim();
            const confirmarSenha = confirmarSenhaInput.value.trim();
            
            // Validações básicas
            if (!nome || !sobrenome || !email || !senha || !confirmarSenha) {
                showAlert('Por favor, preencha todos os campos.');
                return;
            }
            
            if (!email.includes('@')) {
                showAlert('Por favor, insira um email válido.');
                return;
            }
            
            if (senha.length < 6) {
                showAlert('A senha deve ter pelo menos 6 caracteres.');
                return;
            }
            
            if (senha !== confirmarSenha) {
                showAlert('As senhas não coincidem.');
                return;
            }
            
            setLoading(true);
            
            try {
                await cadastrar(nome, sobrenome, email, senha);
            } finally {
                setLoading(false);
            }
        });
    }
}