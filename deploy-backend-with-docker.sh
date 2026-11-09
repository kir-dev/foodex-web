#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REGISTRY="harbor.sch.bme.hu"
IMAGE="${REGISTRY}/org-kir-dev/foodex-backend:latest"
NAMESPACE="foodex"

cd "${ROOT}/apps/backend"

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
# Bust only the runtime COPY --from layer. The classic builder can reuse a
# stale JAR if this instruction text is unchanged after the Gradle stage.
JAR_CACHE_ID="$(
  {
    find gradle src -type f
    printf '%s\n' gradlew settings.gradle.kts build.gradle.kts
  } | sort | xargs sha256sum | sha256sum | awk '{print $1}'
)"
docker build --build-arg JAR_CACHE_ID="${JAR_CACHE_ID}" -t "${IMAGE}" .

echo "==> Pushing ${IMAGE}"
docker push "${IMAGE}"

echo "==> Restarting StatefulSet foodex-backend in ${NAMESPACE}"
kubectl rollout restart statefulset/foodex-backend -n "${NAMESPACE}"
kubectl rollout status statefulset/foodex-backend -n "${NAMESPACE}"

echo "==> Done"
kubectl get pods -n "${NAMESPACE}" -l app=foodex-backend
