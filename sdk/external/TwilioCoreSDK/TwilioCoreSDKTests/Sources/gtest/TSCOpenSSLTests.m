//
//  TSCOpenSSLTests.mm
//  Twilio Signal SDK
//
//  Created by Alexander Trishyn on 02/13/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#import <XCTest/XCTest.h>
#import "TSCGTestRunner.h"

@interface TSCOpenSSLTests : XCTestCase
@end

@implementation TSCOpenSSLTests

- (void)setUp
{
    [super setUp];
}

- (void)tearDown
{
    [super tearDown];
}

#pragma mark-

- (void)testTLSConnect
{
    BOOL result = [TSCGTestRunner runTest:@"SSLStreamAdapterTestTLS.TestTLSConnect"];
    XCTAssertTrue(result, @"");
}

- (void)testDTLSConnect
{
    BOOL result = [TSCGTestRunner runTest:@"SSLStreamAdapterTestDTLSFromPEMStrings.TestDTLSConnect"];
    XCTAssertTrue(result, @"");
}

@end