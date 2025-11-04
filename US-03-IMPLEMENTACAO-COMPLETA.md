# US-03 - Select events for classroom/lab (Agendamento) ✅ CONCLUÍDA

## Resumo da Implementação

**Status:** ✅ **100% COMPLETO**  
**Testes:** 4/4 passando (100%)  
**Build:** SUCCESS  
**Branch:** US-03-Create/select-events-for-classroom/lab

---

## 📋 To-dos Implementados

- [x] Implementar POST /scheduler/eventos para criar eventos com validações
- [x] Implementar GET /scheduler/eventos/{id} para buscar evento por ID
- [x] Implementar GET /scheduler/calendario/professores/{id} para listar eventos do professor
- [x] Implementar detecção de conflitos (sala, professor, turma)
- [x] Implementar sistema de sugestões de horários alternativos
- [x] Criar testes de integração para SchedulerController (4 casos)
- [x] Adicionar documentação Swagger/OpenAPI completa
- [x] Configurar OpenApiConfig para documentação do Swagger

---

## 📦 Arquivos Criados

### Controller
- ✅ `src/main/java/com/sistema/agendamento/sistema_agendamento/controller/SchedulerController.java`

### DTOs
- ✅ `src/main/java/com/sistema/agendamento/sistema_agendamento/dto/CreateEventoRequest.java`
- ✅ `src/main/java/com/sistema/agendamento/sistema_agendamento/dto/EventoResponse.java`
- ✅ `src/main/java/com/sistema/agendamento/sistema_agendamento/dto/SugestaoDTO.java`

### Service
- ✅ `src/main/java/com/sistema/agendamento/sistema_agendamento/service/SchedulerService.java`

### Repository
- ✅ `src/main/java/com/sistema/agendamento/sistema_agendamento/repository/EventoRepository.java`

### Config
- ✅ `src/main/java/com/sistema/agendamento/sistema_agendamento/config/OpenApiConfig.java`

### Testes
- ✅ `src/test/java/com/sistema/agendamento/sistema_agendamento/SchedulerControllerTests.java` (4 testes)

---

## 📝 Arquivos Modificados

- ✅ `pom.xml` - Adicionada dependência springdoc-openapi-starter-webmvc-ui 2.3.0

---

## ✅ Critérios de Aceite Atendidos

### Funcionalidades
✅ **POST /scheduler/eventos** - Criar evento retorna 201  
✅ **Detecção de conflito de sala** - Retorna 409 CONFLICT com sugestões  
✅ **Detecção de conflito de professor** - Professor ocupado no período  
✅ **Detecção de conflito de turma** - Turma ocupada no período  
✅ **GET /scheduler/eventos/{id}** - Busca evento por ID retorna 200  
✅ **GET /scheduler/calendario/professores/{id}** - Lista eventos do período

### Regras de Negócio
✅ **Validação de horário** - Verifica se `inicio < fim`  
✅ **Validação de campos obrigatórios** - Título, tipo, professor, sala e horários  
✅ **Tipo de evento** - Valida contra enum (AULA, PROVA, SEMINARIO, OUTROS)  
✅ **Sistema de sugestões** - Oferece 3 alternativas quando há conflito:
  - Outra sala no mesmo horário
  - Próxima janela (10 min depois)
  - Dia seguinte no mesmo horário

### Qualidade
✅ **Testes de integração** - 4 testes com 100% de sucesso  
✅ **Documentação Swagger** - Completa com @Operation, @ApiResponse, @Schema  
✅ **Tratamento de erros** - Exceptions customizadas com detalhes

---

## 🧪 Resultados dos Testes

```
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Detalhamento:
- ✅ `criaEvento_semConflito_retorna201_eStatusConfirmadoOuAgendado` - Evento criado com sucesso
- ✅ `criarEvento_comConflitoNaMesmaSala_retorna409` - Conflito detectado corretamente
- ✅ `getEvento_porId_200` - Busca por ID funcional
- ✅ `calendarioProfessor_porPeriodo_200_eTemItens` - Calendário retorna lista correta

---

## 🚀 Endpoints Implementados

### 1. Criar Evento
```
POST /scheduler/eventos
Content-Type: application/json

{
  "titulo": "Aula de Cálculo I",
  "descricao": "Primeira aula do semestre",
  "tipoEvento": "AULA",
  "professorId": 1,
  "turmaId": 1,
  "salaId": 10,
  "inicio": "2025-10-27T19:00:00",
  "fim": "2025-10-27T21:00:00"
}

Response: 201 Created
{
  "id": 1,
  "status": "CONFIRMADO",
  "tipo": "AULA",
  "titulo": "Aula de Cálculo I",
  "descricao": "Primeira aula do semestre",
  "professorId": 1,
  "turmaId": 1,
  "recurso": { "tipo": "SALA", "id": 10 },
  "inicio": "2025-10-27T19:00:00",
  "fim": "2025-10-27T21:00:00"
}
```

### 2. Conflito de Horário (409)
```
POST /scheduler/eventos
[mesmo horário em sala ocupada]

