package helpers

def static markAsNotBuilt(currentBuild){ 
    currentBuild.result = "NOT_BUILT"
}

def static markAsFailure(currentBuild) { 
    currentBuild.result = "FAILURE"
}