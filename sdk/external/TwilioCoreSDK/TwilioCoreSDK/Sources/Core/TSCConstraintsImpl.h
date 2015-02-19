//
//  TSCConstraints.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/12/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_CONSTRAINTS_H
#define TSC_CONSTRAINTS_H

#include "TSCLogger.h"
#include "talk/base/stringencode.h"

#include "talk/app/webrtc/mediaconstraintsinterface.h"

namespace twiliosdk {

class TSCConstraints: public webrtc::MediaConstraintsInterface
{
public:
    
    TSCConstraints()
    {
    }
    
    virtual ~TSCConstraints()
    {
    }

    const Constraints& GetMandatory() const
    {
        return m_mandatory;
    }
    
    const Constraints& GetOptional() const
    {
        return m_mandatory;
    }
    
    template<class T>
    void AddMandatory(const std::string& key, const T& value)
    {
        m_mandatory.push_back(Constraint(key, talk_base::ToString<T>(value)));
    }

    template<class T>
    void SetMandatory(const std::string& key, const T& value)
    {
        std::string value_str;
        if (m_mandatory.FindFirst(key, &value_str))
        {
            for (Constraints::iterator iter = m_mandatory.begin();
                 iter != m_mandatory.end(); ++iter) {
                if (iter->key == key) {
                    TS_CORE_LOG_DEBUG("key present: %s", iter->value.c_str());
                    m_mandatory.erase(iter);
                    break;
                }
            }
        }
        m_mandatory.push_back(Constraint(key, talk_base::ToString<T>(value)));
    }

    template<class T>
    void AddOptional(const std::string& key, const T& value)
    {
        m_optional.push_back(Constraint(key, talk_base::ToString<T>(value)));
    }
    
private:

    Constraints m_mandatory;
    Constraints m_optional;
};

}  // namespace twiliosdk

#endif // TSC_CONSTRAINTS_H
