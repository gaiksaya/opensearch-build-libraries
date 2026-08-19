/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import jenkins.ReleaseStateData

void call(Map args = [:]) {
    def rsd = new ReleaseStateData('u', 'a', 's', 't', this)
    echo("Indexing release state for version 3.8.0.")
}
