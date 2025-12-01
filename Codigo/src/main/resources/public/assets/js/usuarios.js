// Configuração da API
const API_BASE = CONFIG?.API_BASE_URL || 'http://localhost:80/api';

// Estado da aplicação
let aplicacaoSelecionada = null;
let aplicacaoInfo = null;
let usuarios = [];
let keys = [];

// Inicialização
document.addEventListener('DOMContentLoaded', function() {
    verificarAutenticacao();
    obterIdAplicacaoDaUrl();
    configurarEventos();
});

// Verificar se o usuário está autenticado
function verificarAutenticacao() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '../login/index.html';
        return;
    }
}

// Obter ID da aplicação da URL
function obterIdAplicacaoDaUrl() {
    const urlParams = new URLSearchParams(window.location.search);
    const idAplicacao = urlParams.get('id');
    
    console.log('Parâmetros da URL:', window.location.search);
    console.log('ID da aplicação extraído:', idAplicacao);
    
    if (!idAplicacao || isNaN(idAplicacao)) {
        console.error('ID da aplicação inválido:', idAplicacao);
        mostrarToast('ID da aplicação não fornecido ou inválido', 'error');
        setTimeout(() => {
            window.location.href = '../dashboard/index.html';
        }, 2000);
        return;
    }
    
    aplicacaoSelecionada = parseInt(idAplicacao);
    console.log('Aplicação selecionada:', aplicacaoSelecionada);
    carregarDadosIniciais();
}

// Carregar dados iniciais da aplicação
async function carregarDadosIniciais() {
    await carregarInfoAplicacao();
    await carregarKeysDaAplicacao(); // Carregar keys antes dos usuários
    await carregarUsuariosDaAplicacao();
}

// Configurar eventos
function configurarEventos() {
    // Botão de refresh
    document.getElementById('btnRefresh').addEventListener('click', function() {
        if (aplicacaoSelecionada) {
            carregarUsuariosDaAplicacao();
        }
    });

    // Toggle de visualização
    document.querySelectorAll('input[name="viewType"]').forEach(radio => {
        radio.addEventListener('change', function() {
            alternarVisualizacao(this.value);
        });
    });

    // Botão de confirmar alteração de key
    document.getElementById('btnConfirmarAlterarKey').addEventListener('click', alterarKeyUsuario);

    // Botão de confirmar remoção
    document.getElementById('btnConfirmarRemover').addEventListener('click', removerUsuario);

    // Logout
    document.getElementById('logoutBtn').addEventListener('click', function() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '../login/index.html';
    });
}

