//
//  TSCGTestRunner.mm
//  Twilio Core SDK
//
//  Created by Alexander Trishyn on 02/16/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#import "TSCGTestRunner.h"
#import "talk/base/gunit.h"

@implementation TSCGTestRunner

+ (instancetype)sharedInstance
{
    static TSCGTestRunner* sharedInstance = nil;
    static dispatch_once_t predicate;
    dispatch_once(&predicate, ^{
        sharedInstance = [[self alloc] init];
    });
    return sharedInstance;
}

#pragma mark-

- (instancetype)init
{
    self = [super init];
    if (self)
    {
        char *argv[] = {"programname", NULL};
        int argc = sizeof(argv) / sizeof(char*) - 1;
        testing::InitGoogleTest(&argc, argv);
    }
    return self;
}

#pragma mark-

+ (BOOL)runTest:(NSString*)aTestName
{
    return [[TSCGTestRunner sharedInstance] runTest:aTestName];
}

- (BOOL)runTest:(NSString*)aTestName
{
    testing::GTEST_FLAG(filter) = [aTestName UTF8String];
    return !RUN_ALL_TESTS();
}

@end