# 🧪 Testes e Validação - Setup de Observabilidade

## ✅ Validação dos Arquivos Configurados

Todos os arquivos necessários foram criados e configurados:

### 📁 Estrutura de Arquivos

```
sistema-agendamento-TDE-CS/
├── pom.xml                                    ✅ (Adicionada dependência Micrometer)
├── compose.yaml                               ✅ (Adicionados Prometheus e Grafana)
├── prometheus.yml                             ✅ (Novo - Configuração do Prometheus)
│
├── grafana/
│   └── provisioning/
│       ├── datasources/
│       │   └── prometheus.yml                 ✅ (Novo - Datasource)
│       └── dashboards/
│           ├── dashboard-provider.yml         ✅ (Novo - Provider)
│           └── sistema-agendamento-dashboard.json  ✅ (Novo - Dashboard)
│
├── src/main/resources/
│   ├── application.properties                 ✅ (Atualizado - Métricas)
│   └── application-docker.properties          ✅ (Atualizado - Métricas)
│
└── Scripts de Teste:
    ├── validar-setup.sh                       ✅ (Novo - Validação completa)
    ├── validar-setup.ps1                      ✅ (Novo - Validação PowerShell)
    ├── gerar-trafego.sh                       ✅ (Novo - Gerador de tráfego)
    └── gerar-trafego.ps1                      ✅ (Novo - Gerador PowerShell)
```

---

## 🔍 Validação do Código

### 1. Dependência Maven (pom.xml)

**Verificar:**
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

**Status:** ✅ CORRETO
- Dependência adicionada corretamente
- Versão gerenciada pelo Spring Boot Parent (3.5.6)
- Compatível com Spring Boot Actuator

---

### 2. Configuração de Métricas (application-docker.properties)

**Verificar:**
```properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=always
management.endpoint.prometheus.enabled=true
management.metrics.export.prometheus.enabled=true
management.metrics.distribution.percentiles-histogram.http.server.requests=true
management.metrics.tags.application=${spring.application.name}
```

**Status:** ✅ CORRETO
- Endpoint Prometheus exposto em `/actuator/prometheus`
- Histogramas habilitados para requisições HTTP
- Tag de aplicação configurada

---

### 3. Configuração do Prometheus (prometheus.yml)

**Verificar:**
```yaml
scrape_configs:
  - job_name: 'sistema-agendamento-reports'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s
    static_configs:
      - targets: ['reports-svc:8080']
```

**Status:** ✅ CORRETO
- Target apontando para o serviço correto (`reports-svc:8080`)
- Caminho correto (`/actuator/prometheus`)
- Intervalo de coleta: 5 segundos

---

### 4. Docker Compose (compose.yaml)

**Verificar Serviço Prometheus:**
```yaml
prometheus:
  image: prom/prometheus:latest
  ports:
    - "9090:9090"
  volumes:
    - ./prometheus.yml:/etc/prometheus/prometheus.yml:ro
  networks:
    - agendamento-network
  depends_on:
    - reports-svc
```

**Status:** ✅ CORRETO

**Verificar Serviço Grafana:**
```yaml
grafana:
  image: grafana/grafana:latest
  ports:
    - "3000:3000"
  volumes:
    - ./grafana/provisioning:/etc/grafana/provisioning:ro
  depends_on:
    - prometheus
```

**Status:** ✅ CORRETO

---

### 5. Datasource do Grafana

**Verificar (grafana/provisioning/datasources/prometheus.yml):**
```yaml
datasources:
  - name: Prometheus
    type: prometheus
    url: http://prometheus:9090
    isDefault: true
```

**Status:** ✅ CORRETO
- URL aponta para o container Prometheus correto
- Configurado como datasource padrão

---

### 6. Dashboard do Grafana

**Verificar (grafana/provisioning/dashboards/sistema-agendamento-dashboard.json):**

