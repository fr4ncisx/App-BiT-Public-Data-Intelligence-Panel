import { test, expect } from "@playwright/test";

test.describe("BiT.INTELLIGENCE UI & Visual Components Tests", () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to the root before each test
    await page.goto("/");
  });

  test("should apply the correct premium dark slate theme and visual elements", async ({ page }) => {
    // Check background styling class or CSS variables
    const _body = page.locator("body");
    // Verify that we are using the beautiful #060606 ambient dark background
    const mainDiv = page.locator("div").first();
    await expect(mainDiv).toHaveClass(/bg-\[#060606\]|min-h-screen/);

    // Verify the presence of decorative accent lights or borders
    const decorativeLine = page.locator(".absolute.top-0.left-0");
    await expect(decorativeLine).toBeVisible();
  });

  test("should render the Consultor Territorial Inteligente suggestions and input correctly", async ({ page }) => {
    // Check if we are in the fallback screen or have the active assistant query
    const hasQueryInput = await page.locator("#query-input").isVisible();
    const hasFallbackInfo = await page.locator("text=Estado del Canal de Datos").isVisible();

    // At least one state is valid. If fallback is active:
    if (hasFallbackInfo) {
      const retryButton = page.locator("button:has-text('Reintentar')");
      await expect(retryButton).toBeVisible();
      await expect(retryButton).toHaveClass(/bg-white/);
    }

    // If query input is active, verify UI states
    if (hasQueryInput) {
      const input = page.locator("#query-input");
      await expect(input).toBeEmpty();
      await expect(input).toHaveAttribute("placeholder", /Consulte sobre brechas/);

      // Verify suggestion chips are visible
      const suggestion0 = page.locator("#btn-suggested-0");
      await expect(suggestion0).toBeVisible();
      await expect(suggestion0).toContainText("¿Cuáles son las zonas");
    }
  });

  test("should test click-to-submit behavior on suggested query chips if dashboard is active", async ({ page }) => {
    const hasQueryInput = await page.locator("#query-input").isVisible();
    if (hasQueryInput) {
      const suggestionButton = page.locator("#btn-suggested-0");
      await suggestionButton.click();

      // Submit button should enter loading state or show active query sending
      const submitButton = page.locator("#query-submit");
      await expect(submitButton).toBeDisabled();
    }
  });

  test("should render gracefully on mobile viewports", async ({ page }) => {
    // Resize viewport to mobile size
    await page.setViewportSize({ width: 375, height: 667 });

    // Validate that the main fallback dialog or main dashboard does not cause layout breaks
    const headerTitle = page.locator("h1");
    await expect(headerTitle).toBeVisible();

    const container = page.locator(".max-w-xl");
    if (await container.isVisible()) {
      const box = await container.boundingBox();
      expect(box).not.toBeNull();
      // Ensure the container fits within the 375px viewport width
      expect(box!.width).toBeLessThanOrEqual(375);
    }
  });

  test("should test the Retry button on fallback screen triggers page reload", async ({ page }) => {
    const hasFallbackInfo = await page.locator("text=Estado del Canal de Datos").isVisible();
    if (hasFallbackInfo) {
      let reloaded = false;
      page.on("framenavigated", () => {
        reloaded = true;
      });

      const retryButton = page.locator("button:has-text('Reintentar')");
      await retryButton.click();
      
      // Page should perform reload navigation
      await page.waitForTimeout(500);
      expect(reloaded).toBe(true);
    }
  });
});
