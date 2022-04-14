package helpers

import helpers.RetriesExceededException
import helpers.CommandRunner

class GerritHelper {
    def context

    def gerritReview(label, value, message=null) {
        def messageParam = (message) ? "--message '\"${message}\"'" : ''
        try {
            //CommandRunner.runCommand(context, "${gerritSSH()} review --label ${label}=${value} " +
            //                        "${messageParam} " + context.params.GERRIT_PATCHSET_REVISION)
            context.println("${gerritSSH()} review --label ${label}=${value} " +
                                    "${messageParam} " + context.params.GERRIT_PATCHSET_REVISION)
        } catch (RetriesExceededException e) {
            context.println e
            throw new Exception(e)
        }
    }

    def gerritReview(message) {
        try {
            //CommandRunner.runCommand(context, "${gerritSSH()} review --message '\"${message}\"' " +
            //                context.params.GERRIT_PATCHSET_REVISION)
            context.println("${gerritSSH()} review --message '\"${message}\"' " +
                            context.params.GERRIT_PATCHSET_REVISION)
        } catch (RetriesExceededException e) {
            context.println e
            throw new Exception(e)
        }
    }

    def gerritSSH() {
        def sshUser = 'svc_gerrit'
        def sshKey = [context.env.JENKINS_HOME, '.ssh', 'id_rsa'].join('/')
        "ssh -i ${sshKey} ${sshUser}@${context.params.GERRIT_HOST} -p ${context.params.GERRIT_PORT} gerrit"
    }
}