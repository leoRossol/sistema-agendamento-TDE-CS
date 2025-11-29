#!/bin/bash

# Script para gerar tráfego e testar métricas do Grafana

echo "🚀 Gerando tráfego para o Sistema de Agendamento..."
echo ""

# Função para fazer requisições
gerar_requisicoes() {
    local endpoint=$1
    local quantidade=$2
    local descricao=$3

    echo "📊 $descricao ($quantidade requisições)..."
    for i in $(seq 1 $quantidade); do
        curl -s "$endpoint" > /dev/null
        echo -n "."
    done
    echo " ✅ Concluído!"
}

# Aguardar aplicação estar pronta
echo "⏳ Aguardando aplicação estar pronta..."
until curl -s http://localhost:8080/actuator/health > /dev/null; do
    echo -n "."
    sleep 1
done
echo " ✅ Aplicação pronta!"
echo ""

# Gerar diferentes tipos de tráfego
gerar_requisicoes "http://localhost:8080/actuator/health" 50 "Health checks"
gerar_requisicoes "http://localhost:8080/actuator/metrics" 30 "Métricas gerais"
gerar_requisicoes "http://localhost:8080/actuator/prometheus" 20 "Prometheus endpoint"
gerar_requisicoes "http://localhost:8080/api/hello" 40 "API Hello"

echo ""
echo "✅ Tráfego gerado com sucesso!"
echo ""
echo "📊 Agora acesse:"
echo "   Grafana: http://localhost:3000 (admin/admin)"
echo "   Prometheus: http://localhost:9090"
echo ""
echo "💡 Dica: Configure o auto-refresh do Grafana para 5s para ver atualizações em tempo real!"
