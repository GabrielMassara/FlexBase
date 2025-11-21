# FlexBase

FlexBase é uma plataforma de Backend as a Service (BaaS) que transforma modelos de banco de dados relacionais em APIs completas e prontas para uso em poucos minutos.

## 🗃️ Estrutura do Banco de Dados

O sistema utiliza PostgreSQL com as seguintes tabelas:

### `tb_usuarios` - Autenticação
- `id` (SERIAL PRIMARY KEY)
- `nome` (VARCHAR(100) NOT NULL)
- `sobrenome` (VARCHAR(100) NOT NULL)
- `email` (VARCHAR(255) NOT NULL UNIQUE)
- `senha` (VARCHAR(255) NOT NULL - MD5 hash)

### `tb_aplicacao`
- `id` (SERIAL PRIMARY KEY)
- `nome` (VARCHAR(100) NOT NULL)
- `readme` (TEXT)
- `id_usuario` (INTEGER NOT NULL - FK para tb_usuarios)
- `nome_banco` (VARCHAR(100))
- `schema_banco` (JSONB)

### `tb_endpoints`
- `id` (SERIAL PRIMARY KEY)
- `id_aplicacao` (INTEGER NOT NULL - FK para tb_aplicacao)
- `rota` (VARCHAR(100) NOT NULL)
- `query` (VARCHAR(255) NOT NULL)
- `metodo` (INTEGER NOT NULL - 0=GET, 1=POST, 2=PUT, 3=DELETE, etc.)

### `tb_registros`
- `id` (SERIAL PRIMARY KEY)
- `tabela` (VARCHAR(100) NOT NULL)
- `valor` (JSONB NOT NULL)
- `id_aplicacao` (INTEGER NOT NULL - FK para tb_aplicacao)

## 🏗️ Arquitetura do Sistema

O sistema segue o padrão em camadas:

```
├── model/          # Entidades do banco
├── dao/            # Data Access Objects
├── filterDTO/      # DTOs para filtros de busca
├── responseDTO/    # DTOs para respostas da API
├── service/        # Lógica de negócio e controle das rotas
├── util/          # Utilitários (AuthFilter, JwtUtil)
└── app/           # Aplicação principal
```

## 📡 API Endpoints

### 🔐 Autenticação

**IMPORTANTE**: Exceto as rotas públicas (`/api/login`, `/api/usuarios` POST), todas as outras rotas requerem autenticação via token JWT no header `Authorization: Bearer <token>`.

#### Login
```
POST /api/login
Body: {
  "email": "usuario@email.com",
  "senha": "senha123"
}

Response: {
  "success": true,
  "message": "Login realizado com sucesso",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "usuario": {
    "id": 1,
    "nome": "Usuario",
    "email": "usuario@email.com"
  }
}
```

### 👥 Usuários

#### Listar usuários
```
GET /api/usuarios
Headers: Authorization: Bearer <token>
```
**Nota**: Usuários só podem ver seus próprios dados.

#### Buscar usuário por ID
```
GET /api/usuarios/:id
Headers: Authorization: Bearer <token>
```

#### Buscar usuários com filtro
```
POST /api/usuarios/buscar
Body: {
  "id": 1,
  "nome": "João",
  "email": "joao@email.com"
}
```

#### Criar usuário
```
POST /api/usuarios
Body: {
  "nome": "João",
  "sobrenome": "Silva",
  "email": "joao@email.com",
  "senha": "senha123"
}
```

#### Atualizar usuário
```
PUT /api/usuarios/:id
Body: {
  "nome": "João Carlos",
  "sobrenome": "Silva Santos",
  "email": "joao.carlos@email.com",
  "senha": "novaSenha123" // opcional
}
```

#### Excluir usuário
```
DELETE /api/usuarios/:id
```

### 📱 Aplicações

#### Listar aplicações
```
GET /api/aplicacoes
Headers: Authorization: Bearer <token>
```
**Nota**: Usuários só podem ver suas próprias aplicações.

