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
@param args.publicationType - github or artifact
@param args.artifactPath <required if publicationType=artifact> - Artifact path to publish to NPM. Defaults to package.json See supported artifacts https://docs.npmjs.com/cli/v9/commands/npm-publish?v=true#description for more details.
*/
void call(env, Map args = [:]) {
    body.delegate = [env: env]
    allowedPublicationType = ['github', 'artifact']
    artifactPath = args.artifactPath ?: ''

    if (!allowedPublicationType.contains(args.publicationType)) {
        currentBuild.result = 'ABORTED'
        error('Invalid publicationType. publicationType can either be github or artifact')
    }
    if (args.publicationType == 'artifact' && !args.artifactPath) {
        currentBuild.result = 'ABORTED'
        error('publicationType: artifact needs an artifactPath. Please provide artifactPath argument. See supported artifacts https://docs.npmjs.com/cli/v9/commands/npm-publish?v=true#description for more details')
    }
    if (args.publicationType == 'github') {
        checkout([$class: 'GitSCM', branches: [[name: "$env.tag" ]], userRemoteConfigs: [[url: "$env.repository" ]]])
    }

    withCredentials([string(credentialsId: 'jenkins-opensearch-publish-to-npm-token', variable: 'NPM_TOKEN')]) {
        sh """
            npm set registry "https://registry.npmjs.org"
            npm set //registry.npmjs.org/:_authToken ${NPM_TOKEN}
            npm publish ${artifactPath} --dry-run && npm publish ${artifactPath} --access public
        """
    }

    println('Cleaning up')
    sh """rm -rf ${WORKSPACE}/.nvmrc && ls -a ~/ | grep .nvmrc"""
}
