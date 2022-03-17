/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.aws.proto.apps.instant.sequential.data;

import dev.aws.proto.apps.appcore.config.DriverClientProperties;
import dev.aws.proto.apps.appcore.config.DriverQueryProperties;
import dev.aws.proto.apps.instant.sequential.domain.planning.PlanningDriver;
import dev.aws.proto.core.exception.DispatcherException;
import dev.aws.proto.core.util.aws.SsmUtility;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import javax.enterprise.context.ApplicationScoped;
import java.net.URI;

@ApplicationScoped
public class DriverQueryManager extends dev.aws.proto.apps.appcore.data.DriverQueryManager<ApiDriver, PlanningDriver> {

    DriverQueryClient driverQueryClient;

    DriverQueryManager(DriverClientProperties driverClientProperties, DriverQueryProperties driverQueryProperties) {
        this.driverClientProperties = driverClientProperties;
        this.driverQueryProperties = driverQueryProperties;

        String driverApiUrl = SsmUtility.getParameterValue(driverClientProperties.driverApiUrlParameterName());

        if (driverApiUrl == null || driverApiUrl.equalsIgnoreCase("")) {
            throw new DispatcherException("Driver API URL empty. Quitting...");
        }

        this.driverQueryClient = RestClientBuilder.newBuilder()
                .baseUri(URI.create(driverApiUrl))
                .build(DriverQueryClient.class);
    }

    @Override
    public DriverQueryClient getDriverQueryClient() {
        return this.driverQueryClient;
    }
}
