/*
 * @flow
*/

var React = require('react-native');

var { Platform } = React;

var { RNWebViewJSContext } = require('NativeModules');

export default class WebViewJSContext {
  ctx: any;

  static async createWithHTML(html: string): Promise {
    if (Platform.OS === 'android') {
      return new Promise((resolve, reject) => {
        RNWebViewJSContext.loadHTML(html, ctx => {
          resolve(new WebViewJSContext(ctx));
        }, reject);
      });
    }
    const ctx = await RNWebViewJSContext.loadHTML(html);
    return new WebViewJSContext(ctx);
  }

  constructor(ctx: any) {
    this.ctx = ctx;
  }

  evaluateScript(script: string): Promise {
    console.log('HERE');
    if (Platform.OS === 'android') {
      return new Promise((resolve, reject) => {
        RNWebViewJSContext.evaluateScript(this.ctx, script, resolve, reject);
      });
    }
    return RNWebViewJSContext.evaluateScript(this.ctx, script);
  }

  destroy() {
    RNWebViewJSContext.destroy(this.ctx);
    this.ctx = null;
  }
}
