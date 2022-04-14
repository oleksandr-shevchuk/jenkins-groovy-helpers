import hudson.console.ModelHyperlinkNote

def runJob(jobName, jobParameters){
    def job = build job: jobName, parameters: jobParameters, propagate: false
    context.println hudson.console.ModelHyperlinkNote.encodeTo("/job/${params.WORKER}/${j.number}/console", jobName + " #" + job.number) + " completed: " + job.result
    if (job.result != "SUCCESS")
    {
        currentBuild.result = 'FAILURE'
    }
}