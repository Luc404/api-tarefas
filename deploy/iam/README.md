# Políticas IAM deste projeto

JSON não aceita comentários, então este arquivo explica cada política.

| Arquivo | Onde usar | Por quê |
| --- | --- | --- |
| `codebuild-trust-policy.json` | Trust relationship da role `api-tarefas-codebuild-role` | Permite o CodeBuild assumir a role. |
| `codebuild-policy.json` | Permission policy da role `api-tarefas-codebuild-role` | Escreve logs no CloudWatch e lê/escreve o artefato no S3. |
| `pipeline-trust-policy.json` | Trust relationship da role `api-tarefas-pipeline-role` | Permite o CodePipeline assumir a role. |
| `pipeline-policy.json` | Permission policy da role `api-tarefas-pipeline-role` | Usa a conexão GitHub, inicia o CodeBuild, chama o CodeDeploy, passa roles e lê/escreve o S3. |
| `codedeploy-trust-policy.json` | Trust relationship da role `api-tarefas-codedeploy-role` | Permite o CodeDeploy assumir a role. |
| `ec2-trust-policy.json` | Trust relationship da role `api-tarefas-ec2-role` | Permite a instância EC2 assumir a role. |
| `ec2-policy.json` | Permission policy da role `api-tarefas-ec2-role` | Baixa o artefato do S3 e o instalador do agente CodeDeploy. |

A role `api-tarefas-codedeploy-role` usa a política gerenciada `AWSCodeDeployRole` em vez de uma política própria.
A role `api-tarefas-ec2-role` também recebe a política gerenciada `AmazonSSMManagedInstanceCore` para acesso via Session Manager.
