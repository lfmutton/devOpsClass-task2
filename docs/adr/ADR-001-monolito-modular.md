# ADR-001 — Leaderboard e gamificação no monólito modular

- **Status:** Aceita
- **Data:** 2026-11-04
- **Contexto do documento:** [README-ARQUITETURA.md](../../README-ARQUITETURA.md)

## Contexto

A Usuario-API (AC1) é um monólito Spring Boot organizado **por camada técnica** (`controller/`, `service/`, `repository/`, `domain/`, `dto/`). As novas funcionalidades, **leaderboard** e **gamificação dos estudos**, trazem três requisitos que definem a arquitetura (ASRs):

| ASR | Exigência |
|---|---|
| RNF 1.2 — Escalabilidade | O leaderboard deve suportar até 100 mil alunos sem degradar a exibição do ranking. |
| RNF 2.2 — Confiabilidade | Pontuações e conquistas não podem ser perdidas, duplicadas nem alteradas indevidamente. |
| RNF 2.3 — Manutenibilidade | As regras de gamificação e do leaderboard devem ficar isoladas em módulos próprios. |

Na estrutura em camadas, as classes de capacidades diferentes ficam misturadas nas mesmas pastas e se acessam livremente. Na AC1, por exemplo, `UsuarioService` e `MensalidadeService` dependiam um do outro, e `MatriculaService` usava diretamente os repositories de usuário, curso e assinatura.

## Decisão

Adotar o **monólito modular**: um único deploy e um único banco PostgreSQL, com o código dividido em **módulos por capacidade de negócio**. As regras são:

1. Cada módulo é um pacote direto de `com.exemplo.usuario` (`usuario`, `curso`, `matricula`, `assinatura`, `mensalidade`, `shared`, e futuramente `ranking`/`gamificacao`).
2. A raiz do pacote é a **API pública** do módulo. O subpacote `internal/` (controller, domain, dto, repository, service) é **privado**.
3. Um módulo fala com outro **só pela API pública** ou por **eventos**, nunca pela entidade ou pelo repository do outro.
4. Os módulos se referenciam **por ID** (`usuarioId`, `cursoId`), sem `@OneToOne`/`@ManyToOne` entre módulos.
5. Não pode haver **ciclos**. Quando duas capacidades precisam uma da outra, a dependência é invertida com uma porta (interface) e um evento. Exemplo: `usuario` define `MensalidadeConsulta` e publica `UsuarioCriadoEvent`, e `mensalidade` implementa a porta e escuta o evento.
6. As regras são **verificadas automaticamente** pelo Spring Modulith (`ModularidadeTest`), que roda em todo `mvn test`.

## Alternativas consideradas

| Alternativa | Vantagens | Desvantagens |
|---|---|---|
| **Monólito modular** (escolhida) | Uma transação garante o RNF 2.2; módulos isolados atendem o RNF 2.3; reaproveita o código da AC1; a fronteira prepara uma extração futura. | Exige disciplina (mitigada pelo teste automático); para o RNF 1.2 é preciso escalar a aplicação inteira. |
| Monólito em camadas (legado) | Já existe e é simples. | O leaderboard fica acoplado às demais camadas e não pode ser extraído separadamente; as regras ficam espalhadas por várias pastas (contra o RNF 2.3). |
| Microsserviço de ranking | Escala o leaderboard de forma independente (RNF 1.2). | Dados em dois serviços dificultam o RNF 2.2 (consistência eventual); mais infraestrutura, rede e observabilidade; custo alto para um time pequeno. |

## Trade-offs e consequências

**Ganhamos:**
- Pontuação e conclusão de curso na **mesma transação**: sem perda nem duplicação de pontos (RNF 2.2).
- **Fronteiras explícitas e testadas**: um acoplamento indevido quebra o build em vez de passar despercebido (RNF 2.3).
- **Um único deploy e um único banco**: menos infraestrutura que microsserviços.
- **Caminho de saída aberto**: um módulo com API própria e sem relação JPA com os outros pode virar serviço depois, se as medições justificarem.

**Pagamos:**
- **Escala no conjunto**: para dar conta do ranking (RNF 1.2) escalamos a aplicação inteira. A mitigação é uma consulta paginada e indexada e, se preciso, mais instâncias atrás de um balanceador.
- **Banco compartilhado**: os módulos não fazem join entre si pelo JPA. Quem precisa de dados de outro módulo pede pela API pública, o que pode gerar mais consultas.
- **Mais classes de "cola"**: portas, adapters, eventos e mappers.
- **Curva de aprendizado** das regras de visibilidade (`internal`, `@NamedInterface`).

## Quando revisitar esta decisão

Extrair o ranking como serviço só se as medições (ver a conclusão no README) mostrarem que:
- ele precisa escalar num ritmo diferente do resto; ou
- muda com frequência muito diferente dos outros módulos; ou
- tolera consistência eventual na pontuação.
