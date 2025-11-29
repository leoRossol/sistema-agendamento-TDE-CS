# 🔧 Resolver Problemas - Localhost Recusado

## ❌ Problema Identificado

**Erro:** Todos os localhost estão sendo recusados (conexão recusada)

**Causa:** Docker Desktop não está rodando ou containers não iniciaram

---

## ✅ Solução Passo a Passo

### PASSO 1: Verificar Docker Desktop

#### 1.1 Abrir Docker Desktop

**Windows:**
1. Pressione `Windows + S`
2. Digite: `Docker Desktop`
3. Clique no aplicativo para abrir
4. **AGUARDE** até o ícone da baleia ficar verde (pode levar 1-2 minutos)

**Indicadores que Docker está pronto:**
- ✅ Ícone da baleia na bandeja do sistema (verde)
- ✅ Mensagem "Docker Desktop is running"
- ✅ Interface do Docker Desktop abre normalmente

#### 1.2 Verificar se Docker Está Respondendo

Abra PowerShell ou Terminal e execute:

```powershell
docker --version
```

**Esperado:**
```
Docker version 24.x.x, build xxxxx
```

Se aparecer erro, **Docker não está rodando!** Volte ao Passo 1.1

Agora teste:
```powershell
docker ps
```

**Esperado:**
```
CONTAINER ID   IMAGE     COMMAND   CREATED   STATUS    PORTS     NAMES
```
(Pode estar vazio, mas NÃO pode dar erro)

---

### PASSO 2: Iniciar os Containers

#### 2.1 Limpar Estado Anterior

```powershell
# Parar e remover containers antigos
docker-compose down -v

# Limpar recursos não utilizados
docker system prune -f
```

#### 2.2 Construir e Iniciar

```powershell
# Buildar e iniciar todos os containers
docker-compose up -d --build
```

**Isso vai demorar 3-5 minutos na primeira vez!**

Você verá mensagens como:
```
[+] Building 45.2s (12/12) FINISHED
[+] Running 5/5
 ✔ Network sistema-agendamento-tde-cs_agendamento-network  Created
 ✔ Volume "sistema-agendamento-tde-cs_mysql_data"         Created
 ✔ Container sistema-agendamento-mysql                     Started
 ✔ Container sistema-agendamento-reports                   Started
 ✔ Container sistema-agendamento-prometheus                Started
 ✔ Container sistema-agendamento-grafana                   Started
```

---

### PASSO 3: Verificar Status dos Containers

```powershell
docker-compose ps
```

**Esperado - TODOS devem estar "Up":**
```
NAME                              STATUS
sistema-agendamento-mysql         Up (healthy)
sistema-agendamento-reports       Up
sistema-agendamento-prometheus    Up
sistema-agendamento-grafana       Up
```

**Se algum não estiver "Up", vá para PASSO 4 (Diagnóstico de Logs)**

---

### PASSO 4: Aguardar Aplicação Iniciar

A aplicação Spring Boot leva ~30-60 segundos para iniciar.

#### 4.1 Acompanhar Logs da Aplicação

```powershell
docker-compose logs -f reports-svc
```

**Aguarde até ver:**
```
Started SistemaAgendamentoApplication in X.XXX seconds
```

**Pressione Ctrl+C para sair dos logs**

#### 4.2 Testar Health Check

```powershell
curl http://localhost:8080/actuator/health
```

**Esperado:**
```json
{"status":"UP"}
```

**Se der erro "Connection refused":** Aguarde mais 30 segundos e tente novamente.

---

### PASSO 5: Verificar Cada Serviço

Execute estes comandos um por um:

#### MySQL
```powershell
docker-compose exec mysql mysqladmin ping -h localhost -u root -pverysecret
```
**Esperado:** `mysqld is alive`

#### Aplicação
```powershell
curl http://localhost:8080/actuator/health
```
**Esperado:** `{"status":"UP"}`

#### Prometheus
```powershell
curl http://localhost:9090
```
**Esperado:** HTML da interface do Prometheus

#### Grafana
```powershell
curl http://localhost:3000
```
**Esperado:** HTML da interface do Grafana

---

### PASSO 6: Abrir no Navegador

Agora sim, abra no navegador:

1. **Grafana**: http://localhost:3000
   - Login: `admin` / `admin`

2. **Prometheus**: http://localhost:9090

3. **Aplicação**: http://localhost:8080/actuator/health

---

## 🔍 Diagnóstico de Problemas Específicos

### Problema: Container "Exited" ou "Restarting"

Ver logs do container com problema:

```powershell
# Ver logs do MySQL
docker-compose logs mysql

# Ver logs da aplicação
docker-compose logs reports-svc

# Ver logs do Prometheus
docker-compose logs prometheus

# Ver logs do Grafana
docker-compose logs grafana
```

**Procure por linhas com "ERROR" ou "Exception"**

---

### Problema: Aplicação não inicia (reports-svc)

#### Erro comum 1: Porta 8080 já em uso

**Solução:**
```powershell
# Verificar o que está usando a porta 8080
netstat -ano | findstr :8080

# Matar o processo (substitua PID pelo número da última coluna)
taskkill /PID <PID> /F

# Reiniciar container
docker-compose restart reports-svc
```

#### Erro comum 2: MySQL não está pronto

**Solução:**
```powershell
# Aguardar MySQL ficar healthy
docker-compose ps

# Se MySQL não estiver healthy, reiniciar
docker-compose restart mysql

# Aguardar 30 segundos e reiniciar aplicação
docker-compose restart reports-svc
```

