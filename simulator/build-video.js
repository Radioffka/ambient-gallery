const { execFileSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const reviewDir = path.join(__dirname, '..', 'docs', 'google-review');
const submissionDir = path.join(reviewDir, 'submission');
const concatListPath = path.join(reviewDir, 'concat_list.txt');
const outputVideo = path.join(reviewDir, 'demo-walkthrough.mp4');
const submissionVideo = path.join(submissionDir, 'demo-walkthrough.mp4');
const ffmpegBin = process.env.FFMPEG_PATH || 'ffmpeg';

if (!fs.existsSync(submissionDir)) {
  fs.mkdirSync(submissionDir, { recursive: true });
}

const files = [
  '01-welcome-connect.png',
  '02-permission-explanation.png',
  '03-device-name.png',
  '04-oauth-qr.png',
  '05-media-source-setup.png',
  '06-connected-account.png',
  '07-photo-ambient.png',
  '08-video-ambient.png',
  '09-settings.png',
  '10-disconnect.png'
];

for (const file of files) {
  const filePath = path.join(reviewDir, file);
  if (!fs.existsSync(filePath)) {
    throw new Error(`Missing screenshot: ${filePath}. Run npm run capture first.`);
  }
}

let concatContent = '';
for (const file of files) {
  const filePath = path.join(reviewDir, file).replace(/\\/g, '/');
  concatContent += `file '${filePath}'\nduration 3.0\n`;
}
const lastPath = path.join(reviewDir, files[files.length - 1]).replace(/\\/g, '/');
concatContent += `file '${lastPath}'\n`;

fs.writeFileSync(concatListPath, concatContent, 'utf8');

console.log(`Building demo walkthrough video with ffmpeg (${ffmpegBin})...`);
try {
  execFileSync(ffmpegBin, [
    '-f', 'concat',
    '-safe', '0',
    '-i', concatListPath,
    '-vf', 'fps=30,format=yuv420p',
    '-c:v', 'libx264',
    '-preset', 'fast',
    '-y', outputVideo
  ], { stdio: 'inherit' });
} finally {
  if (fs.existsSync(concatListPath)) {
    fs.unlinkSync(concatListPath);
  }
}

fs.copyFileSync(outputVideo, submissionVideo);
console.log(`Generated: ${outputVideo}`);
console.log(`Copied: ${submissionVideo}`);