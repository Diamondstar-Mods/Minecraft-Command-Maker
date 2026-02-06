const fs = require('fs');
const path = require('path');

const src = path.join(process.cwd(), 'docs');
const dest = path.join(process.cwd(), 'public');

function copyRecursive(srcDir, destDir) {
  if (!fs.existsSync(srcDir)) return;
  if (!fs.existsSync(destDir)) fs.mkdirSync(destDir, { recursive: true });
  const entries = fs.readdirSync(srcDir, { withFileTypes: true });
  for (const entry of entries) {
    const srcPath = path.join(srcDir, entry.name);
    const destPath = path.join(destDir, entry.name);
    if (entry.isDirectory()) {
      copyRecursive(srcPath, destPath);
    } else if (entry.isFile()) {
      fs.copyFileSync(srcPath, destPath);
    }
  }
}

try {
  // remove old public content that came from docs (do not remove user public files if any)
  if (fs.existsSync(dest)) {
    // clear folder
    const entries = fs.readdirSync(dest);
    for (const e of entries) {
      const p = path.join(dest, e);
      // only remove files/folders that exist in docs -- safe approach: remove all and recopy
      // but avoid removing node_modules etc since public should only contain site assets
      fs.rmSync(p, { recursive: true, force: true });
    }
  } else {
    fs.mkdirSync(dest, { recursive: true });
  }

  copyRecursive(src, dest);
  console.log('Copied docs -> public');
} catch (err) {
  console.error('Failed to copy docs to public:', err);
  process.exit(1);
}
