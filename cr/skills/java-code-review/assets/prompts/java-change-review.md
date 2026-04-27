# Java Backend Change Review Prompt

Use this prompt in agent CLIs that do not support Skills or Claude slash commands.

Review the current Java backend change. Treat the repository root as the target Java project.

Scope:

- If a PR/MR, review the PR/MR diff.
- If a Git ref is provided, review `<ref>...HEAD`.
- Otherwise review local uncommitted changes.

Process:

1. Confirm the target is a Java backend project by checking `pom.xml`, `build.gradle*`, `settings.gradle*`, `src/main/java`, or `src/test/java`.
2. Read `.code-review.yml` if present.
3. Read changed Java files in full, not only diff hunks.
4. Inspect nearby Controller/API/Facade, Service/Application, Domain, Repository/Mapper, DTO/Entity/Converter, Mapper XML, configuration, and tests.
5. Apply Java backend review dimensions: architecture design, service boundary, coupling, layering, API design, performance, security, and Alibaba Java coding standards.
6. Report only issues with concrete code evidence.
7. Only block merge for High-confidence Critical or High issues that are new, modified, or clearly amplified by this change.

Each finding must include:

- rule ID
- severity: Critical, High, Medium, Low
- confidence: High, Medium, Low
- file and line
- dimension
- evidence
- impact
- recommendation

Respect:

- `// review-ignore RULE_ID reason: ...`
- `.code-review.yml` ignore paths, ignored rules, severity overrides, validation commands, and report settings

Output:

- Markdown review report for humans
- JSON report matching the Java code review JSON schema when automation or bot consumption is needed
