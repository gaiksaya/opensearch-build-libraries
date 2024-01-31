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
            if (args.action == 'add') {
                verifyAndCreateMissingLabels(args.label, args.repoUrl)
            }
            def issueNumber = sh(
                    script: "gh issue list --repo ${args.repoUrl} -S \"${args.issueTitle} in:title\" --json number --jq '.[0].number'",
                    returnStdout: true
            ).trim()
            if (!issueNumber.isEmpty()) {
                sh(
                        script: "gh issue edit ${issueNumber} -R ${args.repoUrl} ${action} \"${args.label}\"",
                        returnStdout: true
                )
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
        error ('Invalid action. Valid input: add or remove')
    }
}

def verifyAndCreateMissingLabels(String label, String repoUrl){
    List<String> allLabels = Arrays.asList(label.split(","));
    println('Verifying labels')
    allLabels.each { i ->
        try {
            sh(
                script: "gh label list --repo ${repoUrl} -S ${i}",
                returnStdout: true
            )
        } catch (Exception ex) {
            println("${i} label is missing. Creating the missing label")
            sh(
                script: "gh label create ${i} --repo ${repoUrl}",
                returnStdout: true
            )
        }
    }
}
