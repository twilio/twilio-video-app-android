//
//  TSCEndpointTests.m
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 1/16/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#import "TSCBaseSDKTest.h"
#import "TSCEndpoint.h"
#import "TSCoreError.h"
#import "TSCEndpointObserver.h"


@interface TSCEndpointTests : TSCBaseSDKTest
@property (nonatomic, assign) TSCEndpointObjectRef endpoint;
@property (nonatomic, assign) TSCEndpointObserverObjectRef endpointObserver;
@property (nonatomic, strong) NSString* endpointName;
@property (nonatomic, strong) XCTestExpectation* expectation;
@property (nonatomic, strong) NSCondition* endpointCondition;
@property int lastError;
@end

@implementation TSCEndpointTests

class TSCTestEndpointObserver: public TSCEndpointObserverObject
{
public:
    TSCTestEndpointObserver(TSCEndpointTests* test): m_test(test)
    {};
    
    virtual ~TSCTestEndpointObserver()
    {};
    
protected:
    
    void onRegistrationDidComplete(TSCErrorObject* error)
    {
        [m_test.endpointCondition lock];
        m_test.lastError = error ? error->getCode() : 0;
        [m_test.endpointCondition signal];
        [m_test.endpointCondition unlock];
    }
    
    void onUnregistrationDidComplete(TSCErrorObject* error)
    {
        [m_test.endpointCondition lock];
        m_test.lastError = error ? error->getCode() : 0;
        [m_test.endpointCondition signal];
        [m_test.endpointCondition unlock];
    }
    
private:
    TSCEndpointTests* m_test;
};

- (void)setUp {
    [super setUp];
    self.endpointName = @"Alice";
    self.lastError = 0;
    self.endpointCondition = [[NSCondition alloc] init];
    TSCLogger::instance()->setLogLevel(kTSCoreLogLevelDebug);
}

- (void)tearDown {
    [self.endpointCondition unlock];
    [super tearDown];
}

#pragma mark-

- (void)run:(void(^)(TSCEndpointObjectRef endpoint))exec alternateConfig:(TSCOptions(^)(TSCOptions options))alternate
{
    NSDictionary* authOptions = [self authOptionsWithClientName:self.endpointName];
    TSAuthDataProvider* provider = [[TSAuthDataProvider alloc] initWithOptions:authOptions];
    
    self.expectation = [self expectationWithDescription:@""];
    
    [provider requestAuthData:^(id<TSAuthData> authData, NSError* error)
    {
        if(error)
        {
            XCTFail(@"Failed with error: %@", error);
            [self.expectation fulfill];
            return;
        }
        TSCOptions endpointOptions = [self optionsWithClientName:self.endpointName andAuthData:authData];
        
        endpointOptions = alternate(endpointOptions);
        self.endpoint = self.coreSDK->createEndpoint(endpointOptions, TSCEndpointObserverObjectRef(
            new TSCTestEndpointObserver(self)));
        XCTAssert(self.endpoint != nullptr, @"Endpont not created");
         
        exec(self.endpoint);
    }];
    
    [self waitForExpectationsWithTimeout:100.0 handler:^(NSError *error)
    {
         if(error)
             XCTFail(@"Expectation Failed with error: %@", error);
    }];
}

- (void)run:(void(^)(TSCEndpointObjectRef endpoint))exec
{
    [self run:exec alternateConfig:^TSCOptions (TSCOptions options) {
        return options;
    }];
}

#pragma mark-

- (void)testEndpointCreation
{
    [self run:^(TSCEndpointObjectRef endpoint)
     {
         [self.expectation fulfill];
     }];
}

- (void)testEndpointRegistrationTLS
{
    [self run:^(TSCEndpointObjectRef endpoint)
    {
        [self.endpointCondition lock];
        endpoint->registerEndpoint();
        
        if ([self.endpointCondition waitUntilDate:[NSDate dateWithTimeIntervalSinceNow:5.0]]) {
            if(self.lastError != 0)
            {
                XCTFail(@"Registering failed with error: %d", self.lastError);
                [self.expectation fulfill];
                return;
            }
        } else {
            XCTFail(@"Registration failed with timeout");
            [self.expectation fulfill];
            return;
        }
        [self.endpointCondition unlock];
        self.lastError = 0;
        [self.endpointCondition lock];
        endpoint->unregisterEndpoint();
        
        if ([self.endpointCondition waitUntilDate:[NSDate dateWithTimeIntervalSinceNow:5.0]]) {
            if(self.lastError != 0)
            {
                XCTFail(@"Unregistering failed with error: %d", self.lastError);
                [self.expectation fulfill];
                return;
            }
        } else {
            XCTFail(@"Unregistration failed with timeout");
            [self.expectation fulfill];
            return;
        }
        [self.endpointCondition unlock];
        [self.expectation fulfill];
    }
    alternateConfig:^TSCOptions (TSCOptions options)
    {
         options[kTSCSIPTransportTypeKey] = "tls";
         return options;
    }];
}

- (void)testEndpointRegistrationTCP
{
    [self run:^(TSCEndpointObjectRef endpoint)
     {
         [self.endpointCondition lock];
         endpoint->registerEndpoint();
         
         if ([self.endpointCondition waitUntilDate:[NSDate dateWithTimeIntervalSinceNow:5.0]]) {
             if(self.lastError != 0)
             {
                 XCTFail(@"Registering failed with error: %d", self.lastError);
                 [self.expectation fulfill];
                 return;
             }
         } else {
             XCTFail(@"Registration failed with timeout");
             [self.expectation fulfill];
             return;
         }
         [self.endpointCondition unlock];
         self.lastError = 0;
         [self.endpointCondition lock];
         endpoint->unregisterEndpoint();
         
         if ([self.endpointCondition waitUntilDate:[NSDate dateWithTimeIntervalSinceNow:5.0]]) {
             if(self.lastError != 0)
             {
                 XCTFail(@"Unregistering failed with error: %d", self.lastError);
                 [self.expectation fulfill];
                 return;
             }
         } else {
             XCTFail(@"Unregistration failed with timeout");
             [self.expectation fulfill];
             return;
         }
         [self.endpointCondition unlock];
         [self.expectation fulfill];
     }
     alternateConfig:^TSCOptions (TSCOptions options)
     {
         options[kTSCSIPTransportTypeKey] = "tcp";
         options[kTSCSIPTransportPortKey] = "5060";
         return options;
     }];
}

- (void)testEndpointRegistrationNetworkError
{
    [self run:^(TSCEndpointObjectRef endpoint)
    {
        [self.endpointCondition lock];
        endpoint->registerEndpoint();
        
        if ([self.endpointCondition waitUntilDate:[NSDate dateWithTimeIntervalSinceNow:5.0]]) {
            if(self.lastError == 0)
            {
                XCTFail(@"Endpoint registration should fail");
                [self.expectation fulfill];
                return;
            }
        } else {
            XCTFail(@"Registration failed with timeout");
            [self.expectation fulfill];
            return;
        }
        [self.endpointCondition unlock];
        [self.expectation fulfill];
    }
    alternateConfig:^TSCOptions (TSCOptions options)
    {
        options[kTSCSIPTransportPortKey] = "9999";
        return options;
    }];
}

@end
