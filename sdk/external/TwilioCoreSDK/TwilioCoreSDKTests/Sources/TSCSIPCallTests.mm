//
//  TSCSIPCallTests.m
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 1/19/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#import "TSCBaseSDKTest.h"
#import "TSCSIPCall.h"
#import "TSCSIPCallContext.h"

@interface TSCSIPCallTests : TSCBaseSDKTest
@property (nonatomic, assign) TSCSIPCallObjectRef sipCall;
@end

@implementation TSCSIPCallTests

- (void)setUp
{
    [super setUp];
}

- (void)tearDown
{
    self.sipCall = nullptr;
    [super tearDown];
}

- (void)testCreation
{
    TSCOptions options;
    TSCSIPCallContext *context = new TSCSIPCallContext(0, 0);
    self.sipCall = new TSCSIPCallObject("", options, context);
    XCTAssertFalse(self.sipCall->isValid(), @"Should be not valid initially without callid");
    
    self.sipCall = new TSCSIPCallObject("", options, context, 0);
    XCTAssert(self.sipCall->isValid(), @"");
}

@end
