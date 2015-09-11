/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.index.query;

import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.GeoUtils;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.mapper.geo.GeoPointFieldMapper;

import java.io.IOException;

/**
 * <pre>
 * {
 *     "name.lat" : 1.1,
 *     "name.lon" : 1.2,
 * }
 * </pre>
 */
public class GeoDistanceRangeQueryParser extends BaseQueryParser<GeoDistanceRangeQueryBuilder> {

    public static final ParseField FROM_FIELD = new ParseField("from");
    public static final ParseField TO_FIELD = new ParseField("to");
    public static final ParseField INCLUDE_LOWER_FIELD = new ParseField("include_lower");
    public static final ParseField INCLUDE_UPPER_FIELD = new ParseField("include_upper");
    public static final ParseField GT_FIELD = new ParseField("gt");
    public static final ParseField GTE_FIELD = new ParseField("gte", "ge");
    public static final ParseField LT_FIELD = new ParseField("lt");
    public static final ParseField LTE_FIELD = new ParseField("lte", "le");
    public static final ParseField UNIT_FIELD = new ParseField("unit");
    public static final ParseField DISTANCE_TYPE_FIELD = new ParseField("distance_type");
    public static final ParseField NAME_FIELD = new ParseField("_name");
    public static final ParseField BOOST_FIELD = new ParseField("boost");
    public static final ParseField OPTIMIZE_BBOX_FIELD = new ParseField("optimize_bbox");
    public static final ParseField COERCE_FIELD = new ParseField("coerce", "normalize");
    public static final ParseField IGNORE_MALFORMED_FIELD = new ParseField("ignore_malformed");

    @Inject
    public GeoDistanceRangeQueryParser() {
    }

    @Override
    public String[] names() {
        return new String[]{GeoDistanceRangeQueryBuilder.NAME, "geoDistanceRange"};
    }

    @Override
    public GeoDistanceRangeQueryBuilder getBuilderPrototype() {
        return GeoDistanceRangeQueryBuilder.PROTOTYPE;
    }

