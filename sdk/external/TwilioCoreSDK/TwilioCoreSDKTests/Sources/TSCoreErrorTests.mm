//
//  TSCoreErrorTest.mm
//  Twilio Signal SDK
//
//  Created by Pavlo Lutsan on 1/28/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#import <XCTest/XCTest.h>
#import "TSCoreSDKTypes.h"
#import "TSCoreError.h"


@interface TSCoreErrorTests : XCTestCase
@end

@implementation TSCoreErrorTests

- (void)setUp
{
    [super setUp];
}

- (void)tearDown
{
    [super tearDown];
}

#pragma mark-

- (void)testGetProperties
{
    using namespace twiliosdk;
    
    int testCode = 0;
    std::string testDomain = "";
    std::string testMessage = "";
    TSCErrorObjectRef testError = new TSCErrorObject(testDomain, testCode);
    XCTAssertTrue((testCode == testError->getCode()) &&
                  (0 == testError->getDomain().compare(testDomain)) &&
                  (0 == testError->getMessage().compare(testMessage)),
                  @"testGetProperties failed");
    testError.release();
    
    testDomain.assign("TestCoreError");
    testError = new TSCErrorObject(testDomain, testCode, testMessage);
    XCTAssertTrue((testCode == testError->getCode()) &&
                  (0 == testError->getDomain().compare(testDomain)) &&
                  (0 == testError->getMessage().compare(testMessage)),
                  @"testGetProperties failed");
    testError.release();
    
    testDomain.assign("TestCoreError");
    testMessage.assign("Test Error Message");
    testError = new TSCErrorObject(testDomain, testCode, testMessage);
    XCTAssertTrue((testCode == testError->getCode()) &&
                  (0 == testError->getDomain().compare(testDomain)) &&
                  (0 == testError->getMessage().compare(testMessage)),
                  @"testGetProperties failed");
    testError.release();
    
    testCode = -1;
    testDomain.assign("TestCoreError");
    testMessage.assign("Test Error Message");
    testError = new TSCErrorObject(testDomain, testCode, testMessage);
    XCTAssertTrue((testCode == testError->getCode()) &&
                  (0 == testError->getDomain().compare(testDomain)) &&
                  (0 == testError->getMessage().compare(testMessage)),
                  @"testGetProperties failed");
    testError.release();
    
    testCode = INT_MAX;
    testError = new TSCErrorObject(testDomain, testCode, testMessage);
    XCTAssertTrue((testCode == testError->getCode()) &&
                  (0 == testError->getDomain().compare(testDomain)) &&
                  (0 == testError->getMessage().compare(testMessage)),
                  @"testGetProperties failed");
    testError.release();
    
    testCode = INT_MAX + 1;
    testError = new TSCErrorObject(testDomain, testCode, testMessage);
    XCTAssertTrue((testCode == testError->getCode()) &&
                  (0 == testError->getDomain().compare(testDomain)) &&
                  (0 == testError->getMessage().compare(testMessage)),
                  @"testGetProperties failed");
    testError.release();
}

@end