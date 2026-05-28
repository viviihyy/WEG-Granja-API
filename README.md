# 🍽️ WEG Granja API

> **Sistema inteligente de controle de distribuição de proteína** no refeitório WEG, otimizando refeições por turno com segurança e rastreabilidade.

[![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=flat-square&logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0+-6DB33F?style=flat-square&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36?style=flat-square&logo=apache-maven)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)

---

## 📋 Sobre o Projeto

WEG Granja API é uma solução REST para gerenciar a distribuição de refeições no refeitório corporativo. Com leitura de crachá RFID integrada, o sistema garante:

- ✅ Controle de cotas por turno e tipo de refeição
- ✅ Registro completo de todas as retiradas
- ✅ Interface para configuração diária pela nutricionista
- ✅ Soft delete de colaboradores (auditoria)
- ✅ Histórico detalhado e filtros por data/turno

---

## 🚀 Quick Start

### Pré-requisitos

```bash
Java 17+
Maven 3.9+
MySQL 8.0+ (porta 3306)
```

### Instalação

1. **Clone o repositório**
```bash
git clone https://github.com/viviihyy/WEG-Granja-API.git
cd WEG-Granja-API
```

2. **Configure o banco de dados**
```bash
mysql -u root -p < banco.sql
```

3. **Ajuste as credenciais**
Edite `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/weg_granja
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
```

4. **Inicie a aplicação**
```bash
mvn clean spring-boot:run
```

🎯 API disponível em: **http://localhost:8080**

---

## 📚 Documentação da API

### 🥩 Dispenser (Máquina)

Endpoints para leitura de crachá e consulta de cotas.

#### Dispensar Refeição
```http
POST /dispenser/dispensar/{matricula}
```

**Response (200 OK)**
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

**Response (400 Bad Request)**
```json
{
  "status": "BLOQUEADO",
  "mensagem": "❌ Limite de porções atingido para hoje!",
  "porcoesRestantes": 0
}
```

#### Consultar Cota
```http
GET /dispenser/cota/{matricula}?tipoRefeicao=ALMOCO
```

---

### 👩‍⚕️ Configurações Diárias (Nutricionista)

Gerenciar limites de porções por data e refeição.

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/configuracoes-diarias` | Lista todas as configurações |
| `GET` | `/configuracoes-diarias?data=2025-06-10` | Filtra por data |
| `GET` | `/configuracoes-diarias/{id}` | Busca por ID |
| `POST` | `/configuracoes-diarias` | Cria/atualiza configuração |
| `PUT` | `/configuracoes-diarias/{id}` | Edita configuração |
| `DELETE` | `/configuracoes-diarias/{id}` | Remove configuração |

**Body (POST/PUT)**
```json
{
  "data": "2025-06-10",
  "tipoRefeicao": "ALMOCO",
  "limitePorcoes": 150
}
```

---

### 👷 Colaboradores (CRUD)

Gerenciar cadastro de colaboradores.

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/colaboradores` | Lista ativos |
| `GET` | `/colaboradores?apenasAtivos=false` | Lista todos |
| `GET` | `/colaboradores/{id}` | Busca por ID |
| `GET` | `/colaboradores/matricula/{matricula}` | Busca por matrícula |
| `POST` | `/colaboradores` | Cria colaborador |
| `PUT` | `/colaboradores/{id}` | Edita colaborador |
| `DELETE` | `/colaboradores/{id}` | Inativa (soft delete) |
| `PATCH` | `/colaboradores/{id}/reativar` | Reativa colaborador |

**Body (POST/PUT)**
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

Consultar registro de retiradas de refeições.

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/retiradas` | Retiradas de hoje |
| `GET` | `/retiradas?data=2025-06-10` | Retiradas de uma data |
| `GET` | `/retiradas?tipoRefeicao=JANTA` | Filtro por refeição |
| `GET` | `/retiradas/colaborador/{id}` | Histórico de um colaborador |

---

## 📋 Regras de Negócio

### Matriz Turno × Refeição

| Turno | Refeição Permitida | Segunda Refeição |
|-------|-------------------|------------------|
| 🌅 MANHA | ALMOCO | ❌ Não |
| 🌤️ TARDE | ALMOCO | ❌ Não |
| 🌙 NOITE | JANTA | ❌ Não |
| ➕ Qualquer + Vale 2ª | ALMOCO + JANTA | ✅ Sim |

### Fluxo de Funcionamento

1. **Nutricionista configura** o limite de porções (ex: 150 para almoço)
2. **Colaborador passa crachá** na máquina
3. **Sistema valida**:
   - ✓ Colaborador existe e está ativo?
   - ✓ É o turno correto para essa refeição?
   - ✓ Não ultrapassou o limite do dia?
4. **Se autorizado**: Libera 1 porção e registra no histórico
5. **Se bloqueado**: Retorna mensagem de erro com motivo

---

## 🗄️ Estrutura do Banco de Dados

```
┌─────────────────────────────┐
│     COLABORADORES           │
├─────────────────────────────┤
│ id (PK)                     │
│ nome                        │
│ matricula (UNIQUE)          │
│ turno (MANHA|TARDE|NOITE)   │
│ tem_segunda_refeicao        │
│ ativo (soft delete)         │
│ criado_em                   │
│ atualizado_em               │
└─────────────────────────────┘

┌──────────────────────────────────┐
│   CONFIGURACOES_DIARIAS          │
├──────────────────────────────────┤
│ id (PK)                          │
│ data                             │
│ tipo_refeicao (ALMOCO|JANTA)    │
│ limite_porcoes                   │
│ UNIQUE(data, tipo_refeicao)      │
│ criado_em                        │
│ atualizado_em                    │
└──────────────────────────────────┘

┌────────────────────────────────┐
│      RETIRADAS                 │
├────────────────────────────────┤
│ id (PK)                        │
│ colaborador_id (FK)            │
│ data_hora                      │
│ tipo_refeicao (ALMOCO|JANTA)  │
│ quantidade (sempre 1)          │
│ criado_em                      │
└────────────────────────────────┘
```

---

## 🛠️ Tecnologias

- **Backend**: Java 17 + Spring Boot 3.0+
- **Database**: MySQL 8.0+
- **Build Tool**: Maven 3.9+
- **ORM**: JPA/Hibernate
- **API**: REST com Spring Web
- **Validation**: Spring Validation

---

## 📝 Exemplos de Uso

### Criar um Colaborador
```bash
curl -X POST http://localhost:8080/colaboradores \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Silva",
    "matricula": "100001",
    "turno": "MANHA",
    "temSegundaRefeicao": false
  }'
```

### Dispensar Refeição
```bash
curl -X POST http://localhost:8080/dispenser/dispensar/100001
```

### Configurar Limite do Dia
```bash
curl -X POST http://localhost:8080/configuracoes-diarias \
  -H "Content-Type: application/json" \
  -d '{
    "data": "2025-06-10",
    "tipoRefeicao": "ALMOCO",
    "limitePorcoes": 150
  }'
```

---

## 🎯 Roadmap

- [ ] Autenticação e autorização (JWT)
- [ ] Dashboard com métricas de consumo
- [ ] Integração com sistema de ponto
- [ ] Notificações para nutricionista
- [ ] Exportação de relatórios (PDF/Excel)
- [ ] Mobile app para consulta de cotas
