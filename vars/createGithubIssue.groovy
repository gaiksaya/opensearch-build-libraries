/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

/** Library to create GitHub issue across opensearch-project repositories.
 @param Map args = [:] args A map of the following parameters
 @param args.repoUrl <required> - GitHub repository URL to create issue
 @param args.issueTitle <required> - GitHub issue title
 @param args.issueBody <required> - GitHub issue body
 @param args.label <optional> - GitHub issue label to be attached along with 'untriaged'. Defaults to autocut.
 */
void call(Map args = [:]) {
    label = args.label ?: 'autocut'
    try {
        withCredentials([usernamePassword(credentialsId: 'jenkins-github-bot-token', passwordVariable: 'GITHUB_TOKEN', usernameVariable: 'GITHUB_USER')]) {
            println('Listing issues now!')
            println("Repo URL: $args.repoUrl")
            println("Issue title: $args.issueTitle")
            println("Label: $label")
            println("Script: gh issue list --repo ${args.repoUrl} -S \"${args.issueTitle} in:title\" --label ${label}")
            def issues = sh(
                    script: "gh issue list --repo ${args.repoUrl} -S \"${args.issueTitle} in:title\" --label ${label}",
                    returnStdout: true
            )
            println("Issue value: $issues")
            if (issues) {
                println('Issue already exists, adding a comment.')
                def issuesNumber = sh(
                    script: "gh issue list --repo ${args.repoUrl} -S \"${args.issueTitle} in:title\" --label ${label} --json number --jq '.[0].number'",
                    returnStdout: true
                ).trim()
                sh(
                   script: "gh issue comment ${issuesNumber} --repo ${args.repoUrl} --body \"${args.issueBody}\"",
                   returnStdout: true
                )
            }
            else {
                sh(
                    script: "gh issue create --title \"${args.issueTitle}\" --body \"${args.issueBody}\" --label ${label} --label \"untriaged\" --repo ${args.repoUrl}",
                    returnStdout: true
                )
            }
        }
    } catch (Exception ex) {
        error("Unable to create GitHub issue for ${args.repoUrl}", ex.getMessage())
    }
}