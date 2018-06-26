/*
 * Copyright (C) 2017 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video.token;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.jsonwebtoken.impl.DefaultJwtBuilder;

/*
 * We had to extend DefaultJwtBuilder in order to
 * disable serialization exception in case where object class is empty.
 */
public class VideoJwtBuilder extends DefaultJwtBuilder {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public VideoJwtBuilder() {
    // this is the only difference from default jwt builder
    OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
  }

  @Override
  protected byte[] toJson(Object object) throws JsonProcessingException {
    return OBJECT_MAPPER.writeValueAsBytes(object);
  }
}
