import { test, expect } from '@playwright/test'

test.use({ baseURL: 'http://localhost:3000' })

test('page loads and shows spring-brain brand', async ({ page }) => {
  await page.goto('/')
  await expect(page.getByText('spring-brain')).toBeVisible()
})

test('toolbar renders filter chips', async ({ page }) => {
  await page.goto('/')
  // Wait for graph data to load (App shows "Loading…" until then)
  await expect(page.getByText(/\d+ nodes · \d+ edges/)).toBeVisible({ timeout: 10000 })
  await expect(page.getByRole('button', { name: 'All', exact: true }).first()).toBeVisible()
  await expect(page.getByRole('button', { name: 'Controllers', exact: true })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Services', exact: true })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Repos', exact: true })).toBeVisible()
})

test('graph canvas renders nodes', async ({ page }) => {
  await page.goto('/')
  await expect(page.locator('.react-flow__node').first()).toBeVisible({ timeout: 10000 })
})

test('stats badge shows node and edge counts', async ({ page }) => {
  await page.goto('/')
  await expect(page.getByText(/\d+ nodes · \d+ edges/)).toBeVisible()
})

test('search input is present and functional', async ({ page }) => {
  await page.goto('/')
  const search = page.getByPlaceholder('Search class, route…')
  await expect(search).toBeVisible()
  await search.fill('User')
  await expect(search).toHaveValue('User')
})

test('clicking a node shows detail panel info', async ({ page }) => {
  await page.goto('/')
  await page.waitForSelector('.react-flow__node', { timeout: 10000 })
  await page.locator('.react-flow__node').first().click()
  // DetailPanel renders a div with node label/qualifiedName text after click
  await expect(page.getByText('Selected Node')).toBeVisible({ timeout: 5000 })
})

test('/api/graph returns valid JSON', async ({ request }) => {
  const response = await request.get('http://localhost:3000/api/graph')
  expect(response.status()).toBe(200)
  const json = await response.json()
  expect(json).toHaveProperty('schemaVersion')
  expect(json).toHaveProperty('nodes')
  expect(json).toHaveProperty('edges')
})
