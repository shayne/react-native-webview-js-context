# react-native-webview-js-context [![npm version](https://badge.fury.io/js/react-native-webview-js-context.svg)](http://badge.fury.io/js/react-native-webview-js-context)

Interactive JavaScript between a UIWebView and React Native.

**Example:** Google Charts used to render a chart (base64 encoded image) in a `<Image />` component

<img width="375" src="http://shayne.github.io/react-native-webview-js-context/readme-files/google-charts-screenshot.png?999" />

```javascript
const GC_HTML = `
  <html>
    <head>
      <script type="text/javascript" src="https://www.google.com/jsapi"></script>
      <script type="text/javascript">
        google.load('visualization', '1.0', {'packages':['corechart']});
        google.setOnLoadCallback(resolve); /* <--- resolve() is called by RNWebViewJSContext */
      </script>
    </head>
    <body><div id="chart_div"></div></body>
  </html>`;

const CHART_JS = `
  var data = new google.visualization.DataTable();
  data.addColumn('date', 'Day');
  data.addColumn('number', 'Weight');
  data.addColumn({ type: 'string', role: 'annotation' });
  data.addRows([
      [new Date(2015, 2, 1), 150, '150'],
      [new Date(2015, 2, 2), 152, null],
      [new Date(2015, 2, 3), 146, '146'],
      [new Date(2015, 2, 4), 150, null],
      [new Date(2015, 2, 5), 157, '157'],
      [new Date(2015, 2, 06), 147, null],
      [new Date(2015, 2, 07), 147.5, '147'],
  ]);

  var options = { enableInteractivity: false,
                  legend: {position: 'none'},
                  lineWidth: 3, width:750, height:420,
                  pointShape: 'circle', pointSize: 8,
                  chartArea: { left: 30, width: 690 }, areaOpacity: 0.07,
                  colors: ['#e14c4d'], backgroundColor: { 'fill': '#34343f' },
                  annotations: {
                    textStyle: { fontSize: 26, bold: true, color: '#bbbbbd', auroColor: '#3f3f3f' },
                  },
                  hAxis: {
                    format: 'MMM d',
                    textStyle: {color: '#bbbbbd', fontSize: 16,}, gridlines: { color: 'transparent' },
                  },
                  vAxis: { gridlines: { count: 3, color: '#3f414f' } },
                };

  var chart = new google.visualization.AreaChart(document.getElementById('chart_div'));
  chart.draw(data, options);

  resolve(chart.getImageURI()); /* <--- resolve() is called by RNWebViewJSContext */`;

import WebViewJSContext from 'react-native-webview-js-context';

class RNCharts {
  state: { imageUri: null };

  componentWillMount() {
    WebViewJSContext.createWithHTML(GC_HTML)
      .then(context => {
        this.ctx = context;
        this.loadChart();
      });
  }

  componentWillUnmount() {
    this.ctx && this.ctx.destroy();
  },

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

## Usage

First you need to install react-native-webview-js-context:

```javascript
npm install react-native-webview-js-context --save
```


## `iOS`

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-webview-js-context` ➜ `ios` and add `RNWebViewJSContext.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNWebViewJSContext.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)

### `Android`

* `android/settings.gradle`

```gradle
...
include ':react-native-webview-js-context'
project(':react-native-webview-js-context').projectDir = new File(settingsDir, '../node_modules/react-native-webview-js-context/android')
```
* `android/app/build.gradle`

```gradle
dependencies {
	...
	compile project(':react-native-webview-js-context')
}
```

* register module (in MainActivity.java)

```java
...

import com.shaynesweeney.react_native_webview_js_context.RNWebViewJSContextPackage; // <--- IMPORT

public class MainActivity extends Activity implements DefaultHardwareBackBtnHandler {
	...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReactRootView = new ReactRootView(this);

        mReactInstanceManager = ReactInstanceManager.builder()
                .setApplication(getApplication())
                .setBundleAssetName("index.android.bundle")
                .setJSMainModuleName("index.android")
                .addPackage(new MainReactPackage())
                .addPackage(new RNWebViewJSContextPackage()) // <- ADD HERE
                .setUseDeveloperSupport(BuildConfig.DEBUG)
                .setInitialLifecycleState(LifecycleState.RESUMED)
                .build();

        mReactRootView.startReactApplication(mReactInstanceManager, "YourProject", null);

        setContentView(mReactRootView);
    }
}
```

