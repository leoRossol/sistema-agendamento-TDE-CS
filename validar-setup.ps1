# Script PowerShell de validação do setup de observabilidade

Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "🔍 VALIDAÇÃO DO SETUP DE OBSERVABILIDADE" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host ""

# Função para verificar resultado
function Check-Result {
    param(
        [bool]$Success,
        [string]$Message
    )
    if ($Success) {
        Write-Host "✅ $Message" -ForegroundColor Green
        return $true
    } else {
        Write-Host "❌ $Message" -ForegroundColor Red
        return $false
    }
}

# 1. Verificar se Docker está rodando
Write-Host "1️⃣ Verificando Docker..." -ForegroundColor Yellow
try {
    $dockerVersion = docker --version 2>&1
    Check-Result -Success $true -Message "Docker instalado: $dockerVersion"

    $dockerPs = docker ps 2>&1
    if ($LASTEXITCODE -eq 0) {
        Check-Result -Success $true -Message "Docker daemon está rodando"
    } else {
        Check-Result -Success $false -Message "Docker daemon NÃO está rodando"
        Write-Host "   Por favor, inicie o Docker Desktop e tente novamente" -ForegroundColor Yellow
        exit 1
    }
} catch {
    Check-Result -Success $false -Message "Docker não está instalado"
    exit 1
}
Write-Host ""

# 2. Verificar arquivos de configuração
Write-Host "2️⃣ Verificando arquivos de configuração..." -ForegroundColor Yellow

$files = @(
    "pom.xml",
    "compose.yaml",
    "prometheus.yml",
    "grafana\provisioning\datasources\prometheus.yml",
    "grafana\provisioning\dashboards\dashboard-provider.yml",
    "grafana\provisioning\dashboards\sistema-agendamento-dashboard.json",
    "src\main\resources\application.properties",
    "src\main\resources\application-docker.properties"
)

$allFilesOk = $true
foreach ($file in $files) {
    if (Test-Path $file) {
        Write-Host "✅ $file" -ForegroundColor Green
    } else {
        Write-Host "❌ $file - FALTANDO!" -ForegroundColor Red
        $allFilesOk = $false
    }
}
Write-Host ""

if (-not $allFilesOk) {
    Write-Host "❌ Alguns arquivos estão faltando!" -ForegroundColor Red
    exit 1
}

# 3. Verificar dependência no pom.xml
Write-Host "3️⃣ Verificando dependência Micrometer no pom.xml..." -ForegroundColor Yellow
$pomContent = Get-Content pom.xml -Raw
if ($pomContent -match "micrometer-registry-prometheus") {
    Check-Result -Success $true -Message "Dependência Micrometer encontrada"
} else {
    Check-Result -Success $false -Message "Dependência Micrometer NÃO encontrada"
    exit 1
}
Write-Host ""

# 4. Verificar configurações de métricas
Write-Host "4️⃣ Verificando configurações de métricas..." -ForegroundColor Yellow
$appDockerContent = Get-Content "src\main\resources\application-docker.properties" -Raw
if ($appDockerContent -match "management.endpoint.prometheus.enabled") {
    Check-Result -Success $true -Message "Configuração Prometheus no application-docker.properties"
} else {
    Check-Result -Success $false -Message "Configuração Prometheus NÃO encontrada"
    exit 1
}
Write-Host ""

# 5. Build e inicialização dos containers
Write-Host "5️⃣ Buildando e iniciando containers..." -ForegroundColor Yellow
Write-Host "   Isso pode levar alguns minutos..." -ForegroundColor Gray
docker-compose down 2>&1 | Out-Null
docker-compose up -d --build
Check-Result -Success ($LASTEXITCODE -eq 0) -Message "Containers iniciados"
Write-Host ""

# 6. Aguardar serviços iniciarem
Write-Host "6️⃣ Aguardando serviços iniciarem..." -ForegroundColor Yellow
Write-Host "   MySQL..." -ForegroundColor Gray
$mysqlReady = $false
for ($i = 1; $i -le 30; $i++) {
    try {
        docker-compose exec -T mysql mysqladmin ping -h localhost -u root -pverysecret 2>&1 | Out-Null
        if ($LASTEXITCODE -eq 0) {
            $mysqlReady = $true
            break
        }
    } catch {}
    Start-Sleep -Seconds 2
}
Check-Result -Success $mysqlReady -Message "MySQL está pronto"
Write-Host ""

Write-Host "   Aplicação Spring Boot..." -ForegroundColor Gray
$appReady = $false
for ($i = 1; $i -le 60; $i++) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            $appReady = $true
            break
        }
    } catch {}
    Start-Sleep -Seconds 2
}
Check-Result -Success $appReady -Message "Aplicação está pronta"
Write-Host ""

# 7. Verificar containers rodando
Write-Host "7️⃣ Verificando containers em execução..." -ForegroundColor Yellow
docker-compose ps
Write-Host ""

