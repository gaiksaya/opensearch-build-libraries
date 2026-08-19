/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import jenkins.ReleaseStateData
import jenkins.ReleaseCriterion

void call(Map args = [:]) {
    def rsd = new ReleaseStateData('u', 'a', 's', 't', this)
    def rc = new ReleaseCriterion([version: '3.8.0', criterionType: 'exit', criterionName: 'x', status: 'met'])
    rsd.indexCriterion(rc)
    echo("Indexing release state for version 3.8.0.")
}
