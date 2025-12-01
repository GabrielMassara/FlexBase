// Configuração da API
const API_BASE = CONFIG?.API_BASE_URL || 'http://localhost:80/api';

// Estado da aplicação
let aplicacaoSelecionada = null;
let aplicacaoInfo = null;
let endpoints = [];
let tabelas = [];

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
    await carregarTabelasDaAplicacao();
    await carregarEndpointsDaAplicacao();
}

// Configurar eventos
function configurarEventos() {
    // Botão de refresh
    document.getElementById('btnRefresh').addEventListener('click', function() {
        if (aplicacaoSelecionada) {
            carregarEndpointsDaAplicacao();
        }
    });

    // Toggle de visualização
    document.querySelectorAll('input[name="viewType"]').forEach(radio => {
        radio.addEventListener('change', function() {
            alternarVisualizacao(this.value);
        });
    });

    // Botão novo endpoint
    document.getElementById('btnNovoEndpoint').addEventListener('click', abrirModalNovoEndpoint);

    // Botão salvar endpoint
    document.getElementById('btnSalvarEndpoint').addEventListener('click', salvarEndpoint);

    // Botão confirmar exclusão
    document.getElementById('btnConfirmarExcluir').addEventListener('click', excluirEndpoint);

    // Botão executar teste
    document.getElementById('btnExecutarTeste').addEventListener('click', executarTesteEndpoint);

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
                <i class="bi bi-cloud-arrow-up me-2"></i>
                API: ${aplicacaoInfo.nome}
            `;
            
            // Atualizar nome da aplicação no cabeçalho
            const nomeAplicacaoElement = document.getElementById('nomeAplicacao');
            if (nomeAplicacaoElement) {
                nomeAplicacaoElement.textContent = aplicacaoInfo.nome;
            }
            
            // Atualizar URL base da API
            const urlBaseElement = document.getElementById('urlBaseApi');
            if (urlBaseElement) {
                urlBaseElement.textContent = `${window.location.origin}/api/endpoints/${aplicacaoSelecionada}/`;
            }
            
            document.title = `FlexBase - API de ${aplicacaoInfo.nome}`;
            
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

// Carregar tabelas da aplicação
async function carregarTabelasDaAplicacao() {
    if (!aplicacaoSelecionada) return;
    
    try {
        const token = localStorage.getItem('token');
        console.log('Carregando tabelas da aplicação ID:', aplicacaoSelecionada);
        
        const response = await fetch(`${API_BASE}/registros/tabela/list?idAplicacao=${aplicacaoSelecionada}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const data = await response.json();
            console.log('Tabelas carregadas:', data);
            
            // Tratar tanto formato com wrapper quanto formato direto
            if (data.success && data.data) {
                tabelas = data.data;
            } else if (Array.isArray(data)) {
                tabelas = data;
            } else {
                tabelas = [];
            }
            
            // Preencher select de tabelas
            preencherSelectTabelas();
        } else {
            console.error('Erro ao carregar tabelas:', response.status);
            tabelas = [];
        }
    } catch (error) {
        console.error('Erro ao carregar tabelas:', error);
        tabelas = [];
    }
}

// Preencher select de tabelas
function preencherSelectTabelas() {
    const select = document.getElementById('tabelaAssociada');
    
    select.innerHTML = '<option value="">Selecione uma tabela...</option>';
    
    // Se não tiver tabelas específicas, usar algumas padrão baseadas no schema
    if (tabelas.length === 0 && aplicacaoInfo && aplicacaoInfo.schemaBanco) {
        try {
            const schema = aplicacaoInfo.schemaBanco;
            if (schema.tables) {
                Object.keys(schema.tables).forEach(tableName => {
                    const option = document.createElement('option');
                    option.value = tableName;
                    option.textContent = tableName;
                    select.appendChild(option);
                });
            }
        } catch (error) {
            console.error('Erro ao processar schema:', error);
        }
    } else {
        tabelas.forEach(tabela => {
            const option = document.createElement('option');
            option.value = tabela.nome || tabela;
            option.textContent = tabela.nome || tabela;
            select.appendChild(option);
        });
    }
}

