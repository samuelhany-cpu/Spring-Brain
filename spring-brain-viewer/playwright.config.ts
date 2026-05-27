import { defineConfig, devices } from '@playwright/test'

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  retries: 1,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:3000',
    trace: 'on-first-retry',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: [
    {
      command: 'java -jar ../spring-brain-cli/target/spring-brain-cli-0.2.0.jar serve --path ../spring-brain-samples/clean-crud-app --port 3000',
      url: 'http://localhost:3000/api/graph',
      reuseExistingServer: !process.env.CI,
      timeout: 30000,
    },
    {
      command: 'java -jar ../spring-brain-cli/target/spring-brain-cli-0.2.0.jar serve --path ../spring-brain-samples/broken-controller-direct-repository-app --port 3001',
      url: 'http://localhost:3001/api/graph',
      reuseExistingServer: !process.env.CI,
      timeout: 30000,
    },
  ],
})
