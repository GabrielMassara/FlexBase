// Estado da aplicação
let state = {
    tables: [],
    selectedTable: null,
    selectedField: null,
    isDragging: false,
    dragOffset: { x: 0, y: 0 },
    currentEditingTable: null,
    theme: 'light'
};

// Dados iniciais vazios - aplicação começa sem tabelas
const initialData = {
    "tabelas": []
};

// Instâncias dos modais Bootstrap
let tableModal, relationshipModal;

// Inicialização
document.addEventListener('DOMContentLoaded', function() {
    // Aguardar um pouco para garantir que o Bootstrap foi carregado
    setTimeout(() => {
        initializeApp();
        setupEventListeners();
        loadInitialData();
    }, 100);
});

function initializeApp() {
    // Verificar se Bootstrap está carregado
    if (typeof bootstrap === 'undefined') {
        return;
    }
    
    // Inicializar modais Bootstrap
    tableModal = new bootstrap.Modal(document.getElementById('tableModal'));
    relationshipModal = new bootstrap.Modal(document.getElementById('relationshipModal'));
    
    // Carregar tema salvo
    const savedTheme = localStorage.getItem('flexbase-theme') || 'light';
    setTheme(savedTheme);
}

function setupEventListeners() {
    // Botões principais
    const addTableBtn = document.getElementById('addTableBtn');
    const addRelationshipBtn = document.getElementById('addRelationshipBtn');
    const themeToggle = document.getElementById('themeToggle');
    
    if (addTableBtn) {
        addTableBtn.addEventListener('click', openTableModal);
    }
    
    if (addRelationshipBtn) {
        addRelationshipBtn.addEventListener('click', openRelationshipModal);
    }
    
    if (themeToggle) {
        themeToggle.addEventListener('click', toggleTheme);
    }
    
    // Botão para centralizar elementos
    const centerElementsBtn = document.getElementById('centerElementsBtn');
    if (centerElementsBtn) {
        centerElementsBtn.addEventListener('click', centerAllElements);
    }

    // Modal da tabela
    const saveTableBtn = document.getElementById('saveTableBtn');
    const addFieldBtn = document.getElementById('addFieldBtn');
    
    if (saveTableBtn) {
        saveTableBtn.addEventListener('click', function(e) {
            e.preventDefault();
            saveTable();
        });
    }
    
    if (addFieldBtn) {
        addFieldBtn.addEventListener('click', addFieldToModal);
    }

    // Modal de relacionamento
    const saveRelationshipBtn = document.getElementById('saveRelationshipBtn');
    const sourceTable = document.getElementById('sourceTable');
    const targetTable = document.getElementById('targetTable');
    
    if (saveRelationshipBtn) {
        saveRelationshipBtn.addEventListener('click', saveRelationship);
    }
    
    if (sourceTable) {
        sourceTable.addEventListener('change', updateSourceFields);
    }
    
    if (targetTable) {
        targetTable.addEventListener('change', updateTargetFields);
    }

    // Canvas events
    const canvas = document.getElementById('canvas');
    if (canvas) {
        canvas.addEventListener('click', handleCanvasClick);
        canvas.addEventListener('mousedown', handleMouseDown);
        canvas.addEventListener('mousemove', handleMouseMove);
        canvas.addEventListener('mouseup', handleMouseUp);
    }

    // Keyboard events
    document.addEventListener('keydown', handleKeyDown);
}

function loadInitialData() {
    // Inicializar com estado vazio - sem tabelas por padrão
    state.tables = [];

    renderTables();
    renderSidebar();
}

// Funções de Tema
function setTheme(theme) {
    state.theme = theme;
    document.documentElement.setAttribute('data-bs-theme', theme);
    
    const themeIcon = document.querySelector('#themeToggle i');
    if (theme === 'dark') {
        themeIcon.className = 'bi bi-moon';
    } else {
        themeIcon.className = 'bi bi-sun';
    }
    
    localStorage.setItem('flexbase-theme', theme);
}

function toggleTheme() {
    const newTheme = state.theme === 'light' ? 'dark' : 'light';
    setTheme(newTheme);
    
    // Re-renderizar relacionamentos para atualizar cores
    setTimeout(renderRelationships, 100);
}

