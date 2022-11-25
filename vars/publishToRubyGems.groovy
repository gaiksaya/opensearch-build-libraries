/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

/** Library to publish ruby gems to rubygems.org registry with OpenSearch as the owner.
@param Map args = [:] args A map of the following parameters
@param args.apiKey <required> - Credential id consisting token for publishing the package
@param args.gemsDir <optional> - The directory containing gems to be published. Defaults to 'dist'
*/
void call(Map args = [:]) {
    String releaseArtifactsDir = args.gemsDir ? "${WORKSPACE}/${args.gemsDir}" : "${WORKSPACE}/dist"

    try {
        sh "cd ${releaseArtifactsDir} && gem install `ls *.gem` -P HighSecurity"
    } catch (Exception e) {
        echo "${gemName} is not signed. Please sign the gem before retrying"
    } finally {
        withCredentials([string(credentialsId: "${args.apiKey}", variable: 'API_KEY')]) {
                sh """curl --fail --data-binary @`ls *.gem` -H 'Authorization:${API_KEY}' -H 'Content-Type: application/octet-stream' https://rubygems.org/api/v1/gems"""
        }
    }
}
