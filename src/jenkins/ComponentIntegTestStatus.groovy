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

class ComponentIntegTestStatus {
    OpenSearchMetricsQuery openSearchMetricsQueryObject
    String indexName
    String product
    String version
    String distributionBuildNumber

    ComponentIntegTestStatus(OpenSearchMetricsQuery openSearchMetricsQueryObject, String indexName, String product, String version, String distributionBuildNumber) {
        this.openSearchMetricsQueryObject = openSearchMetricsQueryObject
        this.indexName = indexName
        this.product = product
        this.version = version
        this.distributionBuildNumber = distributionBuildNumber
    }

    def getQuery(String componentIntegTestResult) {
        def queryMap = [
                size   : 50,
                _source: [
                        "component"
                ],
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
                                                        component_category: "${this.product}"
                                                ]
                                        ],
                                        [
                                                match_phrase: [
                                                        distribution_build_number: "${this.distributionBuildNumber}"
                                                ]
                                        ],
                                        [
                                                match_phrase: [
                                                        component_build_result: "${componentIntegTestResult}"
                                                ]
                                        ]
                                ]
                        ]
                ]
        ]
        def query = JsonOutput.toJson(queryMap)
        return query.replace('"', '\\"')
    }

    def componentIntegTestFailedDataQuery(String component) {
        def queryMap = [
                _source: [
                        "platform",
                        "architecture",
                        "distribution",
                        "test_report_manifest_yml",
                        "integ_test_build_url",
                        "rc_number"
                ],
                query  : [
                        bool: [
                                filter: [
                                        [
                                                match_phrase: [
                                                        component: "${component}"
                                                ]
                                        ],
                                        [
                                                match_phrase: [
                                                        version: "${this.version}"
                                                ]
                                        ],
                                        [
                                                match_phrase: [
                                                        distribution_build_number: "${this.distributionBuildNumber}"
                                                ]
                                        ]
                                ]
                        ]
                ]
        ]
        def query = JsonOutput.toJson(queryMap)
        return query.replace('"', '\\"')
    }

    def getComponents(String componentBuildResult) {
        def jsonResponse = this.openSearchMetricsQueryObject.fetchMetrics(indexName, getQuery(componentBuildResult))
        def components = jsonResponse.hits.hits.collect { it._source.component }
        return components
    }

    def getComponentIntegTestFailedData(String component) {
        def jsonResponse = this.openSearchMetricsQueryObject.fetchMetrics(indexName, componentIntegTestFailedDataQuery(component))
        return jsonResponse
    }

}
