CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50),
    email VARCHAR(50),
    senha VARCHAR(255),
    ativo BOOLEAN,
    tipo_usuario VARCHAR(20) NOT NULL
);
