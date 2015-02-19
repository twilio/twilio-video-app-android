/*
 * twilioIdentityService.cpp
 *
 *  Created on: Aug 25, 2014
 *      Author: ykhodosh
 */

#include <openssl/pem.h>

#include <string>
#include <iostream>

#if defined(DARWIN) || defined(WEBRTC_IOS)
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wunused-parameter"
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
#endif

#include "talk/base/opensslidentity.h"
#include "talk/base/sslidentity.h"

#if defined(DARWIN) || defined(WEBRTC_IOS)
#pragma clang diagnostic pop
#pragma GCC diagnostic pop
#endif

#include "twilioLogger.h"
#include "twilioIdentityService.h"

namespace twiliosdk {

bool TwilioIdentityService::RequestIdentity(
        const std::string &identity_name, const std::string &common_name,
        webrtc::DTLSIdentityRequestObserver *observer) {
    MessageData *msg = new MessageData(Request(common_name, observer));
    talk_base::Thread::Current()->Post(this, 1, msg);
    return true;
}

void TwilioIdentityService::OnMessage(talk_base::Message *msg) {
    int clength;
    int klength;
    bool success = false;
    unsigned char *key = NULL;
    unsigned char *cert = NULL;
    unsigned char *key_temp = NULL;
    unsigned char *cert_temp = NULL;

    /* extract message data */
    TwilioIdentityService::MessageData *data =
            static_cast<TwilioIdentityService::MessageData *>(msg->pdata);
    webrtc::DTLSIdentityRequestObserver *cb = data->data().cb_.get();

    /* generate key pair and cert */
    talk_base::OpenSSLKeyPair *sslKey = talk_base::OpenSSLKeyPair::Generate();
    talk_base::SSLIdentityParams params;
    params.common_name = data->data().cn_;
    talk_base::OpenSSLCertificate *sslCert =
            talk_base::OpenSSLCertificate::Generate(sslKey, params);

    /* check the result */
    if (sslKey && sslCert) {
#ifdef DEBUG
        //RSA_print_fp(stdout, sslKey->pkey()->pkey.rsa, 0);
        //X509_print_fp(stdout, sslCert->x509());

        //PEM_write_PrivateKey(stdout, sslKey->pkey(), NULL, NULL, 0, NULL, NULL);
        //PEM_write_X509(stdout, sslCert->x509());
#endif /* DEBUG */

        /* get DER encoded cert/key length  */
        clength = i2d_X509(sslCert->x509(), NULL);
        klength = i2d_RSAPrivateKey(sslKey->pkey()->pkey.rsa, NULL);

        LOG_DEBUG_STREAM << "Cert length: " << clength << ", Key length: " << klength << std::endl;

        /* memory allocation can fail */
        if ((key = key_temp = (unsigned char *) malloc(klength)) && (cert =
                cert_temp = (unsigned char *) malloc(clength))) {
            /* success, we are good to go */
            success = true;

            /* encode cert and key */
            i2d_X509(sslCert->x509(), &cert_temp);
            i2d_RSAPrivateKey(sslKey->pkey()->pkey.rsa, &key_temp);
        }
    }

    /* notify observer */
    if (!success) {
        cb->OnFailure(-1);
    } else {
        std::string cert_s((const char *) cert, (size_t) clength);
        std::string pkey_s((const char *) key, (size_t) klength);
        cb->OnSuccess(cert_s, pkey_s);
    }

    /* cleanup */
    if (sslCert)
        delete sslCert;
    if (sslKey)
        delete sslKey;
    if (cert)
        free(cert);
    if (key)
        free(key);
    delete msg->pdata;
}

}  // namespace twiliosdk