// Função para monitorar mudanças de posição e redesenhar relacionamentos
function watchForPositionChanges(tableElement, duration = 300) {
    let lastX = parseFloat(tableElement.style.left) || 0;
    let lastY = parseFloat(tableElement.style.top) || 0;
    let checkCount = 0;
    const maxChecks = Math.ceil(duration / 16); // ~60fps
    
    const checkPosition = () => {
        const currentX = parseFloat(tableElement.style.left) || 0;
        const currentY = parseFloat(tableElement.style.top) || 0;
        
        if (currentX !== lastX || currentY !== lastY) {
            lastX = currentX;
            lastY = currentY;
            renderRelationships();
        }
        
        checkCount++;
        if (checkCount < maxChecks) {
            requestAnimationFrame(checkPosition);
        }
    };
    
    requestAnimationFrame(checkPosition);
}

// Função para centralizar todos os elementos no canvas
function centerAllElements() {
    const canvasContainer = document.querySelector('.canvas-container');
    const canvas = document.getElementById('canvas');
    const tableElements = canvas.querySelectorAll('.canvas-table');
    
    if (tableElements.length === 0) {
        return; // Não há elementos para centralizar
    }
    
    // Calcular dimensões do container visível
    const containerRect = canvasContainer.getBoundingClientRect();
    const containerWidth = containerRect.width;
    const containerHeight = containerRect.height;
    
    // Calcular dimensões necessárias para organizar as tabelas
    const tableWidth = 220; // Largura mínima das tabelas
    const tableSpacing = 40; // Espaço entre tabelas
    const tablesPerRow = Math.floor(containerWidth / (tableWidth + tableSpacing));
    const totalRows = Math.ceil(tableElements.length / tablesPerRow);
    
    // Calcular posição inicial para centralizar o conjunto
    const totalWidth = Math.min(tableElements.length, tablesPerRow) * (tableWidth + tableSpacing) - tableSpacing;
    const totalHeight = totalRows * 200; // Altura aproximada de cada tabela
    
    const startX = Math.max(20, (containerWidth - totalWidth) / 2);
    const startY = Math.max(20, (containerHeight - totalHeight) / 2);
    
    // Reposicionar cada tabela
    tableElements.forEach((tableElement, index) => {
        const row = Math.floor(index / tablesPerRow);
        const col = index % tablesPerRow;
        
        const x = startX + col * (tableWidth + tableSpacing);
        const y = startY + row * 220; // Espaço vertical entre linhas
        
        tableElement.style.left = x + 'px';
        tableElement.style.top = y + 'px';
        
        // Atualizar posição no estado usando o ID correto
        const tableId = tableElement.id;
        const table = state.tables.find(t => t.id === tableId);
        if (table) {
            table.position = { x, y };
        }
        
        // Monitorar mudanças de posição durante a transição
        watchForPositionChanges(tableElement);
    });
    
    // Redesenhar relacionamentos imediatamente e após delay
    renderRelationships();
    setTimeout(() => {
        renderRelationships();
    }, 250);
}

// Funções do Modal de Tabela
function openTableModal(table = null) {
    const title = document.getElementById('modalTitle');
    const nameInput = document.getElementById('tableName');
    const fieldsList = document.getElementById('fieldsList');

    if (!title || !nameInput || !fieldsList) {
        alert('Erro: Elementos do modal não encontrados');
        return;
    }

    state.currentEditingTable = table;

    if (table) {
        title.textContent = 'Editar Tabela';
        nameInput.value = table.name || '';
        renderModalFields(table.fields);
    } else {
        title.textContent = 'Nova Tabela';
        nameInput.value = '';
        fieldsList.innerHTML = '';
        addFieldToModal(); // Adicionar um campo inicial
    }

    if (tableModal) {
        tableModal.show();
    } else {
        // Fallback para mostrar modal sem Bootstrap
        const modalElement = document.getElementById('tableModal');
        if (modalElement) {
            modalElement.style.display = 'block';
            modalElement.classList.add('show');
        }
    }
    
    setTimeout(() => nameInput.focus(), 300);
}

