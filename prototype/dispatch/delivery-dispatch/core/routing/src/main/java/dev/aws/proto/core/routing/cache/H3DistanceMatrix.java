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

package dev.aws.proto.core.routing.cache;

import com.uber.h3core.H3Core;
import dev.aws.proto.core.routing.H3;
import dev.aws.proto.core.routing.cache.persistence.ICachePersistence;
import dev.aws.proto.core.routing.distance.TravelDistance;
import dev.aws.proto.core.routing.location.Coordinate;
import dev.aws.proto.core.routing.location.ILocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class H3DistanceMatrix {
    private static final Logger logger = LoggerFactory.getLogger(H3DistanceMatrix.class);

    private final H3DistanceCache distanceCache;
    private final TravelDistance[][] matrix;
    private final Map<ILocation, Integer> locIdxLookup;

    public H3DistanceMatrix(H3DistanceCache h3DistanceCache, TravelDistance[][] matrix, Map<ILocation, Integer> locIdxLookup) {
        this.distanceCache = h3DistanceCache;
        this.matrix = matrix;
        this.locIdxLookup = locIdxLookup;
    }

    public TravelDistance distanceBetween(ILocation origin, ILocation destination) {
        logger.trace("Calculating distance between {} and {}", origin, destination);

        int indexFrom = locIdxLookup.get(origin);
        int indexTo = locIdxLookup.get(destination);

        return this.matrix[indexFrom][indexTo];
    }

    public static H3DistanceMatrix generate(ICachePersistence persistence, List<ILocation> locationList) {
        long start = System.currentTimeMillis();
        H3DistanceCache h3DistanceCache = persistence.importCache();
        H3Core h3 = H3.h3();
        Map<ILocation, Integer> locIdxLookup = new HashMap<>();
        int dim = locationList.size();

        for (int i = 0; i < dim; i++) {
            locIdxLookup.put(locationList.get(i), i);
        }

        TravelDistance[][] distances = new TravelDistance[dim][dim];

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                Coordinate coord1 = locationList.get(i).coordinate();
                Coordinate coord2 = locationList.get(j).coordinate();

                long hexa1 = h3.geoToH3(coord1.getLatitude(), coord1.getLongitude(), h3DistanceCache.getH3Resolution());
                long hexa2 = h3.geoToH3(coord2.getLatitude(), coord2.getLongitude(), h3DistanceCache.getH3Resolution());

                distances[i][j] = h3DistanceCache.getDistance(hexa1, hexa2);
            }
        }

        long generatedTime = System.currentTimeMillis() - start;

        logger.debug("H3DistanceMatrix :: calc time = {}ms :: dim = {}x{} :: per cell = {}ms", generatedTime, dim, dim, ((double) generatedTime / (dim * dim)));

        return new H3DistanceMatrix(h3DistanceCache, distances, locIdxLookup);
    }
}