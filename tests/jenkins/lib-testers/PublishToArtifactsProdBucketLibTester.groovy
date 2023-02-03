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

class PublishToArtifactsProdBucketLibTester extends LibFunctionTester {

    private String roleName
    private String source
    private String destination
    private String signingPlatform
    private String sigtype
    private boolean overwrite

    public PublishToArtifactsProdBucketLibTester(roleName, source, destination, signingPlatform, sigtype, overwrite) {
        this.roleName = roleName
        this.source = source
        this.destination = destination
        this.sigtype = sigtype
        this.signingPlatform = signingPlatform
        this.overwrite = overwrite
    }

    public PublishToArtifactsProdBucketLibTester(roleName, source, destination){
        this.roleName = roleName
        this.source = source
        this.destination = destination
    }

    void configure(helper, binding){
        helper.registerAllowedMethod("s3Upload", [Map])
        binding.setVariable('GITHUB_BOT_TOKEN_NAME', 'github_bot_token_name')
        helper.registerAllowedMethod('git', [Map])
        helper.registerAllowedMethod('withCredentials', [Map, Closure], { args, closure ->
            closure.delegate = delegate
            return helper.callClosure(closure)
        })
        helper.registerAllowedMethod('withAWS', [Map, Closure], { args, closure ->
            closure.delegate = delegate
            return helper.callClosure(closure)
        })
    }

    void parameterInvariantsAssertions(call){
        assertThat(call.args.roleName.first(), notNullValue())
        assertThat(call.args.source.first(), notNullValue())
        assertThat(call.args.destination.first(), notNullValue())
    }

    boolean expectedParametersMatcher(call) {
        return call.args.roleName.first().toString().equals(this.roleName)
                && call.args.source.first().toString().equals(this.source)
                && call.args.destination.first().toString().equals(this.destination)
    }

    String libFunctionName() {
        return 'publishToArtifactsProdBucket'
    }
}
