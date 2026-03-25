export interface HashCredentials {
  cookies?: string;
  token?: string;
  refresh_token?: string;
}

/**
 * Check if the current URL has credentials in the hash
 * (from browser extension redirect).
 * Returns credentials if found, null otherwise.
 * Clears the hash whenever auth-related params are present.
 */
export function checkHashForCredentials(): HashCredentials | null {
  const hash = window.location.hash;
  if (!hash || hash.length < 2) return null;

  const params = new URLSearchParams(hash.substring(1));
  const cookies = params.get('cookies') || undefined;
  const token = params.get('token') || undefined;
  const refresh_token = params.get('refresh_token') || undefined;
  const hasAuthParams = !!cookies || !!token || !!refresh_token || params.has('error');

  if (!hasAuthParams) return null;

  // Clear the hash so credentials aren't visible in the URL
  history.replaceState(null, '', window.location.pathname + window.location.search);

  if (!cookies && !token && !refresh_token) return null;

  return { cookies, token, refresh_token };
}
