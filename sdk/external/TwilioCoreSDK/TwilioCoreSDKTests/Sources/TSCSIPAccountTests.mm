//
//  TSCSIPAccountTests.mm
//  Twilio Signal SDK
//
//  Created by Pavlo Lutsan on 1/28/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#import "TSCBaseSDKTest.h"
#include "TSCSIPAccount.h"

#import "TSTestUtils.h"

@interface TSCSIPAccountTests : TSCBaseSDKTest
@property (nonatomic, strong) XCTestExpectation* expectation;
@end

@implementation TSCSIPAccountTests

- (void)setUp
{
    [super setUp];
}

- (void)tearDown
{
    [super tearDown];
}

- (void)testCreateAccount
{

    TSCOptions options;
    TSCSIPAccountObjectRef account = new TSCSIPAccountObject(options);
    
    XCTAssertFalse(nullptr == account.get(), @"Account creation with no options failed");
    XCTAssertFalse(account->isValid(), @"Account should be invalid");
    
    account.release();
}

- (void)testRegisterAccount
{
    NSString* const kClientName = @"alice";
    
    NSDictionary* authOptions = [self authOptionsWithClientName:kClientName];
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
        
        TSCOptions options = [self optionsWithClientName:kClientName andAuthData:authData];
        TSCSIPAccountObjectRef account = new TSCSIPAccountObject(options);
    
        XCTAssertFalse(nullptr == account.get(), @"Account creation with no options failed");
        XCTAssertTrue(account->isValid(), @"Account should be invalid");
    
        XCTAssertTrue(account->registerAccount(), @"Account registration failed");
        [self.expectation fulfill];
    }];
    
    [self waitForExpectationsWithTimeout:100.0 handler:^(NSError *error)
    {
        if(error) {
             XCTFail(@"Expectation Failed with error: %@", error);
        }
    }];
}

@end