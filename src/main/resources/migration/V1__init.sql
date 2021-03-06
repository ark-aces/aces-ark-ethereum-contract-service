CREATE TABLE contracts (
  pid BIGSERIAL PRIMARY KEY,
  id VARCHAR(255) NOT NULL,
  correlation_id VARCHAR(255),
  status VARCHAR(255),
  created_at TIMESTAMP,
  expires_at TIMESTAMP,
  contract_abi_json VARCHAR,
  contract_code VARCHAR,
  gas_limit BIGINT,
  ark_smart_bridge VARCHAR(255),
  service_ark_address VARCHAR(255),
  return_ark_address VARCHAR(255),
  ark_per_eth_exchange_rate DECIMAL(20, 8),
  estimated_gas_cost BIGINT,
  estimated_eth_cost DECIMAL(20, 8),
  ark_flat_fee DECIMAL(20, 8),
  ark_fee_percent DECIMAL(20, 8),
  ark_fee_total DECIMAL(20, 8),
  required_ark DECIMAL(20, 8),
  ark_payment_transaction_id VARCHAR(255),
  ark_payment_amount DECIMAL(20, 8),
  gas_used BIGINT,
  deployment_ark_cost DECIMAL(20, 8),
  return_ark_transaction_id VARCHAR(255),
  return_ark_amount DECIMAL(20, 8),
  eth_contract_transaction_id VARCHAR(255)
);
CREATE INDEX idx_contract_ark_smart_bridge ON contracts(ark_smart_bridge);
