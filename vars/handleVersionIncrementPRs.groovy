/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import jenkins.ReleaseMetricsData

/**
 * Library to handle version increment PRs across all components under opensearch-project. This Library enables auto-merge for version increment PRs, re-runs CI if upstream dependency is merged.
 * @param Map args = [:] args A map of the following parameters
 * @param args.version <required> - Tracking version for version increment PRs.
 */

void call(Map args = [:]) {
    if (!args.version) {
        error ('Version is required parameter.')
    }
    def versionTokenize = args.version.tokenize('-')
    // Qualifiers are not a part of the labels in GitHub. Ignoring it.
    def version = versionTokenize[0]

    withCredentials([
            string(credentialsId: 'jenkins-health-metrics-account-number', variable: 'METRICS_HOST_ACCOUNT'),
            string(credentialsId: 'jenkins-health-metrics-cluster-endpoint', variable: 'METRICS_HOST_URL')]) {
        withAWS(role: 'OpenSearchJenkinsAccessRole', roleAccount: "${METRICS_HOST_ACCOUNT}", duration: 900, roleSessionName: 'jenkins-session') {
            def releaseMetrics = new ReleaseMetricsData(env.METRICS_HOST_URL, env.AWS_ACCESS_KEY_ID, env.AWS_SECRET_ACCESS_KEY, env.AWS_SESSION_TOKEN, version, 'github_pulls', this)
            def pullRequestUrls = releaseMetrics.getVersionIncrementPrsUrls()
            echo pullRequestUrls
        }
    }
}
