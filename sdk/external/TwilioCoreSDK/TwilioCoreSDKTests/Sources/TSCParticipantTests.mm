//
//  TSCParticipantTests.mm
//  Twilio Signal SDK
//
//  Created by Pavlo Lutsan on 1/29/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#import <XCTest/XCTest.h>
#import "TSCoreSDKTypes.h"
#import "TSCParticipant.h"


@interface TSCParticipantTests : XCTestCase
@end

@implementation TSCParticipantTests

- (void)setUp
{
    [super setUp];
}

- (void)tearDown
{
    [super tearDown];
}

#pragma mark-

- (void)testCreateCopyParticipant
{
    using namespace twiliosdk;
    
    std::string participantName = "TestAdress";
    TSCParticipantObjectRef testParticipant = new TSCParticipantObject(participantName);
    
    XCTAssertTrue((0 == testParticipant->getAddress().compare(participantName)),
                  @"Create participant failed");
    
    TSCParticipant sourceParticipant(participantName);
    TSCParticipant destinationParticipant = sourceParticipant;
    
    XCTAssertTrue((0 == destinationParticipant.getAddress().compare(participantName)),
                  @"Copy participant failed");
}

@end