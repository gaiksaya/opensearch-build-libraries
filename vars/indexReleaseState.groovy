/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

void call(Map args = [:]) {
    def a = { checkIntegTestResultsOverview(inputManifest: []) }
    def b = { checkUnpatchedVulnerabilities(version: '3.8.0', product: 'opensearch') }
    echo("Indexing release state for version 3.8.0.")
}
