/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import jenkins.ReleaseMetricsData

/**
 * Library to handle version increment PRs across all components under opensearch-project. This Library enables auto-merge for version increment PRs, re-runs CI if upstream dependency is merged.
 * @param Map args = [:] args A map of the following parameters
 * @param args.inputManifest <required> - Input Manifest Path eg:manifests/3.0.0/opensearch-3.0.0.yml.
 */

void call(Map args = [:]) {
    def inputManifest = readYaml(file: args.inputManifest)
    def version = inputManifest.build.version
    def components = inputManifest.components
    def coreAndCommonDependencies = getCommonDependencies(components)
    def dependencyGraph = buildDependencyGraph(components)
    def mergedComponentRepoPRs = [:]
    def pendingComponentRepoPRs = [:]
    Set processedComponents = []

    withCredentials([
            string(credentialsId: 'jenkins-health-metrics-account-number', variable: 'METRICS_HOST_ACCOUNT'),
            string(credentialsId: 'jenkins-health-metrics-cluster-endpoint', variable: 'METRICS_HOST_URL')]) {
        withAWS(role: 'OpenSearchJenkinsAccessRole', roleAccount: "${METRICS_HOST_ACCOUNT}", duration: 900, roleSessionName: 'jenkins-session') {
            def releaseMetrics = new ReleaseMetricsData(env.METRICS_HOST_URL, env.AWS_ACCESS_KEY_ID, env.AWS_SECRET_ACCESS_KEY, env.AWS_SESSION_TOKEN, version, 'github_pulls', this)
            mergedComponentRepoPRs = releaseMetrics.getVersionIncrementPRs("true")
            pendingComponentRepoPRs = releaseMetrics.getVersionIncrementPRs("false")
            println("mergedComponentRepoPRs: ${mergedComponentRepoPRs}")
            println("pendingComponentRepoPRs: ${pendingComponentRepoPRs}")

        }
    }

    // Process core and common dependencies version increment PRs
    println("Processing core and common dependencies")
    coreAndCommonDependencies.each { component ->
        def name = component.name
        if (!processedComponents.contains(name) && pendingComponentRepoPRs.containsKey(name) && !dependencyGraph.containsKey(name)) {
            processComponent(component, pendingComponentRepoPRs)
            processedComponents.add(name)
        }
        println("Processed ${name}")
    }

    // Process independent components version increment PRs
    println("Processing independent components")
    components.each { component ->
        def name = component.name
        if (!processedComponents.contains(name) && pendingComponentRepoPRs.containsKey(name) && !dependencyGraph.containsKey(name)) {
            processComponent(component, pendingComponentRepoPRs)
            processedComponents.add(name)
            println("Processed ${name}")
        }
    }

    // Process dependent components
    println("Processing dependent components")
    dependencyGraph.keySet().each { component ->
        println("Processing ${component.name}")
        def name = component.name
        if (!processedComponents.contains(name) && pendingComponentRepoPRs.containsKey(name)) {
            def dependencies = dependencyGraph[component]
            def unprocessedDeps = dependencies.findAll {
                !processedComponents.contains(it.name) && !mergedComponentRepoPRs.containsKey(it.name)
            }
            if (!unprocessedDeps.isEmpty()) {
                echo "${component.name} depends on ${dependencies} which are yet to be processed. Skipping!"
            } else {
                processComponent(component, pendingComponentRepoPRs)
                processedComponents.add(name)
            }
        } else {
            println("${component.name} already processed.")
        }
    }
}

/**
 * Merges the pull requests if all checks have passed or Enables auto merge on pull requests for merging it in future.
 * @param prUrl: Full https URL of the Pull Request
 */
private void enableAutoMerge(String prUrl) {
    try {
        withCredentials([usernamePassword(credentialsId: 'jenkins-github-bot-token', passwordVariable: 'GITHUB_TOKEN', usernameVariable: 'GITHUB_USER')]) {
            sh(
                    script: "gh pr merge ${prUrl} --auto -s",
                    returnStdout: true
            )
        }
    } catch (Exception ex) {
        echo("Unable to enable auto merge on ${prUrl}!", ex.getMessage())
    }
}

/**
 * Reruns failed checks for the given Pull Request URL
 * @param prUrl - Pull Request HTML URL
 */
private void reRunFailedChecks(String prUrl) {
    try {
        withCredentials([usernamePassword(credentialsId: 'jenkins-github-bot-token', passwordVariable: 'GITHUB_TOKEN', usernameVariable: 'GITHUB_USER')]) {
            def failedRuns = sh(
                    script: "gh pr checks ${prUrl} --json link,state -q '.[] | select(.state==\"FAILURE\") | .link'",
                    returnStdout: true
            )
            if (failedRuns) {
                failedRuns.split('\n').each { run ->
                    println "Failed run URL: ${run}"
                    def parts =  run.split('/')
                    def runId = parts[-1]
                    def repoName = "${parts[3]}/${parts[4]}"
                    sh(
                            script: "gh run rerun --repo ${repoName} -j ${runId}",
                            returnStdout: true
                    )
                }
            } else {
                echo "All checks passed! PR is ready to merge."
            }
        }
    } catch (Exception ex) {
        echo("Unable to process checks for ${prUrl}", ex.getMessage())
    }
}


private def getCommonDependencies(def components) {
    def dependencyCount = [:]
    components.each { component ->
        if (component.containsKey('depends_on')) {
            component.depends_on.each { dep ->
                dependencyCount[dep] = (dependencyCount[dep] ?: 0) + 1
            }
        }
    }
    def common = dependencyCount.findAll { it.value > 2 }.keySet()
    def commonDep = components.findAll { component ->
        common.contains(component.name)
    }
    // Add OpenSearch and OpenSearch-Dashboards
    commonDep.addAll(components.findAll { component ->
        component.name == 'OpenSearch'
        // Commenting until https://github.com/opensearch-project/OpenSearch-Dashboards/issues/7162 gets resolved.
//                || component.name == 'OpenSearch-Dashboards'
    })
    println("common: ${commonDep}")
    return commonDep
}

private def buildDependencyGraph(def components) {
    def graph = [:]
    components.each { component ->
        if (component.containsKey('depends_on')) {
            graph[component] = components.findAll { comp ->
                component.depends_on.contains(comp.name)
            }
        }
    }
    println "Dependency graph: ${graph}"
    return graph
}

private def processComponent(def component, def pendingComponentRepoPRs) {
    def repo = component.repository.split('/')[-1].replace('.git', '')
    def prUrl = pendingComponentRepoPRs[repo]
    reRunFailedChecks(prUrl)
    enableAutoMerge(prUrl)
}
