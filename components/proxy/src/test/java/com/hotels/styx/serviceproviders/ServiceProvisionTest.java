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
package com.hotels.styx.serviceproviders;

import com.google.common.collect.ImmutableMap;
import com.hotels.styx.AggregatedConfiguration;
import com.hotels.styx.StyxConfig;
import com.hotels.styx.api.Environment;
import com.hotels.styx.support.api.SimpleEnvironment;
import com.hotels.styx.api.configuration.Configuration;
import com.hotels.styx.api.configuration.ServiceFactory;
import org.testng.annotations.Test;

import java.util.Map;

import static com.hotels.styx.serviceproviders.ServiceProvision.loadService;
import static com.hotels.styx.serviceproviders.ServiceProvision.loadServices;
import static com.hotels.styx.support.matchers.IsOptional.isAbsent;
import static com.hotels.styx.support.matchers.IsOptional.isValue;
import static com.hotels.styx.support.matchers.MapMatcher.isMap;
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;

public class ServiceProvisionTest {
    private final String yaml = "" +
            "my:\n" +
            "  factory:\n" +
            "    enabled: true\n" +
            "    class: " + MyFactory.class.getName() + "\n" +
            "    config:\n" +
            "      stringValue: expectedValue\n" +
            "not:\n" +
            "  real:\n" +
            "    class: my.FakeClass\n" +
            "multi:\n" +
            "  factories:\n" +
            "    one:\n" +
            "      enabled: true\n" +
            "      class: " + MyFactory.class.getName() + "\n" +
            "      config:\n" +
            "        stringValue: valueNumber1\n" +
            "    two:\n" +
            "      enabled: true\n" +
            "      class: " + MyFactory.class.getName() + "\n" +
            "      config:\n" +
            "        stringValue: valueNumber2\n" +
            "    three:\n" +
            "      enabled: true\n" +
            "      class: " + MyFactory.class.getName() + "\n" +
            "      config:\n" +
            "        stringValue: valueNumber3\n";

    private final Environment environment = environmentWithConfig(yaml);

    @Test
    public void serviceReturnsCorrectlyFromCall() {
        assertThat(loadService(environment.configuration(), environment, "my.factory", String.class), isValue("expectedValue"));
    }

    @Test
    public void serviceReturnsEmptyWhenFactoryKeyDoesNotExist() {
        assertThat(loadService(environment.configuration(), environment, "invalid.key", String.class), isAbsent());
    }

    @Test
    public void servicesReturnCorrectlyFromCall() {
        Map<String, String> services = loadServices(environment.configuration(), environment, "multi", String.class);

        assertThat(services, isMap(ImmutableMap.of(
                "one", "valueNumber1",
                "two", "valueNumber2",
                "three", "valueNumber3"
        )));
    }

    @Test
    public void servicesReturnEmptyWhenFactoryKeyDoesNotExist() {
        Map<String, String> services = loadServices(environment.configuration(), environment, "invalid.key", String.class);

        assertThat(services, isMap(emptyMap()));
    }

    @Test(expectedExceptions = Exception.class, expectedExceptionsMessageRegExp = "(?s).*No such class 'my.FakeClass'.*")
    public void throwsExceptionWhenClassDoesNotExist() {
        loadService(environment.configuration(), environment, "not.real", String.class);
    }

    public static class MyFactory implements ServiceFactory<String> {
        @Override
        public String create(Environment environment, Configuration serviceConfiguration) {
            return serviceConfiguration.get("stringValue").orElse("VALUE_ABSENT");
        }
    }

    private Environment environmentWithConfig(String yaml) {
        Configuration conf = new AggregatedConfiguration(StyxConfig.fromYaml(yaml));

        return new SimpleEnvironment.Builder()
                .configuration(conf)
                .build();
    }
}