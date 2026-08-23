#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REGISTRY="harbor.sch.bme.hu"
IMAGE="${REGISTRY}/org-kir-dev/foodex-frontend:latest"
NAMESPACE="foodex"

cd "${ROOT}/apps/frontend"

echo "==> Logging in to ${REGISTRY}"
docker login "${REGISTRY}"

echo "==> Building ${IMAGE}"
docker build -t "${IMAGE}" .

echo "==> Pushing ${IMAGE}"
docker push "${IMAGE}"

echo "==> Restarting Deployment foodex-frontend in ${NAMESPACE}"
kubectl rollout restart deployment/foodex-frontend -n "${NAMESPACE}"
kubectl rollout status deployment/foodex-frontend -n "${NAMESPACE}"

echo "==> Done"
kubectl get pods -n "${NAMESPACE}" -l app=foodex-frontend
