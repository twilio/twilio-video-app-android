//
//  TSCSIPUtils.h
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 1/22/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_SIP_UTILS_H
#define TSC_SIP_UTILS_H

#include <pjsua-lib/pjsua.h>

#include "TSCoreSDKTypes.h"
#include "TSCoreConstants.h"

namespace twiliosdk {
    
namespace TSCSIPUtils {
    
TSCSIPTransportType getTransportType(const std::string& transportType);
    
void registerThread();
    
pj_pool_t* getPool();
    
bool createTransport(TSCSIPTransportType type);

/**
 * Copy SDP
 *
 * @param [in] pool Pool for memory allocation
 * @param [in] src Source SDp that need to be copied
 * @param [input] dest Destination SDP
 */
void copySdp(pj_pool_t* pool, const pjmedia_sdp_session* src, pjmedia_sdp_session* dest);

/**
 * Convert string to pj_str_t struct
 *
 * @param [in] input_str String to convert
 * @return Created pj_str_t struct
 */
pj_str_t str2Pj(const std::string& input_str);

/**
 * Convert pj_str_t struct to string
 *
 * @param [in] input_str pj_str_t struct to convert
 * @return Created string
 */
std::string pj2Str(const pj_str_t& input_str);

/**
 * Convert to header structure
 *
 * @param [in] arr Array with header values
 * @param [inout] hdr Created header structure
 */
void copyPjHeader(pj_pool_t* pool, pjsip_hdr* src, pjsip_hdr* dst);

void addPjHeader(pj_pool_t* pool, pjsip_hdr* dst,
                 const std::string& name, const std::string& value);

/**
 * Generate UUIDs for stream/track labels
 *
 * @param [in] seed Seed for generating hash
 * @param [inout] id Generated id as a hash
 * @return false if failed
 */
bool generateUniqueId(const std::string& seed, std::string& id);
    
} // TSCSIPUtils

} // twiliosdk

#endif // TSC_SIP_UTILS_H
