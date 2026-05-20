import fs from 'fs';
import path from 'path';

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
  '.ico': 'image/x-icon',
  '.mp4': 'video/mp4',
};

export default function Page() {
  return null;
}

export async function getServerSideProps({ params, res }) {
  const slugArr = params?.slug || [];
  const requested = slugArr.join('/');
  const baseDir = process.cwd();
  const candidates = [];

  if (!requested) {
    candidates.push('index.html', 'index.htm');
  } else {
    candidates.push(requested);
    if (!path.extname(requested)) {
      candidates.push(`${requested}.html`, `${requested}.htm`);
    }
  }

  for (const candidate of candidates) {
    const filePath = path.join(baseDir, candidate);
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
  }

  res.statusCode = 404;
  res.setHeader('Content-Type', 'text/plain; charset=utf-8');
  res.end('Not found');
  return { props: {} };
}