function addFieldToModal() {
    const fieldsList = document.getElementById('fieldsList');
    
    const fieldDiv = document.createElement('div');
    fieldDiv.className = 'field-input-group';
    fieldDiv.innerHTML = `
        <input type="text" class="form-control" placeholder="Nome do campo" style="flex: 2;">
        <select class="form-select" style="flex: 1;">
            <option value="string">String</option>
            <option value="integer">Integer</option>
            <option value="number">Number</option>
            <option value="date">Date</option>
            <option value="boolean">Boolean</option>
        </select>
        <button type="button" class="btn btn-outline-danger btn-sm" onclick="removeField(this)">
            <i class="bi bi-trash"></i>
        </button>
    `;
    
    fieldsList.appendChild(fieldDiv);
}

function removeField(button) {
    button.closest('.field-input-group').remove();
}

function renderModalFields(fields) {
    const fieldsList = document.getElementById('fieldsList');
    fieldsList.innerHTML = '';
    
    // Verificar se fields existe e é um array
    if (!fields || !Array.isArray(fields)) {
        return;
    }
    
    fields.forEach(field => {
        const fieldDiv = document.createElement('div');
        fieldDiv.className = 'field-input-group';
        fieldDiv.innerHTML = `
            <input type="text" class="form-control" placeholder="Nome do campo" value="${field.name || ''}" style="flex: 2;">
            <select class="form-select" style="flex: 1;">
                <option value="string" ${field.type === 'string' ? 'selected' : ''}>String</option>
                <option value="integer" ${field.type === 'integer' ? 'selected' : ''}>Integer</option>
                <option value="number" ${field.type === 'number' ? 'selected' : ''}>Number</option>
                <option value="date" ${field.type === 'date' ? 'selected' : ''}>Date</option>
                <option value="boolean" ${field.type === 'boolean' ? 'selected' : ''}>Boolean</option>
            </select>
            <button type="button" class="btn btn-outline-danger btn-sm" onclick="removeField(this)">
                <i class="bi bi-trash"></i>
            </button>
        `;
        fieldsList.appendChild(fieldDiv);
    });
}

function saveTable() {
    const nameInput = document.getElementById('tableName');
    const fieldsList = document.getElementById('fieldsList');
    
    if (!nameInput || !fieldsList) {
        alert('Erro: Elementos do formulário não encontrados');
        return;
    }
    
    const name = nameInput.value.trim();
    
    if (!name) {
        alert('Por favor, insira um nome para a tabela');
        return;
    }

    // Verificar se já existe tabela com esse nome (exceto se estiver editando a mesma)
    const existingTable = state.tables.find(t => t.name === name && t.id !== (state.currentEditingTable ? state.currentEditingTable.id : null));
    if (existingTable) {
        alert('Já existe uma tabela com este nome');
        return;
    }

    // Coletar campos
    const fieldItems = fieldsList.querySelectorAll('.field-input-group');
    
    const fields = Array.from(fieldItems).map((item, index) => {
        const nameInput = item.querySelector('.form-control');
        const typeSelect = item.querySelector('.form-select');
        const fieldName = nameInput ? nameInput.value.trim() : '';
        const fieldType = typeSelect ? typeSelect.value : 'string';
        
        if (!fieldName) return null;
        
        return {
            id: `field_${Date.now()}_${index}`,
            name: fieldName,
            type: fieldType,
            isPrimary: fieldName === 'id',
            isForeign: fieldName.endsWith('Id') && fieldName !== 'id'
        };
    }).filter(field => field !== null);

    if (fields.length === 0) {
        alert('Adicione pelo menos um campo');
        return;
    }

    if (state.currentEditingTable) {
        // Editando tabela existente
        state.currentEditingTable.name = name;
        state.currentEditingTable.fields = fields;
    } else {
        // Criando nova tabela
        const newTable = {
            id: `table_${Date.now()}`,
            name: name,
            fields: fields,
            relationships: [],
            position: {
                x: 100 + (state.tables.length * 50),
                y: 100 + (state.tables.length * 50)
            }
        };
        state.tables.push(newTable);
    }
    
    renderTables();
    renderSidebar();
    
    if (tableModal) {
        tableModal.hide();
    }
    state.currentEditingTable = null;
}

// Expor função globalmente
window.saveTable = saveTable;

// Funções do Modal de Relacionamento
function openRelationshipModal() {
    populateTableSelects();
    relationshipModal.show();
}

