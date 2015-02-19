//
//  TSCSIPAccount.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/04/15.
//  Copyright (c) 2014 Twilio. All rights reserved.
//

#include <pjsua-lib/pjsua.h>
#include <pjsua-lib/pjsua_internal.h>

#include <algorithm>

#include "TSCSIPAccount.h"
#include "TSCoreConstants.h"
#include "TSCPJSUA.h"
#include "TSCSIPUtils.h"

namespace twiliosdk {

class TSCSIPAccount::TImpl
{
public:
    
    TImpl(const TSCOptions& options):m_account_id(PJSUA_INVALID_ID)
    {
        m_options = options;

        // TODO: validate options
        m_sip_transport_type = TSCSIPUtils::getTransportType(m_options[kTSCSIPTransportTypeKey]);
        
        addSIPAccount();
    }
    
    ~TImpl()
    {
        deleteSIPAccount();
    }
    
    bool isValid() const {return m_account_id != PJSUA_INVALID_ID;}
    pjsua_acc_id getAccountId() const {return m_account_id;}

    bool registerSIPAccount();
    bool unregisterSIPAccount();
    
private:
    TImpl();

    void addSIPAccount();
    void deleteSIPAccount();
    
    TSCOptions m_options;
    TSCSIPTransportType m_sip_transport_type;
    pjsua_acc_id m_account_id;
};

void
TSCSIPAccount::TImpl::addSIPAccount()
{
    TSCSIPUtils::createTransport(m_sip_transport_type);
    // TODO: log error message if failed

    pjsua_acc_config config;
    pjsua_acc_config_default(&config);
    config.user_data = nullptr;
    config.register_on_acc_add = false;
    
    std::string port = m_options[kTSCSIPTransportPortKey];
    std::string transport_option = ";transport=" + m_options[kTSCSIPTransportTypeKey];

    std::string accound_sid = m_options[kTSCAccountSidKey];
    std::transform(accound_sid.begin(), accound_sid.end(), accound_sid.begin(), ::tolower);
    std::string domain = m_options[kTSCDomainKey];
    std::string alias = "\"" + m_options[kTSCAliasNameKey] +
        "\" <sip:" + m_options[kTSCAliasNameKey] + "@" + accound_sid + "." + domain + transport_option + ">";
    std::string registrar = "sip:" + domain + ":" + port + transport_option;
    std::string proxy = "<sip:" + m_options[kTSCRegistrarKey] + ":" + port + transport_option + ";hide>";
    
    pjsip_hdr sip_headers;
    pj_list_init(&sip_headers);
    TSCSIPUtils::addPjHeader(TSCSIPUtils::getPool(), &sip_headers, std::string(kTSCSIPHeaderUsername), m_options[kTSCUserNameKey]);
    TSCSIPUtils::addPjHeader(TSCSIPUtils::getPool(), &sip_headers, std::string(kTSCSIPHeaderPassword), m_options[kTSCPasswordKey]);
    TSCSIPUtils::addPjHeader(TSCSIPUtils::getPool(), &sip_headers, std::string(kTSCSIPHeaderToken), m_options[kTSCTokenKey]);
    TSCSIPUtils::addPjHeader(TSCSIPUtils::getPool(), &sip_headers, std::string(kTSCSIPHeaderAccountSid), m_options[kTSCAccountSidKey]);
    TSCSIPUtils::addPjHeader(TSCSIPUtils::getPool(), &sip_headers, std::string(kTSCSIPHeaderClientVersion), m_options[KTSCSIPClientVersionKey]);
    TSCSIPUtils::addPjHeader(TSCSIPUtils::getPool(), &sip_headers, std::string(kTSCSIPHeaderUserAgent), m_options[KTSCSIPUserAgentKey]);
    
    TSCSIPUtils::copyPjHeader(TSCSIPUtils::getPool(), &sip_headers, &config.reg_hdr_list);

    config.id = TSCSIPUtils::str2Pj(alias);
    config.reg_uri = TSCSIPUtils::str2Pj(registrar);
    config.proxy[config.proxy_cnt++] = TSCSIPUtils::str2Pj(proxy);
    pj_status_t status = pjsua_acc_add(&config, true, &m_account_id);
    if(status != PJ_SUCCESS)
    {
       // TODO: log error message
    }
}

bool
TSCSIPAccount::TImpl::registerSIPAccount()
{
    TSCSIPUtils::registerThread();
    pj_status_t status = pjsua_acc_set_registration(m_account_id, true);
    return (status == PJ_SUCCESS);
}

bool
TSCSIPAccount::TImpl::unregisterSIPAccount()
{
    TSCSIPUtils::registerThread();
    pj_status_t status = pjsua_acc_set_registration(m_account_id, false);
    return (status == PJ_SUCCESS);
}

void
TSCSIPAccount::TImpl::deleteSIPAccount()
{
    if(m_account_id == PJSUA_INVALID_ID)
       return;
    
    pj_status_t status = pjsua_acc_del(m_account_id);
    if(status != PJ_SUCCESS) {
       // TODO: log error message
    }
}
    
#pragma mark-
    
TSCSIPAccount::TSCSIPAccount(const TSCOptions& options)
{
    m_impl.reset(new TImpl(options));
}

TSCSIPAccount::~TSCSIPAccount()
{
}

#pragma mark-

bool
TSCSIPAccount::isValid() const
{
    if(m_impl.get())
       return m_impl->isValid();
    return false;
}

int
TSCSIPAccount::getId() const
{
    if(m_impl.get())
       return m_impl->getAccountId();
    return PJSUA_INVALID_ID;
}

#pragma mark-
    
bool
TSCSIPAccount::registerAccount()
{
    if(m_impl.get())
       return m_impl->registerSIPAccount();
    return false;
}

bool
TSCSIPAccount::unregisterAccount()
{
    if(m_impl.get())
       return m_impl->unregisterSIPAccount();
    return false;
}
    
} // namespace twiliosdk
