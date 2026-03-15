export interface Env {
  ALLOWED_ORIGIN: string;
  TARGET_HOST: string;
}

const CORS_HEADERS = {
  'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
  'Access-Control-Allow-Headers': 'Content-Type, Accept, Authorization, X-WCF-Cookie',
  'Access-Control-Max-Age': '86400',
};

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const origin = request.headers.get('Origin') ?? '';
    const allowedOrigins = env.ALLOWED_ORIGIN.split(',').map((o) => o.trim());
    const isAllowed = allowedOrigins.includes(origin) || allowedOrigins.includes('*');

    if (!isAllowed) {
      return new Response('Forbidden', { status: 403 });
    }

    const corsHeaders = {
      ...CORS_HEADERS,
      'Access-Control-Allow-Origin': origin,
    };

    // Handle preflight
    if (request.method === 'OPTIONS') {
      return new Response(null, { status: 204, headers: corsHeaders });
    }

    // Build target URL: worker receives requests like /WCF/...
    const url = new URL(request.url);
    const targetUrl = env.TARGET_HOST + url.pathname + url.search;

    // Forward headers, converting X-WCF-Cookie to Cookie
    const headers = new Headers();
    headers.set('Accept', request.headers.get('Accept') ?? 'application/json');
    headers.set('Content-Type', request.headers.get('Content-Type') ?? 'application/json');

    const wcfCookie = request.headers.get('X-WCF-Cookie');
    if (wcfCookie) {
      const cleaned = wcfCookie.replace(/^cookie:\s*/i, '').trim();
      if (cleaned) {
        headers.set('Cookie', cleaned);
      }
    }

    const response = await fetch(targetUrl, {
      method: request.method,
      headers,
      body: request.method !== 'GET' ? request.body : undefined,
    });

    // Return with CORS headers
    const respHeaders = new Headers(response.headers);
    for (const [key, value] of Object.entries(corsHeaders)) {
      respHeaders.set(key, value);
    }

    return new Response(response.body, {
      status: response.status,
      statusText: response.statusText,
      headers: respHeaders,
    });
  },
};