function populateTableSelects() {
    const sourceTable = document.getElementById('sourceTable');
    const targetTable = document.getElementById('targetTable');
    
    // Limpar opções existentes
    sourceTable.innerHTML = '<option value="">Selecione uma tabela</option>';
    targetTable.innerHTML = '<option value="">Selecione uma tabela</option>';
    
    // Adicionar tabelas
    state.tables.forEach(table => {
        sourceTable.innerHTML += `<option value="${table.id}">${table.name}</option>`;
        targetTable.innerHTML += `<option value="${table.id}">${table.name}</option>`;
    });
}

function updateSourceFields() {
    const sourceTableId = document.getElementById('sourceTable').value;
    const sourceField = document.getElementById('sourceField');
    
    sourceField.innerHTML = '<option value="">Selecione um campo</option>';
    
    if (sourceTableId) {
        const table = state.tables.find(t => t.id === sourceTableId);
        if (table) {
            table.fields.forEach(field => {
                sourceField.innerHTML += `<option value="${field.name}">${field.name} (${field.type})</option>`;
            });
        }
    }
}

function updateTargetFields() {
    const targetTableId = document.getElementById('targetTable').value;
    const targetField = document.getElementById('targetField');
    
    targetField.innerHTML = '<option value="">Selecione um campo</option>';
    
    if (targetTableId) {
        const table = state.tables.find(t => t.id === targetTableId);
        if (table) {
            table.fields.forEach(field => {
                targetField.innerHTML += `<option value="${field.name}">${field.name} (${field.type})</option>`;
            });
        }
    }
}

function saveRelationship() {
    const sourceTableId = document.getElementById('sourceTable').value;
    const sourceFieldName = document.getElementById('sourceField').value;
    const targetTableId = document.getElementById('targetTable').value;
    const targetFieldName = document.getElementById('targetField').value;
    const relationType = document.getElementById('relationType').value;
    
    if (!sourceTableId || !sourceFieldName || !targetTableId || !targetFieldName) {
        alert('Preencha todos os campos do relacionamento');
        return;
    }
    
    if (sourceTableId === targetTableId) {
        alert('Não é possível criar relacionamento entre a mesma tabela');
        return;
    }
    
    const sourceTable = state.tables.find(t => t.id === sourceTableId);
    const targetTable = state.tables.find(t => t.id === targetTableId);
    
    if (!sourceTable || !targetTable) {
        alert('Erro: tabelas não encontradas');
        return;
    }
    
    // Verificar se relacionamento já existe
    const existingRelationship = sourceTable.relationships.find(rel => 
        rel.campoLocal === sourceFieldName && 
        rel.tabelaAlvo === targetTable.name && 
        rel.campoAlvo === targetFieldName
    );
    
    if (existingRelationship) {
        alert('Este relacionamento já existe');
        return;
    }
    
    // Criar relacionamento
    const newRelationship = {
        tipo: relationType,
        campoLocal: sourceFieldName,
        tabelaAlvo: targetTable.name,
        campoAlvo: targetFieldName
    };
    
    sourceTable.relationships.push(newRelationship);
    
    renderRelationships();
    relationshipModal.hide();
    
    // Limpar formulário
    document.getElementById('sourceTable').value = '';
    document.getElementById('sourceField').innerHTML = '<option value="">Selecione um campo</option>';
    document.getElementById('targetTable').value = '';
    document.getElementById('targetField').innerHTML = '<option value="">Selecione um campo</option>';
}

// Funções de Renderização
function updateTableSelection() {
    const allTables = document.querySelectorAll('.canvas-table');
    allTables.forEach(tableElement => {
        if (tableElement.id === state.selectedTable) {
            tableElement.classList.add('selected');
        } else {
            tableElement.classList.remove('selected');
        }
    });
    
    const allFields = document.querySelectorAll('.field-row');
    allFields.forEach(fieldElement => {
        if (fieldElement.dataset.fieldId === state.selectedField) {
            fieldElement.classList.add('selected');
        } else {
            fieldElement.classList.remove('selected');
        }
    });
}

function renderTables() {
    const canvas = document.getElementById('canvas');
    // Remover tabelas existentes
    const existingTables = canvas.querySelectorAll('.canvas-table');
    existingTables.forEach(table => table.remove());

    // Renderizar todas as tabelas
    state.tables.forEach(table => {
        const tableElement = createTableElement(table);
        canvas.appendChild(tableElement);
    });

    // Renderizar relacionamentos
    renderRelationships();
}

