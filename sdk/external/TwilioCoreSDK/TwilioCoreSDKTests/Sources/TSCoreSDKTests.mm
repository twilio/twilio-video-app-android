//
//  TSCoreSDKTests.mm
//  TwilioSignalSDK
//
//  Created by Alexander Trishyn on 12/29/14.
//  Copyright (c) 2014 Twilio. All rights reserved.
//

#import "TSCBaseSDKTest.h"
#import "TSCDeviceManager.h"
#import "TSCEndpoint.h"
#import "TSCEndpointObserver.h"

#import "TSCoreError.h"


@interface TSCoreSDKTests : TSCBaseSDKTest
@end

@implementation TSCoreSDKTests

- (void)setUp
{
    [super setUp];
}

- (void)tearDown
{
    [super tearDown];
}

#pragma mark-

- (void)testInitialization
{
   XCTAssertFalse(self.coreSDK == nullptr, @"");
   XCTAssertFalse(self.coreSDK->isInitialized() == false, @"");
}

- (void)testCreateEndpoint
{
    TSCOptions options;
    TSCEndpointObjectRef endpoint = self.coreSDK->createEndpoint(options, TSCEndpointObserverObjectRef(nullptr));
    XCTAssertFalse(nullptr == endpoint.get(), @"Endpoint creation with no options failed");
    endpoint.release();
    
    options[""] = "";
    endpoint = self.coreSDK->createEndpoint(options, TSCEndpointObserverObjectRef(nullptr));
    XCTAssertFalse(nullptr == endpoint.get(), @"Endpoint creation with empty failed");
    endpoint.release();
    
    options["testKey"] = "testValue";
    endpoint = self.coreSDK->createEndpoint(options, TSCEndpointObserverObjectRef(new TSCEndpointObserverObject()));
    XCTAssertFalse(nullptr == endpoint.get(), @"Endpoint creation failed");
    endpoint.release();
}

- (void)testCreateDeviceManager
{
    TSCDeviceManagerObjectRef manager = self.coreSDK->createDeviceManager();
    
    XCTAssertFalse(nullptr == manager.get(), @"Device manager creation failed");
}

- (void)testErrorObject
{
    TSCError errorA(kTSCoreSDKErrorDomain, kTSCErrorGeneric);
    XCTAssertTrue(errorA.getCode() == kTSCErrorGeneric, @"");
    XCTAssertFalse(errorA.getMessage().empty(), @"");

    TSCError errorB(kTSCoreSDKErrorDomain, kTSCErrorGeneric + 1000);
    XCTAssertTrue(errorB.getCode() == (kTSCErrorGeneric + 1000), @"");
    XCTAssertTrue(errorB.getMessage().empty(), @"");
}

@end
