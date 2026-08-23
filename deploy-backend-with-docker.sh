#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REGISTRY="harbor.sch.bme.hu"
IMAGE="${REGISTRY}/org-kir-dev/foodex-backend:latest"
NAMESPACE="foodex"

cd "${ROOT}/apps/backend"

echo "==> Logging in to ${REGISTRY}"
docker login "${REGISTRY}"

echo "==> Building ${IMAGE}"
# --no-cache: the legacy builder can reuse COPY --from=build layers and ship a stale JAR.
docker build --no-cache -t "${IMAGE}" .

echo "==> Pushing ${IMAGE}"
docker push "${IMAGE}"

echo "==> Restarting StatefulSet foodex-backend in ${NAMESPACE}"
kubectl rollout restart statefulset/foodex-backend -n "${NAMESPACE}"
kubectl rollout status statefulset/foodex-backend -n "${NAMESPACE}"

echo "==> Done"
kubectl get pods -n "${NAMESPACE}" -l app=foodex-backend