#### Buscar aplicação por ID
```
GET /api/aplicacoes/:id
Headers: Authorization: Bearer <token>
```
**Nota**: Usuários só podem ver suas próprias aplicações.

#### Buscar aplicações do usuário logado
```
GET /api/aplicacoes/minhas
Headers: Authorization: Bearer <token>
```
**Nota**: Retorna apenas as aplicações do usuário autenticado pelo token.

#### Buscar aplicações com filtro
```
POST /api/aplicacoes/buscar
Body: {
  "id": 1,
  "nome": "MinhaApp",
  "idUsuario": 1,
  "nomeBanco": "app_db"
}
```

#### Criar aplicação
```
POST /api/aplicacoes
Headers: Authorization: Bearer <token>
Body: {
  "nome": "Minha Aplicação",
  "readme": "Descrição da aplicação...",
  "nomeBanco": "minha_app_db",
  "schemaBanco": {
    "tabelas": ["usuarios", "produtos"]
  }
}
```
**Nota**: O `idUsuario` é automaticamente definido com base no usuário logado pelo token. Não é necessário (e será ignorado) enviar no body.

#### Atualizar aplicação
```
PUT /api/aplicacoes/:id
Body: {
  "nome": "Nova Aplicação",
  "readme": "Nova descrição...",
  "nomeBanco": "novo_db",
  "schemaBanco": {...}
}
```

#### Excluir aplicação
```
DELETE /api/aplicacoes/:id
```

### 🔗 Endpoints

#### Listar endpoints
```
GET /api/endpoints
Headers: Authorization: Bearer <token>
```
**Nota**: Usuários só podem ver endpoints de suas próprias aplicações.

#### Buscar endpoint por ID
```
GET /api/endpoints/:id
Headers: Authorization: Bearer <token>
```
**Nota**: Usuários só podem ver endpoints de suas próprias aplicações.

#### Buscar endpoints por aplicação
```
GET /api/endpoints/aplicacao/:idAplicacao
Headers: Authorization: Bearer <token>
```
**Nota**: Usuários só podem ver endpoints de suas próprias aplicações.

#### Buscar endpoints com filtro
```
POST /api/endpoints/buscar
Body: {
  "id": 1,
  "idAplicacao": 1,
  "rota": "/api/users",
  "metodo": 0
}
```

#### Criar endpoint
```
POST /api/endpoints
Body: {
  "idAplicacao": 1,
  "rota": "/api/users",
  "query": "SELECT * FROM users",
  "metodo": 0
}
```

#### Atualizar endpoint
```
PUT /api/endpoints/:id
Body: {
  "rota": "/api/usuarios",
  "query": "SELECT * FROM usuarios WHERE ativo = true",
  "metodo": 0
}
```

#### Excluir endpoint
```
DELETE /api/endpoints/:id
```

### 📊 Registros

#### Listar registros
```
GET /api/registros
Headers: Authorization: Bearer <token>
```
**Nota**: Usuários só podem ver registros de suas próprias aplicações.

#### Buscar registro por ID
```
GET /api/registros/:id
```

#### Buscar registros por aplicação
```
GET /api/registros/aplicacao/:idAplicacao
```

#### Buscar registros por tabela
```
GET /api/registros/tabela/:tabela
```

#### Buscar registros com filtro
```
POST /api/registros/buscar
Body: {
  "id": 1,
  "tabela": "usuarios",
  "idAplicacao": 1
}
```

#### Criar registro
```
POST /api/registros
Body: {
  "tabela": "usuarios",
  "valor": {
    "nome": "João",
    "email": "joao@email.com",
    "idade": 30
  },
  "idAplicacao": 1
}
```

#### Atualizar registro
```
PUT /api/registros/:id
Body: {
  "tabela": "usuarios",
  "valor": {
    "nome": "João Carlos",
    "email": "joao.carlos@email.com",
    "idade": 31
  }
}
```

