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

class ReleaseMetricsData {
    String version
    String indexName
    OpenSearchMetricsQuery openSearchMetricsQueryObject

    ReleaseMetricsData(OpenSearchMetricsQuery openSearchMetricsQueryObject, String version, String indexName = 'opensearch_release_metrics') {
        this.openSearchMetricsQueryObject = openSearchMetricsQueryObject
        this.version = version
        this.indexName = indexName
    }

    String getReleaseOwnerQuery(String component) {
        def queryMap = [
                size   : 1,
                _source: "release_owners",
                query  : [
                        bool: [
                                filter: [
                                        [
                                                match_phrase: [
                                                        version: "${this.version}"
                                                ]
                                        ],
                                        [
                                                match_phrase: [
                                                        "component": "${component}"
                                                ]
                                        ]
                                ]
                        ]
                ],
                sort   : [
                        [
                                current_date: [
                                        order: "desc"
                                ]
                        ]
                ]
        ]
        String query = JsonOutput.toJson(queryMap)
        return query.replace('"', '\\"')
    }

    String getReleaseIssueQuery(String repository) {
        def queryMap = [
                size   : 1,
                _source: "release_issue",
                query  : [
                        bool: [
                                filter: [
                                        [
                                                match_phrase: [
                                                        version: "${this.version}"
                                                ]
                                        ],
                                        [
                                                match_phrase: [
                                                        repository: "${repository}"
                                                ]
                                        ]
                                ]
                        ]
                ],
                sort   : [
                        [
                                current_date: [
                                        order: "desc"
                                ]
                        ]
                ]
        ]
        String query = JsonOutput.toJson(queryMap)
        return query.replace('"', '\\"')
    }

    ArrayList getReleaseOwners(String component) {
        def jsonResponse = this.openSearchMetricsQueryObject.fetchMetrics(indexName, getReleaseOwnerQuery(component))
        def releaseOwners = jsonResponse.hits.hits._source.release_owners.flatten()
        return releaseOwners
    }

    String getReleaseIssue(String repository) {
        def jsonResponse = this.openSearchMetricsQueryObject.fetchMetrics(indexName, getReleaseIssueQuery(repository))
        def releaseIssue = jsonResponse.hits.hits._source.release_issue[0]
        return releaseIssue
    }
}
