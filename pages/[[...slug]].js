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
  '.mp4': 'video/mp4'
};

export default function Page() {
  // we return nothing — response handled in getServerSideProps
  return null;
}

export async function getServerSideProps({ params, res, req }) {
  const slugArr = params?.slug || [];
  const requested = slugArr.join('/');

  // Check several base directories so this works when Vercel serves from /docs
  const baseDirs = [
    path.join(process.cwd(), 'public'),
    path.join(process.cwd(), 'docs'),
    path.join(process.cwd(), 'public', 'docs'),
    path.join(process.cwd(), 'static')
  ];

  // Determine the candidate paths to try
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

  for (const baseDir of baseDirs) {
    for (const cand of candidates) {
      const filePath = path.join(baseDir, cand);
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
  }

  // Not found
  res.statusCode = 404;
  res.setHeader('Content-Type', 'text/plain; charset=utf-8');
  res.end('Not found');
  return { props: {} };
}
