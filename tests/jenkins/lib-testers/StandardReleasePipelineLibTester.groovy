/*
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

class StandardReleasePipelineLibTester extends LibFunctionTester {
    private Map config = [
        overrideAgent: '',
        overrideDockerImage: ''
    ]

    public StandardReleasePipelineLibTester(config){
        this.config.overrideAgent = config.overrideAgent
        this.config.overrideDockerImage = config.overrideDockerImage
    }

    void configure(helper, binding) {
    }

    void parameterInvariantsAssertions(call){
        assertThat(call.args.overrideAgent.first(), notNullValue())
        assertThat(call.args.overrideDockerImage.first(), notNullValue())
        assertThat(call.closure.body, notNullValue())
    }

    boolean expectedParametersMatcher(call) {
        return call.args.config.overrideAgent.first().equals(this.config.overrideAgent)
        && call.args.config.overrideDockerImage.first().equals(this.config.overrideDockerImage)
    }

    String libFunctionName() {
        return 'standardReleasePipeline'
    }
}
