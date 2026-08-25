pipeline {
    agent {
        label 'linux && arm64 && docker'
    }

    options {
        skipDefaultCheckout(true)
        timestamps()
        disableConcurrentBuilds()
        timeout(time: 10, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Validate Toolchain') {
            steps {
                sh '''
                    set -eu

                    test "$(uname -m)" = "aarch64"

                    git --version
                    java -version
                    javac -version
                    mvn --version
                    docker version
                    docker compose version
                    kubectl version --client
                '''
            }
        }

        stage('Docker Smoke Test') {
            steps {
                sh '''
                    set -eu
                    docker run --rm hello-world:latest
                '''
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}