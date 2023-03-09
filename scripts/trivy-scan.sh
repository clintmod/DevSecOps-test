#!/usr/bin/env bash

set -e

OLD_CONTAINER_ID=$(docker ps -a | grep trivy | awk '{print $1}')
if [[ -n "${OLD_CONTAINER_ID}" ]]; then
    docker rm -f "${OLD_CONTAINER_ID}"
fi

set +e

docker run \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v "${HOME}/trivy/.cache:/root/.cache/" \
    -v "${PWD}/.trivyignore:/.trivyignore" \
    --workdir="/app" \
    --name trivy \
aquasec/trivy:0.22.0 \
    image \
    --timeout 10m \
    --output trivy-scan-report \
    --exit-code 1 --severity CRITICAL \
    --ignore-unfixed --timeout 20m \
    --ignorefile /.trivyignore \
    "${IMAGE_NAME}"
EXIT_CODE=$?

set -e

CONTAINER_ID=$(docker ps -a | grep trivy | awk '{print $1}')

docker cp "${CONTAINER_ID}:/app/trivy-scan-report" "${PWD}/reports/trivy-scan-report"

cat reports/trivy-scan-report

if [[ "$EXIT_CODE" -eq 0 ]]; then
    echo
    echo "No critical vulnerabilities found"
    echo
    exit 0
else
    echo
    echo "Critical vulnerabilities found"
    echo
    exit 1
fi
