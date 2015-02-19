//
//  TSCSIPUtilsTest.mm
//  Twilio Signal SDK
//
//  Created by Pavlo Lutsan on 1/26/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#import "TSCBaseSDKTest.h"
#import "TSCSIPUtils.h"
#import "TSCPJSUA.h"


@interface TSCSIPUtilsTests : TSCBaseSDKTest
@property (nonatomic, assign) pj_pool_t* pool;
@end

@implementation TSCSIPUtilsTests

- (void)setUp
{
    [super setUp];

    self.pool = twiliosdk::TSCSIPUtils::getPool();
}

- (void)tearDown
{
    [super tearDown];
}

#pragma mark-

- (void)testCopySdp
{
    char* testSdpSource =
        "v=0\r\n"
        "o=alice 2890844526 2890844526 IN IP4 twilio.com\r\n"
        "s= \r\n"
        "c=IN IP4 twilio.com\r\n"
        "t=0 0\r\n"
        "m=audio 49170 RTP/AVP 0\r\n"
        "a=rtpmap:0 PCMU/8000\r\n"
        "m=video 51372 RTP/AVP 31\r\n"
        "a=rtpmap:31 H261/90000\r\n"
        "m=video 53000 RTP/AVP 32\r\n"
        "a=rtpmap:32 MPV/90000\r\n";
    
    char* testSdpDestination =
        "v=0\r\n"
        "o=bob 2890844526 2890844526 IN IP4 example.com\r\n"
        "s= \r\n"
        "c=IN IP4 example.com\r\n"
        "t=0 0\r\n"
        "m=audio 49170 RTP/AVP 0\r\n"
        "a=rtpmap:0 PCMU/8000\r\n";
    
    pjmedia_sdp_session* source;
    pj_status_t status = pjmedia_sdp_parse(self.pool, testSdpSource, pj_ansi_strlen(testSdpSource), &source);
    XCTAssertTrue(PJ_SUCCESS == status, @"testCopySdp source SDP parse failed");
    status = pjmedia_sdp_validate(source);
    XCTAssertTrue(PJ_SUCCESS == status, @"testCopySdp source SDP validation failed");
    
    pjmedia_sdp_session* destination;
    status = pjmedia_sdp_parse(self.pool, testSdpDestination, pj_ansi_strlen(testSdpDestination), &destination);
    XCTAssertTrue(PJ_SUCCESS == status, @"testCopySdp destination SDP parse failed");
    status = pjmedia_sdp_validate(source);
    XCTAssertTrue(PJ_SUCCESS == status, @"testCopySdp destination SDP validation failed");
    
    twiliosdk::TSCSIPUtils::copySdp(self.pool, source, destination);
    
    status = pjmedia_sdp_validate(destination);
    XCTAssertTrue(PJ_SUCCESS == status, @"testCopySdp destination SDP validation failed");
    
    const size_t kBufferSize = 1024;
    char destinationBuffer[kBufferSize];
    pj_ssize_t length = pjmedia_sdp_print(destination, destinationBuffer, sizeof(destinationBuffer));
    XCTAssertTrue(length >0, @"testCopySdp destination SDP get string failed");
    destinationBuffer[length] = '\0';
    
    char sourceBuffer[kBufferSize];
    length = pjmedia_sdp_print(source, sourceBuffer, sizeof(sourceBuffer));
    XCTAssertTrue(length > 0, @"testCopySdp source SDP get string failed");
    sourceBuffer[length] = '\0';
    
    XCTAssertTrue(0 == strcmp(destinationBuffer, sourceBuffer), @"testCopySdp SDPs are different");
}

- (void)testGetTransportType
{
    XCTAssertTrue(twiliosdk::kTSCSIPTransportTypeUDP == twiliosdk::TSCSIPUtils::getTransportType("udp"),
                  @"getTransportType('udp') failed");
    XCTAssertTrue(twiliosdk::kTSCSIPTransportTypeTCP == twiliosdk::TSCSIPUtils::getTransportType("tcp"),
                  @"getTransportType('tcp') failed");
    XCTAssertTrue(twiliosdk::kTSCSIPTransportTypeTLS == twiliosdk::TSCSIPUtils::getTransportType("tls"),
                  @"getTransportType('tls') failed");
}