// Carregar informações da aplicação
async function carregarInfoAplicacao() {
    if (!aplicacaoSelecionada) {
        console.error('Nenhuma aplicação selecionada');
        mostrarToast('Nenhuma aplicação selecionada', 'error');
        return;
    }
    
    try {
        const token = localStorage.getItem('token');
        console.log('Carregando aplicação ID:', aplicacaoSelecionada);
        
        const response = await fetch(`${API_BASE}/aplicacoes/${aplicacaoSelecionada}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        console.log('Response status:', response.status);
        
        if (response.ok) {
            const data = await response.json();
            console.log('Dados da aplicação:', data);
            
            // Tratar tanto formato com wrapper quanto formato direto
            if (data.success && data.data) {
                aplicacaoInfo = data.data;
            } else if (data.id && data.nome) {
                // Formato direto da API
                aplicacaoInfo = data;
            } else {
                console.error('Resposta inválida da API:', data);
                mostrarToast('Dados da aplicação não encontrados', 'error');
                return;
            }
            
            // Atualizar título da página
            document.querySelector('.page-title').innerHTML = `
                <i class="bi bi-people me-2"></i>
                Usuários: ${aplicacaoInfo.nome}
            `;
            
            // Atualizar nome da aplicação no cabeçalho
            const nomeAplicacaoElement = document.getElementById('nomeAplicacao');
            if (nomeAplicacaoElement) {
                nomeAplicacaoElement.textContent = aplicacaoInfo.nome;
            }
            
            document.title = `FlexBase - Usuários de ${aplicacaoInfo.nome}`;
            
            console.log('Informações da aplicação carregadas:', aplicacaoInfo);
        } else {
            console.error('Erro na resposta:', response.status, response.statusText);
            const errorData = await response.json().catch(() => ({}));
            console.error('Detalhes do erro:', errorData);
            
            mostrarToast(`Erro ao carregar aplicação (${response.status})`, 'error');
            setTimeout(() => {
                window.location.href = '../dashboard/index.html';
            }, 2000);
        }
    } catch (error) {
        console.error('Erro ao carregar aplicação:', error);
        mostrarToast('Erro de conexão ao carregar aplicação', 'error');
    }
}

// Carregar usuários da aplicação
async function carregarUsuariosDaAplicacao() {
    if (!aplicacaoSelecionada) return;

    mostrarLoading();
    
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`${API_BASE}/aplicacao/${aplicacaoSelecionada}/usuarios`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const data = await response.json();
            usuarios = data.data || [];
            await carregarDadosComplementares();
            renderizarUsuarios();
        } else {
            mostrarToast('Erro ao carregar usuários', 'error');
            mostrarEmpty();
        }
    } catch (error) {
        console.error('Erro:', error);
        mostrarToast('Erro de conexão', 'error');
        mostrarEmpty();
    } finally {
        esconderLoading();
    }
}

// Carregar dados complementares dos usuários
async function carregarDadosComplementares() {
    const token = localStorage.getItem('token');
    
    // Para cada usuário, carregar seus dados do sistema principal
    for (let usuario of usuarios) {
        try {
            console.log('Carregando dados para usuário:', usuario);
            
            // Verificar se id_usuario existe
            if (!usuario.id_usuario && !usuario.idUsuario) {
                console.warn('ID do usuário não encontrado:', usuario);
                usuario.nome_completo = 'Usuário não encontrado';
                usuario.email = 'Email não disponível';
                continue;
            }
            
            const idUsuario = usuario.id_usuario || usuario.idUsuario;
            
            // Carregar dados do usuário principal
            const userResponse = await fetch(`${API_BASE}/usuarios/${idUsuario}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });
            
            if (userResponse.ok) {
                const userData = await userResponse.json();
                console.log('Dados do usuário carregados:', userData);
                
                // Tratar tanto formato com wrapper quanto formato direto
                let userInfo;
                if (userData.success && userData.data) {
                    userInfo = userData.data;
                } else if (userData.id) {
                    userInfo = userData;
                } else {
                    console.warn('Formato de dados do usuário inválido:', userData);
                    continue;
                }
                
                usuario.nome_completo = `${userInfo.nome || ''} ${userInfo.sobrenome || ''}`.trim();
                usuario.email = userInfo.email || 'Email não disponível';
            } else {
                console.warn('Erro ao carregar usuário:', userResponse.status);
                usuario.nome_completo = 'Usuário não encontrado';
                usuario.email = 'Email não disponível';
            }

            // Carregar dados da key
            const idKey = usuario.id_key || usuario.idKey;
            console.log('ID da key do usuário:', idKey);
            console.log('Keys disponíveis:', keys);
            
            if (idKey && keys.length > 0) {
                const key = keys.find(k => k.id === idKey);
                console.log('Key encontrada:', key);
                
                if (key) {
                    usuario.nome_key = key.nome;
                    usuario.codigo_key = key.codigo;
                } else {
                    console.warn(`Key com ID ${idKey} não encontrada nas keys disponíveis`);
                    usuario.nome_key = 'Key não encontrada';
                    usuario.codigo_key = '';
                }
            } else {
                console.warn('ID da key não encontrado ou lista de keys vazia');
                usuario.nome_key = 'Sem key';
                usuario.codigo_key = '';
            }
        } catch (error) {
            console.error('Erro ao carregar dados complementares para usuário:', usuario, error);
            usuario.nome_completo = 'Erro ao carregar';
            usuario.email = 'Erro ao carregar';
        }
    }
}

