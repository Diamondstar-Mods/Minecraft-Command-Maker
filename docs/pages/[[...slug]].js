const fs = require('fs');
const path = require('path');

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.htm': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.gif': 'image/gif',
  '.svg': 'image/svg+xml',
  '.mp4': 'video/mp4',
  '.webm': 'video/webm',
  '.ico': 'image/x-icon'
};

export default function Page() {
  // This route uses server-side props to stream static files; it never renders on the client.
  return null;
}

export async function getServerSideProps({ params, res }) {
  const slugArr = params?.slug || [];
  const requested = slugArr.join('/');
  const cwd = process.cwd();

  // Only look for HTML files in /docs (the current working directory when Vercel uses /docs as root)
  const candidates = [];
  if (!requested) {
    candidates.push('index.html');
    candidates.push('index.htm');
  } else {
    candidates.push(requested);
    if (!path.extname(requested)) {
      candidates.push(requested + '.html');
      candidates.push(requested + '.htm');
    }
  }

  for (const cand of candidates) {
    const filePath = path.join(cwd, cand);
    try {
      if (fs.existsSync(filePath) && fs.statSync(filePath).isFile()) {
        const ext = path.extname(filePath).toLowerCase();
        const mime = MIME[ext] || 'application/octet-stream';
        const data = fs.readFileSync(filePath);
        res.setHeader('Content-Type', mime);
        res.setHeader('Cache-Control', 'public, max-age=60');
        res.statusCode = 200;
        res.end(data);
        return { props: {} };
      }
    } catch (err) {
      continue;
    }
  }

  res.statusCode = 404;
  res.setHeader('Content-Type', 'text/plain; charset=utf-8');
  res.end('Not found');
  return { props: {} };
}
