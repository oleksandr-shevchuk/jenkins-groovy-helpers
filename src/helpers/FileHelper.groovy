package helpers

import hudson.FilePath
import hudson.FilePath.FileCallable

class FileHelper{
    def static createFile(fileName, contents, artifactsDir){
        path = new FilePath(artifactsDir)

        def callable = { dir, channel ->
            if (!dir.exists()) {
                dir.mkdir()
            }
            def f = new File(dir, fileName)
            f << contents
        } as FileCallable

        path.act(callable)
    } 
}