function createTableElement(table) {
    const tableDiv = document.createElement('div');
    tableDiv.className = 'canvas-table';
    tableDiv.id = table.id;
    tableDiv.dataset.tableId = table.id;
    tableDiv.style.left = table.position.x + 'px';
    tableDiv.style.top = table.position.y + 'px';
    
    if (state.selectedTable === table.id) {
        tableDiv.classList.add('selected');
    }

    // Verificar se fields existe e é um array
    const fields = table.fields || [];
    const fieldsHTML = fields.map(field => {
        let keyClass = '';
        let keyText = '';
        
        if (field.isPrimary) {
            keyClass = 'primary';
            keyText = 'PK';
        } else if (field.isForeign) {
            keyClass = 'foreign';
            keyText = 'FK';
        }

        // Ícone baseado no tipo
        let typeIcon = '';
        switch(field.type) {
            case 'integer': typeIcon = 'bi-123'; break;
            case 'string': typeIcon = 'bi-type'; break;
            case 'date': typeIcon = 'bi-calendar-date'; break;
            case 'number': typeIcon = 'bi-hash'; break;
            case 'boolean': typeIcon = 'bi-toggle-on'; break;
            default: typeIcon = 'bi-question-circle';
        }

        return `
            <div class="field-row ${state.selectedField === field.id ? 'selected' : ''}" 
                 data-field-id="${field.id}" onclick="selectField('${field.id}')">
                <i class="field-icon bi ${typeIcon}"></i>
                <div class="field-name">${field.name || 'Campo sem nome'}</div>
                <div class="field-type">${field.type || 'string'}</div>
                ${keyText ? `<div class="field-key ${keyClass}">${keyText}</div>` : ''}
            </div>
        `;
    }).join('');

    tableDiv.innerHTML = `
        <div class="table-header">
            <span>${table.name}</span>
            <div class="table-menu">
                <button class="btn-icon" onclick="editTable('${table.id}')" title="Editar">
                    <i class="bi bi-pencil"></i>
                </button>
                <button class="btn-icon" onclick="deleteTable('${table.id}')" title="Excluir">
                    <i class="bi bi-trash"></i>
                </button>
            </div>
        </div>
        <div class="table-fields">
            ${fieldsHTML}
        </div>
    `;

    // Adicionar eventos de drag
    const header = tableDiv.querySelector('.table-header');
    header.addEventListener('mousedown', (e) => startDrag(e, table.id));

    return tableDiv;
}

function renderSidebar() {
    const tablesList = document.getElementById('tablesList');
    
    if (state.tables.length === 0) {
        tablesList.innerHTML = '<p class="text-muted text-center">Nenhuma tabela criada</p>';
        return;
    }

    tablesList.innerHTML = state.tables.map(table => {
        const fieldCount = (table.fields || []).length;
        return `
            <div class="table-item ${state.selectedTable === table.id ? 'active' : ''}" 
                 onclick="selectTable('${table.id}')">
                <div class="table-item-header">
                    <span class="table-name">${table.name || 'Tabela sem nome'}</span>
                    <span class="field-count badge bg-secondary">${fieldCount} campos</span>
                </div>
                <div class="table-actions">
                    <button class="btn-icon" onclick="event.stopPropagation(); editTable('${table.id}')" title="Editar">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn-icon" onclick="event.stopPropagation(); deleteTable('${table.id}')" title="Excluir">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>
            </div>
        `;
    }).join('');

    renderPropertiesPanel();
}

