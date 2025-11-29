# Sistema de Agendamento - TDE Construção de Software

Sistema de agendamento de salas e recursos educacionais desenvolvido com Spring Boot, incluindo observabilidade com Prometheus/Grafana e arquitetura de microserviços com Spring Cloud.

## Membros do Grupo
- Artur Pereira
- Davi Oliveira
- Leonardo Rossol
- Leonardo Monteiro
- Thiago

## Tecnologias Utilizadas

### Backend
- **Java 17**
- **Spring Boot 3.5.6**
- **Spring Data JPA** - Persistência de dados
- **Spring Security** - Autenticação e autorização
- **JWT** - Tokens de autenticação
- **MySQL 8.0** - Banco de dados em produção
- **H2 Database** - Banco de dados em desenvolvimento
- **Lombok** - Redução de boilerplate
- **SpringDoc OpenAPI** - Documentação da API

### Observabilidade
- **Spring Boot Actuator** - Endpoints de monitoramento e métricas
- **Micrometer Prometheus** - Exportação de métricas no formato Prometheus
- **Prometheus** - Coleta e armazenamento de métricas
- **Grafana** - Visualização de métricas e dashboards

### Spring Cloud (Microserviços)
- **Spring Cloud Netflix Eureka** - Service Discovery
- **Spring Cloud Gateway** - API Gateway

### DevOps
- **Docker & Docker Compose** - Containerização e orquestração

## Arquitetura do Sistema

O sistema segue uma arquitetura de microserviços com os seguintes componentes:

```
┌─────────────┐
│   Cliente   │
└──────┬──────┘
       │
       v
┌─────────────────┐
│  API Gateway    │ (Porta 8081)
│ (Spring Cloud)  │
└────────┬────────┘
         │
         v
    ┌────┴────────────────┐
    │                     │
    v                     v
┌──────────────┐   ┌──────────────┐
│   Eureka     │   │   Reports    │
│   Server     │◄──│   Service    │ (Porta 8080)
│ (Discovery)  │   └──────┬───────┘
└──────────────┘          │
                          v
                   ┌─────────────┐
                   │    MySQL    │
                   └─────────────┘

┌──────────────┐   ┌──────────────┐
│  Prometheus  │──►│   Grafana    │
│ (Métricas)   │   │ (Dashboard)  │
└──────────────┘   └──────────────┘
```

## Funcionalidades

### Gestão de Agendamentos
- Criar, editar e cancelar reservas de salas
- Verificar disponibilidade de salas
- Sugestões de horários alternativos
- Lista de espera para salas ocupadas

### Gestão de Recursos
- Cadastro de salas com diferentes tipos e capacidades
- Gestão de equipamentos disponíveis
- Associação de equipamentos às salas

### Gestão Acadêmica
- Cadastro de cursos, disciplinas e turmas
- Gestão de professores e alunos
- Matrículas de alunos em turmas

### Relatórios
- Relatório de ocupação de salas
- Estatísticas de uso
- Agendamentos por período

### Autenticação e Autorização
- Sistema de login com JWT
- Diferentes níveis de acesso (Admin, Professor, Aluno)
- Endpoints protegidos por autenticação

### Observabilidade
- Métricas de aplicação (CPU, memória, threads)
- Métricas de HTTP (requests, latência, status codes)
- Métricas de banco de dados (conexões, queries)
- Dashboards personalizados no Grafana
- Health checks e readiness probes

### Arquitetura de Microserviços (Spring Cloud)

#### Service Discovery (Eureka Server)
- Registro automático de serviços
- Descoberta dinâmica de instâncias
- Health checking de serviços
- Dashboard web para visualização dos serviços registrados
- Balanceamento de carga client-side

#### API Gateway (Spring Cloud Gateway)
- Ponto de entrada único para todas as requisições
- Roteamento dinâmico baseado em service discovery
- Balanceamento de carga automático
- Suporte a filtros e transformações de request/response
- Integração com Eureka para descoberta de serviços

**Benefícios da Arquitetura:**
- **Escalabilidade**: Facilita a adição de novas instâncias de serviços
- **Resiliência**: Falhas em um serviço não afetam os demais
- **Manutenibilidade**: Serviços podem ser atualizados independentemente
- **Monitoramento**: Visibilidade completa da saúde dos serviços

## Pré-requisitos

- **Java 17** ou superior
- **Maven 3.6+**
- **Docker** e **Docker Compose**
- **Git**

## Configuração e Execução

### 1. Clonar o Repositório

```bash
git clone <url-do-repositorio>
cd sistema-agendamento-TDE-CS
```

### 2. Executar com Docker Compose

A forma mais simples de executar o sistema completo é usando Docker Compose:

```bash
docker-compose up -d
```

Isso irá iniciar todos os serviços:
- **MySQL** - Porta 3307
- **Eureka Server** - Porta 8761 (Service Discovery)
- **Reports Service** - Porta 8080 (Backend principal)
- **API Gateway** - Porta 8081 (Ponto de entrada único)
- **Prometheus** - Porta 9090 (Coleta de métricas)
- **Grafana** - Porta 3000 (Visualização de métricas)

### 3. Executar em Desenvolvimento (Sem Docker)

Para desenvolvimento local com H2:

```bash
mvn spring-boot:run
```

O serviço estará disponível em `http://localhost:8080`

### 4. Acessar os Serviços

