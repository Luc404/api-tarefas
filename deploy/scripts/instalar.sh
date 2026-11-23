#!/bin/bash
# Script ilustrativo: a simulação não executa isto em nenhum serviço externo.
# Para o script se qualquer comando der erro.
set -e

# Cria a pasta onde o JAR vai morar na EC2.
mkdir -p /opt/api-tarefas

# Confere se o CodeDeploy entregou o JAR.
if [ ! -f /opt/api-tarefas/app.jar ]; then
    echo "Erro: /opt/api-tarefas/app.jar não foi encontrado"
    exit 1
fi

# Confere se o Java está instalado antes de tentar rodar.
if ! command -v java >/dev/null 2>&1; then
    echo "Erro: Java não está instalado na instância EC2"
    exit 1
fi

# Mostra a versão do Java no log do CodeDeploy.
java -version
