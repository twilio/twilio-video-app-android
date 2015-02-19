#include "Poco/SplitterChannel.h"
#include "Poco/ConsoleChannel.h"
#include "Poco/FileChannel.h"
#include "Poco/FormattingChannel.h"
#include "Poco/Formatter.h"
#include "Poco/PatternFormatter.h"
#include "Poco/AutoPtr.h"
#include "Poco/NumberFormatter.h"
#include "Poco/DateTimeFormat.h"
#include "Poco/DateTimeFormatter.h"
#include "Poco/DateTime.h"
#include "Poco/Timestamp.h"
#include "Poco/Timezone.h"
#include "Poco/Environment.h"
#include "twilioLogger.h"

#ifdef WIN32
#include <windows.h>
#endif

namespace twiliosdk {

using Poco::Logger;
using Poco::SplitterChannel;
using Poco::ConsoleChannel;
using Poco::FileChannel;
using Poco::FormattingChannel;
using Poco::Formatter;
using Poco::PatternFormatter;
using Poco::AutoPtr;
using Poco::Message;

typedef std::map<std::string, std::string> StringMap;

class JsonFormatter : public Poco::Formatter {
public:
    void format(const Message& msg, std::string& text) {
        using namespace Poco;
        //Fill the needed fields
        StringMap fields;

        fields["msg"] = escape(msg.getText());
        fields["host"] = escape(Environment::nodeName());
        fields["pid"] = NumberFormatter::format(msg.getPid());
        fields["tid"] = NumberFormatter::format(msg.getTid());
        fields["priority"] = NumberFormatter::format(msg.getPriority());
        Timestamp tm = msg.getTime();
        fields["datetime"] = DateTimeFormatter::format(tm, "%Y-%m-%d %H:%M:%S.%i");
        fields["timestamp"] = NumberFormatter::format(tm.epochTime());

        // Convert to string
        text.append("{");
        for(StringMap::iterator it = fields.begin(); it != fields.end(); it++) {
            text.append("\"");
            text.append(it->first);
            text.append("\":\"");
            text.append(it->second);
            text.append("\",");
        }
        *text.rbegin() = '}';
    }

private:

    std::string escape(const std::string& str) {
        std::string res;
        for (std::string::const_iterator it = str.begin(); it != str.end(); it++) {
            switch (*it) {
                case '\\' : res += "\\\\"; break;
                case '\"' : res += "\\\""; break;
                case '\'' : res += "\\\'"; break;
                case '&'  : res += "\\&"; break;
                case '\n' : res += "\\n"; break;
                case '\r' : res += "\\r"; break;
                case '\t' : res += "\\t"; break;
                default   : res += *it; break;
            }
        }
        return res;
    }
};

#ifdef WIN32
class DebugOutputChannel : public Poco::Channel {
public:
    void log(const Message& msg) {
        std::string text = msg.getText();
        text.append("\r\n");

        OutputDebugStringA(text.c_str());
    }
};
#endif

void TwilioLogger::init(TwilioLoggerConfig& config) {

    AutoPtr<SplitterChannel> splitter(new SplitterChannel);

#ifdef WIN32
    AutoPtr<DebugOutputChannel> console_logger(new DebugOutputChannel);
#else
    AutoPtr<ConsoleChannel> console_logger(new ConsoleChannel);
#endif
    AutoPtr<PatternFormatter> pattern_formatter(new PatternFormatter);
    pattern_formatter->setProperty("times", "local");
    pattern_formatter->setProperty("pattern",
       "%Y-%m-%d %H:%M:%S.%i TwilioSDK[%P:%I]: [%p] %t");
    AutoPtr<FormattingChannel> pattern_formatter_logger(new FormattingChannel(pattern_formatter, console_logger));
    splitter->addChannel(pattern_formatter_logger);

    AutoPtr<FileChannel> file_logger(new FileChannel(config.file_));
    //disable archiving, just overwrite each time
    file_logger->setProperty("rotateOnOpen", "true");
    file_logger->setProperty("purgeCount", "1");
    AutoPtr<JsonFormatter> json_formatter(new JsonFormatter);
    AutoPtr<FormattingChannel> json_formatter_logger(new FormattingChannel(json_formatter, file_logger));
    splitter->addChannel(json_formatter_logger);

    Logger::root().setChannel(splitter);
    Logger::root().setLevel(config.log_level_);

    LOG_INFO("SDK Logger Initialized");
}

Poco::Logger& TwilioLogger::logger() {
    static Poco::Logger& logger = Poco::Logger::root();
    return logger;
}

Poco::LogStream& TwilioLogger::logger_stream() {
    static Poco::LogStream log_stream(TwilioLogger::logger());
    return log_stream;
}

}  // namespace twiliosdk
