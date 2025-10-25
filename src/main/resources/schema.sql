CREATE TABLE pessoa (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(255) NOT NULL,
  data_nascimento DATE NOT NULL,
  telefone VARCHAR(11) NOT NULL,
  cargo VARCHAR(50) NOT NULL,
  ativo BOOLEAN NOT NULL,
  data_cadastro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  data_alteracao TIMESTAMP NULL,
  usuario_alterador INT NULL
);

CREATE TABLE lixeira (
  id INT AUTO_INCREMENT PRIMARY KEY,
  andar VARCHAR(255) NOT NULL,
  localizacao VARCHAR(255) NOT NULL,
  descricao_localizacao VARCHAR(255) NOT NULL,
  descricao_ticket_usuario VARCHAR(255) NOT NULL
);

CREATE TABLE ticket_mestre (
    id INT AUTO_INCREMENT PRIMARY KEY,
    data_criacao_ticket TIMESTAMP NOT NULL,
    data_atualizacao_ticket TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    prioridade VARCHAR(20) NOT NULL,
    equipe_responsavel VARCHAR(50) NOT NULL,
    andar VARCHAR(255),
    localizacao VARCHAR(255),
    descricao_localizacao VARCHAR(255),
    descricao_ticket_usuario VARCHAR(255),
    imagem VARCHAR(255)
);

CREATE TABLE ticket (
  id INT AUTO_INCREMENT PRIMARY KEY,
  data_criacao_ticket TIMESTAMP NOT NULL,
  data_atualizacao_ticket TIMESTAMP,
  id_usuario INT NULL,
  status VARCHAR(20) NOT NULL,
  prioridade VARCHAR(20) NOT NULL,
  equipe_responsavel VARCHAR(20) NOT NULL,
  andar VARCHAR(255),
  localizacao VARCHAR(255),
  descricao_localizacao VARCHAR(255),
  descricao_ticket_usuario VARCHAR(255),
  imagem VARCHAR(255),
  id_ticket_mestre INT NULL,
  id_lixeira INT NULL,
  CONSTRAINT fk_ticket_usuario FOREIGN KEY (id_usuario) REFERENCES pessoa(id),
  CONSTRAINT fk_ticket_ticket_mestre FOREIGN KEY (id_ticket_mestre) REFERENCES ticket_mestre(id),
  CONSTRAINT fk_ticket_lixeira FOREIGN KEY (id_lixeira) REFERENCES lixeira(id)
);

CREATE TABLE resolucao_ticket (
  id INT AUTO_INCREMENT PRIMARY KEY,
  id_ticket_mestre INT NOT NULL,
  descricao VARCHAR(255),
  data_resolucao TIMESTAMP,
  id_funcionario INT NOT NULL,
  CONSTRAINT fk_resolucao_ticket_ticket_mestre FOREIGN KEY (id_ticket_mestre) REFERENCES ticket_mestre(id),
  CONSTRAINT fk_resolucao_ticket_funcionario FOREIGN KEY (id_funcionario) REFERENCES pessoa(id)
);

CREATE TABLE usuario (
  id INT AUTO_INCREMENT PRIMARY KEY,
  id_pessoa INT NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  senha VARCHAR(255) NOT NULL,
  ativo BOOLEAN NOT NULL,
  CONSTRAINT fk_usuario_pessoa FOREIGN KEY (id_pessoa) REFERENCES pessoa(id)
);