#### Excluir registro
```
DELETE /api/registros/:id
```

## 🔧 Configuração

### Banco de Dados
Ajuste as configurações de conexão em `dao/DAO.java`:

```java
String serverName = "localhost";
String mydatabase = "db_flexbase";
String username = "postgres";
String password = "postgres";
```

### Porta do Servidor
O servidor roda na porta 80 por padrão. Para alterar, modifique em `app/Aplicacao.java`:

```java
port(80); // Altere para a porta desejada
```

## 🚀 Como Executar

1. **Configurar o banco PostgreSQL**:
   - Criar o banco `db_flexbase`
   - Executar o script `src/main/resources/db_flexbase.sql`

2. **Compilar e executar**:
   ```bash
   # Compilar
   javac -cp "lib/*" src/main/java/app/Aplicacao.java
   
   # Executar
   java -cp "lib/*;src/main/java" app.Aplicacao
   ```

3. **Testar as APIs**:
   - Use um cliente REST como Postman ou Insomnia
   - Base URL: `http://localhost:80/api/`
   
4. **Fluxo de Autenticação para Testes**:
   ```bash
   # 1. Criar um usuário (se não existir)
   POST /api/usuarios
   {
     "nome": "Admin",
     "sobrenome": "Sistema",
     "email": "admin@flexbase.com",
     "senha": "admin123"
   }
   
   # 2. Fazer login para obter token
   POST /api/login
   {
     "email": "admin@flexbase.com",
     "senha": "admin123"
   }
   
   # 3. Usar o token retornado em todas as outras requisições
   Authorization: Bearer <token_obtido_no_login>
   ```

## 📝 Métodos HTTP dos Endpoints

Os endpoints utilizam códigos numéricos para os métodos HTTP:
- `0` = GET
- `1` = POST  
- `2` = PUT
- `3` = DELETE
- `4` = PATCH
- `5` = HEAD
- `6` = OPTIONS

## ⚠️ Códigos de Erro Comuns

### Autenticação e Autorização
- `401 Unauthorized`: 
  - "Token de autenticação não fornecido" - Header Authorization ausente
  - "Token inválido ou expirado" - Token JWT inválido ou expirado
  - "Erro ao extrair dados do token" - Problema na estrutura do token
- `403 Forbidden`:
  - "Você não tem permissão para..." - Tentativa de acesso a recurso de outro usuário

### Recursos
- `404 Not Found`: "Recurso não encontrado" - ID inexistente
- `400 Bad Request`: "Dados obrigatórios ausentes" - Campos requeridos não informados

### Respostas de Sucesso
- `200 OK`: Operação realizada com sucesso
- `201 Created`: Recurso criado com sucesso

## 🔒 Segurança

### Autenticação JWT
- **Token obrigatório**: Todas as rotas (exceto login e cadastro) requerem token JWT
- **Header**: `Authorization: Bearer <token>`
- **Expiração**: Tokens expiram em 24 horas
- **Claims**: O token contém `id` e `email` do usuário

### Controle de Acesso
- **Isolamento de dados**: Usuários podem ver/editar apenas seus próprios dados e aplicações
- **Validação de propriedade**: Sistema verifica se o usuário tem permissão para acessar/modificar recursos

### Outras Medidas
- Senhas são armazenadas com hash MD5
- CORS configurado para aceitar requisições de qualquer origem
- Validação de dados de entrada em todos os endpoints
- Proteção contra acesso não autorizado a recursos de outros usuários

### Rotas Públicas (sem autenticação)
- `POST /api/login` - Login de usuário
- `POST /api/usuarios` - Cadastro de novo usuário
- `POST /api/entrar` - Rota alternativa de entrada

### Como Usar a Autenticação
1. Faça login em `POST /api/login` para obter o token
2. Inclua o token no header de todas as outras requisições:
   ```
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```
3. O sistema automaticamente identificará o usuário pelo token
