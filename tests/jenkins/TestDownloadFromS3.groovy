/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import jenkins.tests.BuildPipelineTest
import org.junit.Before
import org.junit.Test
import static org.hamcrest.CoreMatchers.hasItem
import static org.hamcrest.MatcherAssert.assertThat
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString


class TestDownloadFromS3 extends BuildPipelineTest {

    @Before
    void setUp() {

        this.registerLibTester(new DownloadFromS3LibTester(
            'tmp-role',
            'role-credential-id',
            '/download/path',
            'dummy_bucket',
            "/tmp",
            false
            ))

        super.setUp()
    }

    @Test
    public void testDownloadFromS3() {
        super.testPipeline('tests/jenkins/jobs/DownloadFromS3_Jenkinsfile')
    }

    @Test
    void verify_args(){
        runScript('tests/jenkins/jobs/DownloadFromS3_Jenkinsfile')
        def aws = getMethodCall('withAWS')
        assertThat(aws, hasItem('{role=tmp-role, roleAccount=AWS_ACCOUNT_NUMBER, duration=900, roleSessionName=jenkins-session}, groovy.lang.Closure'))
        def download = getMethodCall('s3Download')
        assertThat(download, hasItem('{file=/tmp, bucket=dummy_bucket, path=/download/path, force=false}'))
    }

    def getMethodCall(methodName) {
        def shCommands = helper.callStack.findAll { call ->
            call.methodName == methodName
        }.collect { call ->
            callArgsToString(call)
        }
        return shCommands
    }
}
