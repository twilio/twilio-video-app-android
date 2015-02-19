//
//  TSCSessionTests.m
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 1/26/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#import "TSCBaseSDKTest.h"

#import "TSAuthData.h"
#import "TSAuthDataProvider.h"

#import "TSCEndpoint.h"
#import "TSCEndpointObserver.h"
#import "TSCSession.h"
#import "TSCIncomingSession.h"
#import "TSCOutgoingSession.h"


@interface TSCSessionTests : TSCBaseSDKTest
@property (nonatomic, assign) TSCEndpointObjectRef endpoint;
@property (nonatomic, assign) TSCEndpointObserverObjectRef endpointObserver;
@property (nonatomic, strong) NSString* endpointName;
@property (nonatomic, strong) XCTestExpectation* expectation;
@property (nonatomic, strong) NSCondition* endpointCondition;
@property (nonatomic, assign) TSCOutgoingSessionObjectRef outgoingSession;
@property (nonatomic, assign) TSCIncomingSessionObjectRef incomingSession;
@property int lastError;
@end

@implementation TSCSessionTests

@end