Response: 409 Conflict
{
  "code": "CONFLITO_AGENDA",
  "message": "Conflito detectado com recurso/professor/turma.",
  "sugestoes": [
    {
      "inicio": "2025-10-27T19:10:00",
      "fim": "2025-10-27T21:10:00",
      "recurso": { "tipo": "SALA", "id": 2 },
      "motivo": "Outro recurso no mesmo horário"
    },
    {
      "inicio": "2025-10-27T19:10:00",
      "fim": "2025-10-27T21:10:00",
      "recurso": { "tipo": "SALA", "id": 10 },
      "motivo": "Próxima janela no mesmo dia"
    },
    {
      "inicio": "2025-10-28T19:00:00",
      "fim": "2025-10-28T21:00:00",
      "recurso": { "tipo": "SALA", "id": 10 },
      "motivo": "Mesmo horário no dia seguinte"
    }
  ]
}
```

### 3. Buscar Evento por ID
```
GET /scheduler/eventos/{id}

Response: 200 OK
{
  "id": 1,
  "status": "CONFIRMADO",
  "tipo": "AULA",
  "titulo": "Aula de Cálculo I",
  "professorId": 1,
  "turmaId": 1,
  "recurso": { "tipo": "SALA", "id": 10 },
  "inicio": "2025-10-27T19:00:00",
  "fim": "2025-10-27T21:00:00"
}
```

### 4. Consultar Calendário do Professor
```
GET /scheduler/calendario/professores/{id}?periodo=2025-10-01T00:00:00/2025-10-31T23:59:59

Response: 200 OK
[
  {
    "id": 1,
    "status": "CONFIRMADO",
    "tipo": "AULA",
    "titulo": "Aula de Cálculo I",
    "professorId": 1,
    "turmaId": 1,
    "recurso": { "tipo": "SALA", "id": 10 },
    "inicio": "2025-10-27T19:00:00",
    "fim": "2025-10-27T21:00:00"
  },
  ...
]
```

---

## 📚 Documentação API

Acesse a documentação interativa do Swagger em:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

---

## 🎯 Detecção de Conflitos

### 1. Conflito de Sala
```java
boolean conflitoSala = !eventoRepository
    .findConflitosAgendamento(sala, req.inicio, req.fim)
    .isEmpty();
```

Verifica se a sala está ocupada em qualquer momento que intersecte o período solicitado.

### 2. Conflito de Professor
```java
boolean conflitoProfessor = !eventoRepository
    .findConflitosProfessor(professor, req.inicio, req.fim)
    .isEmpty();
```

Garante que o professor não tenha outro evento no mesmo horário.

### 3. Conflito de Turma
```java
boolean conflitoTurma = (turma != null) && !eventoRepository
    .findConflitosTurma(turma, req.inicio, req.fim)
    .isEmpty();
```

Previne que a turma tenha aula em outro lugar no mesmo horário.

---

## 🎨 Sistema de Sugestões

Quando um conflito é detectado, o sistema oferece **3 sugestões automáticas**:

1. **Outra sala no mesmo horário** - Busca salas disponíveis no período solicitado
2. **Próxima janela (10 min depois)** - Sugere adiar 10 minutos no mesmo dia e sala
3. **Mesmo horário no dia seguinte** - Move o evento para o próximo dia

### Algoritmo de Sugestões

```java
private List<SugestaoDTO> sugerir(CreateEventoRequest req, Sala salaOriginal) {
    List<SugestaoDTO> out = new ArrayList<>();
    
    // A) MESMO HORÁRIO, OUTRA SALA
    var salas = salaRepository.findAll();
    for (var s : salas) {
        if (Objects.equals(s.getId(), salaOriginal.getId())) continue;
        boolean livre = eventoRepository.findConflitosAgendamento(s, req.inicio, req.fim).isEmpty();
        if (livre) {
            out.add(new SugestaoDTO(req.inicio, req.fim, "SALA", s.getId(), "Outro recurso no mesmo horário"));
            if (out.size() >= 3) break;
        }
    }
    
    // B) PRÓXIMA JANELA
    if (out.size() < 3) {
        out.add(new SugestaoDTO(req.inicio.plusMinutes(10), req.fim.plusMinutes(10), 
                                "SALA", salaOriginal.getId(), "Próxima janela no mesmo dia"));
    }
    
    // C) DIA SEGUINTE
    if (out.size() < 3) {
        out.add(new SugestaoDTO(req.inicio.plusDays(1), req.fim.plusDays(1), 
                                "SALA", salaOriginal.getId(), "Mesmo horário no dia seguinte"));
    }
    
    return out;
}
```

---

## 🔒 Tratamento de Erros

### 400 Bad Request
- Horário inválido (fim antes ou igual ao início)
- Campos obrigatórios faltando (título, tipo, professor, sala)
- Tipo de evento inválido

### 409 Conflict
- Sala ocupada no período solicitado
- Professor ocupado no período solicitado
- Turma ocupada no período solicitado
- **Inclui sugestões** de horários alternativos

### 422 Unprocessable Entity
- Professor não encontrado
- Sala não encontrada
- Turma não encontrada (quando fornecida)

### 404 Not Found
- Evento não encontrado na busca por ID
- Professor não encontrado na consulta de calendário

---

## 🎨 Padrões Arquiteturais

- **Controller Layer**: Recebe requisições HTTP e delega ao Service
- **Service Layer**: Contém toda a lógica de negócio e validações de conflitos
- **Repository Layer**: Queries customizadas para detectar conflitos de horário
- **DTO Pattern**: Separação entre entidades de domínio e transferência de dados
- **Exception Handling**: Classe interna `SchedulerConflict` para conflitos com sugestões

---

## 📊 Validações Implementadas

### Validação de Horário
```java
if (!e.inicio.isBefore(e.fim)) 
    throw new IllegalArgumentException("inicio deve ser anterior a fim");