// Carregar keys da aplicação
async function carregarKeysDaAplicacao() {
    if (!aplicacaoSelecionada) return;
    
    try {
        const token = localStorage.getItem('token');
        console.log('Carregando keys da aplicação ID:', aplicacaoSelecionada);
        
        const response = await fetch(`${API_BASE}/keys/aplicacao/${aplicacaoSelecionada}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        console.log('Response keys status:', response.status);

        if (response.ok) {
            const data = await response.json();
            console.log('Resposta das keys:', data);
            
            // Tratar tanto formato com wrapper quanto formato direto
            if (data.success && data.data) {
                keys = data.data;
            } else if (Array.isArray(data)) {
                keys = data;
            } else {
                keys = [];
            }
            
            console.log('Keys carregadas:', keys);
        } else {
            console.error('Erro ao carregar keys:', response.status);
            keys = [];
        }
    } catch (error) {
        console.error('Erro ao carregar keys:', error);
        keys = [];
    }
}

// Alternar tipo de visualização
function alternarVisualizacao(tipo) {
    const cardsList = document.getElementById('usuariosList');
    const table = document.getElementById('usuariosTable');
    
    if (tipo === 'table') {
        cardsList.style.display = 'none';
        table.style.display = 'block';
        renderizarTabelaUsuarios();
    } else {
        cardsList.style.display = 'block';
        table.style.display = 'none';
        renderizarCardsUsuarios();
    }
}

// Renderizar lista de usuários
function renderizarUsuarios() {
    const tipoVisualizacao = document.querySelector('input[name="viewType"]:checked').value;
    
    if (usuarios.length === 0) {
        mostrarEmpty();
        return;
    }

    if (tipoVisualizacao === 'table') {
        renderizarTabelaUsuarios();
    } else {
        renderizarCardsUsuarios();
    }
}

// Renderizar usuários em cards
function renderizarCardsUsuarios() {
    const container = document.getElementById('usuariosList');
    container.innerHTML = '';
    
    usuarios.forEach(usuario => {
        const userCard = criarCardUsuario(usuario);
        container.appendChild(userCard);
    });
}

// Renderizar usuários em tabela
function renderizarTabelaUsuarios() {
    const tbody = document.getElementById('usuariosTableBody');
    tbody.innerHTML = '';
    
    usuarios.forEach(usuario => {
        const row = criarLinhaTabela(usuario);
        tbody.appendChild(row);
    });
}

// Criar card de usuário
function criarCardUsuario(usuario) {
    const col = document.createElement('div');
    col.className = 'col-lg-6 col-xl-4 mb-3';
    
    const statusClass = usuario.ativo ? 'success' : 'secondary';
    const statusText = usuario.ativo ? 'Ativo' : 'Inativo';
    const statusIcon = usuario.ativo ? 'check-circle-fill' : 'x-circle-fill';

    col.innerHTML = `
        <div class="card bg-dark border-secondary h-100">
            <div class="card-header d-flex justify-content-between align-items-center border-secondary">
                <div class="d-flex align-items-center">
                    <i class="bi bi-person-circle text-info me-2" style="font-size: 1.2rem;"></i>
                    <h6 class="mb-0 text-light">${usuario.nome_completo || 'Nome não encontrado'}</h6>
                </div>
                <span class="badge bg-${statusClass}">
                    <i class="bi bi-${statusIcon} me-1"></i>
                    ${statusText}
                </span>
            </div>
            <div class="card-body">
                <div class="mb-2">
                    <small class="text-muted">Email:</small>
                    <div class="text-light">${usuario.email || 'Email não encontrado'}</div>
                </div>
                
                <div class="mb-2">
                    <small class="text-muted">Key de Acesso:</small>
                    <div class="text-warning">
                        <i class="bi bi-key me-1"></i>
                        ${usuario.nome_key || 'Key não encontrada'}
                        ${usuario.codigo_key ? `<small class="text-muted">(${usuario.codigo_key})</small>` : ''}
                    </div>
                </div>
                
                <div class="mb-3">
                    <small class="text-muted">Data de Vínculo:</small>
                    <div class="text-light">${formatarData(usuario.data_vinculo || usuario.dataVinculo)}</div>
                </div>
                
                <div class="d-grid gap-2">
                    <button class="btn btn-outline-primary btn-sm" onclick="abrirModalAlterarKey(${usuario.id}, '${(usuario.nome_completo || '').replace(/'/g, "\\'")}', ${usuario.id_key || usuario.idKey})">
                        <i class="bi bi-key me-1"></i>
                        Alterar Key
                    </button>
                    
                    <div class="btn-group" role="group">
                        <button class="btn btn-outline-${usuario.ativo ? 'warning' : 'success'} btn-sm" 
                                onclick="alterarStatusUsuario(${usuario.id}, ${!usuario.ativo})">
                            <i class="bi bi-${usuario.ativo ? 'pause' : 'play'} me-1"></i>
                            ${usuario.ativo ? 'Desativar' : 'Ativar'}
                        </button>
                        
                        <button class="btn btn-outline-danger btn-sm" 
                                onclick="abrirModalRemoverUsuario(${usuario.id}, '${(usuario.nome_completo || '').replace(/'/g, "\\'")}')">
                            <i class="bi bi-trash me-1"></i>
                            Remover
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    return col;
}

