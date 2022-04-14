import helpers.exceptions.RetriesExceededException

def context

def runCommand(cmd, retries=5) {
    def run = {
        def proc = cmd.execute()
        proc.waitFor()
        def out = proc.in.text
        def err = proc.err.text
        def exitValue = proc.exitValue()

        context.println cmd
        context.println "stdout: ${out}"
        context.println "stderr: ${err}"
        context.println "return code: ${exitValue}"

        return [out, err, exitValue]
    }

    if (retries <= 0) {
        run()
    } else {
        def i = 0
        def out, err, exitValue
        while (i++ < retries) {
        (out, err, exitValue) = run()
        if (exitValue == 0) {
            return [out, err, exitValue]
        }
        context.println "Return code != 0, retrying in ${i*i} seconds (${i}/${retries})"
        sleep(i * i * 1000)
        }
        throw new RetriesExceededException(err)
    }
}