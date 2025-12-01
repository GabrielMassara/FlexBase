-- PostgreSQL Database Script
-- Database: db_flexbase
-- Generated on: November 21, 2025

--
-- Database: db_flexbase
--

-- --------------------------------------------------------

--
-- Table structure for table tb_usuarios
--

CREATE TABLE tb_usuarios (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    sobrenome VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL
);

-- --------------------------------------------------------

--
-- Table structure for table tb_aplicacao
--

CREATE TABLE tb_aplicacao (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    readme TEXT,
    id_usuario INTEGER NOT NULL,
    nome_banco VARCHAR(100) DEFAULT NULL,
    schema_banco JSONB,
    id_key_base INTEGER,
    CONSTRAINT fk_aplicacao_usuario 
        FOREIGN KEY (id_usuario) 
        REFERENCES tb_usuarios(id) 
        ON DELETE CASCADE
);

-- --------------------------------------------------------

--
-- Table structure for table tb_endpoints
--

CREATE TABLE tb_endpoints (
    id SERIAL PRIMARY KEY,
    id_aplicacao INTEGER NOT NULL,
    rota VARCHAR(100) NOT NULL,
    query TEXT NOT NULL,
    metodo INTEGER NOT NULL,
    CONSTRAINT fk_endpoints_aplicacao 
        FOREIGN KEY (id_aplicacao) 
        REFERENCES tb_aplicacao(id) 
        ON DELETE CASCADE
);

-- --------------------------------------------------------

--
-- Table structure for table tb_registros
--

CREATE TABLE tb_registros (
    id SERIAL PRIMARY KEY,
    tabela VARCHAR(100) NOT NULL,
    valor JSONB NOT NULL,
    id_aplicacao INTEGER NOT NULL,
    CONSTRAINT fk_registros_aplicacao 
        FOREIGN KEY (id_aplicacao) 
        REFERENCES tb_aplicacao(id) 
        ON DELETE CASCADE
);

-- --------------------------------------------------------

--
-- Table structure for table tb_sequences
-- Controla o próximo ID lógico de cada (aplicação, tabela)
--

CREATE TABLE tb_sequences (
    id_aplicacao INTEGER NOT NULL,
    tabela       VARCHAR(100) NOT NULL,
    proximo_id   BIGINT NOT NULL DEFAULT 1,
    PRIMARY KEY (id_aplicacao, tabela),
    CONSTRAINT fk_sequences_aplicacao
        FOREIGN KEY (id_aplicacao)
        REFERENCES tb_aplicacao(id)
        ON DELETE CASCADE
);

-- --------------------------------------------------------

--
-- Table structure for table tb_keys
--

CREATE TABLE tb_keys (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(255) NOT NULL UNIQUE,
    id_aplicacao INTEGER NOT NULL,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_keys_aplicacao 
        FOREIGN KEY (id_aplicacao) 
        REFERENCES tb_aplicacao(id) 
        ON DELETE CASCADE
);

-- --------------------------------------------------------

--
-- Table structure for table tb_key_endpoint
--

