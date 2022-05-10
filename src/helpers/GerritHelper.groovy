package helpers

import helpers.RetriesExceededException
import helpers.CommandRunner
import groovy.json.JsonSlurper

class GerritHelper {
    def context

    def gerritReview(label, value, message=null) {
        def messageParam = (message) ? "--message '\"${message}\"'" : ''
        try {
            context.println(context, "${gerritSSH()} review --label ${label}=${value} " +
                                    "${messageParam} " + context.params.GERRIT_PATCHSET_REVISION)
        } catch (RetriesExceededException e) {
            context.println e
            throw new Exception(e)
        }
    }

    def gerritReview(message) {
        try {
            context.println(context, "${gerritSSH()} review --message '\"${message}\"' " +
                            context.params.GERRIT_PATCHSET_REVISION)
        } catch (RetriesExceededException e) {
            context.println e
            throw new Exception(e)
        }
    }

    def gerritSubmit() {
        try {
            context.println "SUBMIT"
            context.println(context, "${gerritSSH()} review --submit ${context.params.GERRIT_PATCHSET_REVISION}")
        } catch (RetriesExceededException e) {
            context.println e
            throw new Exception(e)
        }
    }

    def getChangedPaths() {
        def changeQuery = "${gerritSSH()} query ${context.params.GERRIT_CHANGE_ID} --format=json --files --patch-sets"
        def jsonText, err, exitValue
        try {
            (jsonText, err, exitValue) = context.println(context, changeQuery)
        } catch (RetriesExceededException e) {
            context.println e
            throw new Exception("Timed out trying to fetch change info from Gerrit. " +
                          "Check Gerrit is available and try again")
        }
        def jsonSlurper = new JsonSlurper()
        def json = jsonSlurper.parseText(jsonText)
        for (def patchSet in json['patchSets']) {
            if (patchSet['revision'] == context.params.GERRIT_PATCHSET_REVISION) {
                patchSet['files'].collect { entry -> entry['file'] }
            }
        }
        
        throw new Exception("Error: could not find change info for patchset " +
                        "${context.params.GERRIT_PATCHSET_REVISION} in ${json}")
  }

    def getReviewStatus() {
        def changeQuery = "${gerritSSH()} query ${context.params.GERRIT_CHANGE_ID} --format=json --current-patch-set"
        def jsonText, err, exitValue
        try {
            (jsonText, err, exitValue) = context.println(context, changeQuery)
        } catch (RetriesExceededException e) {
            context.println e
            throw new Exception("Timed out trying to fetch change info from Gerrit. " +
                            "Check Gerrit is available and try again")
        }
        def jsonSlurper = new JsonSlurper()
        def json = jsonSlurper.parseText(jsonText)

        [json['status'], json['currentPatchSet']['approvals']]
    }

    def reVerify(refSpec) {
        def workers = parseWorkers()
        context.println "workers: '${workers}'"

        def changedPaths = getChangedPaths()
        context.println "changedPaths: '${changedPaths}'"

        def jobsToRun = [] as Set
        workers.each { pathFilter, job ->
            changedPaths.each() { changedPath ->
                context.println("'${changedPath}'.matches('${pathFilter}') : ${changedPath.matches(pathFilter)}")
                if (changedPath.matches(pathFilter)) {
                    jobsToRun << job
                }
            }
        }

        def jobResults = []
        if (jobsToRun.size() > 0) {
            def jobsParameters = [string(name: 'GERRIT_REFSPEC', value: refSpec),
                                string(name: 'GERRIT_BRANCH', value: context.params.GERRIT_BRANCH),
                                string(name: 'GERRIT_PATCHSET_REVISION', value: context.params.GERRIT_PATCHSET_REVISION)]
            def parallelJobs = jobsToRun.collect{job ->
                [job, jobsParameters]
            }
            jobResults = CommandRunner.runParallel(context, parallelJobs)
        } else {
            def warning = "Warning: no verification jobs exist for the paths modified by this patch"
            context.println(context, "${gerritSSH()} review --message '\"${warning}\"' "
                                    + context.params.GERRIT_PATCHSET_REVISION)
        }
        
        jobResults
    }

    def gerritSSH() {
        def sshUser = 'svc_gerrit'
        def sshKey = [context.env.JENKINS_HOME, '.ssh', 'id_rsa'].join('/')
        "ssh -i ${sshKey} ${sshUser}@${context.params.GERRIT_HOST} -p ${context.params.GERRIT_PORT} gerrit"
    }

    def parseWorkers() {
        def jsonSlurper = new JsonSlurper()
        jsonSlurper.parseText(context.params['WORKER_JOB'])
    }

    def static isVerified(approvals) {
        approvals.find {it['type'] == 'Verified' && it['value'] == '1'}
    }

    def static isApproved(approvals) {
        approvals.find {it['type'] == 'Code-Review' && it['value'] == '2'}
    }
}