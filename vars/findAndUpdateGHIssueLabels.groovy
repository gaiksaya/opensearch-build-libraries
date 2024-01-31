/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

/** Library to edit GitHub issue labels across opensearch-project repositories.
 @param Map args = [:] args A map of the following parameters
 @param args.repoUrl <required> - GitHub repository URL to create issue
 @param args.issueTitle <required> - GitHub issue title
 @param args.label <required> - Comma separated values of labels to add or remove from GitHub issue
 @param args.action <required> - Either remove or add the given label(s)
 */
void call(Map args = [:]) {
    action = getActionParam(args.action)
    try {
        withCredentials([usernamePassword(credentialsId: 'jenkins-github-bot-token', passwordVariable: 'GITHUB_TOKEN', usernameVariable: 'GITHUB_USER')]) {
            boolean labelExists = verifyAndCreateMissingLabels(args)
            def issueNumber = sh(
                    script: "gh issue list --repo ${args.repoUrl} -S \"${args.issueTitle} in:title\" --json number --jq '.[0].number'",
                    returnStdout: true
            ).trim()
            if (!issueNumber.isEmpty()) {
                if (labelExists) {
                    sh(
                            script: "gh issue edit ${issueNumber} -R ${args.repoUrl} ${action} \"${args.label}\"",
                            returnStdout: true
                    )
                } else {
                    println('Label does not exist. SKipping the action.')
                }
            } else {
                println("No open issues found for ${args.repoUrl}")
            }
        }
    } catch (Exception ex) {
        error("Unable to edit GitHub issue for ${args.repoUrl}", ex.getMessage())
    }
}

def getActionParam(String action) {
    if (action == 'add') {
        return '--add-label'
    } else if (action == 'remove') {
        return '--remove-label'
    } else {
        error('Invalid action specified. Valid input: add or remove')
    }
}

def verifyAndCreateMissingLabels(args) {
    List<String> allLabels = Arrays.asList(args.label.split(','))
    allLabels.each { i ->
        try {
            def name = sh(
                    script: "gh label list --repo ${args.repoUrl} -S ${i} --json name --jq '.[0].name'",
                    returnStdout: true
                )
            if (name != i) {
                if (args.action == 'add') {
                    println("${i} label is missing. Creating the missing label")
                    sh(
                        script: "gh label create ${i} --repo ${args.repoUrl}",
                        returnStdout: true
                    )
                    return true
                } else {
                    println("Label ${i} does not exist. Skipping label creation.")
                    return false
                }
            }
        } catch (Exception ex) {
            error("Unable to create GitHub label for ${args.repoUrl}", ex.getMessage())
        }
    }
}
