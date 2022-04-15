package helpers

import hudson.FilePath

class ArtifactsHelper{
    def context

    def readArtifact(build, path){
        artifactsDir = new FilePath(build.getArtifactsDir());
        artifactFile = artifactsDir.child(path)
        context.println("${artifactFile.getRemote()} exists ? ${artifactFile.exists()}" )
        if (artifactFile.exists()) {
            artifactFile.readToString()
        } else {
            ''
        }
    } 
}