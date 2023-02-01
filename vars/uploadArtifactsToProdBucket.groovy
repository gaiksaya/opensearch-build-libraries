/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

 /**
 * Library to upload artifacts to artifacts.opensearch.org
@param Map[roleName] <required> - IAM role to be assumed for uploading artifacts
@param Map[source] <required> - Path to yml or artifact file.
@param Map[destination] <required> - Artifact type in the manifest, [type] is required for signing yml.
 */
void call(Map args = [:]) {
    withCredentials([
        string(credentialsId: "${args.roleName}", variable: 'ARTIFACT_PROMOTION_ROLE_NAME'),
        string(credentialsId: 'jenkins-aws-production-account', variable: 'AWS_ACCOUNT_ARTIFACT'),
        string(credentialsId: 'jenkins-artifact-production-bucket-name', variable: 'ARTIFACT_PRODUCTION_BUCKET_NAME')]) {
            withAWS(role: "${ARTIFACT_PROMOTION_ROLE_NAME}", roleAccount: "${AWS_ACCOUNT_ARTIFACT}", duration: 900, roleSessionName: 'jenkins-session') {
                s3Upload(file: "${source}", bucket: "${ARTIFACT_PRODUCTION_BUCKET_NAME}", path: "${destination}")
        }
    }
}
