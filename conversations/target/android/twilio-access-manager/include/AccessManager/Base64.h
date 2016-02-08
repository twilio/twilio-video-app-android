#ifndef BASE_64_H
#define BASE_64_H

#include <string>

namespace TwilioCommon {

class Base64 {

public:
    static std::string decode(const std::string& data);
};

} //namespace TwilioCommon 

#endif //BASE_64_H
