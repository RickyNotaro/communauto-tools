export interface OAuthTokens {
  access_token: string;
  refresh_token?: string;
  id_token?: string;
  expires_in?: number;
  token_type?: string;
}

export interface HashCredentials {
  cookies?: string;
  token?: string;
  refresh_token?: string;
}

/**
 * Check if the current URL has credentials in the hash
 * (from browser extension redirect or bookmarklet).
 * Returns credentials if found, null otherwise. Clears the hash after reading.
 */
export function checkHashForCredentials(): HashCredentials | null {
  const hash = window.location.hash;
  if (!hash || hash.length < 2) return null;

  const params = new URLSearchParams(hash.substring(1));
  const cookies = params.get('cookies');
  const token = params.get('token');

  if (!cookies && !token) return null;

  // Clear the hash so credentials aren't visible in the URL
  history.replaceState(null, '', window.location.pathname + window.location.search);

  return {
    cookies: cookies || undefined,
    token: token || undefined,
    refresh_token: params.get('refresh_token') || undefined,
  };
}
