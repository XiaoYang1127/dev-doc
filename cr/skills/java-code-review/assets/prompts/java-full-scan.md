# Java Backend Full Scan Prompt

Use this prompt in agent CLIs that do not support Skills or Claude slash commands.

Run a full Java backend project scan. The goal is not to decide whether a PR can merge; the goal is to identify systemic project risks and remediation priorities.

Scope:

- If a directory/module/package is provided, scan that scope.
- Otherwise scan the current Java backend project.

Process:

1. Build a project inventory from `pom.xml`, `build.gradle*`, `settings.gradle*`, `src/main/java`, `src/test/java`, and `src/main/resources`.
2. Identify the architecture style: layered, DDD, modular monolith, microservice, or unknown.
3. Read `.code-review.yml` if present.
4. Read local rule cards from `.code-review/rules/java/**/*.md` or `.claude/rules/java/**/*.md` if present.
5. Scan architecture design, service boundaries, coupling, layering, API design, performance, security, and Alibaba Java coding standards.
6. Group repeated issues by rule and module; do not dump hundreds of duplicate findings.
7. Produce top risks and prioritized remediation steps.

Each finding must include:

- rule ID
- severity: Critical, High, Medium, Low
- confidence: High, Medium, Low
- file and line
- dimension
- evidence
- impact
- recommendation

Output:

- Markdown full scan report with health status: Good, Moderate Risk, or High Risk
- JSON report with decision `NOT_APPLICABLE` when automation or bot consumption is needed
