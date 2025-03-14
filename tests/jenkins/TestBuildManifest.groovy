/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package jenkins.tests

import org.junit.Test

class TestBuildManifest extends BuildPipelineTest {
    @Test
    void testBuildManifest() {
        super.testPipeline("tests/jenkins/jobs/BuildManifest_Jenkinsfile")
    }

    @Test
    void testBuildManifestWithComponentWithNoArtifact() {
        super.testPipeline("tests/jenkins/jobs/BuildManifest_Jenkinsfile_component_no_artifact")
    }
}
