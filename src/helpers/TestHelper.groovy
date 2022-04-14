package helpers

class TestHelper{
    def context
    
    def gallioTestResultPattern = ~/^(\d+) run, (\d+) passed, (\d+) failed, (\d+) inconclusive, (\d+) skipped.*/
    def nUnitTestResultPattern = [~/.*Tests run: (\d+), Errors: (\d+), Failures: (\d+), Inconclusive: (\d+),.+/,
                                    ~/.*Not run: (\d+), Invalid: (\d+), Ignored: (\d+), Skipped: (\d+).*/]

    def testResultsToJson(stat) {
        def builder = new groovy.json.JsonBuilder()
        def root = builder.tests(stat)
        builder.toPrettyString()
    }

    def testResults(log) {
        def stat
        def msg
        def found = false

        def extractors = [
            (gallioTestResultPattern): { it ->
                stat = [
                    run: it.group(1) as int,
                    passed: it.group(2) as int,
                    failed: it.group(3) as int,
                    inconclusive: it.group(4) as int,
                    skipped: it.group(5) as int
                ]
                found = true
            },
            (nUnitTestResultPattern[0]): { it ->
                def run = it.group(1) as int
                def errors = it.group(2) as int
                def failures = it.group(3) as int
                def inconclusive = it.group(4) as int
                stat = [
                    run: run,
                    errors: errors,
                    failures: failures,
                    inconclusive: inconclusive
                ]
            },
            (nUnitTestResultPattern[1]): { it ->
                def not_run = it.group(1) as int
                def invalid = it.group(2) as int
                def ignored = it.group(3) as int
                def skipped = it.group(4) as int
                stat = [
                    run: stat.run,
                    passed: (stat.run - stat.errors - stat.failures - stat.inconclusive -
                            not_run - invalid - ignored - skipped),
                    failed: (stat.failures + stat.errors + invalid),
                    inconclusive: stat.inconclusive,
                    skipped: (skipped + ignored + not_run)
                ]
                found = true
            }
        ]

        log.eachLine { line ->
            if (!found) {
                extractors.each { pattern, extractor ->
                    def matcher = pattern.matcher(line)
                    if (matcher.matches()) {
                        if (msg) {
                            msg = "${msg} ${line}"
                        } else {
                            msg = line
                        }
                        extractor.call(matcher)
                    }
                }
            }
        }
        [msg, stat]
    }
}