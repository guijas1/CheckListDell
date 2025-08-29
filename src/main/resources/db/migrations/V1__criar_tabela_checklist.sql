-- Criação da tabela principal
CREATE TABLE checklist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    modelo VARCHAR(255),
    patrimonio VARCHAR(255),
    service_tag VARCHAR(255),
    liga BOOLEAN,
    tela_funciona BOOLEAN,
    teclado_funciona BOOLEAN,
    wifi_funciona BOOLEAN,
    carcaca BOOLEAN,
    observacoes TEXT,
    data_criacao TIMESTAMP
);

-- Criação da tabela auxiliar para armazenar fotos
CREATE TABLE checklist_fotos (
    checklist_id BIGINT NOT NULL,
    foto_path VARCHAR(500),
    FOREIGN KEY (checklist_id) REFERENCES checklist(id) ON DELETE CASCADE
);
