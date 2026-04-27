---
name: java-code-review
description: Use when reviewing Java backend code changes or scanning Java backend projects for architecture, service boundary, coupling, layering, API design, performance, security, and Alibaba Java coding standard risks. Use for local diffs, Git refs, PR/MR review preparation, full project scans, .code-review.yml setup, JSON review reports, and reusable Claude command templates.
---

# Java Code Review

Use this skill to review Java backend projects. It supports two modes:

- **Change review**: Decide whether a local diff, Git ref diff, PR, or MR should be approved.
- **Full scan**: Find systemic project risks and recommend remediation priorities.

This skill is for Java backend code, not for reviewing documentation repositories unless the target repository is itself a Java backend project.

## Mode Selection

Use **change review** when the user asks to review:

- local uncommitted changes
- a branch diff
- a PR/MR
- a specific Java backend change

Use **full scan** when the user asks to:

- scan a whole Java project
- find architecture or security debt
- assess project health
- produce a risk inventory

If ambiguous, default to change review when a git diff exists; otherwise ask for the target project/path.

## Required Context

Before making findings, inspect the target Java project:

- Build files: `pom.xml`, `build.gradle*`, `settings.gradle*`
- Source layout: `src/main/java`, `src/test/java`, `src/main/resources`
- Project guidance: `CLAUDE.md`, `README.md`, `CONTRIBUTING.md`
- Review config: `.code-review.yml`
- Local rule cards: `.code-review/rules/java/**/*.md`, `.claude/rules/java/**/*.md`

When a Java file changes, read the full class or config file, then inspect nearby layers: Controller/API/Facade, Service/Application, Domain, Repository/Mapper, DTO/Entity/Converter, related tests, Mapper XML, SQL, cache keys, MQ topics, and remote clients.

## Reference Loading

Load only the references needed:

- For rule format and ignore semantics: `references/rules/rule-format.md`
- For security issues: `references/rules/security.md`
- For performance issues: `references/rules/performance.md`
- For architecture and global design: `references/rules/architecture.md`
- For service boundaries: `references/rules/boundary.md`
- For coupling: `references/rules/coupling.md`
- For layering and transaction/AOP issues: `references/rules/layering.md`
- For HTTP/RPC/MQ contract design: `references/rules/api.md`
- For Alibaba Java coding standard checks: `references/rules/alibaba-java.md`
- For `.code-review.yml` design or interpretation: `references/config/code-review-yml.md`
- For structured output: `references/report/json-report.md`
- For Claude Code slash command templates: `assets/claude-commands/*.md`
- For generic agent CLI prompts: `assets/prompts/*.md`

## Review Rules

Every finding must include:

- rule ID
- severity: `Critical`, `High`, `Medium`, or `Low`
- confidence: `High`, `Medium`, or `Low`
- file and line
- dimension
- evidence
- impact
- recommendation

Only report issues with concrete code evidence. Put uncertain issues in “需要确认” / `confirmations`.

For change reviews, only block on issues that are new, modified, or clearly amplified by the current change. Historical issues belong in `contextRisks`.

Only `High` confidence `Critical` or `High` findings should block merge.

## Ignore Semantics

Respect line-level ignores:

```java
// review-ignore JAVA-SEC-001 reason: orderBy comes from a fixed enum whitelist
```

An ignore is valid only when it has a rule ID and a `reason:`. It applies only to the adjacent statement or nearest code block. Critical security findings must not disappear silently; move them to confirmations or ignored findings with the reason.

Also respect `.code-review.yml` ignore paths, ignored rules, severity overrides, validation commands, and report settings. Read `references/config/code-review-yml.md` when needed.

## Validation

Run only commands that are present and cost-appropriate. Prefer wrappers:

- Maven: `./mvnw -q -DskipTests compile`, `./mvnw test`
- Gradle: `./gradlew compileJava`, `./gradlew test`
- Existing static checks: Checkstyle, PMD, SpotBugs, Error Prone, ArchUnit, JaCoCo

Do not add tools or modify project config just to validate a review.

## Output

For review results, lead with findings ordered by severity. Keep summaries short.

For change review decisions:

- `APPROVE`: no blocking issue
- `APPROVE_WITH_COMMENTS`: only Medium/Low or non-blocking confidence
- `REQUEST_CHANGES`: High-confidence High issue or validation failure
- `BLOCK`: High-confidence Critical issue
- `COMMENT`: draft PR or informational review

For full scans, use `NOT_APPLICABLE` as the JSON decision and provide health status: `Good`, `Moderate Risk`, or `High Risk`.

When automation, CI, bot consumption, or report artifacts are requested, produce JSON following `references/report/json-report.md`.

## Portability

The skill directory is self-contained. To reuse it, copy the whole `java-code-review/` directory into an agent's skills directory if the agent supports Skills.

For agents that do not support Skills, use:

- `assets/prompts/java-change-review.md`
- `assets/prompts/java-full-scan.md`

For Claude Code projects, copy files from `assets/claude-commands/` into the target project's `.claude/commands/`.
