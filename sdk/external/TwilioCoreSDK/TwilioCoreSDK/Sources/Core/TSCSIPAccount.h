//
//  TSCSIPAccount.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/04/15.
//  Copyright (c) 2014 Twilio. All rights reserved.
//

#ifndef TSC_SIP_ACCOUNT_H
#define TSC_SIP_ACCOUNT_H

#include "TSCoreSDKTypes.h"

namespace twiliosdk {

class TSCSIPAccount
{
public:
    TSCSIPAccount(const TSCOptions& options);
    virtual ~TSCSIPAccount();

    bool isValid() const;
    int getId() const;

    bool registerAccount();
    bool unregisterAccount();
    
private:
    TSCSIPAccount();
    TSCSIPAccount(const TSCSIPAccount&);
    TSCSIPAccount& operator=(TSCSIPAccount&);
    
    class TImpl;
    talk_base::scoped_ptr<TImpl> m_impl;
};
    
}  // namespace twiliosdk

#endif  // TSC_SIP_ACCOUNT_H
