#ifndef TWILIOSIPENDPOINT_H
#define TWILIOSIPENDPOINT_H

#include "twiliosdk.h"

namespace twiliosdk {


#define REGISTAR_PORT 5060
#define REGISTAR_PORT_STR "5060"

/**
 * Sip Endpoint manager
 */
class TwilioSipEndpoint {
public:
    /**
     * Initialize sip module
     *
     * @param [in] sip initialization parameters
     * @return true if succeed
     */
    static bool init(bool useTLS);

    /**
     * Deinitialize sip module
     */
    static void destroy();

    /**
     * Register current thread to work with sip, if not registered
     */
    static void register_thread();

    static pj_pool_t* getPool();
};

}  // namespace twiliosdk

#endif  // TWILIOSIPENDPOINT_H
