pipeline {
    agent {
        label 'linux && arm64 && docker'
    }

    environment {
        IMAGE_REPOSITORY = 'medical-visits'
        GHCR_IMAGE = 'ghcr.io/nambintsou32/medical-visits'
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
                        script: 'git rev-parse HEAD',
                        returnStdout: true
                    ).trim()

                    def commitShaShort = commitSha.take(12)

                    env.GIT_SHA_SHORT = commitShaShort
                    env.IMAGE_TAG =
                        "${commitShaShort}-${env.BUILD_NUMBER}-${env.EXECUTOR_NUMBER}"
                    env.RELEASE_TAG = "sha-${commitShaShort}"
                    env.CONTAINER_NAME =
                        "medical-visits-${env.IMAGE_TAG}"

                    echo "Git commit: ${commitSha}"
                    echo "Local image: ${env.IMAGE_REPOSITORY}:${env.IMAGE_TAG}"
                    echo "Registry image: ${env.GHCR_IMAGE}:${env.RELEASE_TAG}"
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

                    test -n "${GIT_SHA_SHORT}"
                    test -n "${IMAGE_TAG}"
                    test -n "${RELEASE_TAG}"
                    test -n "${CONTAINER_NAME}"
                    test -n "${GHCR_IMAGE}"
                '''
            }
        }

        stage('Build and Tests') {
            environment {
                TESTCONTAINERS_RYUK_DISABLED = 'true'
            }

            steps {
                sh '''
            set -eu

            echo "Ryuk disabled: ${TESTCONTAINERS_RYUK_DISABLED}"
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

                    IMAGE_SOURCE="$(
                        docker image inspect \
                            "${IMAGE_REPOSITORY}:${IMAGE_TAG}" \
                            --format '{{index .Config.Labels "org.opencontainers.image.source"}}'
                    )"

                    echo "Image architecture: ${IMAGE_ARCHITECTURE}"
                    echo "Image user: ${IMAGE_USER}"
                    echo "Image source: ${IMAGE_SOURCE}"

                    test "${IMAGE_ARCHITECTURE}" = "arm64"
                    test "${IMAGE_USER}" = "10001:10001"
                    test "${IMAGE_SOURCE}" = \
                        "https://github.com/nambintsou32/medical-visits-devops"
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

        stage('Publish Container Image') {
            when {
                branch 'main'
            }

            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'ghcr-credentials',
                        usernameVariable: 'GHCR_USERNAME',
                        passwordVariable: 'GHCR_TOKEN'
                    )
                ]) {
                    sh '''
                        set -eu

                        REMOTE_SHA_IMAGE="${GHCR_IMAGE}:${RELEASE_TAG}"
                        REMOTE_LATEST_IMAGE="${GHCR_IMAGE}:latest"

                        DOCKER_CONFIG_DIRECTORY="$(mktemp -d)"
                        export DOCKER_CONFIG="${DOCKER_CONFIG_DIRECTORY}"

                        cleanup_registry_authentication() {
                            docker logout ghcr.io >/dev/null 2>&1 || true
                            rm -rf "${DOCKER_CONFIG_DIRECTORY}"
                        }

                        trap cleanup_registry_authentication EXIT HUP INT TERM

                        printf '%s' "${GHCR_TOKEN}" \
                            | docker login \
                                ghcr.io \
                                --username "${GHCR_USERNAME}" \
                                --password-stdin

                        echo "Publishing immutable image ${REMOTE_SHA_IMAGE}"

                        docker tag \
                            "${IMAGE_REPOSITORY}:${IMAGE_TAG}" \
                            "${REMOTE_SHA_IMAGE}"

                        docker push "${REMOTE_SHA_IMAGE}"

                        echo "Publishing convenience image ${REMOTE_LATEST_IMAGE}"

                        docker tag \
                            "${IMAGE_REPOSITORY}:${IMAGE_TAG}" \
                            "${REMOTE_LATEST_IMAGE}"

                        docker push "${REMOTE_LATEST_IMAGE}"

                        echo "Published images:"
                        echo "${REMOTE_SHA_IMAGE}"
                        echo "${REMOTE_LATEST_IMAGE}"
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'Application tests and container pipeline completed successfully.'
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

                if [ -n "${RELEASE_TAG:-}" ]; then
                    docker image rm \
                        --force \
                        "${GHCR_IMAGE}:${RELEASE_TAG}" \
                        >/dev/null 2>&1 || true

                    docker image rm \
                        --force \
                        "${GHCR_IMAGE}:latest" \
                        >/dev/null 2>&1 || true
                fi
            '''

            cleanWs()
        }
    }
}
