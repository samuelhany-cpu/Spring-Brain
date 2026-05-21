import { test, expect } from '@playwright/test'

test.use({ baseURL: 'http://localhost:3001' })

test('diagnostics badge shows warnings for broken app', async ({ page }) => {
  await page.goto('http://localhost:3001/')
  // Broken app has ControllerDirectRepository + ControllerWithoutService warnings
  await expect(page.getByText(/\d+ warning/i)).toBeVisible({ timeout: 10000 })
})

test('/api/diagnostics returns diagnostics for broken app', async ({ request }) => {
  const response = await request.get('http://localhost:3001/api/diagnostics')
  expect(response.status()).toBe(200)
  const json = await response.json()
  expect(json).toHaveProperty('diagnostics')
  expect((json.diagnostics as Array<unknown>).length).toBeGreaterThan(0)
})
