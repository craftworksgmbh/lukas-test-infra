# Set working dir
cd "$(dirname "$0")/.."

# 1. Run e2e tests via docker-compose
docker-compose -f docker-compose.yml -f docker-compose.e2e.yml up --build --exit-code-from e2e
# 2. Extract test-results
docker cp $(docker-compose -f docker-compose.yml -f docker-compose.e2e.yml ps -q e2e):e2e/cypress e2e