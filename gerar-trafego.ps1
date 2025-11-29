# Script PowerShell para gerar tráfego e testar métricas do Grafana

Write-Host "🚀 Gerando tráfego para o Sistema de Agendamento..." -ForegroundColor Green
Write-Host ""

# Função para fazer requisições
function Gerar-Requisicoes {
    param(
        [string]$Endpoint,
        [int]$Quantidade,
        [string]$Descricao
    )

    Write-Host "📊 $Descricao ($Quantidade requisições)..." -NoNewline
    for ($i = 1; $i -le $Quantidade; $i++) {
        try {
            Invoke-WebRequest -Uri $Endpoint -Method Get -UseBasicParsing | Out-Null
            Write-Host "." -NoNewline
        } catch {
            Write-Host "x" -NoNewline
        }
    }
    Write-Host " ✅ Concluído!" -ForegroundColor Green
}

# Aguardar aplicação estar pronta
Write-Host "⏳ Aguardando aplicação estar pronta..." -NoNewline
$ready = $false
while (-not $ready) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -Method Get -UseBasicParsing -TimeoutSec 2
        $ready = $true
    } catch {
        Write-Host "." -NoNewline
        Start-Sleep -Seconds 1
    }
}
Write-Host " ✅ Aplicação pronta!" -ForegroundColor Green
Write-Host ""

# Gerar diferentes tipos de tráfego
Gerar-Requisicoes -Endpoint "http://localhost:8080/actuator/health" -Quantidade 50 -Descricao "Health checks"
Gerar-Requisicoes -Endpoint "http://localhost:8080/actuator/metrics" -Quantidade 30 -Descricao "Métricas gerais"
Gerar-Requisicoes -Endpoint "http://localhost:8080/actuator/prometheus" -Quantidade 20 -Descricao "Prometheus endpoint"
Gerar-Requisicoes -Endpoint "http://localhost:8080/api/hello" -Quantidade 40 -Descricao "API Hello"

Write-Host ""
Write-Host "✅ Tráfego gerado com sucesso!" -ForegroundColor Green
Write-Host ""
Write-Host "📊 Agora acesse:" -ForegroundColor Cyan
Write-Host "   Grafana: http://localhost:3000 (admin/admin)" -ForegroundColor Yellow
Write-Host "   Prometheus: http://localhost:9090" -ForegroundColor Yellow
Write-Host ""
Write-Host "💡 Dica: Configure o auto-refresh do Grafana para 5s para ver atualizações em tempo real!" -ForegroundColor Magenta
