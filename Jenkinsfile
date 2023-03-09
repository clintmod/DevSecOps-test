main()

def main() {
    timeout(time: 2, unit: 'HOURS') {
        node() {
            try {
                setupProperties()
                runPipeline()
            } catch (err) {
                currentBuild.result = 'FAILURE'
                throw err
            } finally {
                postBuildActions()
            }
        }
    }
}

def setupProperties() {
    // set the TERM env var so colors show up
    env.TERM = 'xterm'
    properties([
    buildDiscarder(logRotator(daysToKeepStr: '30')),
    disableConcurrentBuilds(),
  ])
}

def runPipeline() {
    // githubNotify status: 'PENDING', context: 'Pipeline'
    stage('Checkout') { checkout scm }
    stage('Build') { sh 'make docker-build' }
    stage('Scan') { sh 'make docker-scan' }
    stage('Docker Push') { dockerPush() }
    // stage('Deploy') { deploy() }
    // stage('Check SHA1') { checkSha1() }
}

def dockerPush() {
    withDockerHubCreds {
        sh 'make docker-push'
        if (env.BRANCH_NAME == 'master') {
            sh 'make docker-push-latest'
        }
    }
}

def postBuildActions() {
    echo 'Running post build actions'
    notifyGithub()
    notifySlack()
}

def notifyGithub() {
    echo 'Notifying github'
    try {
        def currentResult = currentBuild.result ?: 'SUCCESS'
        if (currentResult == 'FAILURE') {
            // githubNotify status: 'FAILURE', context: 'Pipeline'
    } else {
            // githubNotify status: 'SUCCESS', context: 'Pipeline'
        }
  } catch (err) {
        // githubNotify status: 'FAILURE', context: 'Pipeline'
        throw err
    }
}

def notifySlack() {
    try {
        String currentResult = currentBuild.result ?: 'SUCCESS'
        String previousResult = currentBuild?.previousBuild?.result ?: 'SUCCESS'
        def channel = '#jenkins'
        if (previousResult != currentResult) {
            if (currentResult == 'FAILURE') {
                message = ("Build failed: ${env.JOB_NAME} "
                    + "${env.BUILD_NUMBER} (<${env.RUN_DISPLAY_URL}/|Open>)")
                slackSend channel: channel, color: 'danger',  message: message
            } else {
                message = ("Build fixed: ${env.JOB_NAME} "
                    + "${env.BUILD_NUMBER} (<${env.RUN_DISPLAY_URL}/|Open>)")
                slackSend channel: channel, color: 'good',  message: message
            }
        } else {
            echo "previous/current build status equal: ${previousResult}"
        }
    } catch (err) {
        echo 'There was an error notifying slack.'
        throw err
    }
}

def withDockerHubCreds(Closure block) {
    return withCredentials([usernamePassword(credentialsId: 'docker-hub-creds',
    passwordVariable: 'DOCKER_HUB_PASSWORD',
    usernameVariable: 'DOCKER_HUB_USER')]) {
        block()
    }
}

def withGithubCreds(Closure block) {
    return withCredentials([usernamePassword(credentialsId: 'github-token',
    usernameVariable: 'GITHUB_USERNAME',
    passwordVariable: 'GITHUB_API_TOKEN')]) {
        block()
    }
}
