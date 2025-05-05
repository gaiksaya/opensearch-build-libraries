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
        }
    }


}

/**
 * Merges the pull requests if all checks have passed or Enables auto merge on pull requests for merging it in future.
 * @param prUrl: Full https URL of the Pull Request
 */
private void enableAutoMerge(String prUrl) {
    try {
        withCredentials([usernamePassword(credentialsId: 'jenkins-github-bot-token', passwordVariable: 'GITHUB_TOKEN', usernameVariable: 'GITHUB_USER')]) {
            sh(
                    script: "gh pr merge ${prUrl} --auto -s",
                    returnStdout: true
            )
        }
    } catch (Exception ex) {
        println("Unable to enable auto merge on ${prUrl}!", ex.getMessage())
    }
}

private void reRunChecks(String prUrl) {
    try {
        withCredentials([usernamePassword(credentialsId: 'jenkins-github-bot-token', passwordVariable: 'GITHUB_TOKEN', usernameVariable: 'GITHUB_USER')]) {
            def failedRuns = sh(
                    script: "gh pr checks ${prUrl} --json link,state -q '.[] | select(.state==\"FAILURE\") | .link'",
                    returnStdout: true
            ).trim
            failedRuns.split('\n').each { run ->
                println "Failed run URL: ${run}"
//                // You can extract run ID if needed using regex
//                def runId = run.find(/runs\/(\d+)/) { match, id -> id }
//                println "Run ID: ${runId}"
            }
        }
    } catch (Exception ex) {
        echo("Unable to re-run checks for ${prUrl}")
    }
}
