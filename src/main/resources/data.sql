INSERT INTO pessoa (nome, data_nascimento, telefone, cargo, ativo, data_cadastro) VALUES
('Lucas Willians', '1990-02-21', '19111111111', 'GERENTE', true, CURRENT_TIMESTAMP),
('Rafael Trevisan', '1990-02-21', '11999999999', 'LIMPEZA', true, CURRENT_TIMESTAMP),
('Maria Souza', '1985-03-25', '9876543210', 'CLIENTE', true, CURRENT_TIMESTAMP);

INSERT INTO usuario (id_pessoa, email, senha, ativo)
VALUES (2, 'rafael@email.com', '$2a$10$FRpsw.sm9W9T1n8tPs9dgeQxLWYJGWZ7nsbHlDMerSlmvtPVS8lky', true);

INSERT INTO lixeira (andar, localizacao, descricao_localizacao, descricao_ticket_usuario)VALUES
('1º Andar', 'Restaurantes', 'Próximo ao Bobs', 'Lixeira de plástico cheia.');
