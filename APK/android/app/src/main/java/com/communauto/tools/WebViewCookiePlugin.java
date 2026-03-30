package com.communauto.tools;

import android.webkit.CookieManager;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.JSObject;

@CapacitorPlugin(name = "WebViewCookie")
public class WebViewCookiePlugin extends Plugin {

    @PluginMethod()
    public void getCookies(PluginCall call) {
        String url = call.getString("url", "https://www.reservauto.net");
        CookieManager cookieManager = CookieManager.getInstance();
        String cookieString = cookieManager.getCookie(url);

        JSObject ret = new JSObject();
        ret.put("cookies", cookieString != null ? cookieString : "");
        call.resolve(ret);
    }
}