// Criar linha da tabela de usuário
function criarLinhaTabela(usuario) {
    const row = document.createElement('tr');
    
    const statusClass = usuario.ativo ? 'success' : 'secondary';
    const statusText = usuario.ativo ? 'Ativo' : 'Inativo';
    const statusIcon = usuario.ativo ? 'check-circle-fill' : 'x-circle-fill';

    row.innerHTML = `
        <td>
            <span class="badge bg-${statusClass}">
                <i class="bi bi-${statusIcon} me-1"></i>
                ${statusText}
            </span>
        </td>
        <td>
            <div class="d-flex align-items-center">
                <i class="bi bi-person-circle text-info me-2" style="font-size: 1.2rem;"></i>
                <strong class="text-light">${usuario.nome_completo || 'Nome não encontrado'}</strong>
            </div>
        </td>
        <td class="text-light">${usuario.email || 'Email não encontrado'}</td>
        <td>
            <div class="text-warning">
                <i class="bi bi-key me-1"></i>
                ${usuario.nome_key || 'Key não encontrada'}
                ${usuario.codigo_key ? `<br><small class="text-muted">(${usuario.codigo_key})</small>` : ''}
            </div>
        </td>
        <td class="text-light">${formatarData(usuario.data_vinculo || usuario.dataVinculo)}</td>
        <td class="text-center">
            <div class="btn-group btn-group-sm" role="group">
                <button class="btn btn-outline-primary" 
                        onclick="abrirModalAlterarKey(${usuario.id}, '${(usuario.nome_completo || '').replace(/'/g, "\\'")}', ${usuario.id_key || usuario.idKey})"
                        title="Alterar Key">
                    <i class="bi bi-key"></i>
                </button>
                
                <button class="btn btn-outline-${usuario.ativo ? 'warning' : 'success'}" 
                        onclick="alterarStatusUsuario(${usuario.id}, ${!usuario.ativo})"
                        title="${usuario.ativo ? 'Desativar' : 'Ativar'} Usuário">
                    <i class="bi bi-${usuario.ativo ? 'pause' : 'play'}"></i>
                </button>
                
                <button class="btn btn-outline-danger" 
                        onclick="abrirModalRemoverUsuario(${usuario.id}, '${(usuario.nome_completo || '').replace(/'/g, "\\'")}')"
                        title="Remover Usuário">
                    <i class="bi bi-trash"></i>
                </button>
            </div>
        </td>
    `;
    
    return row;
}

// Abrir modal para alterar key
function abrirModalAlterarKey(idUsuarioAplicacao, nomeUsuario, idKeyAtual) {
    document.getElementById('usuarioAplicacaoId').value = idUsuarioAplicacao;
    document.getElementById('nomeUsuarioSelecionado').textContent = nomeUsuario;
    
    // Preencher select de keys
    const selectKey = document.getElementById('selectNovaKey');
    selectKey.innerHTML = '<option value="">Selecione uma key...</option>';
    
    keys.forEach(key => {
        if (key.ativo) {
            const option = document.createElement('option');
            option.value = key.id;
            option.textContent = `${key.nome} (${key.codigo})`;
            option.selected = key.id === idKeyAtual;
            selectKey.appendChild(option);
        }
    });
    
    const modal = new bootstrap.Modal(document.getElementById('alterarKeyModal'));
    modal.show();
}

// Alterar key do usuário
async function alterarKeyUsuario() {
    const idUsuarioAplicacao = document.getElementById('usuarioAplicacaoId').value;
    const novaIdKey = document.getElementById('selectNovaKey').value;
    
    if (!novaIdKey) {
        mostrarToast('Selecione uma key', 'warning');
        return;
    }

    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`${API_BASE}/aplicacao/${aplicacaoSelecionada}/usuarios/${idUsuarioAplicacao}/key`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                id_key: parseInt(novaIdKey)
            })
        });

        if (response.ok) {
            mostrarToast('Key alterada com sucesso!', 'success');
            bootstrap.Modal.getInstance(document.getElementById('alterarKeyModal')).hide();
            carregarUsuariosDaAplicacao(); // Recarregar lista
        } else {
            const data = await response.json();
            mostrarToast(data.message || 'Erro ao alterar key', 'error');
        }
    } catch (error) {
        console.error('Erro:', error);
        mostrarToast('Erro de conexão', 'error');
    }
}

