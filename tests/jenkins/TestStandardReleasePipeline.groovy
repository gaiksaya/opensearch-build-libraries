/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */


import jenkins.tests.BuildPipelineTest
import org.junit.*


class TestStandardReleasePipeline extends BuildPipelineTest {

    @Before
    void setUp() {
        def config = [
        overrideAgent: 'AL2-X64',
        overrideDockerImage: 'test:image'
        ]
        this.registerLibTester(new StandardReleasePipelineLibTester(config))
        super.setUp()
    }

    @Test
    void testStandardReleasePipeline() {
        super.testPipeline("tests/jenkins/jobs/StandardReleasePipeline_JenkinsFile")
    }
}
