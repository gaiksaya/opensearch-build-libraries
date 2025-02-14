/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package jenkins

import groovy.json.JsonOutput
import utils.OpenSearchMetricsQuery

class ReleaseCandidateStatus {
    OpenSearchMetricsQuery openSearchMetricsQueryObject
    String indexName
    String version

    ReleaseCandidateStatus(OpenSearchMetricsQuery openSearchMetricsQueryObject, String version, String indexName) {
        this.openSearchMetricsQueryObject = openSearchMetricsQueryObject
        this.indexName = indexName
        this.version = version
    }

    def getRcDistributionNumberQuery(Integer rcNumber = null, String componentName) {
        def queryMap = [
                _source: "distribution_build_number",
                sort: [
                    [
                        distribution_build_number: [
                            order: "desc"
                        ],
                        rc_number: [
                            order: "desc"
                        ]
                    ]
                ],
                size: 1,
                query: [
                    bool: [
                        filter: [
                            [
                                match_phrase: [
                                    component: "${componentName}"
                                ]
                            ],
                            [
                                match_phrase: [
                                    rc: "true"
                                ]
                            ],
                            [
                                match_phrase: [
                                    version: "${this.version}"
                                ]
                            ]
                        ]
                    ]
                ]
            ]

        if (rcNumber != null) {
            queryMap = [
                _source: "distribution_build_number",
                sort: [
                    [
                        distribution_build_number: [
                            order: "desc"
                        ]
                    ]
                ],
                size: 1,
                query: [
                    bool: [
                        filter: [
                            [
                                match_phrase: [
                                    component: "${componentName}"
                                ]
                            ],
                            [
                                match_phrase: [
                                    rc: "true"
                                ]
                            ],
                            [
                                match_phrase: [
                                    version: "${this.version}"
                                ]
                            ],
                            [
                                match_phrase: [
                                    rc_number: "${rcNumber}"
                                ]
                            ]
                        ]
                    ]
                ]
            ]
        }

        def query = JsonOutput.toJson(queryMap)
        return query.replace('"', '\\"')
    }

    def getLatestRcNumberQuery(String componentName){
        def queryMap = [
                _source: "rc_number",
                sort: [
                        [
                                distribution_build_number: [
                                        order: "desc"
                                ],
                                rc_number: [
                                        order: "desc"
                                ]
                        ]
                ],
                size: 1,
                query: [
                        bool: [
                                filter: [
                                        [
                                                match_phrase: [
                                                        component: "${componentName}"
                                                ]
                                        ],
                                        [
                                                match_phrase: [
                                                        rc: "true"
                                                ]
                                        ],
                                        [
                                                match_phrase: [
                                                        version: "${this.version}"
                                                ]
                                        ]
                                ]
                        ]
                ]
        ]
        def query = JsonOutput.toJson(queryMap)
        return query.replace('"', '\\"')
    }

    def getRcDistributionNumber(Integer rcNumber = null, String componentName) {
         def jsonResponse = this.openSearchMetricsQueryObject.fetchMetrics(indexName, getRcDistributionNumberQuery(rcNumber, componentName))
         def rcDistributionNumber = jsonResponse.hits.hits[0]._source.distribution_build_number
         return rcDistributionNumber
    }

    def getLatestRcNumber(String componentName) {
        def jsonResponse = this.openSearchMetricsQueryObject.fetchMetrics(indexName, getLatestRcNumberQuery(componentName))
        def rcNumber = jsonResponse.hits.hits[0]._source.rc_number
        return rcNumber
    }

}
