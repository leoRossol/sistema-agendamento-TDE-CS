# Resumo Executivo - Observabilidade com Prometheus e Grafana

**Trabalho:** Sistema de Agendamento - Implementação de Observabilidade
**Integrantes:** Artur Pereira, Davi Oliveira, Leonardo Rossol, Thiago
**Data:** 19/11/2024

---

## 🎯 Objetivo

Implementar solução completa de observabilidade no Sistema de Agendamento usando Prometheus e Grafana, permitindo monitoramento em tempo real de métricas de performance, recursos e comportamento da aplicação.

---

## ✅ O Que Foi Implementado

### 1. Configuração da Aplicação Spring Boot
- ✅ Dependência Micrometer Prometheus adicionada
- ✅ Endpoints de métricas expostos em `/actuator/prometheus`
- ✅ Configuração de histogramas para cálculo de percentis

### 2. Prometheus (Coleta de Métricas)
- ✅ Container Docker configurado (porta 9090)
- ✅ Scraping automático a cada 5 segundos
- ✅ Armazenamento de métricas em time-series database

### 3. Grafana (Visualização)
- ✅ Container Docker configurado (porta 3000)
- ✅ Datasource Prometheus provisionado automaticamente
- ✅ Dashboard com 6 painéis criado e provisionado

### 4. Automação
- ✅ Scripts de validação (diagnostico.ps1)
- ✅ Scripts de geração de tráfego
- ✅ Docker Compose para orquestração completa

### 5. Documentação
- ✅ Documento principal completo (50+ páginas)
- ✅ Guia de uso do Grafana
- ✅ Guia de troubleshooting
- ✅ Documentação de testes

---

## 📊 Dashboard do Grafana - 6 Painéis

| # | Painel | Tipo | Métrica Principal |
|---|--------|------|-------------------|
| 1 | Taxa de Requisições HTTP | Time Series | `rate(http_server_requests_seconds_count[1m])` |
| 2 | Latência P95 | Gauge | `histogram_quantile(0.95, ...)` |
| 3 | Uso de Memória JVM | Time Series | `jvm_memory_used_bytes` |
| 4 | Uso de CPU | Time Series | `system_cpu_usage`, `process_cpu_usage` |
| 5 | Pool de Conexões BD | Time Series | `hikaricp_connections_*` |
| 6 | Status HTTP | Pie Chart | `sum by (status) (...)` |

---

## 🔧 Arquitetura

```
┌─────────────────────────────────────────────────┐
│              Docker Network                      │
│                                                  │
│  MySQL ◄── Reports-SVC ◄── Prometheus ◄── Grafana │
│  (3307)     (8080)          (9090)        (3000) │
│                 │                           │    │
│                 └───────────────────────────┘    │
│             /actuator/prometheus                 │
└─────────────────────────────────────────────────┘
                      │
                      ▼
                  Usuário
```

---

## 📁 Arquivos Principais Entregues

### Configuração
- `pom.xml` - Dependência Micrometer
- `compose.yaml` - Containers Prometheus e Grafana
- `prometheus.yml` - Configuração do Prometheus
- `application-docker.properties` - Endpoints de métricas

### Provisionamento Grafana
- `grafana/provisioning/datasources/prometheus.yml`
- `grafana/provisioning/dashboards/sistema-agendamento-dashboard.json`

### Scripts
- `diagnostico.ps1` - Validação do sistema
- `gerar-trafego.ps1` - Gerador de tráfego

### Documentação
- `ArturPereira_DaviOliveira_LeonardoRossol_Thiago.md` - Documento principal
- `GUIA-GRAFANA.md` - Guia completo
- `TESTES-E-VALIDACAO.md` - Testes realizados

---

## 🧪 Testes Realizados

✅ **Infraestrutura**
- Docker e containers funcionando
- 4/4 containers rodando (MySQL, Reports, Prometheus, Grafana)

✅ **Coleta de Métricas**
- Endpoint `/actuator/prometheus` expondo 200+ métricas
- Prometheus fazendo scraping corretamente
- Target status: UP

✅ **Visualização**
- Login no Grafana funcionando
- Dashboard carregando automaticamente
- 6 painéis exibindo dados em tempo real

✅ **Queries PromQL**
- Taxa de requisições
- Latência P95
- Uso de recursos (CPU, memória)
- Pool de conexões

