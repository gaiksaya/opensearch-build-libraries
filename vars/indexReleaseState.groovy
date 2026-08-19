/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

void call(Map args = [:]) {
    echo("Indexing release state for version 3.8.0.")
}

private String renderDetails(Map<String, List<String>> problemsByKey) {
    return problemsByKey.collect { key, items -> "${key}: ${items.join(', ')}" }.join('; ')
}

private Map renderVulnerabilityResults(Map<String, List<String>> byProject) {
    return [blockingComponents: byProject.keySet().toList(), details: renderDetails(byProject)]
}

private Map renderIntegResults(Map<String, List<String>> byDistArch) {
    Map<String, List<String>> failing = byDistArch.findAll { distArch, components -> components }
    List<String> components = failing.values().flatten().unique()
    return [blockingComponents: components, details: failing ? renderDetails(failing) : null]
}
