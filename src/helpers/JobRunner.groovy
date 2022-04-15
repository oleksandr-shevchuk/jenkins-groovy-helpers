package helpers

import hudson.console.ModelHyperlinkNote

def run(context, jobName, jobParameters = null){
    def job = build job: jobName, parameters: jobParameters, propagate: false
    context.println hudson.console.ModelHyperlinkNote.encodeTo("/job/${jobName}/${job.number}/console", jobName + " #" + job.number) + " completed: " + job.result
    if (job.result != "SUCCESS")
    {
        context.currentBuild.result = 'FAILURE'
    }

    job
}

def runParallel(context, jobs){
    def executions = [:]

    for(int i = 0; i < jobs.size(); i++){
        executions[i] = {
            def (job, parameters) = jobs[i]
            context.println job + " with " + parameters
            //run(context, job, parameters)
        }
    }

    context.println executions

    parallel executions
}