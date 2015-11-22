package com.shaynesweeney.react_native_webview_js_context;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.SparseArray;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

interface JSCallback {
    void invoke(String response);
}

interface JSCallbacks {
    @JavascriptInterface
    void global_resolver();

    @JavascriptInterface
    void global_resolver(String response);

    @JavascriptInterface
    void global_rejecter(String response);

    @JavascriptInterface
    void global_rejecter();

    @JavascriptInterface
    void callback_resolver(String uuid, String response);

    @JavascriptInterface
    void callback_resolver(String uuid);

    @JavascriptInterface
    void callback_rejecter(String uuid, String response);

    @JavascriptInterface
    void callback_rejecter(String uuid);
}

public class RNWebViewJSContextModule extends ReactContextBaseJavaModule {
    @SuppressWarnings("unused")
    private static final String TAG = "RNWebViewJSContext";
    public static final String GLOBAL_REJECT_KEY = "global-reject";
    private final SparseArray<WebView> mWebViews;
    private final Map<String, JSCallback> mCallbacks;
    public static final String GLOBAL_RESOLVE_KEY = "global-resolve";

    public RNWebViewJSContextModule(ReactApplicationContext reactContext) {
        super(reactContext);

        mWebViews = new SparseArray<>();
        mCallbacks = new HashMap<>();
    }

    @Override
    public String getName() {
        return "RNWebViewJSContext";
    }

    @ReactMethod
    public void loadHTML(final String html, final Callback resolveCallback, final Callback rejectCallback) {
        final Integer contextID = html.hashCode();

        final ReactApplicationContext context = getReactApplicationContext();
        Handler mainHandler = new Handler(context.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {

                WebView webView = mWebViews.get(contextID);
                if (webView == null) {
                    webView = createWebView(contextID);
                }

                mCallbacks.put("global-resolve", new JSCallback() {
                    @Override
                    public void invoke(String response) {
                        resolveCallback.invoke(contextID);
                    }
                });

                mCallbacks.put("global-reject", new JSCallback() {
                    @Override
                    public void invoke(String response) {
                        rejectCallback.invoke();
                    }
                });

                webView.loadData(html, "text/html", "UTF-8");
//                webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
            }
        };
        mainHandler.post(myRunnable);
    }

    @ReactMethod
    public void evaluateScript(final Integer contextID, String script, final Callback resolveCallback, final Callback rejectCallback) {
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString().substring(0, 7);

        final String resolverName = "resolve_" + uuidString;
        final String rejecterName = "reject_" + uuidString;

        final String jsWrapper = String.format(
                "setTimeout(function(){"
                        + "var resolve = function(response) {"
                        + "    $RNWebViewJSContext.callback_resolver(\"%s\", response);"
                        + "},"
                        + "reject = function(uuid, response) {"
                        + "    $RNWebViewJSContext.callback_rejecter(\"%s\", response);"
                        + "};"
                        + "try {"
                        + "    %s"
                        + "} catch (e) {"
                        + "    reject(e);"
                        + "}"
                        + "}, 0)",
                resolverName, rejecterName, script
        );

        final ReactApplicationContext context = getReactApplicationContext();
        Handler mainHandler = new Handler(context.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                WebView webView = mWebViews.get(contextID);
                assert webView != null;

                mCallbacks.put(resolverName, new JSCallback() {
                    @Override
                    public void invoke(String response) {
                        resolveCallback.invoke(response);
                    }
                });

                mCallbacks.put(rejecterName, new JSCallback() {
                    @Override
                    public void invoke(String response) {
                        rejectCallback.invoke();
                    }
                });

                webView.evaluateJavascript(jsWrapper, null);
            }
        };
        mainHandler.post(myRunnable);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private WebView createWebView(Integer contextID) {

        WebView webView = new WebView(getReactApplicationContext());
        webView.getSettings().setJavaScriptEnabled(true);
        mWebViews.put(contextID, webView);

        webView.addJavascriptInterface(new JSCallbacks() {

            @JavascriptInterface
            public void global_resolver() {
                JSCallback resolver = mCallbacks.get(GLOBAL_RESOLVE_KEY);
                resolver.invoke(null);
            }

            @JavascriptInterface
            public void global_resolver(String response) {
                JSCallback resolver = mCallbacks.get(GLOBAL_RESOLVE_KEY);
                resolver.invoke(response);
            }

            @JavascriptInterface
            public void global_rejecter() {
                JSCallback rejecter = mCallbacks.get(GLOBAL_REJECT_KEY);
                rejecter.invoke(null);
            }

            @JavascriptInterface
            public void global_rejecter(String response) {
                JSCallback rejecter = mCallbacks.get(GLOBAL_REJECT_KEY);
                rejecter.invoke(response);
            }

            @JavascriptInterface
            public void callback_resolver(String callbackKey) {
                JSCallback resolver = mCallbacks.get(callbackKey);
                resolver.invoke(null);
            }

            @JavascriptInterface
            public void callback_resolver(String callbackKey, String response) {
                JSCallback resolver = mCallbacks.get(callbackKey);
                resolver.invoke(response);
            }

            @JavascriptInterface
            public void callback_rejecter(String callbackKey) {
                JSCallback rejecter = mCallbacks.get(callbackKey);
                rejecter.invoke(null);
            }

            @JavascriptInterface
            public void callback_rejecter(String callbackKey, String response) {
                JSCallback rejecter = mCallbacks.get(callbackKey);
                rejecter.invoke(response);
            }
        }, "$RNWebViewJSContext");


        webView.evaluateJavascript(
                "(function($w){" +
                        "$w.resolve = $RNWebViewJSContext.global_resolver;" +
                        "$w.reject = $RNWebViewJSContext.global_resolver;" +
                        "})(window);", null);


        return webView;
    }
}
