/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
import static org.hamcrest.CoreMatchers.notNullValue
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.CoreMatchers.anyOf
import static org.hamcrest.CoreMatchers.equalTo

class DownloadFromS3LibTester extends LibFunctionTester {

    private String assumedRoleName
    private String roleAccountNumberCred
    private String downloadPath
    private String bucketName
    private String localPath
    private boolean force

    public DownloadFromS3LibTester(assumedRoleName, roleAccountNumberCred, downloadPath, bucketName, localPath, force){
        this.assumedRoleName = assumedRoleName
        this.roleAccountNumberCred = roleAccountNumberCred
        this.downloadPath = downloadPath
        this.bucketName = bucketName
        this.localPath = localPath
        this.force = force
    }

    void parameterInvariantsAssertions(call){
        assertThat(call.args.downloadPath.first(), notNullValue())
        assertThat(call.args.assumedRoleName.first(), notNullValue())
        assertThat(call.args.roleAccountNumberCred.first(), notNullValue())
        assertThat(call.args.bucketName.first(), notNullValue())
        assertThat(call.args.localPath.first(), notNullValue())
        assertThat(call.args.force.first(), notNullValue())
        assertThat(call.args.force.first().toString(), anyOf(equalTo('true'), equalTo('false')))
    }

    boolean expectedParametersMatcher(call) {
        return call.args.downloadPath.first().toString().equals(this.downloadPath)
                && call.args.assumedRoleName.first().toString().equals(this.assumedRoleName)
                && call.args.roleAccountNumberCred.first().toString().equals(this.roleAccountNumberCred)
                && call.args.bucketName.first().toString().equals(this.bucketName)
                && call.args.localPath.first().toString().equals(this.localPath)
                && call.args.force.first().toString().equals(this.force.toString())
    }

    String libFunctionName(){
        return 'downloadFromS3'
    }

    void configure(helper, binding){
        helper.registerAllowedMethod("s3Download", [Map])
        helper.registerAllowedMethod("withCredentials", [Map])
        helper.registerAllowedMethod("withAWS", [Map, Closure], { args, closure ->
            closure.delegate = delegate
            return helper.callClosure(closure)
        })
    }
}
