# react-native-webview-js-context [![npm version](https://badge.fury.io/js/react-native-webview-js-context.svg)](http://badge.fury.io/js/react-native-webview-js-context)

Interactive JavaScript between a UIWebView and React Native.

**Example:** Google Charts used to render a chart (base64 encoded image) in a `<Image />` component

<img width="375" src="http://shayne.github.io/react-native-webview-js-context/readme-files/google-charts-screenshot.png" />

```javascript
const GC_HTML = `
<html>
  <head>
    <script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>
    <script type=\"text/javascript\">
      google.load('visualization', '1.0', {'packages':['corechart', 'line']});
      google.setOnLoadCallback(resolve);
    </script>
  </head>
</html>`;

const CHART_JS = `
  var data = new google.visualization.DataTable();
  data.addColumn('string', 'Topping');
  data.addColumn('number', 'Slices');
  data.addRows([
    ['Mushrooms', 3],
    ['Onions', 1],
    ['Olives', 1],
    ['Zucchini', 1],
    ['Pepperoni', 2]
  ]);
  
  var options = {'title':'How Much Pizza I Ate Last Night',
                 'width':750,
                 'height':600};
                 
  var chart = new google.visualization.PieChart(document.createElement('div'));
  chart.draw(data, options);
  
  resolve(chart.getImageURI());
`;

import WebViewJSContext from 'react-native-webview-js-context';

class RNCharts extends React.Component {
  state: { imageUri: null };
  ctx: WebViewJSContext;

  componentWillMount() {
    WebViewJSContext.createWithHTML(GC_HTML)
      .then(context => {
        this.ctx = context;
        this.loadChart();
      });
  }
  
  render() {
    return this.state.imageUri ?
      <Image style={{ width: 375, height: 300 }} source={{ uri: this.state.imageUri }} />
      : <View />;
  }
  
  async loadChart() {
    var imageUri = await this.ctx.evaluateScript(CHART_JS);
    this.setState({ imageUri });
  }
}
```

## Getting started

1. `npm install react-native-webview-js-context@latest --save`
2. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
3. Go to `node_modules` ➜ `react-native-webview-js-context` and add `RNWebViewJSContext.xcodeproj`
4. In XCode, in the project navigator, select your project. Add `libRNWebViewJSContext.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
5. Run your project (`Cmd+R`)

