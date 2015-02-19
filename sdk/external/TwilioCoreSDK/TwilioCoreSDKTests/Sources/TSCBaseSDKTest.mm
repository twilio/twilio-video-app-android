//
//  TSCBaseSDKTest.mm
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 1/19/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#import "TSCBaseSDKTest.h"

@implementation TSCBaseSDKTest

NSString* const TSCAuthOptionServerURLKey = @"server-url";
NSString* const TSCAuthOptionClientNameKey = @"client-name";

- (void)setUp
{
    [super setUp];
    // create sdk instance
    self.coreSDK = TSCSDK::instance();
    self.environmentRealm = kTSEnvironmentRealmProd;
    self.transportType = kTSTransportTypeTLS;
}

- (void)tearDown
{
    if(self.coreSDK != nullptr)
        TSCSDK::destroy();
    [super tearDown];
}

- (TSCOptions)transformOptions:(NSDictionary*)options
{
    TSCOptions coreOptions;
    for (NSString* key in options.allKeys)
    {
        NSString* value = options[key];
        std::string cKey([key UTF8String], [key lengthOfBytesUsingEncoding:NSUTF8StringEncoding]);
        std::string cValue([value UTF8String], [value lengthOfBytesUsingEncoding:NSUTF8StringEncoding]);
        coreOptions.insert(std::make_pair(cKey, cValue));
    }
    return coreOptions;
}

- (NSDictionary*)authOptionsWithClientName:(NSString*)aClientName
{
    NSMutableDictionary* authOptions = [NSMutableDictionary dictionaryWithCapacity:0];
    [authOptions addEntriesFromDictionary:[TSTestUtils authProviderConfig:self.environmentRealm]];
    [authOptions setObject:aClientName forKey:TSCAuthOptionClientNameKey];
    return authOptions.copy;
}

- (TSCOptions)optionsWithClientName:(NSString*)aClientName andAuthData:(id<TSAuthData>)anAuthData
{
    NSMutableDictionary* options = [NSMutableDictionary dictionaryWithCapacity:0];
    [options addEntriesFromDictionary:[TSTestUtils endpointConfig:self.environmentRealm transport:self.transportType]];
    [options setObject:aClientName forKey: [NSString stringWithUTF8String: kTSCAliasNameKey]];
    [options setObject:anAuthData.username forKey: [NSString stringWithUTF8String: kTSCUserNameKey]];
    [options setObject:anAuthData.password forKey:[NSString stringWithUTF8String: kTSCPasswordKey]];
    [options setObject:anAuthData.capabilityToken forKey: [NSString stringWithUTF8String: kTSCTokenKey]];
    [options setObject:anAuthData.stunServerURL forKey: [NSString stringWithUTF8String: kTSCStunURLKey]];
    [options setObject:anAuthData.turnServerURL forKey: [NSString stringWithUTF8String: kTSCTurnURLKey]];
    [options setObject:anAuthData.accountSid forKey: [NSString stringWithUTF8String: kTSCAccountSidKey]];
    return [self transformOptions: options.copy];
}

@end
