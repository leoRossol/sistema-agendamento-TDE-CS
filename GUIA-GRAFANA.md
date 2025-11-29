# 📊 Guia de Acesso e Uso do Grafana

## 🚀 Passo 1: Iniciar os Serviços

### 1.1 Construir e iniciar os containers:
```bash
docker-compose up -d --build
```

### 1.2 Verificar se os containers estão rodando:
```bash
docker-compose ps
```

Você deve ver 4 containers em execução:
- `sistema-agendamento-mysql` (porta 3307)
- `sistema-agendamento-reports` (porta 8080)
- `sistema-agendamento-prometheus` (porta 9090)
- `sistema-agendamento-grafana` (porta 3000)

### 1.3 Verificar os logs (opcional):
```bash
# Ver logs da aplicação
docker-compose logs -f reports-svc

# Ver logs do Prometheus
docker-compose logs -f prometheus

# Ver logs do Grafana
docker-compose logs -f grafana
```

---

## 🌐 Passo 2: Verificar se as Métricas Estão Sendo Expostas

### 2.1 Testar o endpoint de métricas da aplicação:
Abra no navegador: http://localhost:8080/actuator/prometheus

Você deve ver algo como:
```
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="PS Eden Space",} 1.234567E7
...
# HELP http_server_requests_seconds
# TYPE http_server_requests_seconds summary
http_server_requests_seconds_count{exception="None",method="GET",outcome="SUCCESS",status="200",uri="/actuator/health",} 5.0
```

### 2.2 Verificar se o Prometheus está coletando:
Abra no navegador: http://localhost:9090

- Clique em **Status** → **Targets**
- Verifique se o target `sistema-agendamento-reports` está com status **UP** (verde)

---

## 🎨 Passo 3: Acessar o Grafana

### 3.1 Abrir o Grafana:
Abra no navegador: http://localhost:3000

### 3.2 Fazer Login:
- **Usuário**: `admin`
- **Senha**: `admin`

Na primeira vez, o Grafana pode pedir para alterar a senha. Você pode:
- Criar uma nova senha
- OU clicar em **Skip** para manter `admin/admin`

---

## 📈 Passo 4: Visualizar o Dashboard Pré-configurado

### 4.1 Acessar o Dashboard:

**Opção 1 - Menu Lateral:**
1. No menu lateral esquerdo, clique no ícone de **quatro quadrados** (Dashboards)
2. Você verá o dashboard **"Sistema de Agendamento - Métricas da Aplicação"**
3. Clique nele para abrir

**Opção 2 - Busca:**
1. Pressione `Ctrl + K` (ou `Cmd + K` no Mac) para abrir a busca
2. Digite: `Sistema de Agendamento`
3. Selecione o dashboard

### 4.2 Entendendo o Dashboard:

O dashboard possui 6 painéis principais:

#### 📊 Painel 1: Taxa de Requisições HTTP (req/s)
- **Mostra**: Número de requisições por segundo para cada endpoint
- **Útil para**: Identificar endpoints mais acessados e picos de tráfego

#### ⏱️ Painel 2: Latência P95 (segundos)
- **Mostra**: Tempo de resposta do percentil 95
- **Interpretação**: 95% das requisições são atendidas neste tempo ou menos
- **Cores**:
  - Verde: < 0.5s (bom)
  - Amarelo: 0.5s - 1s (atenção)
  - Vermelho: > 1s (crítico)

#### 💾 Painel 3: Uso de Memória JVM
- **Mostra**: Consumo de memória da JVM (Heap, Non-Heap, etc.)
- **Útil para**: Detectar vazamento de memória ou necessidade de ajuste

#### 🔥 Painel 4: Uso de CPU (%)
- **Mostra**: CPU do sistema e do processo Java
- **Útil para**: Identificar se a aplicação está consumindo muito processamento

#### 🗄️ Painel 5: Pool de Conexões do Banco de Dados
- **Mostra**: Conexões ativas, idle e total do HikariCP
- **Útil para**: Ajustar o tamanho do pool de conexões

#### 📦 Painel 6: Distribuição de Status HTTP
- **Mostra**: Proporção de códigos de resposta HTTP (200, 404, 500, etc.)
- **Útil para**: Identificar erros na aplicação

---

## 🔍 Passo 5: Realizar Consultas Personalizadas

