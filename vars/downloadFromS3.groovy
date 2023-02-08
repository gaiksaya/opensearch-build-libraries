/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
 /**
Library to download the artifacts from S3 bucket to local
@param Map[assumedRoleName] <Required> - Role to be assumed to download artifacts.
@param Map[roleAccountNumberCred] <Required> - AWS account number to download artifacts from.
@param Map[downloadPath] <Required> - S3 path to download artifacts from.
@param Map[bucketName] <Required> -S3 bucket Name.
@param Map[localPath] <Required> - Local path to write artifacts to.
@param Map[force]<Optional> - Force download. Defaults to false
*/
void call(Map args = [:]) {
    boolean forceDownload = args.force ?: false

    withCredentials([string(credentialsId: "${args.roleAccountNumberCred}", variable: 'AWS_ACCOUNT_NUMBER')]) {
            withAWS(role: args.assumedRoleName, roleAccount: "${AWS_ACCOUNT_NUMBER}", duration: 900, roleSessionName: 'jenkins-session') {
                s3Download(file: args.downloadPath, bucket: args.bucketName, path: args.localPath, force: "${forceDownload}")
            }
    }
}