#### Erro comum 3: Erro de build Maven

**Solução:**
```powershell
# Fazer build local primeiro
mvnw.cmd clean package -DskipTests

# Se der erro, verificar Java
java -version

# Deve ser Java 17 ou superior
```

---

### Problema: Prometheus não acessa aplicação

#### Verificar conectividade entre containers

```powershell
# Entrar no container do Prometheus
docker-compose exec prometheus sh

# Dentro do container, testar conexão
wget -O- http://reports-svc:8080/actuator/prometheus

# Sair do container
exit
```

**Se funcionar dentro do container mas não fora:** Problema de rede Docker

**Solução:**
```powershell
docker-compose down
docker network prune -f
docker-compose up -d
```

---

### Problema: Grafana não mostra dashboard

#### Verificar provisionamento

```powershell
# Ver logs do Grafana
docker-compose logs grafana | findstr provisioning
```

**Procure por:**
- ✅ "Provisioning completed"
- ❌ "Error reading provisioning"

**Se houver erro de provisionamento:**

1. Verificar arquivos existem:
```powershell
dir grafana\provisioning\datasources\
dir grafana\provisioning\dashboards\
```

2. Reiniciar Grafana:
```powershell
docker-compose restart grafana
```

---

## 🎯 Script de Diagnóstico Rápido

Criei um script que faz todo o diagnóstico automaticamente:

```powershell
# Execute este script
.\diagnostico.ps1
```

Se não existe, vou criar agora:

```powershell
# diagnostico.ps1
Write-Host "=== DIAGNÓSTICO DO SISTEMA ===" -ForegroundColor Cyan

Write-Host "`n1. Verificando Docker..." -ForegroundColor Yellow
docker --version
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Docker não está instalado ou rodando!" -ForegroundColor Red
    Write-Host "Inicie o Docker Desktop e tente novamente" -ForegroundColor Yellow
    exit
}

Write-Host "`n2. Verificando containers..." -ForegroundColor Yellow
docker-compose ps

Write-Host "`n3. Testando endpoints..." -ForegroundColor Yellow

Write-Host "   MySQL..." -NoNewline
docker-compose exec -T mysql mysqladmin ping -h localhost -u root -pverysecret 2>&1 | Out-Null
if ($LASTEXITCODE -eq 0) { Write-Host " ✅" -ForegroundColor Green } else { Write-Host " ❌" -ForegroundColor Red }

Write-Host "   Aplicação (8080)..." -NoNewline
try {
    $r = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 2
    Write-Host " ✅" -ForegroundColor Green
} catch {
    Write-Host " ❌" -ForegroundColor Red
}

Write-Host "   Prometheus (9090)..." -NoNewline
try {
    $r = Invoke-WebRequest -Uri "http://localhost:9090" -UseBasicParsing -TimeoutSec 2
    Write-Host " ✅" -ForegroundColor Green
} catch {
    Write-Host " ❌" -ForegroundColor Red
}

Write-Host "   Grafana (3000)..." -NoNewline
try {
    $r = Invoke-WebRequest -Uri "http://localhost:3000" -UseBasicParsing -TimeoutSec 2
    Write-Host " ✅" -ForegroundColor Green
} catch {
    Write-Host " ❌" -ForegroundColor Red
}

Write-Host "`n4. Logs recentes (últimas 20 linhas):" -ForegroundColor Yellow
docker-compose logs --tail=20 reports-svc
```

---

## 📋 Checklist de Verificação

Execute passo a passo:

- [ ] Docker Desktop está aberto e rodando (ícone verde)
- [ ] `docker ps` funciona sem erros
- [ ] `docker-compose up -d --build` executado
- [ ] Aguardei 2-3 minutos para containers iniciarem
- [ ] `docker-compose ps` mostra todos containers "Up"
- [ ] MySQL está "healthy"
- [ ] Logs da aplicação mostram "Started SistemaAgendamentoApplication"
- [ ] `curl http://localhost:8080/actuator/health` retorna UP
- [ ] Navegador abre http://localhost:3000 (Grafana)

---

## 🆘 Solução Definitiva - Reiniciar Tudo

Se nada funcionar, execute esta sequência:

```powershell
# 1. Parar tudo
docker-compose down -v

# 2. Limpar Docker
docker system prune -af
docker volume prune -f

# 3. Reiniciar Docker Desktop
# Clique com botão direito no ícone do Docker → Restart

# 4. Aguarde Docker ficar pronto (ícone verde)

# 5. Reconstruir tudo
docker-compose up -d --build

# 6. Acompanhar logs
docker-compose logs -f

# 7. Aguarde mensagem: "Started SistemaAgendamentoApplication"
# Pressione Ctrl+C

# 8. Testar
curl http://localhost:8080/actuator/health
```

---

## 📞 Informações para Reportar Problema

Se ainda não funcionar, me envie estas informações:

```powershell
# 1. Versão do Docker
docker --version

# 2. Status dos containers
docker-compose ps

# 3. Logs da aplicação
docker-compose logs reports-svc --tail=50

# 4. Teste de rede
curl http://localhost:8080/actuator/health

# 5. Portas em uso
netstat -ano | findstr "8080 9090 3000 3307"
```

---

**Lembre-se:** O problema mais comum é simplesmente o Docker Desktop não estar rodando. Sempre verifique isso primeiro! 🐳