// Carregar endpoints da aplicação
async function carregarEndpointsDaAplicacao() {
    if (!aplicacaoSelecionada) return;

    mostrarLoading();
    
    try {
        const token = localStorage.getItem('token');
        console.log('Carregando endpoints da aplicação ID:', aplicacaoSelecionada);
        
        const response = await fetch(`${API_BASE}/endpointsManager/aplicacao/${aplicacaoSelecionada}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        console.log('Response status:', response.status);

        if (response.ok) {
            const data = await response.json();
            console.log('Dados recebidos da API:', data);
            
            // A API pode retornar diretamente um array ou um objeto com wrapper
            if (Array.isArray(data)) {
                endpoints = data;
            } else if (data.data && Array.isArray(data.data)) {
                endpoints = data.data;
            } else if (data.success && Array.isArray(data.data)) {
                endpoints = data.data;
            } else {
                console.warn('Formato de resposta inesperado:', data);
                endpoints = [];
            }
            
            // Normalizar os dados dos endpoints para o formato esperado pelo frontend
            endpoints = endpoints.map(endpoint => {
                const operacao = extrairOperacaoDaQuery(endpoint.query || '');
                const metodoCorreto = obterMetodoHTTPCorreto(endpoint.metodoNome || endpoint.metodo, operacao);
                
                return {
                    id: endpoint.id,
                    metodo: metodoCorreto,
                    caminho: endpoint.rota || endpoint.caminho,
                    descricao: endpoint.descricao || `Endpoint ${metodoCorreto} para ${endpoint.rota || endpoint.caminho}`,
                    tabela: extrairTabelaDaQuery(endpoint.query || ''),
                    operacao: operacao,
                    parametros: extrairParametrosDaQuery(endpoint.query || ''),
                    ativo: endpoint.ativo !== false,
                    query: endpoint.query
                };
            });
            
            console.log('Endpoints processados:', endpoints);
            
            // Atualizar contador de endpoints
            document.getElementById('totalEndpoints').textContent = endpoints.length;
            
            renderizarEndpoints();
        } else {
            console.error('Erro na resposta da API:', response.status, response.statusText);
            mostrarToast('Erro ao carregar endpoints', 'error');
            mostrarEmpty();
        }
    } catch (error) {
        console.error('Erro ao carregar endpoints:', error);
        mostrarToast('Erro de conexão', 'error');
        mostrarEmpty();
    } finally {
        esconderLoading();
    }
}

// Função para extrair nome da tabela da query SQL
function extrairTabelaDaQuery(query) {
    if (!query) return '';
    
    // Procurar por padrões comuns de tabela
    const patterns = [
        /FROM\s+tb_registros.*?tabela\s*=\s*['"]([^'"]+)['"]/i,
        /VALUES\s*\(\s*['"]([^'"]+)['"],/i,
        /tabela\s*=\s*['"]([^'"]+)['"]/i
    ];
    
    for (const pattern of patterns) {
        const match = query.match(pattern);
        if (match && match[1]) {
            return match[1];
        }
    }
    
    return 'tb_registros';
}

// Função para extrair operação SQL da query
function extrairOperacaoDaQuery(query) {
    if (!query) return 'SELECT';
    
    const queryUpper = query.toUpperCase().trim();
    
    if (queryUpper.startsWith('SELECT')) return 'SELECT';
    if (queryUpper.startsWith('INSERT')) return 'INSERT';
    if (queryUpper.startsWith('UPDATE')) return 'UPDATE';
    if (queryUpper.startsWith('DELETE')) return 'DELETE';
    
    return 'SELECT';
}

// Função para extrair parâmetros da query
function extrairParametrosDaQuery(query) {
    if (!query) return '';
    
    const params = [];
    const matches = query.match(/\$\{([^}]+)\}/g);
    
    if (matches) {
        matches.forEach(match => {
            const param = match.replace(/\$\{|\}/g, '');
            if (!params.includes(param)) {
                params.push(param);
            }
        });
    }
    
    return params.join(', ');
}

// Alternar tipo de visualização
function alternarVisualizacao(tipo) {
    const cardsList = document.getElementById('endpointsList');
    const table = document.getElementById('endpointsTable');
    
    if (tipo === 'table') {
        cardsList.style.display = 'none';
        table.style.display = 'block';
        renderizarTabelaEndpoints();
    } else {
        cardsList.style.display = 'block';
        table.style.display = 'none';
        renderizarCardsEndpoints();
    }
}

// Renderizar endpoints
function renderizarEndpoints() {
    const tipoVisualizacao = document.querySelector('input[name="viewType"]:checked').value;
    
    console.log('Renderizando endpoints. Total:', endpoints.length);
    
    if (endpoints.length === 0) {
        mostrarEmpty();
        return;
    }

    // Esconder a mensagem de vazio
    document.getElementById('emptyEndpoints').style.display = 'none';

    if (tipoVisualizacao === 'table') {
        document.getElementById('endpointsList').style.display = 'none';
        document.getElementById('endpointsTable').style.display = 'block';
        renderizarTabelaEndpoints();
    } else {
        document.getElementById('endpointsList').style.display = 'block';
        document.getElementById('endpointsTable').style.display = 'none';
        renderizarCardsEndpoints();
    }
}

// Renderizar endpoints em cards
function renderizarCardsEndpoints() {
    const container = document.getElementById('endpointsList');
    container.innerHTML = '';
    
    endpoints.forEach(endpoint => {
        const endpointCard = criarCardEndpoint(endpoint);
        container.appendChild(endpointCard);
    });
}

// Renderizar endpoints em tabela
function renderizarTabelaEndpoints() {
    const tbody = document.getElementById('endpointsTableBody');
    tbody.innerHTML = '';
    
    endpoints.forEach(endpoint => {
        const row = criarLinhaTabela(endpoint);
        tbody.appendChild(row);
    });
}

// Criar card de endpoint
function criarCardEndpoint(endpoint) {
    const col = document.createElement('div');
    col.className = 'col-lg-6 col-xl-4 mb-3';
    
    const metodoClass = getMetodoClass(endpoint.metodo);
    const statusClass = endpoint.ativo ? 'success' : 'secondary';
    const statusText = endpoint.ativo ? 'Ativo' : 'Inativo';
    const statusIcon = endpoint.ativo ? 'check-circle-fill' : 'x-circle-fill';

    col.innerHTML = `
        <div class="card bg-dark border-secondary h-100">
            <div class="card-header d-flex justify-content-between align-items-center border-secondary">
                <div class="d-flex align-items-center">
                    <span class="badge bg-${metodoClass} me-2">${endpoint.metodo || 'GET'}</span>
                    <h6 class="mb-0 text-light">${endpoint.caminho || 'Sem caminho'}</h6>
                </div>
                <span class="badge bg-${statusClass}">
                    <i class="bi bi-${statusIcon} me-1"></i>
                    ${statusText}
                </span>
            </div>
            <div class="card-body">
                <div class="mb-2">
                    <small class="text-muted">Descrição:</small>
                    <div class="text-light">${endpoint.descricao || 'Sem descrição'}</div>
                </div>
                
                <div class="mb-2">
                    <small class="text-muted">Tabela:</small>
                    <div class="text-info">
                        <i class="bi bi-table me-1"></i>
                        ${endpoint.tabela || 'Não especificada'}
                    </div>
                </div>
                
                <div class="mb-2">
                    <small class="text-muted">Operação SQL:</small>
                    <div class="text-warning">${endpoint.operacao || 'SELECT'}</div>
                </div>
                
                <div class="mb-3">
                    <small class="text-muted">URL Completa:</small>
                    <div class="text-info small" style="word-break: break-all;">
                        ${window.location.origin}/api/endpoints/${aplicacaoSelecionada}/${endpoint.caminho}
                    </div>
                </div>
                
                <div class="d-grid gap-2">
                    <button class="btn btn-outline-primary btn-sm" onclick="abrirModalEditarEndpoint(${endpoint.id})">
                        <i class="bi bi-pencil me-1"></i>
                        Editar
                    </button>
                    
                    <div class="btn-group" role="group">
                        <button class="btn btn-outline-info btn-sm" onclick="abrirModalTestarEndpoint(${endpoint.id})">
                            <i class="bi bi-play me-1"></i>
                            Testar
                        </button>
                        
                        <button class="btn btn-outline-danger btn-sm" 
                                onclick="abrirModalExcluirEndpoint(${endpoint.id}, '${endpoint.metodo} ${endpoint.caminho}')">
                            <i class="bi bi-trash me-1"></i>
                            Excluir
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    return col;
}

// Criar linha da tabela de endpoint
function criarLinhaTabela(endpoint) {
    const row = document.createElement('tr');
    
    const metodoClass = getMetodoClass(endpoint.metodo);
    const statusClass = endpoint.ativo ? 'success' : 'secondary';
    const statusText = endpoint.ativo ? 'Ativo' : 'Inativo';
    const statusIcon = endpoint.ativo ? 'check-circle-fill' : 'x-circle-fill';

    row.innerHTML = `
        <td>
            <span class="badge bg-${metodoClass}">${endpoint.metodo || 'GET'}</span>
        </td>
        <td class="text-light">
            <code>${endpoint.caminho || 'Sem caminho'}</code>
        </td>
        <td class="text-light">${endpoint.descricao || 'Sem descrição'}</td>
        <td>
            <div class="text-info">
                <i class="bi bi-table me-1"></i>
                ${endpoint.tabela || 'N/A'}
            </div>
        </td>
        <td>
            <span class="badge bg-${statusClass}">
                <i class="bi bi-${statusIcon} me-1"></i>
                ${statusText}
            </span>
        </td>
        <td class="text-center">
            <div class="btn-group btn-group-sm" role="group">
                <button class="btn btn-outline-primary" 
                        onclick="abrirModalEditarEndpoint(${endpoint.id})"
                        title="Editar Endpoint">
                    <i class="bi bi-pencil"></i>
                </button>
                
                <button class="btn btn-outline-info" 
                        onclick="abrirModalTestarEndpoint(${endpoint.id})"
                        title="Testar Endpoint">
                    <i class="bi bi-play"></i>
                </button>
                
                <button class="btn btn-outline-danger" 
                        onclick="abrirModalExcluirEndpoint(${endpoint.id}, '${endpoint.metodo} ${endpoint.caminho}')"
                        title="Excluir Endpoint">
                    <i class="bi bi-trash"></i>
                </button>
            </div>
        </td>
    `;
    
    return row;
}

// Obter classe CSS para método HTTP
function getMetodoClass(metodo) {
    switch (metodo) {
        case 'GET': 
        case 'POST': return 'primary'; // GET/POST são azuis
        case 'PUT': return 'warning';   // PUT é amarelo
        case 'DELETE':
        case 'PATCH': return 'danger';  // DELETE/PATCH são vermelhos
        default: return 'secondary';
    }
}

// Mapear método HTTP correto baseado na operação SQL
function obterMetodoHTTPCorreto(metodoOriginal, operacao) {
    // Se a operação SQL não bate com o método HTTP, corrigir
    switch (operacao) {
        case 'SELECT':
            return 'GET';
        case 'INSERT':
            return 'POST';
        case 'UPDATE':
            return 'PUT';
        case 'DELETE':
            return 'DELETE';
        default:
            return metodoOriginal || 'GET';
    }
}

// Abrir modal para novo endpoint
function abrirModalNovoEndpoint() {
    document.getElementById('endpointModalLabel').innerHTML = `
        <i class="bi bi-plus-circle me-2"></i>
        Novo Endpoint
    `;
    
    // Limpar formulário
    document.getElementById('formEndpoint').reset();
    document.getElementById('endpointId').value = '';
    document.getElementById('endpointAtivo').checked = true;
    
    const modal = new bootstrap.Modal(document.getElementById('endpointModal'));
    modal.show();
}

// Abrir modal para editar endpoint
async function abrirModalEditarEndpoint(idEndpoint) {
    const endpoint = endpoints.find(e => e.id === idEndpoint);
    if (!endpoint) return;
    
    document.getElementById('endpointModalLabel').innerHTML = `
        <i class="bi bi-pencil me-2"></i>
        Editar Endpoint
    `;
    
    // Preencher formulário
    document.getElementById('endpointId').value = endpoint.id;
    document.getElementById('metodoHttp').value = endpoint.metodo || 'GET';
    document.getElementById('caminhoEndpoint').value = endpoint.caminho || '';
    document.getElementById('descricaoEndpoint').value = endpoint.descricao || '';
    document.getElementById('tabelaAssociada').value = endpoint.tabela || '';
    document.getElementById('operacaoSql').value = endpoint.operacao || 'SELECT';
    document.getElementById('parametros').value = endpoint.parametros || '';
    document.getElementById('endpointAtivo').checked = endpoint.ativo !== false;
    
    const modal = new bootstrap.Modal(document.getElementById('endpointModal'));
    modal.show();
}

// Salvar endpoint
async function salvarEndpoint() {
    const formData = new FormData(document.getElementById('formEndpoint'));
    const endpointId = document.getElementById('endpointId').value;
    
    const endpointData = {
        metodo: formData.get('metodoHttp'),
        caminho: formData.get('caminhoEndpoint'),
        descricao: formData.get('descricaoEndpoint'),
        tabela: formData.get('tabelaAssociada'),
        operacao: formData.get('operacaoSql'),
        parametros: formData.get('parametros'),
        ativo: document.getElementById('endpointAtivo').checked,
        idAplicacao: aplicacaoSelecionada
    };
    
    try {
        const token = localStorage.getItem('token');
        const url = endpointId ? `${API_BASE}/endpointsManager/${endpointId}` : `${API_BASE}/endpointsManager`;
        const method = endpointId ? 'PUT' : 'POST';
        
        const response = await fetch(url, {
            method: method,
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(endpointData)
        });

        if (response.ok) {
            mostrarToast(endpointId ? 'Endpoint atualizado com sucesso!' : 'Endpoint criado com sucesso!', 'success');
            bootstrap.Modal.getInstance(document.getElementById('endpointModal')).hide();
            carregarEndpointsDaAplicacao();
        } else {
            const data = await response.json();
            mostrarToast(data.message || 'Erro ao salvar endpoint', 'error');
        }
    } catch (error) {
        console.error('Erro:', error);
        mostrarToast('Erro de conexão', 'error');
    }
}

// Abrir modal para excluir endpoint
function abrirModalExcluirEndpoint(idEndpoint, nomeEndpoint) {
    document.getElementById('nomeEndpointExcluir').textContent = nomeEndpoint;
    document.getElementById('btnConfirmarExcluir').setAttribute('data-id', idEndpoint);
    
    const modal = new bootstrap.Modal(document.getElementById('excluirEndpointModal'));
    modal.show();
}

// Excluir endpoint
async function excluirEndpoint() {
    const idEndpoint = document.getElementById('btnConfirmarExcluir').getAttribute('data-id');
    
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`${API_BASE}/endpointsManager/${idEndpoint}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            mostrarToast('Endpoint excluído com sucesso!', 'success');
            bootstrap.Modal.getInstance(document.getElementById('excluirEndpointModal')).hide();
            carregarEndpointsDaAplicacao();
        } else {
            const data = await response.json();
            mostrarToast(data.message || 'Erro ao excluir endpoint', 'error');
        }
    } catch (error) {
        console.error('Erro:', error);
        mostrarToast('Erro de conexão', 'error');
    }
}

// Abrir modal para testar endpoint
function abrirModalTestarEndpoint(idEndpoint) {
    const endpoint = endpoints.find(e => e.id === idEndpoint);
    if (!endpoint) return;
    
    // Armazenar o endpoint sendo testado
    endpointSendoTestado = endpoint;
    
    const url = `${window.location.origin}/api/endpoints/${aplicacaoSelecionada}${endpoint.caminho}`;
    document.getElementById('urlTeste').value = url;
    
    // Limpar resultado anterior
    document.getElementById('resultadoTeste').style.display = 'none';
    
    // Pré-preencher dados de teste baseado no método e parâmetros
    let dadosExemplo = '';
    
    if (endpoint.metodo === 'POST' || endpoint.metodo === 'PUT') {
        // Para métodos que enviam dados, criar um exemplo baseado nos parâmetros
        if (endpoint.parametros) {
            const params = endpoint.parametros.split(', ').filter(p => p !== 'id_aplicacao' && p !== 'id');
            const exemploObj = {};
            
            params.forEach(param => {
                if (param.includes('nome')) {
                    exemploObj[param] = 'Exemplo Nome';
                } else if (param.includes('codigo')) {
                    exemploObj[param] = '12345';
                } else if (param.includes('id_')) {
                    exemploObj[param] = 1;
                } else {
                    exemploObj[param] = 'valor_exemplo';
                }
            });
            
            if (Object.keys(exemploObj).length > 0) {
                dadosExemplo = JSON.stringify(exemploObj, null, 2);
            }
        }
        
        if (!dadosExemplo) {
            // Exemplo genérico para POST/PUT
            dadosExemplo = JSON.stringify({
                "exemplo": "valor",
                "campo": "dados_aqui"
            }, null, 2);
        }
    }
    
    document.getElementById('dadosTeste').value = dadosExemplo;
    
    // Atualizar título do modal com informações do endpoint
    const modalTitle = document.querySelector('#testarEndpointModal .modal-title');
    modalTitle.innerHTML = `
        <i class="bi bi-play me-2"></i>
        Testar: <span class="badge bg-${getMetodoClass(endpoint.metodo)} ms-2">${endpoint.metodo}</span> ${endpoint.caminho}
    `;
    
    // Adicionar informação sobre o método que será usado
    const modalBody = document.querySelector('#testarEndpointModal .modal-body');
    const metodoInfo = modalBody.querySelector('.metodo-info');
    if (metodoInfo) {
        metodoInfo.remove();
    }
    
    const infoDiv = document.createElement('div');
    infoDiv.className = 'alert alert-info metodo-info';
    infoDiv.innerHTML = `
        <i class="bi bi-info-circle me-2"></i>
        Este teste será executado usando o método <strong>${endpoint.metodo}</strong>
    `;
    modalBody.insertBefore(infoDiv, modalBody.firstChild);
    
    const modal = new bootstrap.Modal(document.getElementById('testarEndpointModal'));
    modal.show();
}

// Variável global para armazenar o endpoint sendo testado
let endpointSendoTestado = null;

// Executar teste do endpoint
async function executarTesteEndpoint() {
    const url = document.getElementById('urlTeste').value;
    const dados = document.getElementById('dadosTeste').value;
    
    // Usar o método HTTP do endpoint que está sendo testado
    let metodoHTTP = 'GET'; // Padrão
    
    if (endpointSendoTestado) {
        metodoHTTP = endpointSendoTestado.metodo;
        console.log('Método HTTP do endpoint sendo testado:', metodoHTTP);
    } else {
        // Fallback: tentar determinar pelo caminho da URL
        const endpointPath = url.split(`/api/endpoints/${aplicacaoSelecionada}`)[1];
        if (endpointPath) {
            const endpoint = endpoints.find(e => endpointPath.startsWith(e.caminho));
            if (endpoint) {
                metodoHTTP = endpoint.metodo;
            }
        }
    }
    
    try {
        // Primeiro, obter o token específico da aplicação
        const tokenAplicacao = await obterTokenAplicacao();
        
        if (!tokenAplicacao) {
            document.getElementById('resultadoJson').textContent = 'Erro: Não foi possível obter o token da aplicação';
            document.getElementById('resultadoTeste').style.display = 'block';
            return;
        }
        
        const options = {
            method: metodoHTTP,
            headers: {
                'Authorization': `Bearer ${tokenAplicacao}`,
                'Content-Type': 'application/json'
            }
        };
        
        // Apenas adicionar body se o método suportar e se há dados
        if ((metodoHTTP === 'POST' || metodoHTTP === 'PUT' || metodoHTTP === 'PATCH') && dados.trim()) {
            try {
                // Validar se os dados são JSON válidos
                JSON.parse(dados);
                options.body = dados;
            } catch (e) {
                document.getElementById('resultadoJson').textContent = 'Erro: Dados de teste devem ser um JSON válido';
                document.getElementById('resultadoTeste').style.display = 'block';
                return;
            }
        }
        
        console.log('=== TESTE DE ENDPOINT ===');
        console.log('URL:', url);
        console.log('Método HTTP configurado:', metodoHTTP);
        console.log('Endpoint sendo testado:', endpointSendoTestado);
        console.log('Token da aplicação:', tokenAplicacao?.substring(0, 20) + '...');
        
        const response = await fetch(url, options);
        let result;
        
        try {
            result = await response.json();
        } catch (e) {
            result = await response.text();
        }
        
        const resultadoFormatado = {
            status: response.status,
            statusText: response.statusText,
            method: metodoHTTP,
            url: url,
            success: response.ok,
            timestamp: new Date().toISOString(),
            data: result
        };
        
        // Adicionar informações úteis se a resposta contém dados estruturados
        if (result && typeof result === 'object') {
            if (Array.isArray(result)) {
                resultadoFormatado.recordCount = result.length;
            } else if (result.success !== undefined) {
                resultadoFormatado.apiSuccess = result.success;
            }
        }
        
        document.getElementById('resultadoJson').textContent = JSON.stringify(resultadoFormatado, null, 2);
        document.getElementById('resultadoTeste').style.display = 'block';
        
        // Mostrar notificação de sucesso/erro
        if (response.ok) {
            mostrarToast('Endpoint testado com sucesso!', 'success');
        } else {
            mostrarToast(`Erro no teste: ${response.status} ${response.statusText}`, 'error');
        }
        
    } catch (error) {
        console.error('Erro no teste:', error);
        document.getElementById('resultadoJson').textContent = `Erro: ${error.message}`;
        document.getElementById('resultadoTeste').style.display = 'block';
    }
}

// Obter token específico da aplicação para testes
async function obterTokenAplicacao() {
    try {
        // Solicitar credenciais do usuário
        const credenciais = await solicitarCredenciaisUsuario();
        
        if (!credenciais) {
            mostrarToast('Credenciais necessárias para testar o endpoint', 'warning');
            return null;
        }
        
        const response = await fetch(`${API_BASE}/login/${aplicacaoSelecionada}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                email: credenciais.email,
                senha: credenciais.senha
            })
        });
        
        if (response.ok) {
            const data = await response.json();
            console.log('Token da aplicação obtido:', data);
            
            // Retornar o token, pode estar em diferentes formatos
            return data.token || data.data?.token || data.access_token;
        } else {
            const errorData = await response.json().catch(() => ({}));
            console.error('Erro ao obter token da aplicação:', response.status, errorData);
            mostrarToast(errorData.message || 'Erro ao fazer login na aplicação', 'error');
            return null;
        }
    } catch (error) {
        console.error('Erro ao obter token da aplicação:', error);
        mostrarToast('Erro de conexão ao fazer login', 'error');
        return null;
    }
}