| Serviço | URL | Credenciais |
|---------|-----|-------------|
| **API Gateway** (Recomendado) | http://localhost:8081 | - |
| Reports Service (Direto) | http://localhost:8080 | - |
| Eureka Server Dashboard | http://localhost:8761 | - |
| Swagger UI | http://localhost:8081/swagger-ui.html | Via Gateway |
| Swagger UI (Direto) | http://localhost:8080/swagger-ui.html | Acesso direto |
| H2 Console | http://localhost:8080/h2-console | JDBC URL: `jdbc:h2:mem:testdb`<br>User: `sa`<br>Password: `password` |
| Prometheus | http://localhost:9090 | - |
| Grafana | http://localhost:3000 | User: `admin`<br>Password: `admin` |
| Actuator Health (Gateway) | http://localhost:8081/actuator/health | - |
| Actuator Health (Reports) | http://localhost:8080/actuator/health | - |
| Prometheus Metrics | http://localhost:8080/actuator/prometheus | - |

**Nota:** Recomenda-se acessar todos os endpoints da API através do API Gateway (porta 8081) em produção. O acesso direto aos serviços (porta 8080) deve ser usado apenas para desenvolvimento e debug.

## Configuração do Grafana

Após acessar o Grafana:

1. O datasource Prometheus já está pré-configurado
2. Um dashboard padrão está disponível em "Dashboards"
3. Para criar novos dashboards, use as métricas disponíveis em `http://localhost:8080/actuator/prometheus`

### Métricas Principais Disponíveis

- `http_server_requests_seconds` - Latência de requisições HTTP
- `jvm_memory_used_bytes` - Uso de memória JVM
- `jvm_threads_live` - Threads ativas
- `hikaricp_connections` - Pool de conexões do banco
- `system_cpu_usage` - Uso de CPU do sistema

## Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/sistema/agendamento/
│   │   ├── config/          # Configurações (Security, OpenAPI, JPA)
│   │   ├── controller/      # Controllers REST
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # Entidades JPA
│   │   ├── enums/           # Enumerações
│   │   ├── exception/       # Exceções customizadas
│   │   ├── repository/      # Repositórios JPA
│   │   ├── service/         # Lógica de negócio
│   │   └── utils/           # Utilitários
│   └── resources/
│       ├── application.properties         # Config desenvolvimento
│       └── application-docker.properties  # Config Docker
├── test/                    # Testes unitários e integração
└── ...
```

## API Endpoints Principais

### Autenticação
- `POST /auth/login` - Login de usuário
- `POST /auth/register` - Registro de novo usuário (Admin)

### Agendamentos
- `GET /api/agendamentos` - Listar agendamentos
- `POST /api/agendamentos` - Criar agendamento
- `PUT /api/agendamentos/{id}` - Atualizar agendamento
- `DELETE /api/agendamentos/{id}` - Cancelar agendamento

### Salas
- `GET /api/salas` - Listar salas
- `POST /api/salas` - Cadastrar sala
- `GET /api/salas/disponiveis` - Buscar salas disponíveis
- `GET /api/salas/{id}/sugestoes` - Sugestões de horários

### Turmas
- `GET /api/turmas` - Listar turmas
- `POST /api/turmas` - Criar turma
- `PUT /api/turmas/{id}` - Atualizar turma
- `DELETE /api/turmas/{id}` - Remover turma

### Relatórios
- `POST /api/relatorios/ocupacao` - Relatório de ocupação

## Testes

### Executar Testes Unitários

```bash
mvn test
```

### Executar Testes de Integração

```bash
mvn verify
```

## Profiles do Spring

O projeto utiliza dois profiles:

1. **default** - Desenvolvimento local com H2
   - Banco H2 em memória
   - Console H2 habilitado
   - Logs em DEBUG

2. **docker** - Ambiente containerizado
   - MySQL 8.0
   - Logs em INFO
   - Configurações otimizadas para produção

Para ativar um profile:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

## Observabilidade - Monitoramento

### Verificar Health da Aplicação

```bash
curl http://localhost:8080/actuator/health
```

### Visualizar Métricas Prometheus

```bash
curl http://localhost:8080/actuator/prometheus
```

### Queries Úteis no Prometheus

```promql
# Taxa de requisições por segundo
rate(http_server_requests_seconds_count[1m])

# Latência P95 das requisições
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# Uso de memória heap
jvm_memory_used_bytes{area="heap"}

# Conexões ativas no pool
hikaricp_connections_active
```

## Segurança

- Autenticação via JWT (JSON Web Tokens)
- Senhas criptografadas com BCrypt
- Endpoints protegidos por roles (ADMIN, PROFESSOR, ALUNO)
- CORS configurado
- Validação de dados de entrada

## Troubleshooting

### Porta já em uso

Se houver conflito de portas, edite o `docker-compose.yml` ou `application.properties`:

```properties
server.port=8081
```

### Problema de conexão com MySQL

Verifique se o container está rodando:

```bash
docker ps | grep mysql
```

Verifique os logs:

```bash
docker logs sistema-agendamento-mysql
```

### Prometheus não coleta métricas

Verifique se o endpoint está acessível:

```bash
curl http://localhost:8080/actuator/prometheus
```

Verifique a configuração em `prometheus.yml` e reinicie:

```bash
docker-compose restart prometheus
```

## Contribuindo

1. Crie uma branch para sua feature: `git checkout -b feature/nova-funcionalidade`
2. Commit suas mudanças: `git commit -m 'feat: adiciona nova funcionalidade'`
3. Push para a branch: `git push origin feature/nova-funcionalidade`
4. Abra um Pull Request

## Baseline

A branch `Baseline-ConstrSW-2025-2-parte2` contém a versão congelada do projeto ao final da Parte 2, antes da adição do Spring Cloud.

## Licença

Este projeto foi desenvolvido para fins educacionais como parte da disciplina de Construção de Software.

## Contato

Para dúvidas ou sugestões, entre em contato com os membros do grupo.
