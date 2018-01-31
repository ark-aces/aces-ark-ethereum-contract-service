-- todo
CREATE TABLE contracts (
  pid BIGSERIAL PRIMARY KEY,
  id VARCHAR(255) NOT NULL,
  correlation_id VARCHAR(255),
  status VARCHAR(255),
  created_at TIMESTAMP,
  expires_at TIMESTAMP,
  ark_smart_bridge VARCHAR(255)

);
