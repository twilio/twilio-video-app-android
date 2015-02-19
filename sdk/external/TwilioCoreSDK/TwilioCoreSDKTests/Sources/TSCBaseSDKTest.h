//
//  TSCBaseSDKTest.h
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 1/19/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <XCTest/XCTest.h>
#import "TSCoreSDK.h"
#import "TSCoreSDKTypes.h"
#import "TSCoreConstants.h"
#import "TSCLogger.h"
#import "TSTestUtils.h"
#import "TSAuthData.h"
#import "TSAuthDataProvider.h"

using namespace twiliosdk;

@interface TSCBaseSDKTest : XCTestCase
@property (nonatomic, assign) TSCSDK* coreSDK;
@property (nonatomic, assign) TSEnvironmentRealm environmentRealm;
@property (nonatomic, assign) TSTransportType transportType;

- (NSDictionary*)authOptionsWithClientName:(NSString*)aClientName;
- (TSCOptions)optionsWithClientName:(NSString*)aClientName andAuthData:(id<TSAuthData>)anAuthData;

@end
