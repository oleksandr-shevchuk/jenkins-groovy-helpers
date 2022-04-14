import hudson.console.ModelHyperlinkNote

def run(context, jobName, jobParameters){
    println 'in lib'
    def job = build job: jobName, parameters: jobParameters, propagate: false
    context.println hudson.console.ModelHyperlinkNote.encodeTo("/job/${params.WORKER}/${j.number}/console", jobName + " #" + job.number) + " completed: " + job.result
    if (job.result != "SUCCESS")
    {
        context.currentBuild.result = 'FAILURE'
    }
}

def customLog(text){
    println "custom log " + text
}

def testFunc(){
    println "this is test func"
}