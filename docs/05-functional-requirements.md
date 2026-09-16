# 05 — Functional Requirements

## Identity & Accounts

### FR-AUTH-001
O sistema deve permitir criação e autenticação de contas.

### FR-AUTH-002
Perfis menores de 16 anos devem estar vinculados a responsável maior de 18 anos.

### FR-AUTH-003
Um responsável poderá administrar múltiplos perfis de menores.

## Writer Profile

### FR-PROF-001
O perfil deve armazenar data de nascimento.

### FR-PROF-002
A faixa etária deve ser derivada da data de nascimento.

### FR-PROF-003
O perfil deve armazenar nível de escrita.

### FR-PROF-004
O perfil deve armazenar interesses e preferências.

### FR-PROF-005
O sistema deve atualizar a classificação etária automaticamente a cada aniversário.

### FR-PROF-006
Mudanças de faixa etária devem preservar o histórico.

### FR-PROF-007
O sistema deve manter habilidades e evolução.

## Challenges

### FR-CH-001 — Anti-repetição
Um usuário não deve receber novamente um desafio semanticamente equivalente a outro desafio já apresentado a ele.

### FR-CH-002
Desafios devem considerar idade, nível, interesses, histórico, frequência e habilidades-alvo.

### FR-CH-003
O sistema deve suportar múltiplos tipos de desafio.

### FR-CH-004
Desafios devem passar por validação de qualidade.

### FR-CH-005
Desafios para menores devem passar por filtro de segurança e adequação etária.

### FR-CH-006
O sistema deve registrar versão do desafio entregue.

### FR-CH-007
O sistema deve registrar quais habilidades o desafio pretende exercitar.

## Writing

### FR-WR-001
O usuário deve poder escrever no editor.

### FR-WR-002
O usuário deve poder enviar uma ou mais fotos do texto manuscrito.

### FR-WR-003
O sistema deve permitir confirmação/correção do texto extraído da imagem.

### FR-WR-004
O usuário deve poder combinar editor e texto proveniente de imagem.

### FR-WR-005
O sistema deve manter versões do texto.

## Evaluation

### FR-EV-001
Cada submissão avaliada deve receber nota de 0 a 100.

### FR-EV-002
A avaliação deve considerar, conforme a rubrica vigente, criatividade, desenvolvimento, coesão, coerência, vocabulário, estrutura, gramática, pontuação e aderência ao desafio.

### FR-EV-003
A avaliação deve verificar se o texto efetivamente aborda o desafio proposto.

### FR-EV-004
A avaliação deve considerar a idade/faixa etária.

### FR-EV-005
A avaliação deve produzir feedback positivo e pontos de melhoria.

### FR-EV-006
A avaliação deve registrar a versão da rubrica e do modelo de IA utilizado.

### FR-EV-007
O sistema deve permitir reavaliação de uma nova versão.

## Rubrics

### FR-RUB-001
A rubrica deve ser configurável.

### FR-RUB-002
Pesos devem ser alteráveis por administrador.

### FR-RUB-003
Futuramente, responsáveis e educadores autorizados poderão configurar rubricas em seus respectivos escopos.

### FR-RUB-004
Toda alteração deve gerar nova versão da rubrica.

### FR-RUB-005
Avaliações antigas devem continuar associadas à versão utilizada no momento da avaliação.

## Progress

### FR-PRG-001
O sistema deve registrar evolução por habilidade.

### FR-PRG-002
O sistema deve calcular média e tendência.

### FR-PRG-003
O sistema deve mostrar evolução após reescrita.

## Gamification

### FR-GAM-001
O sistema deve registrar streaks.

### FR-GAM-002
O sistema deve suportar conquistas.

### FR-GAM-003
O sistema deve registrar XP ou mecanismo equivalente.

## Notifications

### FR-NOT-001
Usuários poderão configurar frequência de desafios.

### FR-NOT-002
Notificações não devem expor conteúdo sensível.

## Future — Educator

### FR-EDU-001
Futuro suporte a turmas, desafios de turma, rubricas e relatórios deve ser compatível com o domínio atual.
