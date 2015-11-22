Pod::Spec.new do |s|
  s.name         = "react-native-webview-js-context"
  s.version      = "0.2.0"
  s.summary      = "Utilize the JavaScript VM running inside an iOS UIWebView to exploit libraries targeting the DOM (e.g. Google Charts)"

  s.homepage     = "https://github.com/shayne/react-native-webview-js-context"

  s.license      = "MIT"
  s.platform     = :ios, "8.0"

  s.source       = { :git => "https://github.com/shayne/react-native-webview-js-context" }

  s.source_files  = "ios/*.{h,m}"

  s.dependency 'React'
end
