/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package jenkins.tests

import static org.hamcrest.CoreMatchers.notNullValue
import static org.hamcrest.MatcherAssert.assertThat


class RunGradleCheckLibTester extends LibFunctionTester {

    private String gitRepoUrl
    private String gitReference
    private String bwcCheckoutAlign

    public RunGradleCheckLibTester(gitRepoUrl, gitReference, bwcCheckoutAlign){
        this.gitRepoUrl = gitRepoUrl
        this.gitReference = gitReference
        this.bwcCheckoutAlign = bwcCheckoutAlign
    }

    void configure(helper, binding) {
        // N/A
    }

    void parameterInvariantsAssertions(call) {
        assertThat(call.args.gitRepoUrl.first(), notNullValue())
        assertThat(call.args.gitReference.first(), notNullValue())
        assertThat(call.args.bwcCheckoutAlign.first(), notNullValue())
    }

    boolean expectedParametersMatcher(call) {
        return call.args.gitRepoUrl.first().toString().equals(this.gitRepoUrl)
                && call.args.gitReference.first().toString().equals(this.gitReference)
    }

    String libFunctionName() {
        return 'runGradleCheck'
    }
}
