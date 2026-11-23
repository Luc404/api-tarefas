#!/bin/bash
# Atualiza a lista de pacotes e instala Java 21 e wget.
dnf install -y java-21-amazon-corretto-headless wget

# Vai para /tmp, onde o instalador do agente será baixado.
cd /tmp

# Baixa o instalador oficial do CodeDeploy v2 para sa-east-1.
wget -q https://aws-codedeploy-sa-east-1.s3.sa-east-1.amazonaws.com/latestv2/install

# Dá permissão de execução e instala o agente em modo automático.
chmod +x install
./install auto

# Garante que o agente suba junto com a instância.
systemctl enable --now codedeploy-agent