// Solicitar credenciais do usuário via modal
function solicitarCredenciaisUsuario() {
    return new Promise((resolve) => {
        // Criar modal dinamicamente
        const modalHtml = `
            <div class="modal fade" id="credenciaisModal" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog">
                    <div class="modal-content bg-dark border-secondary">
                        <div class="modal-header border-secondary">
                            <h5 class="modal-title text-light">
                                <i class="bi bi-key me-2"></i>
                                Credenciais para Teste
                            </h5>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <p class="text-muted mb-3">
                                Para testar este endpoint, é necessário fazer login na aplicação. 
                                Digite suas credenciais:
                            </p>
                            <form id="formCredenciais">
                                <div class="mb-3">
                                    <label for="emailTeste" class="form-label text-light">Email:</label>
                                    <input type="email" class="form-control bg-dark border-secondary text-light" 
                                           id="emailTeste" required placeholder="seu@email.com">
                                </div>
                                <div class="mb-3">
                                    <label for="senhaTeste" class="form-label text-light">Senha:</label>
                                    <input type="password" class="form-control bg-dark border-secondary text-light" 
                                           id="senhaTeste" required placeholder="Sua senha">
                                </div>
                            </form>
                        </div>
                        <div class="modal-footer border-secondary">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                            <button type="button" class="btn btn-primary" id="btnConfirmarCredenciais">
                                <i class="bi bi-check-lg me-1"></i>
                                Confirmar
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        // Adicionar modal ao DOM
        document.body.insertAdjacentHTML('beforeend', modalHtml);
        
        const modal = new bootstrap.Modal(document.getElementById('credenciaisModal'));
        
        // Configurar eventos
        document.getElementById('btnConfirmarCredenciais').addEventListener('click', function() {
            const email = document.getElementById('emailTeste').value.trim();
            const senha = document.getElementById('senhaTeste').value.trim();
            
            if (!email || !senha) {
                mostrarToast('Email e senha são obrigatórios', 'warning');
                return;
            }
            
            modal.hide();
            resolve({ email, senha });
        });
        
        // Resolver com null se modal for fechado sem confirmar
        document.getElementById('credenciaisModal').addEventListener('hidden.bs.modal', function() {
            document.getElementById('credenciaisModal').remove();
            resolve(null);
        });
        
        modal.show();
    });
}

// Copiar URL para clipboard
function copiarUrl() {
    const input = document.getElementById('urlTeste');
    input.select();
    document.execCommand('copy');
    mostrarToast('URL copiada para a área de transferência!', 'success');
}

// Funções de UI
function mostrarLoading() {
    document.getElementById('loadingEndpoints').style.display = 'block';
    document.getElementById('emptyEndpoints').style.display = 'none';
    document.getElementById('endpointsList').innerHTML = '';
    document.getElementById('endpointsTableBody').innerHTML = '';
}

function esconderLoading() {
    document.getElementById('loadingEndpoints').style.display = 'none';
}

function mostrarEmpty() {
    document.getElementById('emptyEndpoints').style.display = 'block';
    document.getElementById('loadingEndpoints').style.display = 'none';
    document.getElementById('endpointsList').innerHTML = '';
    document.getElementById('endpointsTableBody').innerHTML = '';
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