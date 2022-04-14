package helpers

import hudson.console.ConsoleNote

class JobSummaryHelper {
    def context
    def pattern = ~/(\S+) #(\S+) completed/

    def getWorkerInfo(jobName, buildNum) {
        def result = []
        getLogLines(jobName, buildNum).each { line ->
            def matcher = pattern.matcher(line)

            if(matcher) {
                def name = matcher.group(1)
                def num = matcher.group(2) as int
                result << [name, num, line, getWorkerInfo(name, num)]
            }
        }
        result
    }

    def getLogLines(jobName, buildNum){
        hudson.console.ConsoleNote.removeNotes(
            Jenkins.getInstance()
            .getItemByFullName(jobName)
            .getBuildByNumber(buildNum)
            .logFile.text)
            .split('\n')
    }

    def addWorkerConsoleUrl(info, summary) {
        if (info.size() > 0) {
            summary.appendText('<ul>', false)
            info.each { it ->
                summary.appendText("<li><a href=\"/job/${it[0]}/${it[1]}/console\">${it[2]}</li>", false)
                addWorkerConsoleUrl(it[3], summary)
            }
            summary.appendText('</ul>', false)
        }
    }

    def createSummary(jobName, buildNum) {
        def info = getWorkerInfo(jobName, buildNum)
        def summary = context.createSummary("terminal.png")
        addWorkerConsoleUrl(info, summary)
    }
}