CREATE TABLE tb_key_endpoint (
    id_key INTEGER NOT NULL,
    id_endpoint INTEGER NOT NULL,
    PRIMARY KEY (id_key, id_endpoint),
    CONSTRAINT fk_key_endpoint_key 
        FOREIGN KEY (id_key) 
        REFERENCES tb_keys(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_key_endpoint_endpoint 
        FOREIGN KEY (id_endpoint) 
        REFERENCES tb_endpoints(id) 
        ON DELETE CASCADE
);

-- --------------------------------------------------------

--
-- Table structure for table tb_usuario_aplicacao
--

CREATE TABLE tb_usuario_aplicacao (
    id SERIAL PRIMARY KEY,
    id_usuario INTEGER NOT NULL,
    id_aplicacao INTEGER NOT NULL,
    id_key INTEGER NOT NULL,
    dados_usuario JSONB,
    data_vinculo TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_usuario_aplicacao_usuario 
        FOREIGN KEY (id_usuario) 
        REFERENCES tb_usuarios(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_usuario_aplicacao_aplicacao 
        FOREIGN KEY (id_aplicacao) 
        REFERENCES tb_aplicacao(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_usuario_aplicacao_key 
        FOREIGN KEY (id_key) 
        REFERENCES tb_keys(id) 
        ON DELETE CASCADE,
    UNIQUE(id_usuario, id_aplicacao)
);

-- --------------------------------------------------------

--
-- Create indexes for better performance
--

CREATE INDEX idx_tb_aplicacao_id_usuario ON tb_aplicacao(id_usuario);
CREATE INDEX idx_tb_aplicacao_id_key_base ON tb_aplicacao(id_key_base);
CREATE INDEX idx_tb_endpoints_id_aplicacao ON tb_endpoints(id_aplicacao);
CREATE INDEX idx_tb_registros_id_aplicacao ON tb_registros(id_aplicacao);
CREATE INDEX idx_tb_registros_valor ON tb_registros USING GIN(valor);
CREATE INDEX idx_tb_aplicacao_schema_banco ON tb_aplicacao USING GIN(schema_banco);
CREATE INDEX idx_tb_usuarios_email ON tb_usuarios(email);
CREATE INDEX idx_tb_keys_codigo ON tb_keys(codigo);
CREATE INDEX idx_tb_keys_id_aplicacao ON tb_keys(id_aplicacao);
CREATE INDEX idx_tb_usuario_aplicacao_id_usuario ON tb_usuario_aplicacao(id_usuario);
CREATE INDEX idx_tb_usuario_aplicacao_id_aplicacao ON tb_usuario_aplicacao(id_aplicacao);
CREATE INDEX idx_tb_usuario_aplicacao_id_key ON tb_usuario_aplicacao(id_key);
CREATE INDEX idx_tb_usuario_aplicacao_dados ON tb_usuario_aplicacao USING GIN(dados_usuario);

-- --------------------------------------------------------

--
-- Function: fn_next_id
-- Retorna o próximo ID lógico para uma tabela de uma aplicação
-- usando tb_sequences como contador por (id_aplicacao, tabela)
--

CREATE OR REPLACE FUNCTION fn_next_id(
    p_id_aplicacao INTEGER,
    p_tabela       VARCHAR
)
RETURNS BIGINT AS $$
DECLARE
    v_next BIGINT;
BEGIN
    INSERT INTO tb_sequences (id_aplicacao, tabela, proximo_id)
    VALUES (p_id_aplicacao, p_tabela, 1)
    ON CONFLICT (id_aplicacao, tabela)
    DO UPDATE SET proximo_id = tb_sequences.proximo_id + 1
    RETURNING proximo_id INTO v_next;

    RETURN v_next;
END;
$$ LANGUAGE plpgsql;

-- --------------------------------------------------------

--
-- Function: fn_generate_key_code
-- Gera um código único para uma key
--

CREATE OR REPLACE FUNCTION fn_generate_key_code()
RETURNS VARCHAR AS $$
DECLARE
    v_code VARCHAR;
    v_exists BOOLEAN;
BEGIN
    LOOP
        -- Gera código com formato FB_ + timestamp + random
        v_code := 'FB_' || EXTRACT(EPOCH FROM NOW())::BIGINT::TEXT || '_' || 
                  LPAD(FLOOR(RANDOM() * 1000000)::TEXT, 6, '0');
        
        -- Verifica se já existe
        SELECT EXISTS(SELECT 1 FROM tb_keys WHERE codigo = v_code) INTO v_exists;
        
        -- Se não existe, sai do loop
        IF NOT v_exists THEN
            EXIT;
        END IF;
    END LOOP;
    
    RETURN v_code;
END;
$$ LANGUAGE plpgsql;

-- --------------------------------------------------------

--
-- Function: fn_create_base_key
-- Cria uma key base para uma aplicação
--

CREATE OR REPLACE FUNCTION fn_create_base_key(
    p_id_aplicacao INTEGER,
    p_nome_aplicacao VARCHAR
)
RETURNS INTEGER AS $$
DECLARE
    v_id_key INTEGER;
BEGIN
    INSERT INTO tb_keys (codigo, id_aplicacao, nome, descricao)
    VALUES (
        fn_generate_key_code(),
        p_id_aplicacao,
        'Key Base - ' || p_nome_aplicacao,
        'Key base para novos usuários da aplicação ' || p_nome_aplicacao
    )
    RETURNING id INTO v_id_key;
    
    RETURN v_id_key;
END;
$$ LANGUAGE plpgsql;

-- --------------------------------------------------------

--
-- Function: fn_create_aplicacao_with_base_key
-- Cria uma aplicação junto com sua key base de forma atômica
--

CREATE OR REPLACE FUNCTION fn_create_aplicacao_with_base_key(
    p_nome VARCHAR,
    p_readme TEXT,
    p_id_usuario INTEGER,
    p_nome_banco VARCHAR DEFAULT NULL,
    p_schema_banco JSONB DEFAULT NULL
)
RETURNS TABLE(id_aplicacao INTEGER, id_key_base INTEGER, codigo_key VARCHAR) AS $$
DECLARE
    v_id_aplicacao INTEGER;
    v_id_key INTEGER;
    v_codigo_key VARCHAR;
BEGIN
    -- Primeiro, cria a aplicação com id_key_base NULL temporariamente
    INSERT INTO tb_aplicacao (nome, readme, id_usuario, nome_banco, schema_banco)
    VALUES (p_nome, p_readme, p_id_usuario, p_nome_banco, p_schema_banco)
    RETURNING id INTO v_id_aplicacao;
    
    -- Depois, cria a key base com o id correto da aplicação
    INSERT INTO tb_keys (codigo, id_aplicacao, nome, descricao)
    VALUES (
        fn_generate_key_code(),
        v_id_aplicacao,
        'Key Base - ' || p_nome,
        'Key base para novos usuários da aplicação ' || p_nome
    )
    RETURNING id, codigo INTO v_id_key, v_codigo_key;
    
    -- Atualiza a aplicação com o id correto da key base
    UPDATE tb_aplicacao SET id_key_base = v_id_key WHERE id = v_id_aplicacao;
    
    RETURN QUERY SELECT v_id_aplicacao, v_id_key, v_codigo_key;
END;
$$ LANGUAGE plpgsql;

-- --------------------------------------------------------

--
-- Constraint: Adiciona a foreign key para id_key_base após a criação das tabelas
-- Usando DEFERRABLE para permitir inserção em ordem específica
--

ALTER TABLE tb_aplicacao 
ADD CONSTRAINT fk_aplicacao_key_base 
FOREIGN KEY (id_key_base) 
REFERENCES tb_keys(id) 
ON DELETE RESTRICT;

-- --------------------------------------------------------

--
-- Exemplo de uso da função fn_create_aplicacao_with_base_key
-- Para cadastrar uma nova aplicação com key base:
--
-- SELECT * FROM fn_create_aplicacao_with_base_key(
--     'Nome da Aplicação',
--     'Descrição da aplicação...',
--     1, -- id do usuário
--     'nome_do_banco_opcional',
--     '{"tabelas": []}' -- schema JSON opcional
-- );
--
-- Retorna: id_aplicacao, id_key_base, codigo_key