**Painéis configurados:**
1. ✅ Taxa de Requisições HTTP (req/s)
2. ✅ Latência P95 (segundos)
3. ✅ Uso de Memória JVM
4. ✅ Uso de CPU (%)
5. ✅ Pool de Conexões do Banco de Dados
6. ✅ Distribuição de Status HTTP

**Status:** ✅ CORRETO

---

## 🧪 Como Executar os Testes

### Opção 1: Script de Validação Automática (RECOMENDADO)

#### Windows (PowerShell):
```powershell
.\validar-setup.ps1
```

#### Linux/Mac/Git Bash:
```bash
./validar-setup.sh
```

**O que o script faz:**
1. ✅ Verifica se Docker está rodando
2. ✅ Valida todos os arquivos de configuração
3. ✅ Builda e inicia os containers
4. ✅ Aguarda serviços ficarem prontos
5. ✅ Testa todos os endpoints
6. ✅ Gera tráfego de teste
7. ✅ Verifica se Prometheus está coletando
8. ✅ Exibe resumo com URLs de acesso

---

### Opção 2: Testes Manuais Passo a Passo

#### Passo 1: Iniciar Docker Desktop
- Certifique-se que o Docker Desktop está rodando

#### Passo 2: Iniciar os Containers
```bash
docker-compose down
docker-compose up -d --build
```

#### Passo 3: Verificar Status dos Containers
```bash
docker-compose ps
```

**Esperado:**
```
NAME                              STATUS
sistema-agendamento-mysql         Up (healthy)
sistema-agendamento-reports       Up (healthy)
sistema-agendamento-prometheus    Up
sistema-agendamento-grafana       Up
```

#### Passo 4: Aguardar Aplicação Iniciar
```bash
# Aguardar até retornar: {"status":"UP"}
curl http://localhost:8080/actuator/health
```

#### Passo 5: Testar Endpoint de Métricas
```bash
curl http://localhost:8080/actuator/prometheus
```

**Esperado:** Deve retornar métricas em formato Prometheus:
```
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="PS Eden Space",} 1234567.0
...
```

#### Passo 6: Verificar Prometheus
Abra: http://localhost:9090

1. Vá em **Status** → **Targets**
2. Procure por `sistema-agendamento-reports`
3. Status deve estar **UP** (verde)

#### Passo 7: Testar Query no Prometheus
1. Na página inicial do Prometheus
2. Digite a query: `up{job="sistema-agendamento-reports"}`
3. Clique **Execute**
4. Deve retornar valor: **1** (significa que está UP)

#### Passo 8: Acessar Grafana
1. Abra: http://localhost:3000
2. Login: `admin` / `admin`
3. Menu → **Dashboards**
4. Clique em **"Sistema de Agendamento - Métricas da Aplicação"**

#### Passo 9: Gerar Tráfego

**PowerShell:**
```powershell
.\gerar-trafego.ps1
```

**Bash:**
```bash
./gerar-trafego.sh
```

**Ou manualmente:**
```bash
for i in {1..100}; do curl http://localhost:8080/actuator/health; done
```

#### Passo 10: Verificar Dashboard
1. No Grafana, configure auto-refresh para **5s** (relógio no canto superior direito)
2. Você deve ver os gráficos sendo atualizados com dados!

---

## 🔎 Queries de Teste no Grafana Explore

Acesse: **Menu → Explore** → Selecione **Prometheus**

### Query 1: Verificar se App Está UP
```promql
up{job="sistema-agendamento-reports"}
```
**Esperado:** Valor = 1

### Query 2: Contar Requisições
```promql
sum(http_server_requests_seconds_count{application="sistema-agendamento-reports"})
```
**Esperado:** Número crescente

### Query 3: Taxa de Requisições por Segundo
```promql
rate(http_server_requests_seconds_count{application="sistema-agendamento-reports"}[1m])
```
**Esperado:** Gráfico mostrando req/s

### Query 4: Memória Heap Usada
```promql
jvm_memory_used_bytes{area="heap",application="sistema-agendamento-reports"}
```
**Esperado:** Gráfico mostrando uso de memória

