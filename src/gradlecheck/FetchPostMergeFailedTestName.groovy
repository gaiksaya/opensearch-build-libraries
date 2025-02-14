/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package gradlecheck

import groovy.json.JsonOutput
import utils.OpenSearchMetricsQuery

class FetchPostMergeFailedTestName  {
    OpenSearchMetricsQuery openSearchMetricsQueryObject
    String indexName

    FetchPostMergeFailedTestName(OpenSearchMetricsQuery openSearchMetricsQueryObject, String indexName) {
        this.openSearchMetricsQueryObject = openSearchMetricsQueryObject
        this.indexName = indexName
    }

    def getQuery(testName, gitReference) {
        def queryMap = [
                size: 200,
                query: [
                        bool: [
                                must: [
                                        [
                                                match: [
                                                        "invoke_type.keyword": [
                                                                query: "Post Merge Action",
                                                                operator: "OR",
                                                                prefix_length: 0,
                                                                max_expansions: 50,
                                                                fuzzy_transpositions: true,
                                                                lenient: false,
                                                                zero_terms_query: "NONE",
                                                                auto_generate_synonyms_phrase_query: true,
                                                                boost: 1
                                                        ]
                                                ]
                                        ],
                                        [
                                                match: [
                                                        test_status: [
                                                                query: "FAILED",
                                                                operator: "OR",
                                                                prefix_length: 0,
                                                                max_expansions: 50,
                                                                fuzzy_transpositions: true,
                                                                lenient: false,
                                                                zero_terms_query: "NONE",
                                                                auto_generate_synonyms_phrase_query: true,
                                                                boost: 1
                                                        ]
                                                ]
                                        ],
                                        [
                                                match: [
                                                        test_class: [
                                                                query: testName,
                                                                operator: "OR",
                                                                prefix_length: 0,
                                                                max_expansions: 50,
                                                                fuzzy_transpositions: true,
                                                                lenient: false,
                                                                zero_terms_query: "NONE",
                                                                auto_generate_synonyms_phrase_query: true,
                                                                boost: 1
                                                        ]
                                                ]
                                        ],
                                        [
                                                match: [
                                                        "git_reference.keyword": [
                                                                query: gitReference,
                                                                operator: "OR",
                                                                prefix_length: 0,
                                                                max_expansions: 50,
                                                                fuzzy_transpositions: true,
                                                                lenient: false,
                                                                zero_terms_query: "NONE",
                                                                auto_generate_synonyms_phrase_query: true,
                                                                boost: 1
                                                        ]
                                                ]
                                        ]
                                ],
                                adjust_pure_negative: true,
                                boost: 1
                        ]
                ],
                aggregations: [
                        test_name_keyword_agg: [
                                terms: [
                                        field: "test_name",
                                        size: 500
                                ]
                        ],
                        build_number_agg: [
                                terms: [
                                        field: "build_number",
                                        size: 500
                                ]
                        ],
                        pull_request_agg: [
                                terms: [
                                        field: "pull_request",
                                        size: 500
                                ]
                        ]
                ]
        ]

        def query = JsonOutput.toJson(queryMap)
        return query.replace('"', '\\"')

    }
    def getPostMergeFailedTestName(testName, gitReference) {
        return this.openSearchMetricsQueryObject.fetchMetrics(indexName, getQuery(testName, gitReference))
    }
}
