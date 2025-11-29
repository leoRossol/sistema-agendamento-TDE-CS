# Sistema de Agendamento - Implementação de Observabilidade

**Disciplina:** Computação em Nuvem
**Trabalho:** Implementação de Observabilidade com Prometheus e Grafana
**Integrantes:**
- Artur Pereira
- Davi Oliveira
- Leonardo Rossol
- Thiago

**Data:** 19/11/2024

---

## 📋 Índice

1. [Resumo Executivo](#resumo-executivo)
2. [Objetivos](#objetivos)
3. [Tecnologias Utilizadas](#tecnologias-utilizadas)
4. [Arquitetura da Solução](#arquitetura-da-solução)
5. [Implementação](#implementação)
6. [Arquivos Modificados e Criados](#arquivos-modificados-e-criados)
7. [Configuração e Execução](#configuração-e-execução)
8. [Métricas Implementadas](#métricas-implementadas)
9. [Dashboard do Grafana](#dashboard-do-grafana)
10. [Testes e Validação](#testes-e-validação)
11. [Conclusões](#conclusões)
12. [Referências](#referências)

---

## 1. Resumo Executivo

Este documento descreve a implementação de uma solução completa de observabilidade para o Sistema de Agendamento, utilizando Prometheus para coleta de métricas e Grafana para visualização. A solução permite monitoramento em tempo real da aplicação Spring Boot, incluindo métricas de performance, utilização de recursos e comportamento do sistema.

---

## 2. Objetivos

### 2.1 Objetivo Geral
Implementar uma solução de observabilidade completa para o sistema de agendamento, permitindo monitoramento e análise de métricas da aplicação em tempo real.

### 2.2 Objetivos Específicos
- Configurar exportação de métricas no formato Prometheus na aplicação Spring Boot
- Implementar coleta automática de métricas usando Prometheus
- Criar dashboards de visualização no Grafana
- Containerizar Prometheus e Grafana usando Docker Compose
- Documentar processo de instalação e uso

---

## 3. Tecnologias Utilizadas

| Tecnologia | Versão | Propósito |
|------------|--------|-----------|
| **Spring Boot** | 3.5.6 | Framework da aplicação |
| **Micrometer** | (gerenciado pelo Spring Boot) | Biblioteca de métricas |
| **Prometheus** | latest | Coleta e armazenamento de métricas |
| **Grafana** | latest | Visualização de métricas |
| **Docker** | - | Containerização |
| **Docker Compose** | - | Orquestração de containers |
| **MySQL** | 8.0 | Banco de dados |

---

## 4. Arquitetura da Solução

### 4.1 Diagrama de Arquitetura

```
┌─────────────────────────────────────────────────────────────────┐
│                         Docker Network                           │
│                     (agendamento-network)                        │
│                                                                   │
│  ┌──────────────┐      ┌──────────────┐      ┌──────────────┐  │
│  │              │      │              │      │              │  │
│  │    MySQL     │◄─────┤  Reports-SVC │      │  Prometheus  │  │
│  │   (3307)     │      │   (8080)     │◄─────┤   (9090)     │  │
│  │              │      │              │      │              │  │
│  └──────────────┘      └──────┬───────┘      └──────┬───────┘  │
│                               │                      │           │
│                               │                      │           │
│                               │                      ▼           │
│                               │              ┌──────────────┐   │
│                               │              │              │   │
│                               └─────────────►│   Grafana    │   │
│                         /actuator/prometheus │   (3000)     │   │
│                                               │              │   │
│                                               └──────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
                         ┌────────────────┐
                         │   Usuário      │
                         │  (Navegador)   │
                         └────────────────┘
```

### 4.2 Fluxo de Dados

1. **Aplicação Spring Boot** expõe métricas em `/actuator/prometheus`
2. **Prometheus** faz scraping das métricas a cada 5 segundos
3. **Prometheus** armazena métricas em time-series database
4. **Grafana** consulta Prometheus para exibir dashboards
5. **Usuário** acessa Grafana via navegador (porta 3000)

---

## 5. Implementação

### 5.1 Fase 1: Configuração da Aplicação Spring Boot

#### 5.1.1 Adicionar Dependência Micrometer

**Arquivo:** `pom.xml`

```xml
<!-- Micrometer Prometheus para métricas -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

**Justificativa:** Micrometer é a biblioteca de métricas padrão do Spring Boot e fornece integração nativa com Prometheus.

#### 5.1.2 Configurar Endpoints de Métricas

**Arquivo:** `src/main/resources/application-docker.properties`

```properties
# Management endpoints - Expondo Prometheus para observabilidade
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=always
management.endpoint.prometheus.enabled=true
management.metrics.export.prometheus.enabled=true
management.metrics.distribution.percentiles-histogram.http.server.requests=true
management.metrics.tags.application=${spring.application.name}
```

**Explicação das configurações:**
- `exposure.include`: Lista de endpoints expostos via HTTP
- `prometheus.enabled`: Habilita endpoint específico do Prometheus
- `percentiles-histogram`: Gera histogramas para cálculo de percentis
- `tags.application`: Adiciona tag de identificação nas métricas

### 5.2 Fase 2: Configuração do Prometheus

**Arquivo:** `prometheus.yml` (novo)

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    monitor: 'sistema-agendamento-monitor'

scrape_configs:
  # Job para coletar métricas do serviço de relatórios
  - job_name: 'sistema-agendamento-reports'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s
    static_configs:
      - targets: ['reports-svc:8080']
        labels:
          application: 'sistema-agendamento-reports'
          environment: 'docker'

  # Job para monitorar o próprio Prometheus
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']
```

**Parâmetros importantes:**
- `scrape_interval: 5s`: Coleta métricas a cada 5 segundos
- `metrics_path`: Caminho do endpoint de métricas
- `targets`: Lista de serviços a monitorar
- `labels`: Metadados adicionados a todas as métricas

### 5.3 Fase 3: Configuração do Grafana

#### 5.3.1 Datasource Prometheus

**Arquivo:** `grafana/provisioning/datasources/prometheus.yml` (novo)

```yaml
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: true
    jsonData:
      timeInterval: "5s"
```

**Provisionamento automático:** Grafana configura o datasource automaticamente na inicialização.

#### 5.3.2 Dashboard Provider

**Arquivo:** `grafana/provisioning/dashboards/dashboard-provider.yml` (novo)

```yaml
apiVersion: 1

providers:
  - name: 'Sistema Agendamento Dashboards'
    orgId: 1
    folder: ''
    type: file
    disableDeletion: false
    updateIntervalSeconds: 10
    allowUiUpdates: true
    options:
      path: /etc/grafana/provisioning/dashboards
```

#### 5.3.3 Dashboard de Métricas

**Arquivo:** `grafana/provisioning/dashboards/sistema-agendamento-dashboard.json` (novo)

Dashboard com 6 painéis principais (detalhes na seção 9).

### 5.4 Fase 4: Configuração Docker Compose

**Arquivo:** `compose.yaml` (modificado)

Adicionados dois novos serviços:

```yaml
# Prometheus - Coleta de Métricas
prometheus:
  image: prom/prometheus:latest
  container_name: sistema-agendamento-prometheus
  ports:
    - "9090:9090"
  volumes:
    - ./prometheus.yml:/etc/prometheus/prometheus.yml:ro
    - prometheus_data:/prometheus
  command:
    - '--config.file=/etc/prometheus/prometheus.yml'
    - '--storage.tsdb.path=/prometheus'
    - '--web.enable-lifecycle'
  networks:
    - agendamento-network
  depends_on:
    - reports-svc

# Grafana - Visualização de Métricas
grafana:
  image: grafana/grafana:latest
  container_name: sistema-agendamento-grafana
  ports:
    - "3000:3000"
  environment:
    - GF_SECURITY_ADMIN_USER=admin
    - GF_SECURITY_ADMIN_PASSWORD=admin
    - GF_USERS_ALLOW_SIGN_UP=false
  volumes:
    - grafana_data:/var/lib/grafana
    - ./grafana/provisioning:/etc/grafana/provisioning:ro
  networks:
    - agendamento-network
  depends_on:
    - prometheus
```

**Volumes adicionados:**
```yaml
volumes:
  mysql_data:
  prometheus_data:
  grafana_data:
```

---

## 6. Arquivos Modificados e Criados

### 6.1 Arquivos Modificados

| Arquivo | Modificações | Linhas |
|---------|--------------|--------|
| `pom.xml` | Adicionada dependência Micrometer Prometheus | 114-117 |
| `application.properties` | Configuração de endpoints de métricas | 37-43 |
| `application-docker.properties` | Configuração de endpoints de métricas | 33-39 |
| `compose.yaml` | Adicionados serviços Prometheus e Grafana | 49-95 |

### 6.2 Arquivos Criados

#### Configuração de Infraestrutura
- `prometheus.yml` - Configuração do Prometheus
- `grafana/provisioning/datasources/prometheus.yml` - Datasource do Grafana
- `grafana/provisioning/dashboards/dashboard-provider.yml` - Provider de dashboards
- `grafana/provisioning/dashboards/sistema-agendamento-dashboard.json` - Dashboard principal

#### Scripts de Automação
- `validar-setup.ps1` - Script de validação completa (PowerShell)
- `validar-setup.sh` - Script de validação completa (Bash)
- `diagnostico.ps1` - Script de diagnóstico rápido
- `gerar-trafego.ps1` - Gerador de tráfego para testes (PowerShell)
- `gerar-trafego.sh` - Gerador de tráfego para testes (Bash)

#### Documentação
- `GUIA-GRAFANA.md` - Guia completo de uso do Grafana
- `TESTES-E-VALIDACAO.md` - Documentação de testes
- `RESOLVER-PROBLEMAS.md` - Guia de troubleshooting
- `ArturPereira_DaviOliveira_LeonardoRossol_Thiago.md` - Este documento

---

## 7. Configuração e Execução

### 7.1 Pré-requisitos

- Docker Desktop instalado e rodando
- Porta 8080, 9090 e 3000 disponíveis
- Java 17+ (para desenvolvimento local)
- Maven (para build local)

### 7.2 Instalação

#### Passo 1: Clonar/Navegar para o Projeto
```bash
cd C:\Users\abuil\sistema-agendamento-TDE-CS
```

#### Passo 2: Iniciar Containers
```bash
docker-compose up -d --build
```

Este comando:
- Builda a aplicação Spring Boot
- Inicia MySQL
- Inicia a aplicação
- Inicia Prometheus
- Inicia Grafana

#### Passo 3: Aguardar Inicialização
```bash
docker-compose logs -f reports-svc
```

Aguarde até ver: `Started SistemaAgendamentoApplication in X.XXX seconds`

Pressione `Ctrl+C` para sair.

#### Passo 4: Validar Instalação
```bash
.\diagnostico.ps1
```

### 7.3 Verificação

```bash
# Verificar containers
docker-compose ps

# Testar métricas
curl http://localhost:8080/actuator/prometheus

# Testar Prometheus
curl http://localhost:9090

# Testar Grafana
curl http://localhost:3000
```

### 7.4 Acesso às Interfaces

| Serviço | URL | Credenciais |
|---------|-----|-------------|
| Grafana | http://localhost:3000 | admin / admin |
| Prometheus | http://localhost:9090 | - |
| Aplicação | http://localhost:8080 | - |
| Métricas | http://localhost:8080/actuator/prometheus | - |
| Health | http://localhost:8080/actuator/health | - |
| Swagger | http://localhost:8080/swagger-ui.html | - |

---

## 8. Métricas Implementadas

### 8.1 Métricas HTTP

| Métrica | Descrição | Tipo |
|---------|-----------|------|
| `http_server_requests_seconds_count` | Contador de requisições HTTP | Counter |
| `http_server_requests_seconds_sum` | Tempo total de processamento | Counter |
| `http_server_requests_seconds_max` | Tempo máximo de resposta | Gauge |
| `http_server_requests_seconds_bucket` | Histograma de latências | Histogram |

**Labels:** method, uri, status, exception, outcome

**Exemplo de query:**
```promql
rate(http_server_requests_seconds_count{application="sistema-agendamento-reports"}[1m])
```

### 8.2 Métricas JVM

#### Memória
- `jvm_memory_used_bytes` - Memória usada
- `jvm_memory_max_bytes` - Memória máxima
- `jvm_memory_committed_bytes` - Memória comprometida

**Labels:** area (heap/non-heap), id (pool name)

#### Threads
- `jvm_threads_live` - Threads ativas
- `jvm_threads_daemon` - Threads daemon
- `jvm_threads_peak` - Pico de threads

#### Garbage Collection
- `jvm_gc_pause_seconds_count` - Número de pausas de GC
- `jvm_gc_pause_seconds_sum` - Tempo total em GC
- `jvm_gc_memory_promoted_bytes_total` - Memória promovida
- `jvm_gc_memory_allocated_bytes_total` - Memória alocada

### 8.3 Métricas de Sistema

- `system_cpu_usage` - Uso de CPU do sistema (0-1)
- `process_cpu_usage` - Uso de CPU do processo (0-1)
- `system_load_average_1m` - Carga média do sistema

### 8.4 Métricas do Banco de Dados (HikariCP)

- `hikaricp_connections_active` - Conexões ativas
- `hikaricp_connections_idle` - Conexões idle
- `hikaricp_connections` - Total de conexões
- `hikaricp_connections_pending` - Conexões aguardando
- `hikaricp_connections_timeout_total` - Timeouts de conexão
- `hikaricp_connections_creation_seconds` - Tempo de criação de conexões

---

## 9. Dashboard do Grafana

### 9.1 Visão Geral

O dashboard "Sistema de Agendamento - Métricas da Aplicação" fornece visão completa da saúde e performance da aplicação.

### 9.2 Painéis Implementados

#### Painel 1: Taxa de Requisições HTTP (req/s)
- **Tipo:** Time Series (gráfico de linha)
- **Query:**
  ```promql
  rate(http_server_requests_seconds_count{application="sistema-agendamento-reports"}[1m])
  ```
- **Visualização:** Requisições por segundo, agrupadas por método e URI
- **Utilidade:** Identificar picos de tráfego e endpoints mais utilizados

#### Painel 2: Latência P95 (segundos)
- **Tipo:** Gauge (medidor)
- **Query:**
  ```promql
  histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{application="sistema-agendamento-reports"}[5m])) by (le))
  ```
- **Visualização:** Medidor com thresholds
  - Verde: < 0.5s
  - Amarelo: 0.5s - 1s
  - Vermelho: > 1s
- **Utilidade:** Monitorar latência percebida por 95% dos usuários

#### Painel 3: Uso de Memória JVM
- **Tipo:** Time Series
- **Query:**
  ```promql
  jvm_memory_used_bytes{application="sistema-agendamento-reports"}
  ```
- **Visualização:** Múltiplas séries por área de memória (Heap, Non-Heap, etc.)
- **Unidade:** Bytes
- **Utilidade:** Detectar vazamento de memória, ajustar heap size

#### Painel 4: Uso de CPU (%)
- **Tipo:** Time Series
- **Queries:**
  ```promql
  system_cpu_usage{application="sistema-agendamento-reports"} * 100
  process_cpu_usage{application="sistema-agendamento-reports"} * 100
  ```
- **Visualização:** Duas linhas (CPU do sistema e do processo)
- **Unidade:** Porcentagem
- **Utilidade:** Identificar alta carga de CPU

#### Painel 5: Pool de Conexões do Banco de Dados
- **Tipo:** Time Series
- **Queries:**
  ```promql
  hikaricp_connections_active{application="sistema-agendamento-reports"}
  hikaricp_connections_idle{application="sistema-agendamento-reports"}
  hikaricp_connections{application="sistema-agendamento-reports"}
  ```
- **Visualização:** Três linhas (ativas, idle, total)
- **Utilidade:** Otimizar tamanho do pool, detectar connection leaks

#### Painel 6: Distribuição de Status HTTP
- **Tipo:** Pie Chart (gráfico de pizza)
- **Query:**
  ```promql
  sum by (status) (increase(http_server_requests_seconds_count{application="sistema-agendamento-reports"}[5m]))
  ```
- **Visualização:** Proporção de cada código de status HTTP
- **Utilidade:** Identificar taxa de erros (4xx, 5xx)

### 9.3 Configurações do Dashboard

- **Refresh:** 5 segundos (configurável)
- **Time Range:** Últimos 15 minutos (padrão)
- **Timezone:** Browser
- **Tags:** spring-boot, sistema-agendamento

---

## 10. Testes e Validação

### 10.1 Testes Realizados

#### Teste 1: Validação de Infraestrutura
```bash
# Executado
.\diagnostico.ps1

# Resultado
[OK] Docker instalado
[OK] Docker daemon rodando
[OK] 4/4 containers rodando
[OK] MySQL pronto
[OK] Aplicação UP
[OK] Métricas expostas
[OK] Prometheus acessível
[OK] Grafana acessível
```

#### Teste 2: Coleta de Métricas
```bash
# Verificar endpoint de métricas
curl http://localhost:8080/actuator/prometheus

# Resultado: 200+ linhas de métricas em formato Prometheus
# Exemplo:
# jvm_memory_used_bytes{area="heap",id="PS Eden Space",} 2.5165824E7
# http_server_requests_seconds_count{exception="None",method="GET",outcome="SUCCESS",status="200",uri="/actuator/health",} 142.0
```

#### Teste 3: Prometheus Scraping
1. Acessar: http://localhost:9090/targets
2. Verificar target `sistema-agendamento-reports`
3. Status: **UP** (verde)
4. Last Scrape: < 5s ago

#### Teste 4: Query no Prometheus
```promql
# Query executada
up{job="sistema-agendamento-reports"}

# Resultado: 1 (UP)
```

#### Teste 5: Dashboard do Grafana
1. Login no Grafana (admin/admin)
2. Acessar dashboard "Sistema de Agendamento - Métricas da Aplicação"
3. Configurar auto-refresh: 5s
4. Gerar tráfego: `.\gerar-trafego.ps1`
5. Verificar atualização dos 6 painéis em tempo real

**Resultado:** ✅ Todos os painéis exibindo dados corretamente

### 10.2 Geração de Tráfego para Testes

**Script:** `gerar-trafego.ps1`

```powershell
# Gera 140 requisições distribuídas entre diferentes endpoints
- 50 requisições para /actuator/health
- 30 requisições para /actuator/metrics
- 20 requisições para /actuator/prometheus
- 40 requisições para /api/hello
```

### 10.3 Validação de Queries PromQL

| Query | Propósito | Resultado Esperado |
|-------|-----------|-------------------|
| `up` | Verificar targets ativos | 1 |
| `rate(http_server_requests_seconds_count[1m])` | Taxa de requisições | > 0 após tráfego |
| `jvm_memory_used_bytes{area="heap"}` | Memória heap | Valor em bytes |
| `hikaricp_connections_active` | Conexões ativas | >= 0 |

### 10.4 Testes de Resiliência

#### Cenário 1: Restart da Aplicação
```bash
docker-compose restart reports-svc
```
- Prometheus detecta down (up = 0)
- Após reiniciar, volta a coletar automaticamente
- Dados históricos preservados

#### Cenário 2: Alto Volume de Requisições
```bash
# 500 requisições simultâneas
1..500 | ForEach-Object -Parallel {
    Invoke-WebRequest http://localhost:8080/actuator/health
}
```
- Dashboard mostra pico de requisições
- Latência P95 aumenta temporariamente
- Pool de conexões aumenta conforme necessário

---

## 11. Conclusões

### 11.1 Resultados Alcançados

✅ **Implementação completa de observabilidade**
- Métricas da aplicação sendo coletadas e armazenadas
- Dashboards funcionais e informativos
- Sistema totalmente containerizado

✅ **Automação**
- Provisionamento automático do Grafana
- Scripts de validação e testes
- Docker Compose para orquestração

✅ **Documentação**
- Guias de instalação e uso
- Troubleshooting
- Exemplos de queries

### 11.2 Benefícios Implementados

1. **Visibilidade:** Monitoramento em tempo real de todas as camadas da aplicação
2. **Diagnóstico:** Identificação rápida de problemas de performance
3. **Capacidade:** Dados para planejamento de capacidade
4. **Histórico:** Armazenamento de métricas para análise temporal
5. **Alertas:** Base para implementação de alertas (futuro)

### 11.3 Lições Aprendidas

**Técnicas:**
- Importância de tags e labels nas métricas
- Configuração de histogramas para percentis
- Provisionamento automático do Grafana
- Resolução de problemas de encoding em scripts PowerShell

**Operacionais:**
- Necessidade de documentação clara
- Importância de scripts de validação
- Testes são essenciais para garantir funcionamento

### 11.4 Melhorias Futuras

1. **Alertas:**
   - Configurar alertas no Grafana para métricas críticas
   - Integração com Slack/Email para notificações

2. **Métricas de Negócio:**
   - Adicionar métricas customizadas (eventos criados, salas reservadas, etc.)
   - KPIs de negócio nos dashboards

3. **Distributed Tracing:**
   - Implementar Jaeger ou Zipkin para tracing distribuído
   - Correlação entre requisições

4. **Log Aggregation:**
   - Adicionar ELK Stack (Elasticsearch, Logstash, Kibana)
   - Correlação entre logs e métricas

5. **SLOs/SLIs:**
   - Definir Service Level Objectives
   - Criar dashboards de SLO

6. **Multi-ambiente:**
   - Configuração para dev, staging, production
   - Labels para distinguir ambientes

---

## 12. Referências

### Documentação Oficial

1. **Spring Boot Actuator**
   - https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html

2. **Micrometer**
   - https://micrometer.io/docs

3. **Prometheus**
   - https://prometheus.io/docs/introduction/overview/
   - https://prometheus.io/docs/prometheus/latest/querying/basics/

4. **Grafana**
   - https://grafana.com/docs/grafana/latest/
   - https://grafana.com/docs/grafana/latest/dashboards/

5. **Docker Compose**
   - https://docs.docker.com/compose/

### Tutoriais e Guias

1. **Spring Boot with Prometheus and Grafana**
   - https://www.baeldung.com/spring-boot-self-hosted-monitoring

2. **PromQL Tutorial**
   - https://promlabs.com/promql-cheat-sheet/

3. **Grafana Best Practices**
   - https://grafana.com/docs/grafana/latest/best-practices/

---

## Anexos

### Anexo A: Estrutura de Diretórios

```
sistema-agendamento-TDE-CS/
├── src/
│   └── main/
│       ├── java/
│       └── resources/
│           ├── application.properties (modificado)
│           └── application-docker.properties (modificado)
├── grafana/
│   └── provisioning/
│       ├── datasources/
│       │   └── prometheus.yml (novo)
│       └── dashboards/
│           ├── dashboard-provider.yml (novo)
│           └── sistema-agendamento-dashboard.json (novo)
├── pom.xml (modificado)
├── compose.yaml (modificado)
├── prometheus.yml (novo)
├── Dockerfile
├── diagnostico.ps1 (novo)
├── gerar-trafego.ps1 (novo)
├── validar-setup.ps1 (novo)
├── GUIA-GRAFANA.md (novo)
├── TESTES-E-VALIDACAO.md (novo)
├── RESOLVER-PROBLEMAS.md (novo)
└── ArturPereira_DaviOliveira_LeonardoRossol_Thiago.md (este arquivo)
```

### Anexo B: Comandos Úteis

```bash
# Iniciar sistema
docker-compose up -d --build

# Ver logs
docker-compose logs -f reports-svc
docker-compose logs -f prometheus
docker-compose logs -f grafana

# Parar sistema
docker-compose down

# Parar e limpar volumes
docker-compose down -v

# Reiniciar serviço específico
docker-compose restart reports-svc

# Ver status
docker-compose ps

# Executar diagnóstico
.\diagnostico.ps1

# Gerar tráfego
.\gerar-trafego.ps1

# Acessar container
docker-compose exec reports-svc bash
docker-compose exec prometheus sh
docker-compose exec grafana bash
```

### Anexo C: Queries PromQL Úteis

```promql
# Taxa de requisições por segundo
rate(http_server_requests_seconds_count[1m])

# Latência média
rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])

# Latência P95
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le))

# Latência P99
histogram_quantile(0.99, sum(rate(http_server_requests_seconds_bucket[5m])) by (le))

# Taxa de erros (status 5xx)
sum(rate(http_server_requests_seconds_count{status=~"5.."}[1m]))

# Uso de memória heap (%)
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100

# Threads JVM
jvm_threads_live

# Taxa de GC
rate(jvm_gc_pause_seconds_count[1m])

# Tempo em GC
rate(jvm_gc_pause_seconds_sum[1m])

# Pool de conexões - utilização (%)
(hikaricp_connections_active / hikaricp_connections) * 100

# Requisições por endpoint (top 10)
topk(10, sum by (uri) (rate(http_server_requests_seconds_count[5m])))

# Endpoints mais lentos (top 5)
topk(5, avg by (uri) (rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])))
```

### Anexo D: Troubleshooting

Consulte o arquivo `RESOLVER-PROBLEMAS.md` para guia completo de troubleshooting.

---

## Checklist de Entrega

- [x] Código implementado e funcional
- [x] Containers Prometheus e Grafana configurados
- [x] Dashboard do Grafana criado e provisionado
- [x] Métricas sendo coletadas corretamente
- [x] Documentação completa
- [x] Scripts de validação e testes
- [x] Testes realizados e validados
- [x] Arquivo de entrega criado

---

**Data de Conclusão:** 19/11/2024
**Status:** ✅ CONCLUÍDO E TESTADO

---

## Assinaturas

**Integrantes do Grupo:**
- Artur Pereira
- Davi Oliveira
- Leonardo Rossol
- Thiago

**Observação:** Este sistema foi desenvolvido como parte do trabalho prático da disciplina de Computação em Nuvem, demonstrando a implementação completa de uma solução de observabilidade utilizando tecnologias modernas e boas práticas de DevOps.
