/**
 * Copyright (C) 2013-2017 Expedia Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hotels.styx.admin

import com.hotels.styx.api.HttpRequest.Builder.get
import com.hotels.styx.infrastructure.HttpResponseImplicits
import com.hotels.styx.{DefaultStyxConfiguration, StyxClientSupplier, StyxProxySpec}
import io.netty.handler.codec.http.HttpResponseStatus.OK
import org.scalatest.FunSpec



class AdminSpec extends FunSpec
  with StyxProxySpec
  with DefaultStyxConfiguration
  with StyxClientSupplier
  with HttpResponseImplicits {

  describe("health check") {
    it("should return 200 and string 'OK' on /admin/status") {
      val (response, body) = decodedRequest(get(styxServer.adminURL("/admin/status")).build())
      assert(response.status() == OK)
      assert(response.isNotCacheAble())
      assert(body == "OK")
    }

    it("should return 200 and string 'OK' on /admin/healthcheck") {
      val (response, body) = decodedRequest(get(styxServer.adminURL("/admin/healthcheck")).build())
      assert(response.status() == OK)
      assert(response.isNotCacheAble())
      body should include("\"healthy\":true")
    }

    it("should return 200 and string 'PONG' on /admin/ping") {
      val (response, body) = decodedRequest(get(styxServer.adminURL("/admin/ping")).build())
      assert(response.status() == OK)
      assert(response.isNotCacheAble())
      body should include("pong")
    }
  }
}
