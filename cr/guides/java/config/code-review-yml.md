<!-- @format -->

# .code-review.yml 配置规范

`.code-review.yml` 是目标 Java 项目的轻量审核配置。它用于告诉审核工具：项目是什么架构、哪些路径要忽略、规则严重级别如何覆盖、验证命令如何运行。

这个配置不是必须的；不存在时，审核命令使用默认 Java 后端规则。

## 最小配置

```yaml
version: 1

project:
  language: java
  name: demo-service
  architecture:
    style: layered

rules:
  ignorePaths:
    - 'src/test/**'
    - 'target/**'
    - 'build/**'
    - 'generated/**'

report:
  formats:
    - markdown
    - json
```

## 推荐配置

```yaml
version: 1

project:
  language: java
  name: order-service
  architecture:
    style: layered
    layers:
      controller: '..controller..|..web..'
      api: '..api..|..facade..'
      service: '..service..|..application..'
      domain: '..domain..'
      repository: '..repository..|..mapper..|..dao..'
      infrastructure: '..infrastructure..|..integration..'
    forbiddenDependencies:
      - from: '..controller..'
        to: '..mapper..|..repository..|..dao..'
        rule: JAVA-LYR-001
      - from: '..api..'
        to: '..impl..|..entity..|..po..|..do..'
        rule: JAVA-BND-001

rules:
  enabledDimensions:
    - 架构设计
    - 服务边界
    - 服务耦合
    - 模块分层设计
    - 接口设计
    - 代码性能
    - 安全问题
  ignoreRules:
    - JAVA-NAME-001
  ignorePaths:
    - 'src/test/**'
    - 'target/**'
    - 'build/**'
    - 'generated/**'
  severityOverrides:
    JAVA-API-002: Medium

review:
  change:
    onlyNewOrAmplifiedIssues: true
    blockOnConfidence: High
    blockOnSeverity:
      - Critical
      - High
  scan:
    maxFindingsPerRule: 10
    maxTopRisks: 10

validation:
  compile:
    - './mvnw -q -DskipTests compile'
  test:
    - './mvnw test'
  staticChecks:
    - './mvnw -q checkstyle:check'

report:
  formats:
    - markdown
    - json
  outputDir: '.code-review/reports'
  includeIgnored: false
```

## 字段说明

| 字段                                         | 必填 | 说明                                                            |
| -------------------------------------------- | ---- | --------------------------------------------------------------- |
| `version`                                    | 是   | 配置版本，当前使用 `1`                                          |
| `project.language`                           | 建议 | 当前只支持 `java`                                               |
| `project.architecture.style`                 | 建议 | `layered`、`ddd`、`modular-monolith`、`microservice`、`unknown` |
| `project.architecture.layers`                | 否   | 分层包名匹配规则                                                |
| `project.architecture.forbiddenDependencies` | 否   | 禁止依赖规则                                                    |
| `rules.enabledDimensions`                    | 否   | 启用的审核维度                                                  |
| `rules.ignoreRules`                          | 否   | 项目级忽略规则                                                  |
| `rules.ignorePaths`                          | 否   | 忽略路径，使用 glob 风格                                        |
| `rules.severityOverrides`                    | 否   | 规则严重级别覆盖                                                |
| `review.change.onlyNewOrAmplifiedIssues`     | 否   | 变更审核是否只报告新增或放大问题                                |
| `review.change.blockOnConfidence`            | 否   | 阻塞合并所需最低置信度，建议 `High`                             |
| `review.change.blockOnSeverity`              | 否   | 可阻塞合并的严重级别                                            |
| `review.scan.maxFindingsPerRule`             | 否   | 全量巡检中每条规则最多展示的问题数                              |
| `validation.*`                               | 否   | 项目已有验证命令                                                |
| `report.formats`                             | 否   | 输出格式，建议同时包含 `markdown` 和 `json`                     |
| `report.outputDir`                           | 否   | 报告输出目录                                                    |
| `report.includeIgnored`                      | 否   | 是否在 JSON 中记录已忽略 finding                                |

## Ignore 优先级

忽略规则按以下顺序生效：

1. 行级 `review-ignore RULE_ID reason: ...`
2. `.code-review.yml` 的 `rules.ignorePaths`
3. `.code-review.yml` 的 `rules.ignoreRules`

Critical 安全问题即使被 ignore，也不能静默丢弃，必须进入 JSON 的 `confirmations` 或 `ignoredFindings`。

## 变更审核基线

变更审核默认只报告：

- diff 新增行上的问题；
- 变更方法或变更类中被本次修改触发的问题；
- 历史问题被本次修改扩大影响面的情况。

历史存量问题不应作为变更审核的阻塞项，应进入 `contextRisks`。
