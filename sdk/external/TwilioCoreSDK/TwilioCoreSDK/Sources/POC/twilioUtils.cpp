#include "twilioUtils.h"

#include <openssl/evp.h>
#include <openssl/rand.h>

namespace twiliosdk {

#define LABEL_SIZE          18
#define SHA1_DIGEST_SIZE    20


void copySdp(pj_pool_t* pool, const pjmedia_sdp_session* src, pjmedia_sdp_session* dest) {
    /* Copy origin line. */
    pj_strdup(pool, &dest->origin.user, &src->origin.user);
    dest->origin.id = src->origin.id;
    dest->origin.version = src->origin.version;
    pj_strdup(pool, &dest->origin.net_type, &src->origin.net_type);
    pj_strdup(pool, &dest->origin.addr_type, &src->origin.addr_type);
    pj_strdup(pool, &dest->origin.addr, &src->origin.addr);

    /* Copy subject line. */
    pj_strdup(pool, &dest->name, &src->name);

    /* Copy connection line */
    if (src->conn) {
        dest->conn = pjmedia_sdp_conn_clone(pool, src->conn);
    }

    /* Copy bandwidth info */
    dest->bandw_count = src->bandw_count;
    for (unsigned i = 0; i<src->bandw_count; ++i) {
        dest->bandw[i] = pjmedia_sdp_bandw_clone(pool, src->bandw[i]);
    }

    /* Copy time line. */
    dest->time.start = src->time.start;
    dest->time.stop = src->time.stop;

    /* Copy session attributes. */
    dest->attr_count = src->attr_count;
    for (unsigned i = 0; i<src->attr_count; ++i) {
        dest->attr[i] = pjmedia_sdp_attr_clone(pool, src->attr[i]);
    }

    /* Copy media descriptors. */
    dest->media_count = src->media_count;
    for (unsigned i = 0; i<src->media_count; ++i) {
        dest->media[i] = pjmedia_sdp_media_clone(pool, src->media[i]);
    }
}

pj_str_t str2Pj(const std::string& input_str) {
    pj_str_t output_str;
    output_str.ptr = (char*)input_str.c_str();
    output_str.slen = input_str.size();
    return output_str;
}

std::string pj2Str(const pj_str_t& input_str) {
    if (input_str.ptr) {
        return std::string(input_str.ptr, input_str.slen);
    }

    return std::string();
}

#if 0
void arr2Pj(const pj::SipHeaderVector& arr, pjsip_hdr& hdr) {
    for (unsigned int i = 0; i< arr.size(); ++i) {
        pj_list_push_back(&hdr, &arr[i].toPj());
        if (!hdr.vptr) {
            hdr.vptr = hdr.next->vptr;
        }
    }
}
#endif

void copyPjHeader(pj_pool_t* pool, pjsip_hdr* src, pjsip_hdr* dst) {
    pjsip_hdr* hdr = src->next;
    while (hdr && hdr != src) {
        pjsip_hdr *new_hdr = (pjsip_hdr*)pjsip_hdr_clone(pool, hdr);
        pj_list_push_back(dst, new_hdr);
        hdr = hdr->next;
    }
}

void addPjHeader(pj_pool_t* pool, pjsip_hdr* dst,
                 const std::string& name, const std::string& value) {

    pj_str_t name_str  = str2Pj(name);
    pj_str_t value_str = str2Pj(value);
    pjsip_generic_string_hdr *header =
            pjsip_generic_string_hdr_create(pool, &name_str, &value_str);
    pj_list_push_back(dst, header);
}

bool getRandomBytes(unsigned char* buffer, size_t size) {
    bool result = true;

    int rc = RAND_pseudo_bytes(buffer, size);
    if (1 != rc && 0 != rc) {
        result = false;
    }
    return result;
}

bool sha1Hash(const std::string& data, std::string& hash) {
    bool result = true;
    EVP_MD_CTX* mdctx;
    unsigned int length;
    unsigned char digest[SHA1_DIGEST_SIZE];

    if (NULL == (mdctx = EVP_MD_CTX_create())) {
        result = false;
    }
    else {
        if (1 != EVP_DigestInit_ex(mdctx, EVP_sha1(), NULL)) {
            result = false;
        }
        else {
            if (1 != EVP_DigestUpdate(mdctx, data.c_str(), data.length())) {
                result = false;
            }
            else {
                if (1 != EVP_DigestFinal_ex(mdctx, digest, &length)) {
                    result = false;
                }
                else {
                    char message_digest[LABEL_SIZE * 2 + 1];
                    for (int i = 0; i < LABEL_SIZE; i++) {
                        ::sprintf(message_digest + i * 2, "%.2x", digest[i]);
                    }
                    message_digest[LABEL_SIZE * 2] = '\0';
                    hash = message_digest;
                }
            }
        }

        EVP_MD_CTX_destroy(mdctx);
    }
    return result;
}

bool TwilioUtils::generateUniqueId(const std::string& seed, std::string& id) {
    bool result = true;
    const time_t now = time(0);

    // get random bytes
    unsigned char buffer[64];
    if (!getRandomBytes(buffer, sizeof(buffer))) {
        result = false;
    } else {
        // concatenate
        std::string data(seed);
        data += (const char *) buffer;
        data += ::ctime(&now);

        // generate hash
        if ( !sha1Hash(data, id) ) {
            result = false;
        }
    }
    // return the result
    return result;
}

}  // namespace twiliosdk

