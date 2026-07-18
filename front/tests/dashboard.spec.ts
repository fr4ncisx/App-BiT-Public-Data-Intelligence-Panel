import { test, expect } from "@playwright/test";

test.describe("BiT.INTELLIGENCE Dashboard E2E Tests", () => {
  test("should load the application and show the brand title", async ({ page }) => {
    // Navigate to the root of the app
    await page.goto("/");

    // We expect the brand header "BiT.INTELLIGENCE" to be visible
    // (It appears in the header on success and also in the fallback connection screen)
    const brandHeader = page.locator("h1");
    await expect(brandHeader).toContainText("BiT.INTELLIGENCE");
  });

  test("should display a responsive period selector when connected or a fallback status", async ({ page }) => {
    await page.goto("/");

    // Let's check if the page has loaded successfully or is in the fallback state
    const fallbackText = page.locator("text=La plataforma de monitoreo territorial");
    const periodSelect = page.locator("#period-select");

    // At least one of these states should be true depending on backend connectivity
    const isFallbackVisible = await fallbackText.isVisible();
    const isPeriodSelectVisible = await periodSelect.isVisible();

    expect(isFallbackVisible || isPeriodSelectVisible).toBe(true);
  });

  test("should check for privacy protection label in footer if dashboard loaded", async ({ page }) => {
    await page.goto("/");

    const periodSelect = page.locator("#period-select");
    if (await periodSelect.isVisible()) {
      const footer = page.locator("footer");
      await expect(footer).toContainText("Privacidad Asegurada");
    }
  });
});