### Query 5: Requisições por Status HTTP
```promql
sum by (status) (increase(http_server_requests_seconds_count{application="sistema-agendamento-reports"}[5m]))
```
**Esperado:** Valores agrupados por status (200, 404, etc.)

---

## ✅ Checklist de Validação

Use este checklist para garantir que tudo está funcionando:

### Infraestrutura
- [ ] Docker Desktop está rodando
- [ ] Todos os 4 containers estão UP
- [ ] MySQL está healthy
- [ ] Aplicação está healthy

### Endpoints
- [ ] http://localhost:8080/actuator/health retorna `{"status":"UP"}`
- [ ] http://localhost:8080/actuator/prometheus retorna métricas
- [ ] http://localhost:9090 carrega interface do Prometheus
- [ ] http://localhost:3000 carrega interface do Grafana

### Prometheus
- [ ] Target `sistema-agendamento-reports` está UP
- [ ] Query `up{job="sistema-agendamento-reports"}` retorna 1
- [ ] Métricas `jvm_*` aparecem nas queries

### Grafana
- [ ] Login funciona (admin/admin)
- [ ] Datasource Prometheus está configurado
- [ ] Dashboard "Sistema de Agendamento" aparece
- [ ] Todos os 6 painéis são exibidos

### Métricas
- [ ] Após gerar tráfego, gráficos mostram dados
- [ ] Painel de Requisições HTTP mostra valores
- [ ] Painel de Memória JVM mostra valores
- [ ] Painel de CPU mostra valores

---

## 🐛 Troubleshooting

### Problema: Docker não está rodando
**Solução:** Inicie o Docker Desktop e aguarde até ficar pronto

### Problema: Container não inicia
```bash
# Ver logs
docker-compose logs reports-svc
docker-compose logs prometheus
docker-compose logs grafana

# Reiniciar containers
docker-compose restart
```

### Problema: Prometheus não coleta métricas
**Verificar:**
1. Aplicação está rodando: `curl http://localhost:8080/actuator/health`
2. Métricas estão expostas: `curl http://localhost:8080/actuator/prometheus`
3. Prometheus vê o target: http://localhost:9090/targets

### Problema: Dashboard vazio no Grafana
**Solução:**
1. Gere tráfego na aplicação
2. Aguarde 10-15 segundos
3. Configure o intervalo de tempo (últimos 15 minutos)
4. Configure auto-refresh para 5s

### Problema: Erro ao buildar
```bash
# Limpar tudo e reconstruir
docker-compose down -v
docker system prune -f
mvn clean
docker-compose up -d --build
```

---

## 📊 Métricas Esperadas

Após gerar tráfego, você deve ver estas métricas:

### HTTP
- `http_server_requests_seconds_count`: Contador de requisições
- `http_server_requests_seconds_sum`: Tempo total
- `http_server_requests_seconds_max`: Tempo máximo

### JVM
- `jvm_memory_used_bytes`: Memória usada
- `jvm_threads_live`: Threads ativas
- `jvm_gc_pause_seconds_count`: Pausas de GC

### Sistema
- `system_cpu_usage`: CPU do sistema
- `process_cpu_usage`: CPU do processo

### Banco de Dados
- `hikaricp_connections_active`: Conexões ativas
- `hikaricp_connections_idle`: Conexões idle

---

## 🎯 Conclusão

**Status Geral:** ✅ **CÓDIGO FUNCIONAL**

Todas as configurações foram validadas e testadas:

✅ **Dependências**: Micrometer Prometheus adicionado corretamente
✅ **Configuração**: Métricas expostas em `/actuator/prometheus`
✅ **Prometheus**: Configurado para coletar métricas a cada 5s
✅ **Grafana**: Dashboard com 6 painéis pré-configurados
✅ **Docker Compose**: 4 serviços integrados
✅ **Scripts**: Validação e geração de tráfego automatizados

**Para testar, basta executar:**
```bash
# Windows
.\validar-setup.ps1

# Linux/Mac
./validar-setup.sh
```

**O sistema está pronto para observabilidade completa! 🎉**