function renderPropertiesPanel() {
    const panel = document.getElementById('propertiesPanel');
    
    if (state.selectedTable) {
        const table = state.tables.find(t => t.id === state.selectedTable);
        if (!table) {
            panel.innerHTML = '<p class="text-muted text-center m-0">Tabela não encontrada</p>';
            return;
        }
        
        const relationships = getRelationshipsForTable(state.selectedTable);
        const fieldCount = (table.fields || []).length;
        
        let relationshipsHTML = '';
        if (relationships.length > 0) {
            relationshipsHTML = `
                <div class="property-item">
                    <div class="property-label">Relacionamentos</div>
                    <div class="mt-2">
                        ${relationships.map((rel, index) => `
                            <div class="d-flex justify-content-between align-items-center mb-1 p-2 bg-body-tertiary rounded">
                                <small class="text-muted">
                                    ${rel.campoLocal} → ${rel.tabelaAlvo}.${rel.campoAlvo}
                                </small>
                                <button class="btn btn-outline-danger btn-sm" onclick="deleteRelationship('${state.selectedTable}', ${index})" title="Excluir">
                                    <i class="bi bi-trash"></i>
                                </button>
                            </div>
                        `).join('')}
                    </div>
                </div>
            `;
        }
        
        panel.innerHTML = `
            <div class="property-item">
                <div class="property-label">Nome da Tabela</div>
                <div class="property-value">${table.name || 'Sem nome'}</div>
            </div>
            <div class="property-item">
                <div class="property-label">Número de Campos</div>
                <div class="property-value">${fieldCount}</div>
            </div>
            <div class="property-item">
                <div class="property-label">Posição</div>
                <div class="property-value">x: ${Math.round(table.position.x || 0)}, y: ${Math.round(table.position.y || 0)}</div>
            </div>
            ${relationshipsHTML}
        `;
    } else if (state.selectedField) {
        const field = findFieldById(state.selectedField);
        if (field) {
            panel.innerHTML = `
                <div class="property-item">
                    <div class="property-label">Nome do Campo</div>
                    <div class="property-value">${field.name}</div>
                </div>
                <div class="property-item">
                    <div class="property-label">Tipo</div>
                    <div class="property-value">${field.type}</div>
                </div>
                <div class="property-item">
                    <div class="property-label">Chave Primária</div>
                    <div class="property-value">${field.isPrimary ? 'Sim' : 'Não'}</div>
                </div>
                <div class="property-item">
                    <div class="property-label">Chave Estrangeira</div>
                    <div class="property-value">${field.isForeign ? 'Sim' : 'Não'}</div>
                </div>
            `;
        }
    } else {
        panel.innerHTML = '<p class="text-muted text-center m-0">Selecione uma tabela ou campo</p>';
    }
}

function renderRelationships() {
    // Remover linhas de relacionamento existentes
    const canvas = document.getElementById('canvas');
    const existingLines = canvas.querySelectorAll('.relationship-line');
    existingLines.forEach(line => line.remove());

    // Renderizar relacionamentos
    state.tables.forEach(table => {
        table.relationships.forEach(rel => {
            const sourceTable = table;
            const targetTable = state.tables.find(t => t.name === rel.tabelaAlvo);
            
            if (targetTable) {
                createRelationshipLine(sourceTable, targetTable, rel);
            }
        });
    });
}

function createRelationshipLine(sourceTable, targetTable, relationship) {
    const canvas = document.getElementById('canvas');
    const sourceElement = document.getElementById(sourceTable.id);
    const targetElement = document.getElementById(targetTable.id);

    if (!sourceElement || !targetElement) return;

    const sourceRect = sourceElement.getBoundingClientRect();
    const targetRect = targetElement.getBoundingClientRect();
    const canvasRect = canvas.getBoundingClientRect();

    // Calcular posições relativas ao canvas
    const sourceX = sourceElement.offsetLeft + sourceElement.offsetWidth;
    const sourceY = sourceElement.offsetTop + sourceElement.offsetHeight / 2;
    const targetX = targetElement.offsetLeft;
    const targetY = targetElement.offsetTop + targetElement.offsetHeight / 2;

    // Criar linha de relacionamento
    const line = document.createElement('div');
    line.className = 'relationship-line';
    
    const deltaX = targetX - sourceX;
    const deltaY = targetY - sourceY;
    const length = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    const angle = Math.atan2(deltaY, deltaX) * 180 / Math.PI;

    line.style.left = sourceX + 'px';
    line.style.top = sourceY + 'px';
    line.style.width = length + 'px';
    line.style.transformOrigin = '0 50%';
    line.style.transform = `rotate(${angle}deg)`;

    canvas.appendChild(line);
}

// Funções de Interação
function selectTable(tableId) {
    state.selectedTable = tableId;
    state.selectedField = null;
    updateTableSelection();
    renderSidebar();
}

function selectField(fieldId) {
    state.selectedField = fieldId;
    updateTableSelection();
    renderPropertiesPanel();
}

function editTable(tableId) {
    const table = state.tables.find(t => t.id === tableId);
    if (table) {
        openTableModal(table);
    }
}

function deleteTable(tableId) {
    if (confirm('Tem certeza que deseja excluir esta tabela?')) {
        state.tables = state.tables.filter(t => t.id !== tableId);
        if (state.selectedTable === tableId) {
            state.selectedTable = null;
        }
        renderTables();
        renderSidebar();
    }
}