// Abrir modal para remover usuário
function abrirModalRemoverUsuario(idUsuarioAplicacao, nomeUsuario) {
    document.getElementById('nomeUsuarioRemover').textContent = nomeUsuario;
    document.getElementById('btnConfirmarRemover').setAttribute('data-id', idUsuarioAplicacao);
    
    const modal = new bootstrap.Modal(document.getElementById('removerUsuarioModal'));
    modal.show();
}

// Remover usuário da aplicação
async function removerUsuario() {
    const idUsuarioAplicacao = document.getElementById('btnConfirmarRemover').getAttribute('data-id');
    
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`${API_BASE}/aplicacao/${aplicacaoSelecionada}/usuarios/${idUsuarioAplicacao}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            mostrarToast('Usuário removido com sucesso!', 'success');
            bootstrap.Modal.getInstance(document.getElementById('removerUsuarioModal')).hide();
            carregarUsuariosDaAplicacao(); // Recarregar lista
        } else {
            const data = await response.json();
            mostrarToast(data.message || 'Erro ao remover usuário', 'error');
        }
    } catch (error) {
        console.error('Erro:', error);
        mostrarToast('Erro de conexão', 'error');
    }
}

// Alterar status do usuário
async function alterarStatusUsuario(idUsuarioAplicacao, novoStatus) {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`${API_BASE}/aplicacao/${aplicacaoSelecionada}/usuarios/${idUsuarioAplicacao}/status`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                ativo: novoStatus
            })
        });

        if (response.ok) {
            const status = novoStatus ? 'ativado' : 'desativado';
            mostrarToast(`Usuário ${status} com sucesso!`, 'success');
            carregarUsuariosDaAplicacao(); // Recarregar lista
        } else {
            const data = await response.json();
            mostrarToast(data.message || 'Erro ao alterar status', 'error');
        }
    } catch (error) {
        console.error('Erro:', error);
        mostrarToast('Erro de conexão', 'error');
    }
}

// Funções de UI
function mostrarLoading() {
    document.getElementById('loadingUsers').style.display = 'block';
    document.getElementById('emptyUsers').style.display = 'none';
    document.getElementById('usuariosList').innerHTML = '';
    document.getElementById('usuariosTableBody').innerHTML = '';
}

function esconderLoading() {
    document.getElementById('loadingUsers').style.display = 'none';
}

function mostrarEmpty() {
    document.getElementById('emptyUsers').style.display = 'block';
    document.getElementById('loadingUsers').style.display = 'none';
    document.getElementById('usuariosList').innerHTML = '';
    document.getElementById('usuariosTableBody').innerHTML = '';
}

// Formatar data
function formatarData(dataString) {
    if (!dataString) return 'Data não disponível';
    
    try {
        const data = new Date(dataString);
        return data.toLocaleString('pt-BR', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch (error) {
        return 'Data inválida';
    }
}

// Função para voltar ao dashboard com ID da aplicação
function voltarParaDashboard() {
    if (aplicacaoSelecionada) {
        window.location.href = `../dashboard/?id=${aplicacaoSelecionada}`;
    } else {
        window.location.href = '../dashboard/';
    }
}

// Mostrar toast de notificação
function mostrarToast(mensagem, tipo = 'info') {
    const toastContainer = document.querySelector('.toast-container');
    
    const toastElement = document.createElement('div');
    toastElement.className = `toast align-items-center text-bg-${tipo === 'error' ? 'danger' : tipo} border-0`;
    toastElement.setAttribute('role', 'alert');
    toastElement.setAttribute('aria-live', 'assertive');
    toastElement.setAttribute('aria-atomic', 'true');
    
    toastElement.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">
                ${mensagem}
            </div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
    `;
    
    toastContainer.appendChild(toastElement);
    
    const toast = new bootstrap.Toast(toastElement);
    toast.show();
    
    // Remove o elemento após ser ocultado
    toastElement.addEventListener('hidden.bs.toast', () => {
        toastElement.remove();
    });
}