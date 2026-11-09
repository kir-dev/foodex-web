#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REGISTRY="harbor.sch.bme.hu"
IMAGE="${REGISTRY}/org-kir-dev/foodex-frontend:latest"
NAMESPACE="foodex"

cd "${ROOT}/apps/frontend"

echo "==> Logging in to ${REGISTRY}"
if [[ -n "${HARBOR_USERNAME:-}" || -n "${HARBOR_PASSWORD:-}" ]]; then
  if [[ -z "${HARBOR_USERNAME:-}" || -z "${HARBOR_PASSWORD:-}" ]]; then
    echo "Set both HARBOR_USERNAME and HARBOR_PASSWORD, or leave both unset." >&2
    exit 1
  fi
  printf '%s' "${HARBOR_PASSWORD}" | docker login "${REGISTRY}" --username "${HARBOR_USERNAME}" --password-stdin
else
  docker login "${REGISTRY}"
fi

echo "==> Building ${IMAGE}"
docker build -t "${IMAGE}" .

echo "==> Pushing ${IMAGE}"
docker push "${IMAGE}"

echo "==> Restarting Deployment foodex-frontend in ${NAMESPACE}"
kubectl rollout restart deployment/foodex-frontend -n "${NAMESPACE}"
kubectl rollout status deployment/foodex-frontend -n "${NAMESPACE}"

echo "==> Done"
kubectl get pods -n "${NAMESPACE}" -l app=foodex-frontend
