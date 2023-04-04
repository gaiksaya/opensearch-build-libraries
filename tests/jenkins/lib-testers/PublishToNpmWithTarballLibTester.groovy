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

class PublishToNpmWithTarballLibTester extends LibFunctionTester {

    private String artifactPath = ''

    public PublishToNpmWithTarballLibTester(artifactPath) {
        this.artifactPath = artifactPath
    }

    void configure(helper, binding) {
        helper.registerAllowedMethod("checkout", [Map], {})
        helper.registerAllowedMethod("withCredentials", [Map, Closure], { args, closure ->
            closure.delegate = delegate
            return helper.callClosure(closure)
        })
    }
    void parameterInvariantsAssertions(call) {
        assertThat(call.args.artifactPath.first(), notNullValue())
    }

    boolean expectedParametersMatcher(call) {
        return call.args.artifactPath.first().toString().equals(this.artifactPath)
    }

    String libFunctionName() {
        return 'publishToNpm'
    }
}
