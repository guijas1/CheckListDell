ALTER TABLE checklist
ADD COLUMN localidade VARCHAR(3)
CHECK (localidade IN ('RJ','BSB','SC','PE') OR localidade IS NULL);