```

### Validação de Campos Obrigatórios
```java
if (!StringUtils.hasText(e.titulo)) 
    throw new IllegalArgumentException("titulo é obrigatório");
if (e.professorId == null) 
    throw new IllegalArgumentException("professorId é obrigatório");
```

### Validação de Tipo
```java
try { 
    TipoEvento.valueOf(e.tipoEvento); 
} catch (IllegalArgumentException ex) { 
    throw new IllegalArgumentException("tipoEvento inválido"); 
}
```

---

## 🔍 Queries JPQL

### Eventos do Período
```java
@Query("""
    select e from Evento e
    where e.dataInicio < :fim
      and e.dataFim > :inicio
""")
List<Evento> findEventosDoPeriodo(LocalDateTime inicio, LocalDateTime fim);
```

### Conflitos de Sala
```java
@Query("""
    select e from Evento e
    where e.sala = :sala
      and e.dataInicio < :fim
      and e.dataFim > :inicio
""")
List<Evento> findConflitosAgendamento(Sala sala, LocalDateTime inicio, LocalDateTime fim);
```

### Conflitos de Professor
```java
@Query("""
    select e from Evento e
    where e.professor = :professor
      and e.dataInicio < :fim
      and e.dataFim > :inicio
""")
List<Evento> findConflitosProfessor(Usuario professor, LocalDateTime inicio, LocalDateTime fim);
```

### Conflitos de Turma
```java
@Query("""
    select e from Evento e
    where e.turma = :turma
      and e.dataInicio < :fim
      and e.dataFim > :inicio
""")
List<Evento> findConflitosTurma(Turma turma, LocalDateTime inicio, LocalDateTime fim);
```

---

## 🌐 Formato de Período

O endpoint de calendário aceita período no formato:
```
{dataInicio}/{dataFim}
```

Exemplo:
```
2025-10-01T00:00:00/2025-10-31T23:59:59
```

O sistema faz **URL decoding automático** para lidar com encodings múltiplos (`%252F` → `%2F` → `/`).

---

## 🎁 Features Extras

### 1. Status de Eventos
- ✅ **AGENDADO** - Evento criado, ainda não confirmado
- ✅ **CONFIRMADO** - Evento confirmado e validado
- ✅ **CANCELADO** - Evento cancelado

### 2. Tipos de Eventos
- ✅ **AULA** - Aula regular
- ✅ **PROVA** - Prova ou avaliação
- ✅ **SEMINARIO** - Seminário ou apresentação
- ✅ **OUTROS** - Outros tipos de eventos

### 3. Eventos Recorrentes
Suporte para eventos que se repetem (campo `recorrente` na entidade Evento).

---

## 📊 Cobertura de Testes

- ✅ **Criar evento sem conflito** - Status 201, evento confirmado
- ✅ **Criar evento com conflito** - Status 409, sugestões retornadas
- ✅ **Buscar evento por ID** - Status 200, dados corretos
- ✅ **Calendário professor** - Lista ordenada por data de início

---

## 🚀 Pronto para Merge

A branch US-03 está **completamente funcional** e pronta para merge na develop:
- ✅ Funcionalidades implementadas
- ✅ Documentação Swagger completa
- ✅ Testes (4/4) passando
- ✅ Tratamento de erros robusto
- ✅ Sistema de sugestões inteligente

**Branch:** US-03-Create/select-events-for-classroom/lab  
**Data de Conclusão:** 2025  
**Status:** ✅ Pronto para merge em develop

