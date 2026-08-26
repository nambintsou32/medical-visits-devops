pipeline {
    agent {
        label 'linux && arm64 && docker'
    }

    environment {
        IMAGE_REPOSITORY = 'medical-visits'
    }

    options {
        skipDefaultCheckout(true)
        timestamps()
        disableConcurrentBuilds()
        timeout(time: 15, unit: 'MINUTES')
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
                    curl --version | head -n 1
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

        stage('Build Container Image') {
            steps {
                sh '''
                    set -eu

                    docker build \
                        --platform linux/arm64 \
                        --tag "${IMAGE_REPOSITORY}:${BUILD_TAG}" \
                        .

                    IMAGE_ARCHITECTURE="$(
                        docker image inspect \
                            "${IMAGE_REPOSITORY}:${BUILD_TAG}" \
                            --format '{{.Architecture}}'
                    )"

                    IMAGE_USER="$(
                        docker image inspect \
                            "${IMAGE_REPOSITORY}:${BUILD_TAG}" \
                            --format '{{.Config.User}}'
                    )"

                    echo "Image architecture: ${IMAGE_ARCHITECTURE}"
                    echo "Image user: ${IMAGE_USER}"

                    test "${IMAGE_ARCHITECTURE}" = "arm64"
                    test "${IMAGE_USER}" = "10001:10001"
                '''
            }
        }

        stage('Container Smoke Test') {
            steps {
                sh '''
                    set -eu

                    CONTAINER_NAME="${BUILD_TAG}"

                    docker run \
                        --detach \
                        --name "${CONTAINER_NAME}" \
                        --publish 127.0.0.1::8080 \
                        "${IMAGE_REPOSITORY}:${BUILD_TAG}"

                    HOST_PORT="$(
                        docker port "${CONTAINER_NAME}" 8080/tcp \
                        | awk -F: 'NR == 1 {print $NF}'
                    )"

                    test -n "${HOST_PORT}"
                    echo "Container port: ${HOST_PORT}"

                    ATTEMPT=1

                    while [ "${ATTEMPT}" -le 30 ]; do
                        if HEALTH_RESPONSE="$(
                            curl \
                                --fail \
                                --silent \
                                --show-error \
                                "http://127.0.0.1:${HOST_PORT}/health" \
                                2>/dev/null
                        )"; then
                            echo "Health response: ${HEALTH_RESPONSE}"
                            break
                        fi

                        if [ "${ATTEMPT}" -eq 30 ]; then
                            echo "Health endpoint did not become ready."
                            exit 1
                        fi

                        ATTEMPT=$((ATTEMPT + 1))
                        sleep 1
                    done

                    docker exec "${CONTAINER_NAME}" id

                    test "$(
                        docker exec "${CONTAINER_NAME}" id -u
                    )" = "10001"

                    test "$(
                        docker exec "${CONTAINER_NAME}" id -g
                    )" = "10001"
                '''
            }

            post {
                always {
                    sh '''
                        docker logs "${BUILD_TAG}" || true
                        docker rm --force "${BUILD_TAG}" || true
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'Application tests and container validation completed successfully.'
        }

        failure {
            echo 'Pipeline failed. Review the failed stage and logs.'
        }

        always {
            sh '''
                docker rm --force "${BUILD_TAG}" >/dev/null 2>&1 || true
                docker image rm \
                    --force \
                    "${IMAGE_REPOSITORY}:${BUILD_TAG}" \
                    >/dev/null 2>&1 || true
            '''

            cleanWs()
        }
    }
}
