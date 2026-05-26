# WEG Granja — Sistema de Controle de Proteína

Sistema para controlar a distribuição de proteína no refeitório da WEG, por turno e refeição.

---

## Como rodar

**Pré-requisitos:** Java 17+, Maven, MySQL rodando na porta 3306.

1. Crie o banco de dados:
```bash
mysql -u root -p < banco.sql
```

2. Ajuste as credenciais em `src/main/resources/application.properties`.

3. Suba a aplicação:
```bash
mvn spring-boot:run
```

A API fica disponível em `http://localhost:8080`.

---

## Regras de negócio

| Turno       | Refeição permitida     |
|-------------|------------------------|
| MANHA       | ALMOCO                 |
| TARDE       | ALMOCO                 |
| NOITE       | JANTA                  |
| Qualquer + vale 2ª refeição | ALMOCO e JANTA |

- A nutricionista define o **limite de porções** por data e refeição.
- Cada leitura de crachá libera **exatamente 1 porção** (RN03).
- O sistema **bloqueia** quando o limite é atingido ou o colaborador não pertence ao turno.
- Toda retirada é registrada com matrícula, data/hora e tipo de refeição (RN04).

---

## Endpoints

### 🥩 Dispenser (Máquina)

| Método | URL | Descrição |
|--------|-----|-----------|
| `POST` | `/dispenser/dispensar/{matricula}` | Leitura do crachá — libera 1 porção |
| `GET`  | `/dispenser/cota/{matricula}?tipoRefeicao=ALMOCO` | Consulta cota sem dispensar |

**Exemplo — leitura de crachá:**
```
POST /dispenser/dispensar/100001
```
```json
{
  "status": "LIBERADO",
  "mensagem": "✅ Porção liberada! Bom apetite, Ana!",
  "colaboradorNome": "Ana Paula Ferreira",
  "matricula": "100001",
  "refeicao": "ALMOCO",
  "porcoesUtilizadas": 1,
  "porcoesRestantes": 2,
  "limiteTotal": 3,
  "dataHora": "2025-06-10T12:05:33"
}
```

---

### 👩‍⚕️ Configuração Diária (Nutricionista)

| Método | URL | Descrição |
|--------|-----|-----------|
| `GET`  | `/configuracoes-diarias` | Lista todas as configurações |
| `GET`  | `/configuracoes-diarias?data=2025-06-10` | Filtra por data |
| `GET`  | `/configuracoes-diarias/{id}` | Busca por ID |
| `POST` | `/configuracoes-diarias` | Cadastra/atualiza limite do dia |
| `PUT`  | `/configuracoes-diarias/{id}` | Edita configuração |
| `DELETE` | `/configuracoes-diarias/{id}` | Remove configuração |

**Body POST/PUT:**
```json
{
  "data": "2025-06-10",
  "tipoRefeicao": "ALMOCO",
  "limitePorcoes": 3
}
```

---

### 👷 Colaboradores (CRUD)

| Método | URL | Descrição |
|--------|-----|-----------|
| `GET`  | `/colaboradores` | Lista colaboradores ativos |
| `GET`  | `/colaboradores?apenasAtivos=false` | Lista todos |
| `GET`  | `/colaboradores/{id}` | Busca por ID |
| `GET`  | `/colaboradores/matricula/{matricula}` | Busca por matrícula |
| `POST` | `/colaboradores` | Cadastra colaborador |
| `PUT`  | `/colaboradores/{id}` | Edita colaborador |
| `DELETE` | `/colaboradores/{id}` | Inativa (soft delete) |
| `PATCH` | `/colaboradores/{id}/reativar` | Reativa |

**Body POST/PUT:**
```json
{
  "nome": "Gabrielli Glowatski",
  "matricula": "100010",
  "turno": "NOITE",
  "temSegundaRefeicao": false
}
```

---

### 📋 Histórico de Retiradas

| Método | URL | Descrição |
|--------|-----|-----------|
| `GET`  | `/retiradas` | Retiradas de hoje |
| `GET`  | `/retiradas?data=2025-06-10` | Retiradas de uma data |
| `GET`  | `/retiradas?tipoRefeicao=JANTA` | Filtro por refeição |
| `GET`  | `/retiradas/colaborador/{id}` | Histórico de um colaborador |

---

## Estrutura do banco

```
colaboradores
 ├── id
 ├── nome
 ├── matricula           (unique)
 ├── turno               MANHA | TARDE | NOITE
 ├── tem_segunda_refeicao
 └── ativo

configuracoes_diarias
 ├── id
 ├── data
 ├── tipo_refeicao       ALMOCO | JANTA
 └── limite_porcoes      (unique por data+refeição)

retiradas
 ├── id
 ├── colaborador_id      (FK)
 ├── data_hora
 ├── tipo_refeicao       ALMOCO | JANTA
 └── quantidade          sempre 1
```
