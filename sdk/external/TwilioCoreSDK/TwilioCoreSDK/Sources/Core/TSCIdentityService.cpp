//
//  TSCIdentityService.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/12/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCIdentityService.h"
#include "TSCLogger.h"

#include <openssl/pem.h>
#include "talk/base/opensslidentity.h"
#include "talk/base/sslidentity.h"
#include "talk/base/thread.h"

namespace twiliosdk {

class TSCIdentityService::TImpl: public talk_base::MessageHandler
{
public:

    struct Request {
        Request(const std::string &cn, webrtc::DTLSIdentityRequestObserver* cb)
        : cn_(cn),
        cb_(cb){}

        std::string cn_;
        talk_base::scoped_refptr<webrtc::DTLSIdentityRequestObserver> cb_;
    };
    typedef talk_base::TypedMessageData<Request> MessageData;
    
    TImpl()
    {
    }
    
    ~TImpl()
    {
    }
    
    bool RequestIdentity(const std::string &identity_name,
                         const std::string &common_name,
                         webrtc::DTLSIdentityRequestObserver* observer);
    
private:
    void OnMessage(talk_base::Message* msg);
    
};
    
void
TSCIdentityService::TImpl::OnMessage(talk_base::Message* msg)
{
    // TODO: make this service customizable
    
    int clength;
    int klength;
    bool success = false;
    unsigned char *key = NULL;
    unsigned char *cert = NULL;
    unsigned char *key_temp = NULL;
    unsigned char *cert_temp = NULL;
    
    /* extract message data */
    MessageData* data = static_cast<MessageData*>(msg->pdata);
    webrtc::DTLSIdentityRequestObserver* cb = data->data().cb_.get();
    
    /* generate key pair and cert */
    talk_base::OpenSSLKeyPair* sslKey = talk_base::OpenSSLKeyPair::Generate();
    talk_base::SSLIdentityParams params;
    params.common_name = data->data().cn_;
    talk_base::OpenSSLCertificate* sslCert = talk_base::OpenSSLCertificate::Generate(sslKey, params);
    
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
        
        TS_CORE_LOG_DEBUG("Cert length: %d, Key length: %d", clength, klength);
        
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
    
bool
TSCIdentityService::TImpl::RequestIdentity(const std::string &identity_name,
                                           const std::string &common_name,
                                           webrtc::DTLSIdentityRequestObserver* observer)
{
    MessageData* msg = new MessageData(Request(common_name, observer));
    talk_base::Thread::Current()->Post(this, 1, msg);
    return true;
}

#pragma mark-
    
TSCIdentityService::TSCIdentityService()
{
    m_impl.reset(new TImpl());
}

TSCIdentityService::~TSCIdentityService()
{
}

#pragma mark-
    
bool
TSCIdentityService::RequestIdentity(const std::string &identity_name,
                                    const std::string &common_name,
                                    webrtc::DTLSIdentityRequestObserver* observer)
{
    return m_impl->RequestIdentity(identity_name, common_name, observer);
}

}  // namespace twiliosdk