### 5.1 Acessar o Explore:
1. No menu lateral esquerdo, clique no ícone de **bússola** (Explore)
2. Selecione **Prometheus** como datasource (se não estiver selecionado)

### 5.2 Exemplos de Queries:

#### Query 1: Total de Requisições por Endpoint
```promql
sum by (uri, method) (http_server_requests_seconds_count{application="sistema-agendamento-reports"})
```

**Como usar:**
1. Cole a query no campo de texto
2. Clique em **Run query** (ou pressione Shift + Enter)
3. Visualize o gráfico

#### Query 2: Taxa de Requisições (últimos 5 minutos)
```promql
rate(http_server_requests_seconds_count{application="sistema-agendamento-reports"}[5m])
```

#### Query 3: Tempo Médio de Resposta por Endpoint
```promql
rate(http_server_requests_seconds_sum{application="sistema-agendamento-reports"}[5m])
/
rate(http_server_requests_seconds_count{application="sistema-agendamento-reports"}[5m])
```

#### Query 4: Erros HTTP (status 4xx e 5xx)
```promql
sum by (status) (rate(http_server_requests_seconds_count{application="sistema-agendamento-reports", status=~"[45].."}[5m]))
```

#### Query 5: Uso de Memória Heap
```promql
jvm_memory_used_bytes{application="sistema-agendamento-reports", area="heap"}
```

#### Query 6: Threads da JVM
```promql
jvm_threads_live{application="sistema-agendamento-reports"}
```

#### Query 7: Conexões Ativas do Banco
```promql
hikaricp_connections_active{application="sistema-agendamento-reports"}
```

#### Query 8: Taxa de GC (Garbage Collection)
```promql
rate(jvm_gc_pause_seconds_count{application="sistema-agendamento-reports"}[1m])
```

### 5.3 Filtros e Agregações:

**Filtrar por método HTTP:**
```promql
http_server_requests_seconds_count{method="GET"}
```

**Filtrar por status:**
```promql
http_server_requests_seconds_count{status="200"}
```

**Somar todas as requisições:**
```promql
sum(http_server_requests_seconds_count{application="sistema-agendamento-reports"})
```

**Agrupar por status:**
```promql
sum by (status) (http_server_requests_seconds_count)
```

---

## 🎯 Passo 6: Criar um Novo Painel no Dashboard

### 6.1 Editar o Dashboard:
1. Abra o dashboard "Sistema de Agendamento - Métricas da Aplicação"
2. Clique no botão **Edit** (ícone de lápis) no canto superior direito
3. Clique em **Add** → **Visualization**

### 6.2 Configurar o Painel:

1. **Escolha o datasource**: Prometheus
2. **Digite a query**, por exemplo:
   ```promql
   jvm_threads_live{application="sistema-agendamento-reports"}
   ```
3. **Configure o painel**:
   - **Title**: Digite um título (ex: "Threads JVM Ativas")
   - **Legend**: Escolha como exibir a legenda
   - **Unit**: Selecione a unidade (ex: "short" para números)

4. **Escolha o tipo de visualização**:
   - **Time series**: Para gráficos de linha ao longo do tempo
   - **Stat**: Para valores únicos
   - **Gauge**: Para medidores
   - **Bar chart**: Para gráficos de barra
   - **Table**: Para tabelas

5. **Clique em Apply** para salvar

### 6.3 Salvar o Dashboard:
1. Clique no ícone de **disquete** (Save dashboard)
2. Opcionalmente, adicione uma nota sobre as mudanças
3. Clique em **Save**

---

## 📊 Passo 7: Gerar Tráfego para Ver Métricas

Para ver dados nos gráficos, você precisa gerar requisições:

### 7.1 Acessar endpoints da aplicação:
```bash
# Health check
curl http://localhost:8080/actuator/health

# Endpoint de hello
curl http://localhost:8080/api/hello

# Swagger UI
# Abra no navegador: http://localhost:8080/swagger-ui.html
```

### 7.2 Fazer múltiplas requisições (para gerar carga):
```bash
# Linux/Mac/Git Bash
for i in {1..100}; do curl http://localhost:8080/actuator/health; done

# PowerShell
1..100 | ForEach-Object { Invoke-WebRequest -Uri http://localhost:8080/actuator/health }
```

Após gerar tráfego, volte ao Grafana e você verá os gráficos sendo preenchidos!

