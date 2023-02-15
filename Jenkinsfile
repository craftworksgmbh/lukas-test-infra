@Library('craftworks-jenkins-library@feature/k8s2') _

Boolean STAGE_BUILD_E2E = false
Boolean STAGE_BUILD_PUSH_DEPLOY = false
String  STAGE_SEND_SLACK_MESSAGE = null
Boolean STAGE_GIT_TAG = false
Boolean STAGE_CHECK_GIT_TAG_EXISTS = false
Boolean STAGE_DEPLOY_CLEANUP = false

String BUILD_VERSION = null
String IMAGE_VERSION = null
String RELEASE_NAME = null

String DEPLOY_URL = null

at.craftworks.Cluster CLUSTER = at.craftworks.Cluster.CW_K8S2

pipeline {
    agent {
        dockerfile {
            filename 'Dockerfile.builder'
            additionalBuildArgs '--pull'
            // Adjust the memory limits if necessary, but try to keep the limits low.
            args '-v $HOME/.docker:/root/.docker --memory="6g"'
        }
    }

    options {
        // Specifying a global execution timeout of 180 minutes, after which Jenkins will abort the Pipeline run.
        timeout(time: 180, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '300', artifactNumToKeepStr: '150'))
    }

    parameters {
        // If this parameter is set to true, the check if the git tag exists is skipped and no new git tag is created
        booleanParam(name: 'BUILD_SAME_GIT_TAG', defaultValue: false, description: 'Set to true to be able to build the same git tag again')
        // If this parameter is set to true, the pull request gets deployed
        booleanParam(name: 'DEPLOY_PR', defaultValue: false, description: 'Set to true to build PR')
    }

    environment {
        // project specific variables
        // Note that variables defined in the environment cannot be changed afterwards

        // branches of this repository
        NAME_DEV_BRANCH = "dev"
        NAME_STAGING_BRANCH = "master"
        // branch that uses git tag
        NAME_GIT_TAG_BRANCH = "$NAME_STAGING_BRANCH"
        // maven profile that is used to set the version
        VERSION_SET_PROFILE = "version-set"
        // maven profile that is used for backend
        BACKEND_MAVEN_PROFILE = "backend"
        // maven profile that is used for frontend
        FRONTEND_MAVEN_PROFILE = "frontend"
        // maven profile that is used for backend if test-only
        TEST_BACKEND_MAVEN_PROFILE = "backend"
        // maven profile that is used for frontend if test-only
        TEST_FRONTEND_MAVEN_PROFILE = "frontend-test"
        // maven profile that is used for sonarqube analysis
        SONAR_MAVEN_PROFILE = "sonar-with-prebuilt-artifacts"
        SONAR_PROJECT_KEY = "lukas-test"
        // slack channel that is used for notifications
        SLACK_CHANNEL = "pr-lukas-test"

        // If the following label is assigned to the PR, the PR is deployed
        DEPLOY_PR_LABEL = "deploy"

        // K8S / argoCD
        K8S_NAMESPACE = "lukas-test"
        K8S_REPO = "git@github.com:craftworksgmbh/lukas-test-infra.git"
        K8S_DEPLOY_FOLDER = "deploy"
        K8S_DEPLOY_URL = "https://__RELEASE_NAME_PLACEHOLDER__.k8s1.craftworks.io"
        K8S_DEPLOY_URL_RELEASE_NAME_PLACEHOLDER = "__RELEASE_NAME_PLACEHOLDER__"
        K8S_CREDENTIALS_ID_ARGO_CD_API_TOKEN = "argo-cd-api-token-lukas-test"
        K8S_AGRO_CD_PROJECT_NAME = "lukas-test"
        K8S_AGRO_CD_CLEANUP_BRANCH = "$NAME_DEV_BRANCH"

        // E2E Tests
        E2E_DOCKER_COMPOSE_PROJECT_NAME = "${env.BUILD_TAG}"
        E2E_DOCKER_COMPOSE = "docker-compose -p ${env.E2E_DOCKER_COMPOSE_PROJECT_NAME} -f docker-compose.yml -f docker-compose.e2e.yml"
        E2E_DOCKER_COMPOSE_SERVICE = "e2e"

        VERSION = readMavenPom().getVersion()
    }

    stages {
        // DEBUG
        // might be helpful if you want to extend the pipeline
        /*
        stage('debug') {
            steps {
                script {
                    sh 'printenv'
                }
            }
        }
        */

        // STAGE_DEFINE_STAGES_AND_VERSIONS
        stage('define stages and versions') {
            steps {
                script {
                    if (at.craftworks.CommonUtils.isBranch(env, env.NAME_STAGING_BRANCH)) {
                        STAGE_BUILD_E2E = true
                        STAGE_BUILD_PUSH_DEPLOY = true
                        STAGE_SEND_SLACK_MESSAGE = "always"
                        // --
                        BUILD_VERSION = "${env.VERSION}"
                        IMAGE_VERSION = "${env.VERSION}"
                        RELEASE_NAME = "staging"
                    } else if (at.craftworks.CommonUtils.isBranch(env, env.NAME_DEV_BRANCH)) {
                        STAGE_BUILD_E2E = true
                        STAGE_BUILD_PUSH_DEPLOY = true
                        STAGE_SEND_SLACK_MESSAGE = "no-success"
                        // --
                        BUILD_VERSION = "${env.VERSION}-build.${env.BUILD_NUMBER}"
                        IMAGE_VERSION = "latest"
                        RELEASE_NAME = "dev"
                    } else if (at.craftworks.CommonUtils.isPR(env)) {
                        STAGE_BUILD_E2E = true
                        STAGE_BUILD_PUSH_DEPLOY = params.DEPLOY_PR || pullRequest.labels.contains(env.DEPLOY_PR_LABEL)
                        STAGE_SEND_SLACK_MESSAGE = null
                        // --
                        BUILD_VERSION = "${env.VERSION}-build.${at.craftworks.CommonUtils.replaceInvalidVersionChars(env.BUILD_TAG, '-')}"
                        // A image with the CRAFTWORKS-PR prefix is cleaned up automatically
                        // See https://github.com/craftworksgmbh/infrastructure/blob/dev/roles/nexus/templates/cleanup/config.py
                        IMAGE_VERSION = "CRAFTWORKS-PR${env.CHANGE_ID}"
                        RELEASE_NAME = "${env.BRANCH_NAME}"
                    } else if (at.craftworks.CommonUtils.isBranch(env)) {
                        STAGE_BUILD_E2E = false
                        STAGE_BUILD_PUSH_DEPLOY = false
                        STAGE_SEND_SLACK_MESSAGE = null
                        // --
                        BUILD_VERSION = "${env.VERSION}-build.${at.craftworks.CommonUtils.replaceInvalidVersionChars(env.BUILD_TAG, '-')}"
                        IMAGE_VERSION = null
                        RELEASE_NAME = null
                    }

                    // Other stages
                    STAGE_GIT_TAG = at.craftworks.CommonUtils.isBranch(env, env.NAME_GIT_TAG_BRANCH) && !params.BUILD_SAME_GIT_TAG
                    STAGE_CHECK_GIT_TAG_EXISTS = (at.craftworks.CommonUtils.isBranch(env, env.NAME_GIT_TAG_BRANCH) || at.craftworks.CommonUtils.isPRToBranch(env, env.NAME_GIT_TAG_BRANCH)) && !params.BUILD_SAME_GIT_TAG
                    STAGE_DEPLOY_CLEANUP = at.craftworks.CommonUtils.isBranch(env, env.K8S_AGRO_CD_CLEANUP_BRANCH)

                    // Print
                    echo "STAGE_BUILD_E2E: ${STAGE_BUILD_E2E}"
                    echo "STAGE_BUILD_PUSH_DEPLOY: ${STAGE_BUILD_PUSH_DEPLOY}"
                    echo "STAGE_SEND_SLACK_MESSAGE: ${STAGE_SEND_SLACK_MESSAGE}"
                    echo "STAGE_GIT_TAG: ${STAGE_GIT_TAG}"
                    echo "STAGE_CHECK_GIT_TAG_EXISTS: ${STAGE_CHECK_GIT_TAG_EXISTS}"
                    echo "STAGE_DEPLOY_CLEANUP: ${STAGE_DEPLOY_CLEANUP}"
                    // --
                    echo "VERSION: ${env.VERSION}"
                    echo "BUILD_VERSION: ${BUILD_VERSION}"
                    echo "IMAGE_VERSION: ${IMAGE_VERSION}"
                    echo "RELEASE_NAME: ${RELEASE_NAME}"
                }
            }
        }

        // STAGE_CHECK_GIT_TAG_EXISTS
        // Requires: STAGE_DEFINE_STAGES_AND_VERSIONS
        // Checks if a git tag exists, if not, stage fails
        stage('check if git tag exists') {
            when {
                beforeAgent true
                expression { STAGE_CHECK_GIT_TAG_EXISTS }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'github-app-craftworks',
                                        usernameVariable: 'GITHUB_APP',
                                        passwordVariable: 'GITHUB_ACCESS_TOKEN')]) {
                    script {
                        gh.ensureTagDoesNotExist(env, GITHUB_ACCESS_TOKEN, env.VERSION)
                    }
                }
            }
        }

        // STAGE_SET_VERSION
        // Requires: STAGE_DEFINE_STAGES_AND_VERSIONS
        stage('set version') {
            steps {
                script {
                    milestone()
                    sh "mvn -B versions:set -DnewVersion='${BUILD_VERSION}'"
                    sh "mvn -B validate -P ${env.VERSION_SET_PROFILE}"
                }
            }
        }

        // STAGE_TEST
        // Note: This stage is not needed if running STAGE_TEST_AND_BUILD
        stage('test') {
            when {
                beforeAgent true
                expression { !STAGE_BUILD_E2E && !STAGE_BUILD_PUSH_DEPLOY }
            }
            parallel {
                stage('test backend') {
                    steps {
                        withCwDockerRegistries {
                            script {
                                sh "mvn -B -e -P ${ env.TEST_BACKEND_MAVEN_PROFILE} test"
                            }
                        }
                    }
                    post {
                        always {
                            junit checksName: 'Backend Tests', testResults: '**/surefire-reports/*.xml'
                        }
                    }
                }
                stage('test frontend') {
                    steps {
                        withCwDockerRegistries {
                            script {
                                sh "mvn -B -e -P ${ env.TEST_FRONTEND_MAVEN_PROFILE} test"
                            }
                        }
                    }
                    post {
                        always {
                            junit checksName: 'Frontend Tests', testResults: '**/test-results/test-results.xml'
                        }
                    }
                }
            }
        }

        // STAGE_TEST_AND_BUILD
        // Note: If running this stage, the stage STAGE_TEST is not needed
        stage('test and build') {
            when {
                beforeAgent true
                expression { STAGE_BUILD_E2E || STAGE_BUILD_PUSH_DEPLOY }
            }
            parallel {
                stage('test and build backend') {
                    steps {
                        withCwDockerRegistries {
                            script {
                                sh "mvn -B -e -P ${ env.BACKEND_MAVEN_PROFILE} package"
                            }
                        }
                    }
                    post {
                        always {
                            junit checksName: 'Backend Tests', testResults: '**/surefire-reports/*.xml'
                        }
                    }
                }
                stage('test and build frontend') {
                    steps {
                        withCwDockerRegistries {
                            script {
                                sh "mvn -B -e -P ${ env.FRONTEND_MAVEN_PROFILE} package"
                            }
                        }
                    }
                    post {
                        always {
                            junit checksName: 'Frontend Tests', testResults: '**/test-results/test-results.xml'
                        }
                    }
                }
            }
        }

        // SonarQube
        // Requires: STAGE_TEST or STAGE_TEST_AND_BUILD
        // Note: STAGE_TEST or STAGE_TEST_AND_BUILD is required because the `sonar-with-prebuilt-artifacts`-profile depends on artifacts (test-results) of this stages in order to optimize build time
        stage('SonarQube') {
            steps {
                withCwDockerRegistries {
                    script {
                        withSonarQubeEnv('sonarqube') {
                            milestone()
                            at.craftworks.MavenUtils.sonar(steps, env, env.SONAR_PROJECT_KEY, env.SONAR_MAVEN_PROFILE)
                        }
                    }
                }
            }
        }

        // STAGE_BUILD_DOCKER_IMAGES
        // Requires: STAGE_TEST_AND_BUILD
        stage('build docker images') {
            // TODO: run in parallel?
            when {
                beforeAgent true
                expression { STAGE_BUILD_E2E || STAGE_BUILD_PUSH_DEPLOY }
            }
            steps {
                withCwDockerRegistries {
                    script {
                        milestone()
                        sh "mvn -B dockerfile:build"
                    }
                }
            }
        }

        // STAGE_E2E_TESTS
        // Requires: STAGE_BUILD_DOCKER_IMAGES
        stage('E2E tests') {
            when {
                beforeAgent true
                expression { STAGE_BUILD_E2E }
            }
            steps {
                withCwDockerRegistries {
                    script {
                        milestone()
                        try {
                            sh "${env.E2E_DOCKER_COMPOSE} up --build --exit-code-from ${env.E2E_DOCKER_COMPOSE_SERVICE}"
                        } finally {
                            sh "docker cp \$(${env.E2E_DOCKER_COMPOSE} ps -q ${env.E2E_DOCKER_COMPOSE_SERVICE}):e2e/cypress e2e-cypress"
                            sh "${env.E2E_DOCKER_COMPOSE} down --remove-orphans"
                        }
                    }
                }
            }
            post {
                always {
                    junit checksName: 'E2E Tests', testResults: 'e2e-cypress/results/test-results-*.xml'
                }
                failure {
                    archiveArtifacts artifacts: 'e2e-cypress/screenshots/**/*.png', fingerprint: false, allowEmptyArchive: true
                    archiveArtifacts artifacts: 'e2e-cypress/videos/**/*.mp4', fingerprint: false, allowEmptyArchive: true
                }
            }
        }

        // STAGE_PUSH_DOCKER_IMAGES
        // Requires: STAGE_BUILD_DOCKER_IMAGES
        stage('push docker images') {
            when {
                beforeAgent true
                expression { STAGE_BUILD_PUSH_DEPLOY }
            }
            steps {
                withCwDockerRegistries {
                    script {
                        milestone()
                        sh "mvn -B dockerfile:tag -Ddockerfile.tag=${IMAGE_VERSION}"
                        sh "mvn -B dockerfile:push -Ddockerfile.tag=${IMAGE_VERSION}"
                    }
                }
            }
        }

        // STAGE_GIT_TAG
        // Requires: STAGE_CHECK_GIT_TAG_EXISTS
        stage('add git tag') {
            when {
                beforeAgent true
                expression { STAGE_GIT_TAG }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'github-app-craftworks',
                                        usernameVariable: 'GITHUB_APP',
                                        passwordVariable: 'GITHUB_ACCESS_TOKEN')]) {
                    script {
                        gh.createGitTag(env, GITHUB_ACCESS_TOKEN, env.VERSION)
                    }
                }
            }
        }

        // STAGE_DEPLOY
        // Requires: STAGE_PUSH_DOCKER_IMAGES
        stage('deploy k8s deployment') {
            when {
                beforeAgent true
                expression { STAGE_BUILD_PUSH_DEPLOY }
            }
            agent none
            steps {
                withCredentials([string(credentialsId: env.K8S_CREDENTIALS_ID_ARGO_CD_API_TOKEN, variable: 'ARGO_CD_API_TOKEN')]) {
                    script {
                        (DEPLOY_URL) = at.craftworks.ArgoCDUtils.appCreateOrUpdate(
                            steps,
                            env,
                            ARGO_CD_API_TOKEN,
                            env.K8S_AGRO_CD_PROJECT_NAME,
                            env.K8S_NAMESPACE,
                            env.K8S_REPO,
                            env.K8S_DEPLOY_FOLDER,
                            env.K8S_DEPLOY_URL,
                            env.K8S_DEPLOY_URL_RELEASE_NAME_PLACEHOLDER,
                            RELEASE_NAME,
                            CLUSTER,
                            "--helm-set image.version=${IMAGE_VERSION} --helm-set build.number=${env.BUILD_NUMBER}"
                        )
                    }
                }
            }
        }

        // STAGE_POST_URL_TO_PR
        // Requires: STAGE_DEPLOY
        stage('post deployment URL to GitHub PR') {
            when {
                beforeAgent true
                expression { STAGE_BUILD_PUSH_DEPLOY && at.craftworks.CommonUtils.isPR(env) }
            }
            agent none
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    script {
                        at.craftworks.PrUtils.addDeploymentUrlToPr(DEPLOY_URL, pullRequest)
                    }
                }
            }
        }

        // STAGE_DEPLOY_CLEANUP
        // TODO(CWDEV-217): Move out of Jenkinsfile as this stage has nothing to do with the current build and hurts build time
        stage('cleanup k8s deployments') {
            when {
                beforeAgent true
                expression { STAGE_DEPLOY_CLEANUP }
            }
            agent none
            steps {
                withCredentials([string(credentialsId: env.K8S_CREDENTIALS_ID_ARGO_CD_API_TOKEN, variable: 'ARGO_CD_API_TOKEN')]) {
                    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                        script {
                            at.craftworks.ArgoCDUtils.appDeleteForDeletedGitBranches(
                                steps,
                                env,
                                ARGO_CD_API_TOKEN,
                                CLUSTER
                            )
                        }
                    }
                }
            }
        }
    }

    post {
        aborted {
            script {
                if (STAGE_SEND_SLACK_MESSAGE) {
                    at.craftworks.SlackUtils.sendAbortedMessage(currentBuild, steps, env, env.SLACK_CHANNEL);
                }
            }
        }
        failure {
            script {
                if (STAGE_SEND_SLACK_MESSAGE) {
                    at.craftworks.SlackUtils.sendFailureMessage(currentBuild, steps, env, env.SLACK_CHANNEL);
                }
            }
        }
        success {
            script {
                if (STAGE_SEND_SLACK_MESSAGE) {
                    boolean sendSlackMessage = false
                    if (STAGE_SEND_SLACK_MESSAGE == "no-success") {
                        sendSlackMessage = at.craftworks.SlackUtils.isPreviousBuildResultNoSuccess(currentBuild)
                    } else {
                        sendSlackMessage = true
                    }

                    if (sendSlackMessage) {
                        if(STAGE_BUILD_PUSH_DEPLOY) {
                            at.craftworks.SlackUtils.sendSuccessBuiltPushedDeployedMessage(currentBuild, steps, env, env.SLACK_CHANNEL, BUILD_VERSION, IMAGE_VERSION, DEPLOY_URL);
                        } else {
                            at.craftworks.SlackUtils.sendSuccessMessage(currentBuild, steps, env, env.SLACK_CHANNEL);
                        }
                    }
                }
            }
        }
        always {
            script {
                cleanWs()
                // workaround. Seems to be a new bug
                currentBuild.result = currentBuild.currentResult
                // sh 'git checkout -- .'
            }
        }
    }
}
