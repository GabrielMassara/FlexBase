// Database JavaScript
let currentApplicationId = null;
let currentTableName = null;
let currentRecordId = null;
let tablesData = [];

document.addEventListener('DOMContentLoaded', function() {
    initializeDatabase();
    setupEventListeners();
    loadApplicationData();
});

function initializeDatabase() {
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
    
    // Obter ID da aplicação da URL
    currentApplicationId = getApplicationIdFromUrl();
    if (!currentApplicationId) {
        showNotification('ID da aplicação não fornecido na URL. Redirecionando...', 'warning');
        setTimeout(() => {
            window.location.href = '../home/index.html';
        }, 2000);
        return;
    }
    
    console.log('Database inicializado para aplicação ID:', currentApplicationId);
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
    
    // Voltar para dashboard
    const backToDashboardBtn = document.getElementById('backToDashboardBtn');
    if (backToDashboardBtn) {
        backToDashboardBtn.addEventListener('click', () => {
            window.location.href = `../dashboard/index.html?id=${currentApplicationId}`;
        });
    }
    
    // Atualizar dados
    const refreshBtn = document.getElementById('refreshBtn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', () => {
            loadApplicationData();
        });
    }
    
    // Exportar dados
    const exportBtn = document.getElementById('exportBtn');
    if (exportBtn) {
        exportBtn.addEventListener('click', exportTableData);
    }
    
    // Buscar tabelas
    const searchTables = document.getElementById('searchTables');
    if (searchTables) {
        searchTables.addEventListener('input', filterTables);
    }
    
    // Adicionar novo registro
    const addRecordBtn = document.getElementById('addRecordBtn');
    if (addRecordBtn) {
        addRecordBtn.addEventListener('click', () => openRecordModal());
    }
    
    // Salvar registro
    const saveRecordBtn = document.getElementById('saveRecordBtn');
    if (saveRecordBtn) {
        saveRecordBtn.addEventListener('click', saveRecord);
    }
    
    // Confirmar exclusão
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener('click', confirmDeleteRecord);
    }
}

function getApplicationIdFromUrl() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('id');
}

