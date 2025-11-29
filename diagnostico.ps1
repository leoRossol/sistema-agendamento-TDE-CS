# Script de Diagnostico do Sistema de Observabilidade

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "DIAGNOSTICO DO SISTEMA" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host ""

# Funcao para check
function Test-Service {
    param([bool]$Success, [string]$Name)
    if ($Success) {
        Write-Host "[OK] $Name" -ForegroundColor Green
    } else {
        Write-Host "[ERRO] $Name" -ForegroundColor Red
    }
}

# 1. Docker
Write-Host "1. Verificando Docker..." -ForegroundColor Yellow
try {
    $dockerVersion = docker --version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Test-Service -Success $true -Name "Docker instalado: $dockerVersion"

        docker ps 2>&1 | Out-Null
        if ($LASTEXITCODE -eq 0) {
            Test-Service -Success $true -Name "Docker daemon rodando"
        } else {
            Test-Service -Success $false -Name "Docker daemon NAO esta rodando"
            Write-Host ""
            Write-Host "[!] SOLUCAO: Abra o Docker Desktop e aguarde ele iniciar completamente!" -ForegroundColor Yellow
            Write-Host "    Procure o icone da baleia na bandeja do sistema (deve ficar verde)" -ForegroundColor Yellow
            exit
        }
    } else {
        Test-Service -Success $false -Name "Docker nao encontrado"
        Write-Host ""
        Write-Host "[!] SOLUCAO: Instale o Docker Desktop" -ForegroundColor Yellow
        exit
    }
} catch {
    Test-Service -Success $false -Name "Erro ao verificar Docker"
    exit
}
Write-Host ""

# 2. Containers
Write-Host "2. Verificando containers..." -ForegroundColor Yellow
$containers = docker-compose ps 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host $containers
    Write-Host ""

    # Contar containers rodando
    $runningCount = (docker-compose ps --filter "status=running" -q 2>&1 | Measure-Object).Count
    Write-Host "Containers rodando: $runningCount / 4" -ForegroundColor $(if ($runningCount -eq 4) { "Green" } else { "Yellow" })
} else {
    Write-Host "[ERRO] Nao foi possivel verificar containers" -ForegroundColor Red
    Write-Host "Execute: docker-compose up -d --build" -ForegroundColor Yellow
}
Write-Host ""

# 3. Testando endpoints
Write-Host "3. Testando endpoints..." -ForegroundColor Yellow

# MySQL
Write-Host "   MySQL (porta 3307)..." -NoNewline
try {
    docker-compose exec -T mysql mysqladmin ping -h localhost -u root -pverysecret 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host " [OK] PRONTO" -ForegroundColor Green
    } else {
        Write-Host " [ERRO] NAO RESPONDE" -ForegroundColor Red
    }
} catch {
    Write-Host " [ERRO]" -ForegroundColor Red
}

# Aplicacao
Write-Host "   Aplicacao (porta 8080)..." -NoNewline
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
    if ($response.StatusCode -eq 200 -and $response.Content -match "UP") {
        Write-Host " [OK] UP" -ForegroundColor Green
    } else {
        Write-Host " [AVISO] PARCIAL" -ForegroundColor Yellow
    }
} catch {
    Write-Host " [ERRO] NAO RESPONDE (Connection refused)" -ForegroundColor Red
    Write-Host "      > Aguarde a aplicacao iniciar ou execute: docker-compose logs reports-svc" -ForegroundColor Gray
}

# Metricas
Write-Host "   Metricas Prometheus..." -NoNewline
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/prometheus" -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
    if ($response.Content -match "jvm_memory_used_bytes") {
        Write-Host " [OK] EXPONDO METRICAS" -ForegroundColor Green
    } else {
        Write-Host " [AVISO] SEM METRICAS" -ForegroundColor Yellow
    }
} catch {
    Write-Host " [ERRO] NAO RESPONDE" -ForegroundColor Red
}

