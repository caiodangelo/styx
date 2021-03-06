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
package com.hotels.styx.proxy;

import com.hotels.styx.api.HttpInterceptor;

import java.util.List;

import static java.util.Collections.EMPTY_LIST;

/**
 * Factory for creating the list of interceptors.
 */
public interface InterceptorsFactory {
    InterceptorsFactory NO_INTERCEPTORS = () -> EMPTY_LIST;

    /**
     * Creates the interceptors.
     *
     * @return list of interceptors.
     */
    List<HttpInterceptor> create();
}
