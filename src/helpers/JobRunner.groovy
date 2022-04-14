import hudson.console.ModelHyperlinkNote

def run(context, jobName, jobParameters){
    def job = build job: jobName, parameters: jobParameters, propagate: false
    context.println hudson.console.ModelHyperlinkNote.encodeTo("/job/${params.WORKER}/${job.number}/console", jobName + " #" + job.number) + " completed: " + job.result
    if (job.result != "SUCCESS")
    {
        context.currentBuild.result = 'FAILURE'
    }
}