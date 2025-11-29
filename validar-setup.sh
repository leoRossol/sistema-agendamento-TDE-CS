#!/bin/bash

# Script de validação do setup de observabilidade

echo "=============================================="
echo "🔍 VALIDAÇÃO DO SETUP DE OBSERVABILIDADE"
echo "=============================================="
echo ""

# Cores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Função para verificar sucesso/falha
check_result() {
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ $1${NC}"
        return 0
    else
        echo -e "${RED}❌ $1${NC}"
        return 1
    fi
}

# 1. Verificar se Docker está rodando
echo "1️⃣ Verificando Docker..."
docker --version > /dev/null 2>&1
if [ $? -eq 0 ]; then
    check_result "Docker instalado"
    docker ps > /dev/null 2>&1
    check_result "Docker daemon está rodando"
else
    echo -e "${RED}❌ Docker não está instalado ou não está rodando${NC}"
    echo "   Por favor, inicie o Docker Desktop e tente novamente"
    exit 1
fi
echo ""

# 2. Verificar arquivos de configuração
echo "2️⃣ Verificando arquivos de configuração..."

files=(
    "pom.xml"
    "compose.yaml"
    "prometheus.yml"
    "grafana/provisioning/datasources/prometheus.yml"
    "grafana/provisioning/dashboards/dashboard-provider.yml"
    "grafana/provisioning/dashboards/sistema-agendamento-dashboard.json"
    "src/main/resources/application.properties"
    "src/main/resources/application-docker.properties"
)

all_files_ok=true
for file in "${files[@]}"; do
    if [ -f "$file" ]; then
        echo -e "${GREEN}✅${NC} $file"
    else
        echo -e "${RED}❌${NC} $file - FALTANDO!"
        all_files_ok=false
    fi
done
echo ""

if [ "$all_files_ok" = false ]; then
    echo -e "${RED}❌ Alguns arquivos estão faltando!${NC}"
    exit 1
fi

# 3. Verificar dependência no pom.xml
echo "3️⃣ Verificando dependência Micrometer no pom.xml..."
if grep -q "micrometer-registry-prometheus" pom.xml; then
    check_result "Dependência Micrometer encontrada"
else
    echo -e "${RED}❌ Dependência Micrometer NÃO encontrada${NC}"
    exit 1
fi
echo ""

# 4. Verificar configurações de métricas
echo "4️⃣ Verificando configurações de métricas..."
if grep -q "management.endpoint.prometheus.enabled" src/main/resources/application-docker.properties; then
    check_result "Configuração Prometheus no application-docker.properties"
else
    echo -e "${RED}❌ Configuração Prometheus NÃO encontrada${NC}"
    exit 1
fi
echo ""

# 5. Build e inicialização dos containers
echo "5️⃣ Buildando e iniciando containers..."
echo "   Isso pode levar alguns minutos..."
docker-compose down > /dev/null 2>&1
docker-compose up -d --build
check_result "Containers iniciados"
echo ""

# 6. Aguardar serviços iniciarem
echo "6️⃣ Aguardando serviços iniciarem..."
echo "   MySQL..."
for i in {1..30}; do
    docker-compose exec -T mysql mysqladmin ping -h localhost -u root -pverysecret > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        check_result "MySQL está pronto"
        break
    fi
    sleep 2
done
echo ""

echo "   Aplicação Spring Boot..."
for i in {1..60}; do
    curl -s http://localhost:8080/actuator/health > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        check_result "Aplicação está pronta"
        break
    fi
    sleep 2
done
echo ""

# 7. Verificar containers rodando
echo "7️⃣ Verificando containers em execução..."
docker-compose ps
echo ""

# 8. Testar endpoints
echo "8️⃣ Testando endpoints..."

echo "   Health check..."
curl -s http://localhost:8080/actuator/health | grep -q "UP"
check_result "Health check respondendo"

echo "   Métricas Prometheus..."
curl -s http://localhost:8080/actuator/prometheus | grep -q "jvm_memory_used_bytes"
check_result "Endpoint Prometheus expondo métricas"

echo "   Prometheus UI..."
curl -s http://localhost:9090 > /dev/null
check_result "Prometheus UI acessível"

echo "   Grafana UI..."
curl -s http://localhost:3000 > /dev/null
check_result "Grafana UI acessível"
echo ""

# 9. Gerar tráfego de teste
echo "9️⃣ Gerando tráfego de teste..."
for i in {1..20}; do
    curl -s http://localhost:8080/actuator/health > /dev/null
    curl -s http://localhost:8080/actuator/metrics > /dev/null
done
check_result "Tráfego gerado (40 requisições)"
echo ""

# 10. Aguardar Prometheus coletar
echo "🔟 Aguardando Prometheus coletar métricas (15 segundos)..."
sleep 15
echo ""

# 11. Verificar se Prometheus está coletando
echo "1️⃣1️⃣ Verificando coleta do Prometheus..."
curl -s http://localhost:9090/api/v1/targets | grep -q "sistema-agendamento-reports"
check_result "Prometheus configurado para coletar da aplicação"

# Verificar se há dados
curl -s "http://localhost:9090/api/v1/query?query=up{job='sistema-agendamento-reports'}" | grep -q '"value":\[.*,.*"1"\]'
check_result "Prometheus está coletando dados da aplicação"
echo ""

# 12. Resumo final
echo "=============================================="
echo "📊 RESUMO DOS SERVIÇOS"
echo "=============================================="
echo ""
echo "✅ Aplicação:     http://localhost:8080"
echo "✅ Métricas:      http://localhost:8080/actuator/prometheus"
echo "✅ Health:        http://localhost:8080/actuator/health"
echo "✅ Swagger:       http://localhost:8080/swagger-ui.html"
echo "✅ Prometheus:    http://localhost:9090"
echo "✅ Grafana:       http://localhost:3000 (admin/admin)"
echo ""
echo "=============================================="
echo "🎯 PRÓXIMOS PASSOS"
echo "=============================================="
echo ""
echo "1. Acesse o Grafana: http://localhost:3000"
echo "2. Login: admin / admin"
echo "3. Vá em Dashboards → Sistema de Agendamento - Métricas da Aplicação"
echo "4. Configure auto-refresh para 5s (relógio no canto superior direito)"
echo "5. Execute: ./gerar-trafego.sh para gerar mais tráfego"
echo ""
echo "✅ Setup de observabilidade VALIDADO COM SUCESSO! 🎉"
echo ""