- (void)testStr2Pj
{
    const std::string kTestString = "Test String";
    
    pj_str_t result = twiliosdk::TSCSIPUtils::str2Pj(kTestString);
    XCTAssertTrue(nullptr != result.ptr, @"str2Pj failed");
    XCTAssertEqual(result.slen, (long)kTestString.length(), @"Wrong string length");
}

- (void)testPj2Str
{
    const pj_str_t kPjTestString = pj_str("Test String");
    
    std::string result = twiliosdk::TSCSIPUtils::pj2Str(kPjTestString);
    XCTAssertTrue(0 == result.compare("Test String"), @"pj2Str failed");
}

- (void)testAddPjHeader
{
    char* testMessage =
        "INVITE sip:alice@twilio.com SIP/2.0\r\n"
        "To: \"Alice\" <sip:alice@twilio.com>\r\n"
        "From: \"-Test Message-\" <sip:bob@twilio.com>;tag=838293\r\n"
        "Call-ID: 12345678901234567890@twilio\r\n"
        "\r\n";
    
    char* expectedMessage =
        "INVITE sip:alice@twilio.com SIP/2.0\r\n"
        "To: \"Alice\" <sip:alice@twilio.com>\r\n"
        "From: \"-Test Message-\" <sip:bob@twilio.com>;tag=838293\r\n"
        "Call-ID: 12345678901234567890@twilio\r\n"
        "X-Twilio-Client: {}\r\n"
        "Content-Length:  0\r\n"
        "\r\n";
    
    pjsip_parser_err_report err_list;
    
    pjsip_msg* message = pjsip_parse_msg(self.pool, testMessage, pj_ansi_strlen(testMessage), &err_list);
    XCTAssertTrue(nullptr != message, @"Parse message failed");
    pjsip_hdr header = message->hdr;
    
    twiliosdk::TSCSIPUtils::addPjHeader(self.pool, &header, std::string(twiliosdk::kTSCSIPHeaderClient), "{}");
    
    char buffer[1024];
    size_t length = pjsip_msg_print(message, buffer, sizeof(buffer));
    XCTAssertTrue(0 != length, @"Header with zero length");
    buffer[length] = '\0';

    XCTAssertTrue(0 == strncmp(buffer, expectedMessage, length), @"testAddPjHeader messages are different");
}

- (void)testCopyAddPjHeader
{
    pjsip_hdr sourceHeader;
    pj_list_init(&sourceHeader);
    TSCSIPUtils::addPjHeader(self.pool, &sourceHeader, std::string(kTSCSIPHeaderClient), "{client}");
    
    pjsip_hdr destinationHeader;
    pj_list_init(&destinationHeader);
    
    twiliosdk::TSCSIPUtils::copyPjHeader(self.pool, &sourceHeader, &destinationHeader);
    
    const size_t kBufferSize = 128;
    char sourceBuffer[kBufferSize];
    size_t sourceLength = pjsip_hdr_print_on(sourceHeader.next, sourceBuffer, sizeof(sourceBuffer));
    XCTAssertTrue(0 != sourceLength, @"Header with zero length");
    sourceBuffer[sourceLength] = '\0';
    
    char destinationBuffer[kBufferSize];
    size_t destinationLength = pjsip_hdr_print_on(destinationHeader.next, destinationBuffer, sizeof(destinationBuffer));
    XCTAssertTrue(0 != destinationLength, @"Header with zero length");
    destinationBuffer[destinationLength] = '\0';
    
    XCTAssertTrue(0 == strcmp(sourceBuffer, destinationBuffer), @"testCopyAddPjHeader are different");
}

- (void)testGenerateUniqueId
{
    const size_t kIdLength = 36;
    bool result = false;
    
    std::string id = "";
    
    std::string seed = "";
    result = twiliosdk::TSCSIPUtils::generateUniqueId(seed, id);
    XCTAssertTrue(result, @"generateUniqueId with empty seed failed");
    XCTAssertEqual(id.length(), kIdLength, @"Wrong id length");
    
    seed.assign("seed");
    id.clear();
    result = twiliosdk::TSCSIPUtils::generateUniqueId(seed, id);
    XCTAssertTrue(result, @"generateUniqueId with seed failed");
    XCTAssertEqual(id.length(), kIdLength, @"Wrong id length");
}

@end