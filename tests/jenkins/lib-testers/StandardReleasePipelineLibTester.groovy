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

    private Map config

    public StandardReleasePipelineLibTester(config){
        this.config = config

    }

    void configure(helper, binding) {
    }

    void parameterInvariantsAssertions(call){
        assertThat(call.args.config.first(), notNullValue())
            assertThat(call.closure.body, notNullValue())
    }

    boolean expectedParametersMatcher(call) {
            return call.args.config.first().equals(this.config)
    }

    String libFunctionName() {
        return 'standardReleasePipeline'
    }
}
