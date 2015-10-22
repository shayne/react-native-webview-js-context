/*
 * @flow
*/

var { RNWebViewJSContext } = require('NativeModules');

export default class WebViewJSContext {
  ctx: any;

  static async createWithHTML(html: string): Promise {
    const ctx = await RNWebViewJSContext.loadHTML(html);
    return new WebViewJSContext(ctx);
  }

  constructor(ctx: any) {
    this.ctx = ctx;
  }

  evaluateScript(script: string): Promise {
    return RNWebViewJSContext.evaluateScript(this.ctx, script);
  }

  destroy() {
    RNWebViewJSContext.destroy(this.ctx);
    this.ctx = null;
  }
}
