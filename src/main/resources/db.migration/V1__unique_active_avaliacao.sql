CREATE UNIQUE INDEX unique_active_avaliacao_por_empresa
ON avaliacao_mensal (empresa_id)
WHERE is_active = true;

COMMENT ON INDEX unique_active_avaliacao_por_empresa IS 'Ensures only one active monthly evaluation per company';