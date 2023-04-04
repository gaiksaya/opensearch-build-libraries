/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

/** Library to publish artifacts to NPM registry under @opensearch-project namespace.
@param Map args = [:] args A map of the following parameters
@param args.repository <required> - Repository to be used to publish the artifact to npm.
@param args.tag <required> - Tag reference to be used to publish the artifact.
OR
@param args.artifactPath <required> - Artifact path to publish to NPM. Defaults to package.json See supported artifacts https://docs.npmjs.com/cli/v9/commands/npm-publish?v=true#description for more details.
*/
void call(Map args = [:]) {
    artifactPath = args.artifactPath ?: ''

    if (args.artifactPath && args.repository && args.tag) {
        currentBuild.result = 'ABORTED'
        error('Artifact Path and GitHub repository details are mutually exclusive. Please use either repository and tag OR artifactPath as arguments')
    }
    if (!args.artifactPath) {
        checkout([$class: 'GitSCM', branches: [[name: "${args.tag}" ]], userRemoteConfigs: [[url: "${args.repository}" ]]])
    }

    withCredentials([string(credentialsId: 'jenkins-opensearch-publish-to-npm-token', variable: 'NPM_TOKEN')]) {
        sh """
            npm set registry "https://registry.npmjs.org"
            npm set //registry.npmjs.org/:_authToken ${NPM_TOKEN}
            npm publish ${artifactPath} --dry-run
            npm publish ${artifactPath} --access public
        """
    }
}