async function loadApplicationData() {
    try {
        const token = localStorage.getItem('token');
        if (!token) return;
        
        // Carregar informações da aplicação
        const appResponse = await fetch(`${CONFIG.API_BASE_URL}/aplicacoes/${currentApplicationId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        
        if (appResponse.ok) {
            const app = await appResponse.json();
            updatePageTitle(app);
            
            // Extrair tabelas do schema da aplicação
            await loadTablesFromSchema(app);
        }
        
    } catch (error) {
        console.error('Erro ao carregar dados da aplicação:', error);
        showNotification('Erro ao carregar dados da aplicação', 'error');
    }
}

function updatePageTitle(app) {
    const pageTitle = document.querySelector('.page-title');
    const pageSubtitle = document.getElementById('appSubtitle');
    
    if (pageTitle) {
        pageTitle.textContent = `Banco de Dados - ${app.nome}`;
    }
    
    if (pageSubtitle) {
        pageSubtitle.textContent = `Visualize e gerencie os dados da aplicação "${app.nome}"`;
    }
}

async function loadTablesFromSchema(app) {
    try {
        // Extrair tabelas do schema da aplicação
        const schema = app.schemaBanco;
        const tables = schema && schema.tabelas ? schema.tabelas : [];
        
        // Processar cada tabela e buscar seus registros
        tablesData = [];
        
        for (const tableSchema of tables) {
            const tableData = {
                name: tableSchema.nome,
                schema: tableSchema,
                records: [],
                count: 0
            };
            
            // Buscar registros para esta tabela
            await loadTableRecords(tableData);
            tablesData.push(tableData);
        }
        
        // Atualizar contador de tabelas
        const tablesCount = document.getElementById('tablesCount');
        if (tablesCount) {
            tablesCount.textContent = tablesData.length;
        }
        
        renderTablesList();
        
    } catch (error) {
        console.error('Erro ao processar schema:', error);
        showNotification('Erro ao processar schema da aplicação', 'error');
    }
}

async function loadTableRecords(tableData) {
    try {
        const token = localStorage.getItem('token');
        if (!token) return;
        
        const response = await fetch(`${CONFIG.API_BASE_URL}/registros/tabela/${tableData.name}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        
        if (response.ok) {
            const records = await response.json();
            // Filtrar apenas registros da aplicação atual
            const appRecords = records.filter(record => record.idAplicacao == currentApplicationId);
            tableData.records = appRecords;
            tableData.count = appRecords.length;
        } else {
            console.log(`Nenhum registro encontrado para a tabela ${tableData.name}`);
            tableData.records = [];
            tableData.count = 0;
        }
    } catch (error) {
        console.error(`Erro ao carregar registros da tabela ${tableData.name}:`, error);
        tableData.records = [];
        tableData.count = 0;
    }
}



function renderTablesList() {
    const tablesList = document.getElementById('tablesList');
    if (!tablesList) return;
    
    if (tablesData.length === 0) {
        tablesList.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">
                    <i class="bi bi-table"></i>
                </div>
                <p class="fw-semibold mb-1">Nenhuma tabela encontrada</p>
                <small class="text-muted">Crie tabelas no diagrama primeiro</small>
            </div>
        `;
        return;
    }
    
    const tablesHTML = tablesData.map(table => {
        const fieldsCount = table.schema && table.schema.campos ? table.schema.campos.length : 0;
        return `
            <div class="table-item" onclick="selectTable('${table.name}')">
                <div class="table-name">${table.name}</div>
                <div class="table-stats">
                    <span>${table.count} registro${table.count !== 1 ? 's' : ''}</span>
                    <span>${fieldsCount} campo${fieldsCount !== 1 ? 's' : ''}</span>
                </div>
            </div>
        `;
    }).join('');
    
    tablesList.innerHTML = tablesHTML;
}

function selectTable(tableName) {
    currentTableName = tableName;
    
    // Atualizar visual da seleção
    document.querySelectorAll('.table-item').forEach(item => {
        item.classList.remove('active');
    });
    
    // Usar event.currentTarget se disponível, senão buscar o elemento pela tabela
    if (event && event.currentTarget) {
        event.currentTarget.classList.add('active');
    } else {
        // Buscar o elemento correspondente à tabela selecionada
        const tableItems = document.querySelectorAll('.table-item');
        tableItems.forEach(item => {
            const itemName = item.querySelector('.table-item-name').textContent;
            if (itemName === tableName) {
                item.classList.add('active');
            }
        });
    }
    
    // Mostrar conteúdo da tabela
    showTableContent(tableName);
}

function showTableContent(tableName) {
    const table = tablesData.find(t => t.name === tableName);
    if (!table) return;
    
    // Ocultar estado inicial e mostrar conteúdo da tabela
    document.getElementById('noTableSelected').style.display = 'none';
    document.getElementById('selectedTableContent').style.display = 'block';
    
    // Atualizar cabeçalho da tabela
    const selectedTableName = document.getElementById('selectedTableName');
    const selectedTableRecordCount = document.getElementById('selectedTableRecordCount');
    
    if (selectedTableName) {
        selectedTableName.textContent = table.name;
    }
    
    if (selectedTableRecordCount) {
        selectedTableRecordCount.textContent = `${table.count} registro${table.count !== 1 ? 's' : ''}`;
    }
    
    // Renderizar tabela de registros
    renderRecordsTable(table.records);
}

function renderRecordsTable(records) {
    const container = document.getElementById('recordsTableContainer');
    if (!container) return;
    
    if (records.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">
                    <i class="bi bi-database-x"></i>
                </div>
                <h4 class="empty-state-title">Nenhum registro encontrado</h4>
                <p class="empty-state-text">Esta tabela não possui registros ainda.</p>
                <button class="btn btn-primary-modern" onclick="openRecordModal()">
                    <i class="bi bi-plus-lg"></i>
                    <span>Adicionar Primeiro Registro</span>
                </button>
            </div>
        `;
        return;
    }
    
    // Buscar o schema da tabela atual para definir os cabeçalhos
    const currentTable = tablesData.find(t => t.name === currentTableName);
    let headers = [];
    
    if (currentTable && currentTable.schema && currentTable.schema.campos) {
        // Usar campos definidos no schema
        headers = currentTable.schema.campos.map(campo => campo.nome);
    } else {
        // Fallback: usar chaves dos registros existentes
        let allKeys = new Set();
        records.forEach(record => {
            try {
                const data = typeof record.valor === 'string' ? JSON.parse(record.valor) : record.valor;
                Object.keys(data).forEach(key => allKeys.add(key));
            } catch (e) {
                allKeys.add('valor');
            }
        });
        headers = Array.from(allKeys);
    }
    
    let tableHTML = `
        <table class="table data-table">
            <thead>
                <tr>
                    <th>ID</th>
                    ${headers.map(header => `<th>${header}</th>`).join('')}
                    <th width="120">Ações</th>
                </tr>
            </thead>
            <tbody>
    `;
    
    records.forEach(record => {
        let recordData = {};
        try {
            recordData = typeof record.valor === 'string' ? JSON.parse(record.valor) : record.valor;
        } catch (e) {
            recordData = { valor: record.valor };
        }
        
        tableHTML += `
            <tr>
                <td><span class="fw-semibold">${record.id}</span></td>
                ${headers.map(header => {
                    const value = recordData[header];
                    return `<td>${formatFieldValue(value)}</td>`;
                }).join('')}
                <td>
                    <div class="action-buttons">
                        <button class="btn btn-sm-modern btn-edit" onclick="editRecord(${record.id})" title="Editar">
                            <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-sm-modern btn-delete" onclick="deleteRecord(${record.id})" title="Excluir">
                            <i class="bi bi-trash"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    });
    
    tableHTML += `
            </tbody>
        </table>
    `;
    
    container.innerHTML = tableHTML;
}

function formatFieldValue(value) {
    if (value === null || value === undefined) {
        return '<span class="text-muted fst-italic">null</span>';
    }
    
    if (typeof value === 'boolean') {
        return `<span class="badge ${value ? 'bg-success' : 'bg-secondary'}">${value}</span>`;
    }
    
    if (typeof value === 'number') {
        return `<span class="fw-semibold">${value}</span>`;
    }
    
    if (typeof value === 'object') {
        const jsonStr = JSON.stringify(value, null, 2);
        if (jsonStr.length > 100) {
            return `<code class="text-truncate d-block" style="max-width: 200px;" title="${jsonStr}">${jsonStr.substring(0, 100)}...</code>`;
        }
        return `<code>${jsonStr}</code>`;
    }
    
    if (typeof value === 'string') {
        if (value.length === 0) {
            return '<span class="text-muted fst-italic">vazio</span>';
        }
        if (value.length > 50) {
            return `<span class="text-truncate d-block" style="max-width: 200px;" title="${value}">${value.substring(0, 50)}...</span>`;
        }
        return `<span>${value}</span>`;
    }
    
    return value;
}

function openRecordModal(recordId = null) {
    const modal = new bootstrap.Modal(document.getElementById('recordModal'));
    const title = document.getElementById('recordModalTitle');
    
    currentRecordId = recordId;
    
    if (recordId) {
        title.textContent = 'Editar Registro';
        // Gerar formulário e carregar dados do registro para edição
        generateDynamicForm();
        loadRecordForEdit(recordId);
    } else {
        title.textContent = 'Novo Registro';
        // Gerar formulário limpo
        generateDynamicForm();
    }
    
    // Configurar toggle do modo avançado
    setupAdvancedModeToggle();
    
    modal.show();
}

function generateDynamicForm() {
    const formContainer = document.getElementById('formFields');
    const currentTable = tablesData.find(t => t.name === currentTableName);
    
    if (!currentTable || !currentTable.schema || !currentTable.schema.campos) {
        // Fallback para formulário genérico
        formContainer.innerHTML = `
            <div class="col-12">
                <div class="alert alert-warning">
                    <i class="bi bi-exclamation-triangle me-2"></i>
                    Schema da tabela não encontrado. Use o modo avançado para inserir dados.
                </div>
            </div>
        `;
        document.getElementById('advancedMode').checked = true;
        toggleAdvancedMode();
        return;
    }
    
    let formHTML = '';
    
    currentTable.schema.campos.forEach(campo => {
        // Não incluir campos ID (auto-gerados) ou chaves primárias
        if (campo.tipo === 'id' || campo.chave_primaria) return;
        
        const isRequired = campo.nome.toLowerCase().includes('nome') || campo.nome.toLowerCase().includes('email');
        const fieldId = `field_${campo.nome}`;
        
        formHTML += `
            <div class="col-md-6">
                <div class="field-container">
                    <label for="${fieldId}" class="field-label">
                        ${campo.nome}
                        ${isRequired ? '<span class="required-field">*</span>' : ''}
                        <span class="field-type-badge bg-secondary text-white">${getFieldTypeLabel(campo.tipo)}</span>
                    </label>
                    ${generateFieldInput(campo, fieldId, isRequired)}
                    <div class="field-help">${getFieldHelp(campo.tipo)}</div>
                </div>
            </div>
        `;
    });
    
    formContainer.innerHTML = formHTML;
}

function generateFieldInput(campo, fieldId, isRequired) {
    const baseClasses = 'field-input';
    const requiredAttr = isRequired ? 'required' : '';
    
    switch (campo.tipo) {
        case 'string':
        case 'criptografia':
            return `<input type="text" id="${fieldId}" class="${baseClasses}" placeholder="Digite o ${campo.nome}" ${requiredAttr}>`;
        
        case 'integer':
            return `<input type="number" id="${fieldId}" class="${baseClasses}" step="1" placeholder="0" ${requiredAttr}>`;
        
        case 'decimal':
            return `<input type="number" id="${fieldId}" class="${baseClasses}" step="0.01" placeholder="0.00" ${requiredAttr}>`;
        
        case 'boolean':
            return `
                <select id="${fieldId}" class="${baseClasses}" ${requiredAttr}>
                    <option value="">Selecione...</option>
                    <option value="true">Verdadeiro</option>
                    <option value="false">Falso</option>
                </select>
            `;
        
        case 'date':
            return `<input type="date" id="${fieldId}" class="${baseClasses}" ${requiredAttr}>`;
        
        case 'datetime':
            return `<input type="datetime-local" id="${fieldId}" class="${baseClasses}" ${requiredAttr}>`;
        
        case 'email':
            return `<input type="email" id="${fieldId}" class="${baseClasses}" placeholder="exemplo@email.com" ${requiredAttr}>`;
        
        case 'url':
            return `<input type="url" id="${fieldId}" class="${baseClasses}" placeholder="https://exemplo.com" ${requiredAttr}>`;
        
        case 'text':
            return `<textarea id="${fieldId}" class="${baseClasses}" rows="3" placeholder="Digite o ${campo.nome}" ${requiredAttr}></textarea>`;
        
        default:
            return `<input type="text" id="${fieldId}" class="${baseClasses}" placeholder="Digite o ${campo.nome}" ${requiredAttr}>`;
    }
}

function getFieldTypeLabel(tipo) {
    const labels = {
        'string': 'Texto',
        'integer': 'Número',
        'decimal': 'Decimal',
        'boolean': 'Verdadeiro/Falso',
        'date': 'Data',
        'datetime': 'Data e Hora',
        'email': 'Email',
        'url': 'URL',
        'text': 'Texto Longo',
        'criptografia': 'Criptografado',
        'id': 'ID'
    };
    return labels[tipo] || 'Texto';
}

function getFieldHelp(tipo) {
    const helps = {
        'string': 'Digite um texto',
        'integer': 'Digite um número inteiro',
        'decimal': 'Digite um número com casas decimais',
        'boolean': 'Escolha verdadeiro ou falso',
        'date': 'Selecione uma data',
        'datetime': 'Selecione data e hora',
        'email': 'Digite um endereço de email válido',
        'url': 'Digite uma URL válida',
        'text': 'Digite um texto longo',
        'criptografia': 'Este campo será criptografado automaticamente',
        'id': 'Campo gerado automaticamente'
    };
    return helps[tipo] || 'Digite um valor';
}

function setupAdvancedModeToggle() {
    const advancedToggle = document.getElementById('advancedMode');
    
    // Remover listeners anteriores
    advancedToggle.removeEventListener('change', toggleAdvancedMode);
    advancedToggle.addEventListener('change', toggleAdvancedMode);
    
    // Resetar estado
    advancedToggle.checked = false;
    toggleAdvancedMode();
    
    // Adicionar validação em tempo real aos campos
    setupFieldValidation();
}

function setupFieldValidation() {
    const currentTable = tablesData.find(t => t.name === currentTableName);
    if (!currentTable || !currentTable.schema) return;
    
    currentTable.schema.campos.forEach(campo => {
        if (campo.tipo === 'id' || campo.chave_primaria) return;
        
        const fieldId = `field_${campo.nome}`;
        const field = document.getElementById(fieldId);
        
        if (field) {
            field.addEventListener('input', function() {
                validateField(this, campo);
            });
            
            field.addEventListener('blur', function() {
                validateField(this, campo);
            });
        }
    });
}

function validateField(field, campo) {
    const value = field.value.trim();
    const isRequired = field.hasAttribute('required');
    
    // Limpar estado de erro
    field.classList.remove('is-invalid');
    
    // Validar campo obrigatório
    if (isRequired && value === '') {
        field.classList.add('is-invalid');
        return false;
    }
    
    // Validações por tipo
    if (value !== '') {
        switch (campo.tipo) {
            case 'email':
                const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                if (!emailRegex.test(value)) {
                    field.classList.add('is-invalid');
                    return false;
                }
                break;
            
            case 'url':
                try {
                    new URL(value);
                } catch (e) {
                    field.classList.add('is-invalid');
                    return false;
                }
                break;
            
            case 'integer':
                if (isNaN(parseInt(value))) {
                    field.classList.add('is-invalid');
                    return false;
                }
                break;
            
            case 'decimal':
                if (isNaN(parseFloat(value))) {
                    field.classList.add('is-invalid');
                    return false;
                }
                break;
        }
    }
    
    return true;
}

function toggleAdvancedMode() {
    const advancedToggle = document.getElementById('advancedMode');
    const formFields = document.getElementById('formFields');
    const jsonContainer = document.getElementById('jsonEditorContainer');
    
    if (advancedToggle.checked) {
        formFields.style.display = 'none';
        jsonContainer.style.display = 'block';
        
        // Sincronizar dados do formulário para JSON
        syncFormToJson();
    } else {
        formFields.style.display = 'block';
        jsonContainer.style.display = 'none';
    }
}

function syncFormToJson() {
    const currentTable = tablesData.find(t => t.name === currentTableName);
    if (!currentTable || !currentTable.schema) return;
    
    const data = {};
    
    currentTable.schema.campos.forEach(campo => {
        if (campo.tipo === 'id') return;
        
        const fieldId = `field_${campo.nome}`;
        const field = document.getElementById(fieldId);
        
        if (field && field.value !== '') {
            let value = field.value;
            
            // Converter valores baseado no tipo
            switch (campo.tipo) {
                case 'integer':
                    value = parseInt(value) || 0;
                    break;
                case 'decimal':
                    value = parseFloat(value) || 0.0;
                    break;
                case 'boolean':
                    value = value === 'true';
                    break;
            }
            
            data[campo.nome] = value;
        }
    });
    
    const jsonEditor = document.getElementById('recordData');
    if (jsonEditor) {
        jsonEditor.value = JSON.stringify(data, null, 2);
    }
}

async function loadRecordForEdit(recordId) {
    try {
        const token = localStorage.getItem('token');
        if (!token) return;
        
        const response = await fetch(`${CONFIG.API_BASE_URL}/registros/${recordId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        
        if (response.ok) {
            const record = await response.json();
            
            let recordData = {};
            try {
                recordData = typeof record.valor === 'string' ? JSON.parse(record.valor) : record.valor;
            } catch (e) {
                recordData = { valor: record.valor };
            }
            
            // Preencher campos do formulário
            populateFormFields(recordData);
            
            // Também atualizar o JSON editor (caso o usuário mude para modo avançado)
            const dataField = document.getElementById('recordData');
            if (dataField) {
                dataField.value = JSON.stringify(recordData, null, 2);
            }
        } else {
            showNotification('Erro ao carregar registro', 'error');
        }
    } catch (error) {
        console.error('Erro ao carregar registro:', error);
        showNotification('Erro ao carregar registro', 'error');
    }
}

function populateFormFields(data) {
    const currentTable = tablesData.find(t => t.name === currentTableName);
    if (!currentTable || !currentTable.schema) return;
    
    currentTable.schema.campos.forEach(campo => {
        if (campo.tipo === 'id' || campo.chave_primaria) return;
        
        const fieldId = `field_${campo.nome}`;
        const field = document.getElementById(fieldId);
        
        if (field && data.hasOwnProperty(campo.nome)) {
            const value = data[campo.nome];
            
            if (campo.tipo === 'boolean') {
                field.value = value.toString();
            } else if (campo.tipo === 'date' && value) {
                // Converter para formato de data do input
                const date = new Date(value);
                field.value = date.toISOString().split('T')[0];
            } else if (campo.tipo === 'datetime' && value) {
                // Converter para formato datetime-local
                const date = new Date(value);
                field.value = date.toISOString().slice(0, 16);
            } else {
                field.value = value || '';
            }
        }
    });
}

async function saveRecord() {
    try {
        let recordData;
        const advancedMode = document.getElementById('advancedMode').checked;
        
        if (advancedMode) {
            // Modo avançado: usar JSON editor
            const dataField = document.getElementById('recordData');
            try {
                recordData = JSON.parse(dataField.value);
            } catch (e) {
                showNotification('JSON inválido no modo avançado', 'error');
                return;
            }
        } else {
            // Modo formulário: coletar dados dos campos
            recordData = collectFormData();
            
            if (!recordData) {
                showNotification('Preencha os campos obrigatórios', 'error');
                return;
            }
        }
        
        const token = localStorage.getItem('token');
        if (!token) return;
        
        const payload = {
            tabela: currentTableName,
            valor: recordData,
            idAplicacao: parseInt(currentApplicationId)
        };
        
        let url = `${CONFIG.API_BASE_URL}/registros`;
        let method = 'POST';
        
        if (currentRecordId) {
            url += `/${currentRecordId}`;
            method = 'PUT';
        }
        
        const response = await fetch(url, {
            method: method,
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });
        
        if (response.ok) {
            showNotification(currentRecordId ? 'Registro atualizado com sucesso' : 'Registro criado com sucesso', 'success');
            bootstrap.Modal.getInstance(document.getElementById('recordModal')).hide();
            
            // Recarregar apenas os dados da tabela atual
            if (currentTableName) {
                const currentTable = tablesData.find(t => t.name === currentTableName);
                if (currentTable) {
                    await loadTableRecords(currentTable);
                    showTableContent(currentTableName);
                }
            }
        } else {
            const errorData = await response.json();
            showNotification(errorData.message || 'Erro ao salvar registro', 'error');
        }
    } catch (error) {
        console.error('Erro ao salvar registro:', error);
        showNotification('Erro ao salvar registro', 'error');
    }
}

function collectFormData() {
    const currentTable = tablesData.find(t => t.name === currentTableName);
    if (!currentTable || !currentTable.schema) return null;
    
    const data = {};
    let hasRequiredFields = true;
    
    currentTable.schema.campos.forEach(campo => {
        if (campo.tipo === 'id') return;
        
        const fieldId = `field_${campo.nome}`;
        const field = document.getElementById(fieldId);
        
        if (field) {
            const isRequired = field.hasAttribute('required');
            const value = field.value.trim();
            
            if (isRequired && value === '') {
                hasRequiredFields = false;
                field.classList.add('is-invalid');
                return;
            } else {
                field.classList.remove('is-invalid');
            }
            
            if (value !== '') {
                // Converter valores baseado no tipo
                switch (campo.tipo) {
                    case 'integer':
                        data[campo.nome] = parseInt(value) || 0;
                        break;
                    case 'decimal':
                        data[campo.nome] = parseFloat(value) || 0.0;
                        break;
                    case 'boolean':
                        data[campo.nome] = value === 'true';
                        break;
                    case 'date':
                    case 'datetime':
                        data[campo.nome] = value;
                        break;
                    default:
                        data[campo.nome] = value;
                }
            }
        }
    });
    
    return hasRequiredFields ? data : null;
}

function editRecord(recordId) {
    openRecordModal(recordId);
}

function deleteRecord(recordId) {
    currentRecordId = recordId;
    const modal = new bootstrap.Modal(document.getElementById('confirmDeleteModal'));
    modal.show();
}

async function confirmDeleteRecord() {
    try {
        const token = localStorage.getItem('token');
        if (!token) return;
        
        const response = await fetch(`${CONFIG.API_BASE_URL}/registros/${currentRecordId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        
        if (response.ok) {
            showNotification('Registro excluído com sucesso', 'success');
            bootstrap.Modal.getInstance(document.getElementById('confirmDeleteModal')).hide();
            
            // Recarregar apenas os dados da tabela atual
            if (currentTableName) {
                const currentTable = tablesData.find(t => t.name === currentTableName);
                if (currentTable) {
                    await loadTableRecords(currentTable);
                    showTableContent(currentTableName);
                }
            }
        } else {
            const errorData = await response.json();
            showNotification(errorData.message || 'Erro ao excluir registro', 'error');
        }
    } catch (error) {
        console.error('Erro ao excluir registro:', error);
        showNotification('Erro ao excluir registro', 'error');
    }
}

function filterTables() {
    const searchTerm = document.getElementById('searchTables').value.toLowerCase();
    const tableItems = document.querySelectorAll('.table-item');
    
    tableItems.forEach(item => {
        const tableName = item.querySelector('.table-item-name').textContent.toLowerCase();
        
        if (tableName.includes(searchTerm)) {
            item.style.display = 'block';
        } else {
            item.style.display = 'none';
        }
    });
}

function exportTableData() {
    if (!currentTableName) {
        showNotification('Selecione uma tabela para exportar', 'warning');
        return;
    }
    
    const table = tablesData.find(t => t.name === currentTableName);
    if (!table || table.records.length === 0) {
        showNotification('Nenhum registro para exportar', 'warning');
        return;
    }
    
    // Preparar dados para exportação
    const exportData = table.records.map(record => {
        try {
            const data = typeof record.valor === 'string' ? JSON.parse(record.valor) : record.valor;
            return {
                id: record.id,
                ...data
            };
        } catch (e) {
            return {
                id: record.id,
                valor: record.valor
            };
        }
    });
    
    // Criar e baixar arquivo JSON
    const dataStr = JSON.stringify(exportData, null, 2);
    const dataBlob = new Blob([dataStr], {type: 'application/json'});
    
    const link = document.createElement('a');
    link.href = URL.createObjectURL(dataBlob);
    link.download = `${currentTableName}_${new Date().toISOString().split('T')[0]}.json`;
    
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    showNotification(`Dados da tabela ${currentTableName} exportados com sucesso`, 'success');
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
    
    const typeClass = {
        'success': 'text-bg-success',
        'error': 'text-bg-danger',
        'warning': 'text-bg-warning',
        'info': 'text-bg-info'
    }[type] || 'text-bg-info';
    
    const toast = document.createElement('div');
    toast.className = `toast align-items-center ${typeClass} border-0`;
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