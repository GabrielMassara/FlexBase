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
    query VARCHAR(255) NOT NULL,
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
-- Create indexes for better performance
--

CREATE INDEX idx_tb_aplicacao_id_usuario ON tb_aplicacao(id_usuario);
CREATE INDEX idx_tb_endpoints_id_aplicacao ON tb_endpoints(id_aplicacao);
CREATE INDEX idx_tb_registros_id_aplicacao ON tb_registros(id_aplicacao);
CREATE INDEX idx_tb_registros_valor ON tb_registros USING GIN(valor);
CREATE INDEX idx_tb_aplicacao_schema_banco ON tb_aplicacao USING GIN(schema_banco);
CREATE INDEX idx_tb_usuarios_email ON tb_usuarios(email);
