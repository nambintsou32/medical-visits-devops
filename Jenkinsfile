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
                '''
            }
        }

        stage('Build and Tests') {
            steps {
                sh '''
                    set -eu
                    mvn -B -ntp clean verify
                '''
            }

            post {
                always {
                    junit(
                        testResults: 'target/surefire-reports/TEST-*.xml,target/failsafe-reports/TEST-*.xml',
                        allowEmptyResults: false
                    )
                }
            }
        }

        stage('Archive WAR') {
            steps {
                sh '''
                    set -eu
                    test -s target/medical-visits.war
                    ls -lh target/medical-visits.war
                '''

                archiveArtifacts(
                    artifacts: 'target/medical-visits.war',
                    fingerprint: true
                )
            }
        }
    }

    post {
        success {
            echo 'Java application build completed successfully.'
        }

        failure {
            echo 'Java application build failed. Review the failed stage and logs.'
        }

        always {
            cleanWs()
        }
    }
}
