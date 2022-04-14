import helpers.exceptions.RetriesExceededException

class GerritHelper {
    def context
    def commandRunner

    def gerritReview(label, value, message=null) {
        def messageParam = (message) ? "--message '\"${message}\"'" : ''
        try {
            commandRunner.runCommand("${gerritSSH()} review --label ${label}=${value} " +
                                    "${messageParam} " + context.params.GERRIT_PATCHSET_REVISION)
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

    def run() {
        gerritReview("Verified", 0, "Starting Check job ${context.currentBuild.absoluteUrl}")
    }
}