# Prometheus
Write-Host "   Prometheus (porta 9090)..." -NoNewline
try {
    $response = Invoke-WebRequest -Uri "http://localhost:9090" -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Host " [OK] ACESSIVEL" -ForegroundColor Green
    } else {
        Write-Host " [AVISO] STATUS $($response.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    Write-Host " [ERRO] NAO RESPONDE" -ForegroundColor Red
}

# Grafana
Write-Host "   Grafana (porta 3000)..." -NoNewline
try {
    $response = Invoke-WebRequest -Uri "http://localhost:3000" -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Host " [OK] ACESSIVEL" -ForegroundColor Green
    } else {
        Write-Host " [AVISO] STATUS $($response.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    Write-Host " [ERRO] NAO RESPONDE" -ForegroundColor Red
}
Write-Host ""

# 4. Portas em uso
Write-Host "4. Verificando portas em uso..." -ForegroundColor Yellow
$ports = @(3000, 3307, 8080, 9090)
foreach ($port in $ports) {
    $inUse = netstat -ano | Select-String ":$port " | Select-Object -First 1
    if ($inUse) {
        Write-Host "   Porta $port > EM USO" -ForegroundColor Green
    } else {
        Write-Host "   Porta $port > LIVRE (container pode nao estar rodando)" -ForegroundColor Yellow
    }
}
Write-Host ""

# 5. Logs recentes
Write-Host "5. Logs recentes da aplicacao (ultimas 15 linhas)..." -ForegroundColor Yellow
Write-Host "=============================================" -ForegroundColor Gray
try {
    docker-compose logs --tail=15 reports-svc 2>&1
} catch {
    Write-Host "[ERRO] Nao foi possivel obter logs" -ForegroundColor Red
}
Write-Host "=============================================" -ForegroundColor Gray
Write-Host ""

# 6. Recomendacoes
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "RECOMENDACOES" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host ""

$hasIssues = $false

# Verificar se Docker esta rodando
docker ps 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "[!] Docker Desktop nao esta rodando!" -ForegroundColor Red
    Write-Host "    > Abra o Docker Desktop e aguarde ele iniciar" -ForegroundColor Yellow
    $hasIssues = $true
}

# Verificar se containers estao rodando
$runningCount = (docker-compose ps --filter "status=running" -q 2>&1 | Measure-Object).Count
if ($runningCount -lt 4) {
    Write-Host "[!] Alguns containers nao estao rodando!" -ForegroundColor Red
    Write-Host "    > Execute: docker-compose up -d --build" -ForegroundColor Yellow
    $hasIssues = $true
}

# Verificar se aplicacao responde
try {
    Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 2 -ErrorAction Stop | Out-Null
} catch {
    Write-Host "[!] Aplicacao nao esta respondendo!" -ForegroundColor Red
    Write-Host "    > Aguarde 1-2 minutos ou verifique logs: docker-compose logs reports-svc" -ForegroundColor Yellow
    $hasIssues = $true
}

if (-not $hasIssues) {
    Write-Host "[OK] Tudo funcionando corretamente!" -ForegroundColor Green
    Write-Host ""
    Write-Host "URLs disponiveis:" -ForegroundColor Cyan
    Write-Host "   Grafana:    http://localhost:3000 (admin/admin)" -ForegroundColor White
    Write-Host "   Prometheus: http://localhost:9090" -ForegroundColor White
    Write-Host "   Aplicacao:  http://localhost:8080/actuator/health" -ForegroundColor White
    Write-Host "   Metricas:   http://localhost:8080/actuator/prometheus" -ForegroundColor White
    Write-Host "   Swagger:    http://localhost:8080/swagger-ui.html" -ForegroundColor White
    Write-Host ""
    Write-Host "Proximo passo: Gere trafego com: .\gerar-trafego.ps1" -ForegroundColor Green
}

Write-Host ""
