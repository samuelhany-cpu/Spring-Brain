import { chromium } from 'playwright'
import { spawn } from 'node:child_process'
import { mkdir, rm } from 'node:fs/promises'
import path from 'node:path'

const viewerDir = process.cwd()
const repoRoot = path.resolve(viewerDir, '..')
const outputDir = path.resolve(repoRoot, 'docs', 'assets')
const videoDir = path.resolve(outputDir, 'playwright-video')
const webmPath = path.resolve(outputDir, 'viewer-demo.webm')
const gifPath = path.resolve(outputDir, 'viewer-demo.gif')
const palettePath = path.resolve(outputDir, 'viewer-demo-palette.png')
const jarPath = path.resolve(repoRoot, 'spring-brain-cli', 'target', 'spring-brain-cli-0.1.0.jar')
const samplePath = path.resolve(repoRoot, 'spring-brain-samples', 'clean-crud-app')
const port = process.env.SPRING_BRAIN_DEMO_PORT ?? '3000'
const baseUrl = `http://localhost:${port}`

async function waitForServer() {
  const deadline = Date.now() + 30_000
  while (Date.now() < deadline) {
    try {
      const response = await fetch(`${baseUrl}/api/graph`)
      if (response.ok) return
    } catch {
      // Server is still starting.
    }
    await new Promise((resolve) => setTimeout(resolve, 500))
  }
  throw new Error(`Timed out waiting for ${baseUrl}`)
}

async function run(command, args) {
  await new Promise((resolve, reject) => {
    const child = spawn(command, args, {
      cwd: repoRoot,
      stdio: 'inherit',
      windowsHide: true,
    })
    child.on('error', reject)
    child.on('exit', (code) => {
      if (code === 0) resolve()
      else reject(new Error(`${command} exited with code ${code}`))
    })
  })
}

await mkdir(outputDir, { recursive: true })
await rm(videoDir, { recursive: true, force: true })

const server = spawn(
  'java',
  ['-jar', jarPath, 'serve', '--path', samplePath, '--port', port],
  {
    cwd: viewerDir,
    stdio: ['ignore', 'pipe', 'pipe'],
    windowsHide: true,
  },
)

try {
  await waitForServer()

  const browser = await chromium.launch()
  const context = await browser.newContext({
    viewport: { width: 1280, height: 720 },
    recordVideo: {
      dir: videoDir,
      size: { width: 1280, height: 720 },
    },
  })
  const page = await context.newPage()

  await page.goto(baseUrl)
  await page.getByText(/\d+ nodes . \d+ edges/).waitFor({ timeout: 10_000 })
  await page.waitForSelector('.react-flow__node', { timeout: 10_000 })
  await page.waitForTimeout(900)

  await page.locator('input').first().fill('User')
  await page.waitForTimeout(1_000)

  await page.locator('.react-flow__node').first().click()
  await page.getByText('Selected Node').waitFor({ timeout: 5_000 })
  await page.waitForTimeout(1_200)

  await page.getByRole('button', { name: 'Services', exact: true }).click()
  await page.waitForTimeout(800)
  await page.getByRole('button', { name: 'All', exact: true }).first().click()
  await page.waitForTimeout(1_000)

  const video = page.video()
  if (!video) {
    throw new Error('Playwright did not create a video recording')
  }

  await page.close()
  await video.saveAs(webmPath)
  await context.close()
  await browser.close()

  await run('ffmpeg', [
    '-y',
    '-i',
    webmPath,
    '-vf',
    'fps=10,scale=960:-1:flags=lanczos,palettegen',
    '-frames:v',
    '1',
    '-update',
    '1',
    palettePath,
  ])
  await run('ffmpeg', [
    '-y',
    '-i',
    webmPath,
    '-i',
    palettePath,
    '-filter_complex',
    'fps=10,scale=960:-1:flags=lanczos[x];[x][1:v]paletteuse=dither=bayer:bayer_scale=5',
    gifPath,
  ])

  await rm(palettePath, { force: true })
  await rm(videoDir, { recursive: true, force: true })

  console.log(`Recorded ${path.relative(repoRoot, webmPath)}`)
  console.log(`Generated ${path.relative(repoRoot, gifPath)}`)
} finally {
  server.kill()
}
