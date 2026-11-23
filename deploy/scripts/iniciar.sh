#!/bin/bash
# Para o script se qualquer comando der erro.
set -e

# Descobre onde o Java está instalado na EC2.
JAVA_BIN="$(command -v java)"

# Cria o serviço systemd que mantém a API sempre ligada.
cat > /etc/systemd/system/api-tarefas.service <<EOF
[Unit]
Description=API de Tarefas Spring Boot
After=network.target

[Service]
WorkingDirectory=/opt/api-tarefas
ExecStart=${JAVA_BIN} -jar /opt/api-tarefas/app.jar
Restart=always
RestartSec=5
User=root

[Install]
WantedBy=multi-user.target
EOF

# Recarrega o systemd e sobe a API com a versão nova.
systemctl daemon-reload
systemctl enable api-tarefas
systemctl restart api-tarefas
