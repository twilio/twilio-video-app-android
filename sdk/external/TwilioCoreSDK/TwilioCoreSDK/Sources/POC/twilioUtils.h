#ifndef TWILIOUTILS_H_
#define TWILIOUTILS_H_

#include <string>

#ifdef stricmp
#undef stricmp
#endif

#ifdef strnicmp
#undef strnicmp
#endif

#ifndef PJ_IS_LITTLE_ENDIAN
#define PJ_IS_LITTLE_ENDIAN 1
#endif

#ifndef PJ_IS_BIG_ENDIAN
#define PJ_IS_BIG_ENDIAN 0
#endif

#include <pjsua-lib/pjsua.h>
#include <pjsua2/siptypes.hpp>

namespace twiliosdk {

/**
 * Class with helper methods
 */
class TwilioUtils {
public:
    /**
     * Generate UUIDs for stream/track labels
     *
     * @param [in] seed Seed for generating hash
     * @param [inout] id Generated id as a hash
     * @return false if failed
     */
    static bool generateUniqueId (const std::string& seed, std::string& id);
};

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
}

#endif /* TWILIOUTILS_H_ */
