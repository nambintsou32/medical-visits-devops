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

                script {
                    def commitSha = sh(
                        script: 'git rev-parse --short=12 HEAD',
                        returnStdout: true
                    ).trim()

                    env.IMAGE_TAG =
                        "${commitSha}-${env.BUILD_NUMBER}-${env.EXECUTOR_NUMBER}"

                    env.CONTAINER_NAME =
                        "medical-visits-${env.IMAGE_TAG}"

                    echo "Git commit: ${commitSha}"
                    echo "Container image: ${env.IMAGE_REPOSITORY}:${env.IMAGE_TAG}"
                    echo "Container name: ${env.CONTAINER_NAME}"
                }
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

                    test -n "${IMAGE_TAG}"
                    test -n "${CONTAINER_NAME}"
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

                    echo "Building image ${IMAGE_REPOSITORY}:${IMAGE_TAG}"

                    docker build \
                        --platform linux/arm64 \
                        --tag "${IMAGE_REPOSITORY}:${IMAGE_TAG}" \
                        .

                    IMAGE_ARCHITECTURE="$(
                        docker image inspect \
                            "${IMAGE_REPOSITORY}:${IMAGE_TAG}" \
                            --format '{{.Architecture}}'
                    )"

                    IMAGE_USER="$(
                        docker image inspect \
                            "${IMAGE_REPOSITORY}:${IMAGE_TAG}" \
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

                    echo "Starting container ${CONTAINER_NAME}"

                    docker run \
                        --detach \
                        --name "${CONTAINER_NAME}" \
                        --publish 127.0.0.1::8080 \
                        "${IMAGE_REPOSITORY}:${IMAGE_TAG}"

                    HOST_PORT="$(
                        docker port "${CONTAINER_NAME}" 8080/tcp \
                        | awk -F: 'NR == 1 {print $NF}'
                    )"

                    test -n "${HOST_PORT}"
                    echo "Container port: ${HOST_PORT}"

                    ATTEMPT=1
                    HEALTH_RESPONSE=""

                    while [ "${ATTEMPT}" -le 30 ]; do
                        echo "Health check attempt ${ATTEMPT}/30"

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

                    test "${HEALTH_RESPONSE}" = '{"status":"UP"}'

                    echo "Container process identity:"
                    docker exec "${CONTAINER_NAME}" id

                    CONTAINER_UID="$(
                        docker exec "${CONTAINER_NAME}" id -u
                    )"

                    CONTAINER_GID="$(
                        docker exec "${CONTAINER_NAME}" id -g
                    )"

                    echo "Container UID: ${CONTAINER_UID}"
                    echo "Container GID: ${CONTAINER_GID}"

                    test "${CONTAINER_UID}" = "10001"
                    test "${CONTAINER_GID}" = "10001"
                '''
            }

            post {
                always {
                    sh '''
                        set +e

                        if [ -n "${CONTAINER_NAME:-}" ]; then
                            echo "Container logs:"
                            docker logs "${CONTAINER_NAME}" || true

                            docker rm \
                                --force \
                                "${CONTAINER_NAME}" \
                                >/dev/null 2>&1 || true
                        fi
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
                set +e

                if [ -n "${CONTAINER_NAME:-}" ]; then
                    docker rm \
                        --force \
                        "${CONTAINER_NAME}" \
                        >/dev/null 2>&1 || true
                fi

                if [ -n "${IMAGE_TAG:-}" ]; then
                    docker image rm \
                        --force \
                        "${IMAGE_REPOSITORY}:${IMAGE_TAG}" \
                        >/dev/null 2>&1 || true
                fi
            '''

            cleanWs()
        }
    }
}