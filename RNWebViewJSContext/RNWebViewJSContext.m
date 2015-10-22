//
//  RNWebViewJSContext.m
//  RNWebViewJSContext
//
//  Created by Shayne Sweeney on 10/21/15.
//  Copyright © 2015 Shayne Sweeney. All rights reserved.
//

@import UIKit;
@import JavaScriptCore;

#import "RNWebViewJSContext.h"

#pragma mark - UIWebView (JSContext)

@interface UIWebView (JSContext)

- (JSContext *)JSContext;

@end


@implementation UIWebView (JSContext)

- (JSContext *)JSContext {
    return [self valueForKeyPath:@"documentView.webView.mainFrame.javaScriptContext"];
}

@end

#pragma mark -


@interface RNWebViewJSContext () <UIWebViewDelegate>

@property (nonatomic, strong) NSMutableDictionary *webViews;

@end

@implementation RNWebViewJSContext

RCT_EXPORT_MODULE()

- (instancetype)init {
    if (self = [super init]) {
        self.webViews = [NSMutableDictionary dictionary];
    }
    return self;
}


RCT_EXPORT_METHOD(destroy:(nonnull NSNumber *)contextID)
{
    NSLog(@"WebView being destroyed: %p", self.webViews[contextID]);
    NSLog(@"Context being destroyed: %p", [self.webViews[contextID] JSContext]);
    [self.webViews removeObjectForKey:contextID];
}


RCT_EXPORT_METHOD(loadHTML:(NSString *)htmlString resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    
    NSUInteger hash = [htmlString hash];
    
    __weak RNWebViewJSContext *weakSelf = self;
    dispatch_async(dispatch_get_main_queue(), ^{
        __strong RNWebViewJSContext *strongSelf = weakSelf;
        UIWebView *webView = self.webViews[@(hash)];
        if (!webView) {
            webView = [[UIWebView alloc] initWithFrame:CGRectZero];
            strongSelf.webViews[@(hash)] = webView;
        }
        
        JSContext *context = [webView JSContext];
        
        RCTPromiseResolveBlock resolveWrapper = ^(id obj) {
            resolve(@(hash));
        };
        
        [self generateCallbacksInContext:context resolver:resolveWrapper rejecter:reject UUIDSuffix:false];
        
        __weak UIWebView *weakWebView = webView;
        dispatch_async(dispatch_get_main_queue(), ^{
            __strong UIWebView *strongWebView = weakWebView;
            [strongWebView loadHTMLString:htmlString baseURL:nil];
        });
    });
}


RCT_REMAP_METHOD(evaluateScript, evaluateScriptInContext:(nonnull NSNumber *)contextID script:(NSString *)script resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    UIWebView *webView = self.webViews[contextID];
    NSAssert(webView, @"WebView was nil!");
    JSContext *context = [webView JSContext];
    
    NSDictionary *callbacks = [self generateCallbacksInContext:context resolver:resolve rejecter:reject UUIDSuffix:true];
    
    NSString *jsWrapper = [NSString stringWithFormat:@"\
                           setTimeout(function(){ \
                           var resolve = %@, reject = %@; \
                           try { \
                           %@ \
                           } catch (e) { \
                           reject(e); \
                           } \
                           }, 0)", callbacks[@"resolveName"], callbacks[@"rejectName"], script];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [context evaluateScript:jsWrapper];
    });
    
}


- (NSDictionary *)generateCallbacksInContext:(JSContext *)context resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject UUIDSuffix:(BOOL)withSuffix {
    NSString *resolveName = @"resolve";
    NSString *rejectName = @"reject";
    
    if (withSuffix) {
        NSString *uniquePrefix = [[NSUUID UUID].UUIDString substringToIndex:7];
        resolveName = [NSString stringWithFormat:@"resolve_%@", uniquePrefix];
        rejectName = [NSString stringWithFormat:@"reject_%@", uniquePrefix];
    }
    
    context[resolveName] = ^(JSValue *val) {
        resolve([val toString]);
    };
    
    context[rejectName] = ^(JSValue *val) {
        NSDictionary *userInfo = @{ NSLocalizedDescriptionKey: [val toString] };
        NSError *error = [NSError errorWithDomain:@"JSWebViewManagerErrorDomain"
                                             code:-57
                                         userInfo:userInfo];
        
        reject(error);
    };
    
    return @{@"resolveName": resolveName, @"rejectName": rejectName};
}

@end