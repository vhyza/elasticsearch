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
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.query.support.QueryInnerHits;

import java.io.IOException;


public class HasParentQueryParser extends BaseQueryParser  {

    private static final HasParentQueryBuilder PROTOTYPE = new HasParentQueryBuilder("", EmptyQueryBuilder.PROTOTYPE);
    private static final ParseField QUERY_FIELD = new ParseField("query", "filter");
    private static final ParseField SCORE_FIELD = new ParseField("score_type", "score_mode").withAllDeprecated("score");
    private static final ParseField TYPE_FIELD = new ParseField("parent_type", "type");

    @Override
    public String[] names() {
        return new String[]{HasParentQueryBuilder.NAME, Strings.toCamelCase(HasParentQueryBuilder.NAME)};
    }

    @Override
    public QueryBuilder fromXContent(QueryParseContext parseContext) throws IOException, QueryParsingException {
        XContentParser parser = parseContext.parser();

        float boost = AbstractQueryBuilder.DEFAULT_BOOST;
        String parentType = null;
        boolean score = false;
        String queryName = null;
        QueryInnerHits innerHits = null;

        String currentFieldName = null;
        XContentParser.Token token;
        QueryBuilder iqb = null;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token == XContentParser.Token.START_OBJECT) {
                // Usually, the query would be parsed here, but the child
                // type may not have been extracted yet, so use the
                // XContentStructure.<type> facade to parse if available,
                // or delay parsing if not.
                if (parseContext.parseFieldMatcher().match(currentFieldName, QUERY_FIELD)) {
                    iqb = parseContext.parseInnerQueryBuilder();
                } else if ("inner_hits".equals(currentFieldName)) {
                    innerHits = new QueryInnerHits(parser);
                } else {
                    throw new QueryParsingException(parseContext, "[has_parent] query does not support [" + currentFieldName + "]");
                }
            } else if (token.isValue()) {
                if (parseContext.parseFieldMatcher().match(currentFieldName, TYPE_FIELD)) {
                    parentType = parser.text();
                } else if (parseContext.parseFieldMatcher().match(currentFieldName, SCORE_FIELD)) {
                    // deprecated we use a boolean now
                    String scoreTypeValue = parser.text();
                    if ("score".equals(scoreTypeValue)) {
                        score = true;
                    } else if ("none".equals(scoreTypeValue)) {
                        score = false;
                    }
                } else if ("score".equals(currentFieldName)) {
                    score = parser.booleanValue();
                } else if ("boost".equals(currentFieldName)) {
                    boost = parser.floatValue();
                } else if ("_name".equals(currentFieldName)) {
                    queryName = parser.text();
                } else {
                    throw new QueryParsingException(parseContext, "[has_parent] query does not support [" + currentFieldName + "]");
                }
            }
        }
        return new HasParentQueryBuilder(parentType, iqb, score, innerHits).queryName(queryName).boost(boost);
    }

    @Override
    public HasParentQueryBuilder getBuilderPrototype() {
        return PROTOTYPE;
    }
}