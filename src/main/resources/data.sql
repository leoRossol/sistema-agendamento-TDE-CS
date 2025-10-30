-- Para testes, usamos senha em texto simples "123456" (AutenticacaoService aceita plain text em ambiente de teste)
INSERT INTO usuarios (nome, email, senha, ativo, tipo_usuario)
VALUES ('Thiago', 'thiago@teste.com', '123456', true, 'PROFESSOR');
