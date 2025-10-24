-- ===========================================================
-- ✅ Adiciona colunas novas à tabela CHECKLIST (sem duplicar service_tag)
-- Data: 2025-10-20
-- ===========================================================

ALTER TABLE checklist
    ADD COLUMN borracha_protecao_ok BOOLEAN,
    ADD COLUMN borracha_protecao_faltantes INT,
    ADD COLUMN parafusos_ok BOOLEAN,
    ADD COLUMN parafusos_faltantes INT,
    ADD COLUMN tampa_inferior_ok BOOLEAN,
    ADD COLUMN tampa_inferior_faltantes INT,
    ADD COLUMN portas_funcionando BOOLEAN,
    ADD COLUMN dobradicas_ok BOOLEAN,
    ADD COLUMN manchas_tela BOOLEAN,
    ADD COLUMN trincos_tela BOOLEAN,
    ADD COLUMN camera_funciona BOOLEAN,
    ADD COLUMN microfone_funciona BOOLEAN,
    ADD COLUMN alto_falante_funciona BOOLEAN,
    ADD COLUMN demanda VARCHAR(255),
    ADD COLUMN tag VARCHAR(50),
    ADD COLUMN de_para VARCHAR(255);