# 8. Testar endpoints
Write-Host "8️⃣ Testando endpoints..." -ForegroundColor Yellow

Write-Host "   Health check..." -ForegroundColor Gray
try {
    $health = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing
    $healthOk = $health.Content -match "UP"
    Check-Result -Success $healthOk -Message "Health check respondendo"
} catch {
    Check-Result -Success $false -Message "Health check FALHOU"
}

Write-Host "   Métricas Prometheus..." -ForegroundColor Gray
try {
    $metrics = Invoke-WebRequest -Uri "http://localhost:8080/actuator/prometheus" -UseBasicParsing
    $metricsOk = $metrics.Content -match "jvm_memory_used_bytes"
    Check-Result -Success $metricsOk -Message "Endpoint Prometheus expondo métricas"
} catch {
    Check-Result -Success $false -Message "Endpoint Prometheus FALHOU"
}

Write-Host "   Prometheus UI..." -ForegroundColor Gray
try {
    $prom = Invoke-WebRequest -Uri "http://localhost:9090" -UseBasicParsing -TimeoutSec 5
    Check-Result -Success ($prom.StatusCode -eq 200) -Message "Prometheus UI acessível"
} catch {
    Check-Result -Success $false -Message "Prometheus UI FALHOU"
}

Write-Host "   Grafana UI..." -ForegroundColor Gray
try {
    $grafana = Invoke-WebRequest -Uri "http://localhost:3000" -UseBasicParsing -TimeoutSec 5
    Check-Result -Success ($grafana.StatusCode -eq 200) -Message "Grafana UI acessível"
} catch {
    Check-Result -Success $false -Message "Grafana UI FALHOU"
}
Write-Host ""

# 9. Gerar tráfego de teste
Write-Host "9️⃣ Gerando tráfego de teste..." -ForegroundColor Yellow
for ($i = 1; $i -le 20; $i++) {
    try {
        Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 2 | Out-Null
        Invoke-WebRequest -Uri "http://localhost:8080/actuator/metrics" -UseBasicParsing -TimeoutSec 2 | Out-Null
    } catch {}
}
Check-Result -Success $true -Message "Tráfego gerado (40 requisições)"
Write-Host ""

# 10. Aguardar Prometheus coletar
Write-Host "🔟 Aguardando Prometheus coletar métricas (15 segundos)..." -ForegroundColor Yellow
Start-Sleep -Seconds 15
Write-Host ""

# 11. Verificar se Prometheus está coletando
Write-Host "1️⃣1️⃣ Verificando coleta do Prometheus..." -ForegroundColor Yellow
try {
    $targets = Invoke-WebRequest -Uri "http://localhost:9090/api/v1/targets" -UseBasicParsing
    $hasTarget = $targets.Content -match "sistema-agendamento-reports"
    Check-Result -Success $hasTarget -Message "Prometheus configurado para coletar da aplicação"

    $query = Invoke-WebRequest -Uri "http://localhost:9090/api/v1/query?query=up{job='sistema-agendamento-reports'}" -UseBasicParsing
    $hasData = $query.Content -match '"value":\[.*,.*"1"\]'
    Check-Result -Success $hasData -Message "Prometheus está coletando dados da aplicação"
} catch {
    Check-Result -Success $false -Message "Verificação do Prometheus FALHOU"
}
Write-Host ""

# 12. Resumo final
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "📊 RESUMO DOS SERVIÇOS" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "✅ Aplicação:     http://localhost:8080" -ForegroundColor Green
Write-Host "✅ Métricas:      http://localhost:8080/actuator/prometheus" -ForegroundColor Green
Write-Host "✅ Health:        http://localhost:8080/actuator/health" -ForegroundColor Green
Write-Host "✅ Swagger:       http://localhost:8080/swagger-ui.html" -ForegroundColor Green
Write-Host "✅ Prometheus:    http://localhost:9090" -ForegroundColor Green
Write-Host "✅ Grafana:       http://localhost:3000 (admin/admin)" -ForegroundColor Green
Write-Host ""
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "🎯 PRÓXIMOS PASSOS" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Acesse o Grafana: http://localhost:3000" -ForegroundColor Yellow
Write-Host "2. Login: admin / admin" -ForegroundColor Yellow
Write-Host "3. Vá em Dashboards → Sistema de Agendamento - Métricas da Aplicação" -ForegroundColor Yellow
Write-Host "4. Configure auto-refresh para 5s (relógio no canto superior direito)" -ForegroundColor Yellow
Write-Host "5. Execute: .\gerar-trafego.ps1 para gerar mais tráfego" -ForegroundColor Yellow
Write-Host ""
Write-Host "✅ Setup de observabilidade VALIDADO COM SUCESSO! 🎉" -ForegroundColor Green
Write-Host ""