    @Override
    public GeoDistanceRangeQueryBuilder fromXContent(QueryParseContext parseContext) throws IOException, QueryParsingException {
        XContentParser parser = parseContext.parser();

        XContentParser.Token token;

        Float boost = null;
        String queryName = null;
        String currentFieldName = null;
        GeoPoint point = null;
        String geohash = null;
        String fieldName = null;
        Object vFrom = null;
        Object vTo = null;
        Boolean includeLower = null;
        Boolean includeUpper = null;
        DistanceUnit unit = null;
        GeoDistance geoDistance = null;
        String optimizeBbox = null;
        Boolean coerce = null;
        Boolean ignoreMalformed = null;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (parseContext.isDeprecatedSetting(currentFieldName)) {
                // skip
            } else if (token == XContentParser.Token.START_ARRAY) {
                if (point == null) {
                    point = new GeoPoint();
                }
                GeoUtils.parseGeoPoint(parser, point);
                fieldName = currentFieldName;
            } else if (token == XContentParser.Token.START_OBJECT) {
                // the json in the format of -> field : { lat : 30, lon : 12 }
                fieldName = currentFieldName;
                if (point == null) {
                    point = new GeoPoint();
                }
                GeoUtils.parseGeoPoint(parser, point);
            } else if (token.isValue()) {
                if (parseContext.parseFieldMatcher().match(currentFieldName, FROM_FIELD)) {
                    if (token == XContentParser.Token.VALUE_NULL) {
                    } else if (token == XContentParser.Token.VALUE_STRING) {
                        vFrom = parser.text(); // a String
                    } else {
                        vFrom = parser.numberValue(); // a Number
                    }
                } else if (parseContext.parseFieldMatcher().match(currentFieldName, TO_FIELD)) {
                    if (token == XContentParser.Token.VALUE_NULL) {
                    } else if (token == XContentParser.Token.VALUE_STRING) {
                        vTo = parser.text(); // a String
                    } else {
                        vTo = parser.numberValue(); // a Number
                    }
                } else if (parseContext.parseFieldMatcher().match(currentFieldName, INCLUDE_LOWER_FIELD)) {
                    includeLower = parser.booleanValue();
                } else if (parseContext.parseFieldMatcher().match(currentFieldName, INCLUDE_UPPER_FIELD)) {
                    includeUpper = parser.booleanValue();
                } else if (parseContext.parseFieldMatcher().match(currentFieldName, GT_FIELD)) {
                    if (token == XContentParser.Token.VALUE_NULL) {
                    } else if (token == XContentParser.Token.VALUE_STRING) {
                        vFrom = parser.text(); // a String
                    } else {
                        vFrom = parser.numberValue(); // a Number
                    }
                    includeLower = false;
                } else if (parseContext.parseFieldMatcher().match(currentFieldName, GTE_FIELD)) {
                    if (token == XContentParser.Token.VALUE_NULL) {
                    } else if (token == XContentParser.Token.VALUE_STRING) {
                        vFrom = parser.text(); // a String
                    } else {
                        vFrom = parser.numberValue(); // a Number
                    }
                    includeLower = true;
                } else if (parseContext.parseFieldMatcher().match(currentFieldName, LT_FIELD)) {
                    if (token == XContentParser.Token.VALUE_NULL) {
                    } else if (token == XContentParser.Token.VALUE_STRING) {
                        vTo = parser.text(); // a String
                    } else {
                        vTo = parser.numberValue(); // a Number
                    }
                    includeUpper = false;
                } else if (parseContext.parseFieldMatcher().match(currentFieldName, LTE_FIELD)) {
                    if (token == XContentParser.Token.VALUE_NULL) {
                    } else if (token == XContentParser.Token.VALUE_STRING) {
                        vTo = parser.text(); // a String
                    } else {
                        vTo = parser.numberValue(); // a Number
                    }
                    includeUpper = true;
                } else if (parseContext.parseFieldMatcher().match(currentFieldName, UNIT_FIELD)) {
                    unit = DistanceUnit.fromString(parser.text());
                } else if (parseContext.parseFieldMatcher().match(currentFieldName, DISTANCE_TYPE_FIELD)) {
                    geoDistance = GeoDistance.fromString(parser.text());
                } else if (currentFieldName.endsWith(GeoPointFieldMapper.Names.LAT_SUFFIX)) {
                    if (point == null) {
                        point = new GeoPoint();
                    }
                    point.resetLat(parser.doubleValue());
                    fieldName = currentFieldName.substring(0, currentFieldName.length() - GeoPointFieldMapper.Names.LAT_SUFFIX.length());
                } else if (currentFieldName.endsWith(GeoPointFieldMapper.Names.LON_SUFFIX)) {
                    if (point == null) {
                        point = new GeoPoint();
                    }
                    point.resetLon(parser.doubleValue());
                    fieldName = currentFieldName.substring(0, currentFieldName.length() - GeoPointFieldMapper.Names.LON_SUFFIX.length());
                } else if (currentFieldName.endsWith(GeoPointFieldMapper.Names.GEOHASH_SUFFIX)) {
                    geohash = parser.text();
                    fieldName = currentFieldName.substring(0, currentFieldName.length() - GeoPointFieldMapper.Names.GEOHASH_SUFFIX.length());
                } else if (parseContext.parseFieldMatcher().match(currentFieldName, NAME_FIELD)) {
                    queryName = parser.text();
                } else if (parseContext.parseFieldMatcher().match(currentFieldName, BOOST_FIELD)) {
                    boost = parser.floatValue();
                } else if (parseContext.parseFieldMatcher().match(currentFieldName, OPTIMIZE_BBOX_FIELD)) {
                    optimizeBbox = parser.textOrNull();
                } else if (parseContext.parseFieldMatcher().match(currentFieldName, COERCE_FIELD)) {
                    coerce = parser.booleanValue();
                } else if (parseContext.parseFieldMatcher().match(currentFieldName, IGNORE_MALFORMED_FIELD)) {
                    ignoreMalformed = parser.booleanValue();
                } else {
                    if (point == null) {
                        point = new GeoPoint();
                    }
                    point.resetFromString(parser.text());
                    fieldName = currentFieldName;
                }
            }
        }

        GeoDistanceRangeQueryBuilder queryBuilder = new GeoDistanceRangeQueryBuilder(fieldName);

        if (boost != null) {
            queryBuilder.boost(boost);
        }

        if (queryName != null) {
            queryBuilder.queryName(queryName);
        }

        if (point != null) {
            queryBuilder.point(point.lat(), point.lon());
        }

        if (geohash != null) {
            queryBuilder.geohash(geohash);
        }

        if (vFrom != null) {
            queryBuilder.from(vFrom);
        }

        if (vTo != null) {
            queryBuilder.to(vTo);
        }

        if (includeUpper != null) {
            queryBuilder.includeUpper(includeUpper);
        }

        if (includeLower != null) {
            queryBuilder.includeLower(includeLower);
        }

        if (unit != null) {
            queryBuilder.unit(unit);
        }

        if (geoDistance != null) {
            queryBuilder.geoDistance(geoDistance);
        }

        if (optimizeBbox != null) {
            queryBuilder.optimizeBbox(optimizeBbox);
        }

        if (coerce != null) {
            queryBuilder.coerce(coerce);
        }

        if (ignoreMalformed != null) {
            queryBuilder.ignoreMalformed(ignoreMalformed);
        }
        return queryBuilder;
    }
}