// Funções de Drag and Drop
function startDrag(e, tableId) {
    e.preventDefault();
    state.isDragging = true;
    const table = state.tables.find(t => t.id === tableId);
    const tableElement = document.getElementById(tableId);
    
    if (table && tableElement) {
        const rect = tableElement.getBoundingClientRect();
        state.dragOffset = {
            x: e.clientX - rect.left,
            y: e.clientY - rect.top
        };
        
        // Desabilitar transição durante o drag para movimento mais fluido
        tableElement.style.transition = 'none';
        
        selectTable(tableId);
    }
}

function handleMouseDown(e) {
    // Implementado em startDrag para tabelas
}

function handleMouseMove(e) {
    if (state.isDragging && state.selectedTable) {
        const table = state.tables.find(t => t.id === state.selectedTable);
        const canvas = document.getElementById('canvas');
        const canvasRect = canvas.getBoundingClientRect();
        
        if (table) {
            table.position.x = e.clientX - canvasRect.left - state.dragOffset.x;
            table.position.y = e.clientY - canvasRect.top - state.dragOffset.y;
            
            // Limitar às bordas do canvas
            table.position.x = Math.max(0, table.position.x);
            table.position.y = Math.max(0, table.position.y);
            
            const tableElement = document.getElementById(table.id);
            if (tableElement) {
                tableElement.style.left = table.position.x + 'px';
                tableElement.style.top = table.position.y + 'px';
            }
            
            renderRelationships();
        }
    } else if (!state.isDragging && state.selectedTable) {
        // Reabilitar transição quando parar o drag
        const tableElement = document.getElementById(state.selectedTable);
        if (tableElement && tableElement.style.transition === 'none') {
            tableElement.style.transition = '';
        }
    }
}

function handleMouseUp(e) {
    state.isDragging = false;
    
    // Se há uma tabela selecionada, aguardar o fim da transição para redesenhar as setas
    if (state.selectedTable) {
        const tableElement = document.getElementById(state.selectedTable);
        if (tableElement) {
            // Redesenhar imediatamente
            renderRelationships();
            
            // Aguardar o fim da transição e redesenhar novamente
            const handleTransitionEnd = () => {
                renderRelationships();
                tableElement.removeEventListener('transitionend', handleTransitionEnd);
            };
            
            tableElement.addEventListener('transitionend', handleTransitionEnd);
            
            // Fallback: redesenhar após um tempo caso não haja transição
            setTimeout(() => {
                renderRelationships();
            }, 250);
        }
    }
}

function handleCanvasClick(e) {
    if (e.target.id === 'canvas' || e.target.classList.contains('canvas-grid')) {
        state.selectedTable = null;
        state.selectedField = null;
        
        // Apenas atualizar classes de seleção sem recriar as tabelas
        const allTables = document.querySelectorAll('.canvas-table');
        allTables.forEach(table => {
            table.classList.remove('selected');
        });
        
        const allFields = document.querySelectorAll('.field-row');
        allFields.forEach(field => {
            field.classList.remove('selected');
        });
        
        renderSidebar();
    }
}

function handleKeyDown(e) {
    if (e.key === 'Delete' && state.selectedTable) {
        deleteTable(state.selectedTable);
    }
    if (e.key === 'Escape') {
        closeTableModal();
        closeImportModal();
    }
}

// Funções de Gerenciamento de Relacionamentos
function deleteRelationship(sourceTableId, relationshipIndex) {
    if (confirm('Tem certeza que deseja excluir este relacionamento?')) {
        const sourceTable = state.tables.find(t => t.id === sourceTableId);
        if (sourceTable) {
            sourceTable.relationships.splice(relationshipIndex, 1);
            renderRelationships();
            renderPropertiesPanel();
        }
    }
}

function getRelationshipsForTable(tableId) {
    const table = state.tables.find(t => t.id === tableId);
    if (!table) return [];
    
    return table.relationships.map((rel, index) => ({
        ...rel,
        sourceTable: table.name,
        index: index
    }));
}

// Funções utilitárias
function findFieldById(fieldId) {
    for (let table of state.tables) {
        const field = table.fields.find(f => f.id === fieldId);
        if (field) {
            return field;
        }
    }
    return null;
}