---

## ⚙️ Passo 8: Ajustar Intervalo de Tempo

No canto superior direito do dashboard:

1. **Intervalo de tempo**: Clique no seletor (ex: "Last 15 minutes")
   - Last 5 minutes
   - Last 15 minutes
   - Last 1 hour
   - Last 6 hours
   - Last 24 hours
   - Custom range

2. **Auto-refresh**: Clique no ícone de relógio
   - Off
   - 5s
   - 10s
   - 30s
   - 1m
   - 5m

---

## 🔔 Passo 9: Criar Alertas (Opcional)

### 9.1 Criar uma regra de alerta:
1. Edite um painel existente
2. Vá para a aba **Alert**
3. Clique em **Create alert rule from this panel**
4. Configure:
   - **Condition**: Defina a condição (ex: latência > 1s)
   - **Evaluation**: A cada quanto tempo avaliar
   - **For**: Por quanto tempo a condição deve ser verdadeira
5. Salve o alerta

---

## 📝 Métricas Disponíveis Importantes

### Métricas HTTP:
- `http_server_requests_seconds_count`: Contador de requisições
- `http_server_requests_seconds_sum`: Soma dos tempos de resposta
- `http_server_requests_seconds_max`: Tempo máximo de resposta

### Métricas JVM:
- `jvm_memory_used_bytes`: Memória em uso
- `jvm_memory_max_bytes`: Memória máxima
- `jvm_threads_live`: Threads ativas
- `jvm_gc_pause_seconds_count`: Pausas de GC

### Métricas do Sistema:
- `system_cpu_usage`: Uso de CPU do sistema
- `process_cpu_usage`: Uso de CPU do processo

### Métricas do Banco de Dados:
- `hikaricp_connections_active`: Conexões ativas
- `hikaricp_connections_idle`: Conexões idle
- `hikaricp_connections_pending`: Conexões pendentes

---

## 🛠️ Troubleshooting

### Problema: Não vejo dados no Grafana

**Solução:**
1. Verifique se a aplicação está rodando: `docker-compose ps`
2. Verifique se as métricas estão sendo expostas: http://localhost:8080/actuator/prometheus
3. Verifique se o Prometheus está coletando: http://localhost:9090/targets
4. Gere tráfego na aplicação para criar métricas

### Problema: Erro "No data" nos painéis

**Solução:**
1. Verifique o intervalo de tempo (canto superior direito)
2. Gere requisições na aplicação
3. Aguarde alguns segundos para o Prometheus coletar

### Problema: Dashboard não aparece

**Solução:**
1. Reinicie o Grafana: `docker-compose restart grafana`
2. Verifique os logs: `docker-compose logs grafana`
3. Verifique se os arquivos de provisionamento estão montados corretamente

---

## 🎓 Dicas Avançadas

### 1. Usar Variáveis no Dashboard:
- Crie variáveis para filtrar por ambiente, serviço, etc.
- Menu: Dashboard settings → Variables

### 2. Exportar Dashboard:
- Dashboard settings → JSON Model
- Copie o JSON para backup

### 3. Importar Dashboards da Comunidade:
1. Vá em Dashboards → Import
2. Use IDs populares como:
   - **4701**: JVM (Micrometer)
   - **11378**: Spring Boot 2.1 Statistics
   - **12900**: Spring Boot 2.1 System Monitor

### 4. Criar Playlists:
- Rotacione entre vários dashboards automaticamente
- Útil para TVs de monitoramento

---

## 📚 Recursos Adicionais

- **Documentação Prometheus**: https://prometheus.io/docs/
- **Documentação Grafana**: https://grafana.com/docs/
- **PromQL Cheat Sheet**: https://promlabs.com/promql-cheat-sheet/
- **Grafana Dashboards**: https://grafana.com/grafana/dashboards/

---

## ✅ Checklist Rápido

- [ ] Containers rodando (`docker-compose ps`)
- [ ] Métricas expostas (http://localhost:8080/actuator/prometheus)
- [ ] Prometheus coletando (http://localhost:9090/targets)
- [ ] Grafana acessível (http://localhost:3000)
- [ ] Dashboard visível no Grafana
- [ ] Tráfego gerado na aplicação
- [ ] Gráficos mostrando dados

---

**Pronto! Agora você tem um sistema completo de observabilidade configurado! 🎉**
