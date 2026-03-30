import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.communauto.tools',
  appName: 'Communauto Tools',
  webDir: '../dist',
  plugins: {
    CapacitorHttp: {
      enabled: true,
    },
    CapacitorCookies: {
      enabled: true,
    },
  },
  server: {
    androidScheme: 'https',
    allowNavigation: [
      'quebec.client.reservauto.net',
      '*.reservauto.net',
      'foidentityprovider.reservauto.net',
    ],
  },
};

export default config;