---

## 🚀 Como Executar

```bash
# 1. Iniciar containers
docker-compose up -d --build

# 2. Aguardar 2-3 minutos

# 3. Executar diagnóstico
.\diagnostico.ps1

# 4. Gerar tráfego
.\gerar-trafego.ps1

# 5. Acessar Grafana
http://localhost:3000 (admin/admin)
```

---

## 📈 Métricas Implementadas

### HTTP (Spring Boot Actuator)
- Requisições por segundo
- Latência (P50, P95, P99)
- Taxa de erros
- Distribuição de status

### JVM (Micrometer)
- Uso de memória (Heap, Non-Heap)
- Threads ativas
- Garbage Collection
- Classes carregadas

### Sistema (OS)
- CPU (sistema e processo)
- Load average
- Uptime

### Banco de Dados (HikariCP)
- Conexões ativas/idle/total
- Timeouts
- Tempo de criação de conexões

---

## 💡 Benefícios Alcançados

✅ **Visibilidade Total**
- Monitoramento de todas as camadas da aplicação
- Métricas em tempo real
- Histórico de dados

✅ **Diagnóstico Rápido**
- Identificação imediata de problemas
- Análise de tendências
- Correlação entre métricas

✅ **Otimização**
- Dados para ajuste de recursos
- Identificação de gargalos
- Planejamento de capacidade

✅ **Automação**
- Deploy automatizado (Docker Compose)
- Provisionamento automático (Grafana)
- Scripts de validação e testes

---

## 🎓 Tecnologias e Conceitos Aplicados

### Tecnologias
- Spring Boot 3.5.6
- Micrometer
- Prometheus
- Grafana
- Docker / Docker Compose

### Conceitos
- Observabilidade (Observability)
- Métricas (Metrics)
- Time-Series Database
- Service Level Indicators (SLIs)
- Percentis e Histogramas
- PromQL (Prometheus Query Language)
- Infrastructure as Code
- Containerização

---

## 📊 Queries PromQL Destacadas

### Taxa de Requisições
```promql
rate(http_server_requests_seconds_count{application="sistema-agendamento-reports"}[1m])
```

### Latência P95
```promql
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{application="sistema-agendamento-reports"}[5m])) by (le))
```

### Uso de Memória (%)
```promql
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100
```

### Pool de Conexões - Utilização
```promql
(hikaricp_connections_active / hikaricp_connections) * 100
```

---

## ✨ Diferenciais da Implementação

1. **Provisionamento Automático** - Dashboard e datasource configurados automaticamente
2. **Scripts de Validação** - Diagnóstico completo em um comando
3. **Documentação Completa** - Mais de 50 páginas de documentação detalhada
4. **Testes Validados** - Sistema totalmente testado e funcional
5. **Queries Otimizadas** - PromQL queries seguindo boas práticas

---

## 📝 Conclusão

A implementação de observabilidade foi concluída com sucesso, atingindo todos os objetivos propostos:

✅ Prometheus coletando métricas automaticamente
✅ Grafana visualizando dados em tempo real
✅ Dashboard com 6 painéis informativos
✅ Sistema totalmente containerizado
✅ Documentação completa e detalhada
✅ Scripts de automação e validação
✅ Testes realizados e validados

**Status Final:** ✅ **APROVADO PARA ENTREGA**

---

## 📞 URLs de Acesso

| Serviço | URL | Credenciais |
|---------|-----|-------------|
| **Grafana** | http://localhost:3000 | admin / admin |
| **Prometheus** | http://localhost:9090 | - |
| **Métricas** | http://localhost:8080/actuator/prometheus | - |
| **Health** | http://localhost:8080/actuator/health | - |

---

## 📚 Documentos de Referência

Para informações detalhadas, consulte:

1. **ArturPereira_DaviOliveira_LeonardoRossol_Thiago.md** - Documento completo (principal)
2. **GUIA-GRAFANA.md** - Guia de uso do Grafana
3. **TESTES-E-VALIDACAO.md** - Validação técnica
4. **RESOLVER-PROBLEMAS.md** - Troubleshooting

---

**Desenvolvido por:** Artur Pereira, Davi Oliveira, Leonardo Rossol, Thiago
**Data:** 19/11/2024
**Status:** ✅ CONCLUÍDO
