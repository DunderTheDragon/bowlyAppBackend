#!/usr/bin/env bash
# Smoke test API Bowly — wymaga działającego backendu i REGISTRATION_SECRET w .env
set -euo pipefail

BASE_URL="${1:-http://localhost:8742}"
SECRET="${REGISTRATION_SECRET:-test-registration-secret-min-32-chars}"
USER="smoke_$(date +%s)"
PASS="smokepass12"

echo "==> Status"
curl -sf "$BASE_URL/api/system/status" | head -c 200
echo

echo "==> Register $USER"
REGISTER=$(curl -sf -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USER\",\"password\":\"$PASS\",\"registrationSecret\":\"$SECRET\"}")
TOKEN=$(echo "$REGISTER" | python3 -c "import sys,json; print(json.load(sys.stdin).get('token',''))")
if [ -z "$TOKEN" ]; then echo "Brak tokena po rejestracji"; exit 1; fi

echo "==> Create product"
PRODUCT=$(curl -sf -X POST "$BASE_URL/api/products/local" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Smoke Ryż","source":"LOCAL","calories":130,"protein":2.7,"fat":0.3,"carbohydrates":28}')
PID=$(echo "$PRODUCT" | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))")

echo "==> Create batch meal"
BATCH=$(curl -sf -X POST "$BASE_URL/api/batch-meals" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Smoke patelnia\",\"saveAsRecipe\":false,\"recipeSections\":[],\"segments\":[{\"name\":\"Sekcja\",\"productId\":\"$PID\",\"initialWeightG\":500,\"totalKcal\":500,\"totalProtein\":10,\"totalFat\":5,\"totalCarbs\":50}]}")
SID=$(echo "$BATCH" | python3 -c "import sys,json; print(json.load(sys.stdin)['segments'][0]['id'])")

echo "==> Consume portion"
curl -sf -X POST "$BASE_URL/api/batch-meals/consume" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"segmentId\":$SID,\"weightG\":100,\"mealType\":\"LUNCH\",\"mealDate\":\"2026-06-06\"}" >/dev/null

echo "==> Daily diary"
curl -sf "$BASE_URL/api/diary/daily?date=2026-06-06" \
  -H "Authorization: Bearer $TOKEN" | head -c 300
echo
echo "OK — smoke test zakończony pomyślnie"
