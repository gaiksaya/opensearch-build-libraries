/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
import jenkins.ComponentRepoData
import utils.TemplateProcessor
import java.time.LocalDate

/**
 * Library to check and notify missing code coverage.
 * @param Map args = [:] args A map of the following parameters
 * @param args.inputManifest <required> - Array of input manifest(s). eg: ["manifests/2.0.0/opensearch-2.0.0.yml", "manifests/2.0.0/opensearch-dashboards-2.0.0.yml"]
 * @param args.action <optional> - Action to perform. Default is 'check'. Acceptable values are 'check' and 'notify'.
 */
void call(Map args = [:]) {
    def lib = library(identifier: 'jenkins@main', retriever: legacySCM(scm))
    def templateProcessor = lib.utils.TemplateProcessor.new(this)
    String action = args.action ?: 'check'
    // Parameter check
    validateParameters(args, action)
    def inputManifests = args.inputManifest
    def inputManifestYaml = readYaml(file: args.inputManifest[0])
    def version = inputManifestYaml.build.version
    version = version.tokenize('-')[0] // Get only version and skip the qualifier
    def now = LocalDate.now()
    def monthYear = String.format("%02d-%d", now.monthValue, now.year)
    def codeCoverageIndex = "opensearch-codecov-metrics-${monthYear}"
    def componentsMissingCodeCoverageWithUrl = [:]

    inputManifests.each { inputManifestFile ->
        def inputManifestObj = readYaml(file: inputManifestFile)
        withCredentials([
                string(credentialsId: 'jenkins-health-metrics-account-number', variable: 'METRICS_HOST_ACCOUNT'),
                string(credentialsId: 'jenkins-health-metrics-cluster-endpoint', variable: 'METRICS_HOST_URL')]) {
            withAWS(role: 'OpenSearchJenkinsAccessRole', roleAccount: "${METRICS_HOST_ACCOUNT}", duration: 900, roleSessionName: 'jenkins-session') {
                def metricsUrl = env.METRICS_HOST_URL
                def awsAccessKey = env.AWS_ACCESS_KEY_ID
                def awsSecretKey = env.AWS_SECRET_ACCESS_KEY
                def awsSessionToken = env.AWS_SESSION_TOKEN

                def componentRepoData = new ComponentRepoData(metricsUrl, awsAccessKey, awsSecretKey, awsSessionToken, version, this)
                inputManifestObj.components.each { component ->
                    String repoName = component.repository.toString().split('/')[-1].replace('.git', '')
                    def codeCoverage = componentRepoData.getCodeCoverage(repoName, codeCoverageIndex)
                    if (!codeCoverage.isEmpty() && codeCoverage.state == "no-coverage") { // Also equivalent to codeCoverage.coverage == 0
                        componentsMissingCodeCoverageWithUrl[component.name] = codeCoverage.url
                        if (args.action == 'notify') {
                            def binding = [
                                    BRANCH: codeCoverage.branch,
                                    CODECOV_URL: codeCoverage.url
                            ]
                            templateProcessor.process("release/missing-code-coverage.md", binding, "${WORKSPACE}")
                        }
                    }
                }
            }
        }
    }

    echo("Components missing code coverage are: ${componentsMissingCodeCoverageWithUrl}")
}

/**
 * Validates input parameters
 */
private void validateParameters(Map args, action) {
    if (!args.inputManifest || args.inputManifest.isEmpty()) {
        error "inputManifest parameter is required."
    } else {
        args.inputManifest.each { inputManifestFile ->
            if (!fileExists(inputManifestFile)) {
                error("Invalid path. Input manifest file does not exist at ${inputManifestFile}")
            }
        }
    }

    List<String> validActions = ['check', 'notify']
    if (!validActions.contains(action)) {
        error "Invalid action '${action}'. Valid values: ${validActions.join(', ')}"
    }
}

///**
// * Notify components regarding the missing release notes by adding a comment to the release issue.
// * @param version: Release version.
// * @param componentsWithFalseStatus: Parsed content with status as False.
// */
//private void notifyReleaseOwners(String version,def componentsWithFalseStatus) {
//    componentsWithFalseStatus.each { component ->
//        withCredentials([
//                string(credentialsId: 'jenkins-health-metrics-account-number', variable: 'METRICS_HOST_ACCOUNT'),
//                string(credentialsId: 'jenkins-health-metrics-cluster-endpoint', variable: 'METRICS_HOST_URL')]) {
//            withAWS(role: 'OpenSearchJenkinsAccessRole', roleAccount: "${METRICS_HOST_ACCOUNT}", duration: 900, roleSessionName: 'jenkins-session') {
//                def metricsUrl = env.METRICS_HOST_URL
//                def awsAccessKey = env.AWS_ACCESS_KEY_ID
//                def awsSecretKey = env.AWS_SECRET_ACCESS_KEY
//                def awsSessionToken = env.AWS_SESSION_TOKEN
//
//                ReleaseMetricsData releaseMetricsData = new ReleaseMetricsData(metricsUrl, awsAccessKey, awsSecretKey, awsSessionToken, version, this)
//                def componentName = component["Component"]
//                if(componentName != 'functionalTestDashboards') {
//                    def releaseIssueUrl = releaseMetricsData.getReleaseIssue("${componentName}", "component.keyword")
//                    def ghCommentContent = getNotificationMessageBodyFile(componentName, component["Branch"] )
//                    addComment(releaseIssueUrl, ghCommentContent)
//                }
//            }
//        }
//    }
//}
//
///**
// * Add a comment on the Release issue.
// * @param releaseIssueUrl: Component release issue URL.
// * @param commentBodyFile: Path to the file containing GitHub comment content.
// */
//private void addComment(String releaseIssueUrl, def commentBodyFile) {
//    withCredentials([usernamePassword(credentialsId: 'jenkins-github-bot-token', passwordVariable: 'GITHUB_TOKEN', usernameVariable: 'GITHUB_USER')]) {
//        sh(
//                script: "gh issue comment ${releaseIssueUrl} --body-file ${commentBodyFile}",
//                returnStdout: true
//        )
//    }
//}
/**
 * Get notification message body file.
 * @param component: Component Name.
 * @param branch: Branch against which release notes are checked.
 */
//def getNotificationMessageBodyFile(String branch, String codecovUrl) {
//    def lib = library(identifier: 'jenkins@main', retriever: legacySCM(scm))
//    def binding = [
//            BRANCH: branch,
//            CODECOV_URL: codecovUrl
//    ]
//    lib.utils.TemplateProcessor.new(this).process("release/missing-code-coverage.md", binding, "${WORKSPACE}")
//}
