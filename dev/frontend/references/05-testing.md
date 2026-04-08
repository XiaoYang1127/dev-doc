# 测试规范

> 通用测试原则参考 `~/.kiro/steering/testing-standards.md`

## 单元测试（Vitest）

```typescript
import { describe, it, expect, beforeEach } from "vitest";
import { mount } from "@vue/test-utils";
import UserCard from "@/components/UserCard.vue";

describe("UserCard", () => {
  it("renders user name correctly", () => {
    const wrapper = mount(UserCard, {
      props: { user: mockUser },
    });
    expect(wrapper.text()).toContain("test");
  });

  it("emits update event when button clicked", async () => {
    const wrapper = mount(UserCard, {
      props: { user: mockUser },
    });
    await wrapper.find("button").trigger("click");
    expect(wrapper.emitted("update")).toBeTruthy();
  });
});
```

## Composables 测试

```typescript
import { describe, it, expect, vi } from "vitest";
import { useRequest } from "@/composables/useRequest";

describe("useRequest", () => {
  it("executes request successfully", async () => {
    const mockData = { id: 1 };
    const requestFn = vi.fn().mockResolvedValue(mockData);
    const { data, execute } = useRequest(requestFn);

    await execute();
    expect(data.value).toEqual(mockData);
  });
});
```

## E2E 测试（Playwright）

```typescript
import { test, expect } from "@playwright/test";

test.describe("Login Flow", () => {
  test("should login successfully with valid credentials", async ({ page }) => {
    await page.goto("/login");
    await page.fill('input[name="username"]', "test");
    await page.fill('input[name="password"]', "Test123456");
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL("/home");
  });
});
```

## 测试命名

- 格式：`should_[预期行为]_when_[条件]`
- 单元测试：每个 describe 对应一个组件或函数
- E2E 测试：按用户流程组织
