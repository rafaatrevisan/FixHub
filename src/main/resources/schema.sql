CREATE TABLE pessoa (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(255) NOT NULL,
  data_nascimento DATE NOT NULL,
  telefone VARCHAR(11) NOT NULL,
  cargo VARCHAR(50) NOT NULL,
  data_cadastro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ticket (
  id INT AUTO_INCREMENT PRIMARY KEY,
  data_ticket TIMESTAMP NOT NULL,
  id_usuario INT NOT NULL,
  status VARCHAR(20) NOT NULL,
  prioridade VARCHAR(20) NOT NULL,
  equipe_responsavel VARCHAR(20) NOT NULL,
  andar VARCHAR(255) NOT NULL,
  localizacao VARCHAR(255) NOT NULL,
  descricao_localizacao VARCHAR(255) NOT NULL,
  descricao_ticket_usuario VARCHAR(255),
  imagem VARCHAR(255),
  CONSTRAINT fk_ticket_usuario FOREIGN KEY (id_usuario) REFERENCES pessoa(id)
);

CREATE TABLE resolucao_ticket (
  id INT AUTO_INCREMENT PRIMARY KEY,
  id_ticket INT NOT NULL,
  descricao VARCHAR(255) NOT NULL,
  data_resolucao TIMESTAMP NOT NULL,
  id_funcionario INT NOT NULL,
  CONSTRAINT fk_resolucao_ticket_ticket FOREIGN KEY (id_ticket) REFERENCES ticket(id),
  CONSTRAINT fk_resolucao_ticket_funcionario FOREIGN KEY (id_funcionario) REFERENCES pessoa(id)
);

CREATE TABLE usuario (
  id INT AUTO_INCREMENT PRIMARY KEY,
  id_pessoa INT NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  senha VARCHAR(255) NOT NULL,
  CONSTRAINT fk_usuario_pessoa FOREIGN KEY (id_pessoa) REFERENCES pessoa(id)
);
