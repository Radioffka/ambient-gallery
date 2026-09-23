const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');

async function captureReviewScreenshots() {
  const outputDir = path.join(__dirname, '..', 'docs', 'google-review');
  const submissionDir = path.join(__dirname, '..', 'docs', 'google-review', 'submission');
  
  if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir, { recursive: true });
  }
  if (!fs.existsSync(submissionDir)) {
    fs.mkdirSync(submissionDir, { recursive: true });
  }

  const browser = await chromium.launch({
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  });

  const page = await browser.newPage({
    viewport: { width: 1920, height: 1080 },
    deviceScaleFactor: 1
  });

  const htmlPath = 'file://' + path.resolve(__dirname, 'index.html').replace(/\\/g, '/');
  console.log(`Loading simulation from: ${htmlPath}`);
  await page.goto(htmlPath, { waitUntil: 'networkidle' });

  // Wait for Google Fonts to load
  await page.evaluate(() => document.fonts.ready);
  await page.waitForTimeout(1000);

  const screens = [
    { id: 'screen-01', filename: '01-welcome-connect.png', desc: 'Welcome Screen with Google Photos Connect CTA' },
    { id: 'screen-02', filename: '02-permission-explanation.png', desc: 'Privacy & Permissions Explanation' },
    { id: 'screen-03', filename: '03-device-name.png', desc: 'Device Naming (displayName)' },
    { id: 'screen-04', filename: '04-oauth-qr.png', desc: 'OAuth 2.0 Limited-Input Device QR Code' },
    { id: 'screen-05', filename: '05-media-source-setup.png', desc: 'Waiting for Media Source Setup Polling' },
    { id: 'screen-06', filename: '06-connected-account.png', desc: 'Connected Account & Settings' },
    { id: 'screen-07', filename: '07-photo-ambient.png', desc: 'Ambient Slideshow (Photo Mode)' },
    { id: 'screen-08', filename: '08-video-ambient.png', desc: 'Ambient Slideshow (Video Mode)' },
    { id: 'screen-09', filename: '09-settings.png', desc: 'Settings & Media Sources Overview' },
    { id: 'screen-10', filename: '10-disconnect.png', desc: 'Disconnect Google Photos Confirmation Dialog' }
  ];

  for (const screen of screens) {
    console.log(`Capturing ${screen.filename} (${screen.desc})...`);
    await page.evaluate((sId) => {
      // Hide reviewer bar during evidence screenshot capture for pristine TV capture
      const reviewerBar = document.getElementById('reviewer-bar');
      if (reviewerBar) reviewerBar.style.display = 'none';

      window.showScreen(sId);
    }, screen.id);

    // Wait for animations and images to render
    await page.waitForTimeout(1200);

    const targetPath = path.join(outputDir, screen.filename);
    const submissionPath = path.join(submissionDir, screen.filename);
    
    await page.screenshot({
      path: targetPath,
      clip: { x: 0, y: 0, width: 1920, height: 1080 }
    });
    fs.copyFileSync(targetPath, submissionPath);
    console.log(`✓ Saved: ${targetPath} & submission/${screen.filename}`);
  }

  await browser.close();
  console.log('\nAll 10 Google review evidence screenshots successfully captured to docs/google-review/ and docs/google-review/submission/!');
}

captureReviewScreenshots().catch(err => {
  console.error('Error during screenshot capture:', err);
  process.exit(1);
});