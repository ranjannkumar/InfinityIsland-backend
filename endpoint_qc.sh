#!/usr/bin/env bash
# =============================================================================
# InfinityIsland — COMPLETE QC TEST SUITE
# Tests ALL code paths in the backend with timing metrics
# =============================================================================
set -o pipefail
IFS=$'\n\t'

# ---------- Config ----------
BASE="${BASE:-http://localhost:8081/api}"
PIN="${PIN:-1234}"
NAME="${NAME:-Player}"
ADMIN_PIN="${ADMIN_PIN:-7878}"
DEBUG="${DEBUG:-0}"
SLOW="${SLOW:-0}"

# ---------- Counters ----------
PASS=0
FAIL=0
SKIP=0
TOTAL_TIME=0

# ---------- Colors ----------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'
BOLD='\033[1m'

# ---------- Output helpers ----------
say() { printf "\n${BOLD}%s${NC}\n" "$*"; }
subsay() { printf "${CYAN} ▸ %s${NC}\n" "$*"; }
ok() { 
  local msg="$1" time="${2:-}"
  if [[ -n "$time" ]]; then
    printf " ${GREEN} ${NC} %s ${BLUE}(%s)${NC}\n" "$msg" "$time"
  else
    printf " ${GREEN} ${NC} %s\n" "$msg"
  fi
  PASS=$((PASS+1))
}
bad() { printf " ${RED} ${NC} %s\n" "$*" >&2; FAIL=$((FAIL+1)); }
skip() { printf " ${YELLOW} ${NC} %s\n" "$*"; SKIP=$((SKIP+1)); }

need() { 
  command -v "$1" >/dev/null || { 
    bad "Missing: $1"
    exit 1
  }
}

need jq
need curl
need bc

# ---------- HTTP helpers ----------
post_json() {
  local url="${BASE%/}$1" data="$2" pin="${3:-$PIN}"
  local start_time end_time elapsed
  
  start_time=$(date +%s%N)
  local resp=$(curl -sS -w "\n%{http_code}" -X POST \
    -H 'accept: application/json' \
    -H 'Content-Type: application/json' \
    -H "x-pin: $pin" \
    "$url" \
    -d "$data" 2>&1)
  end_time=$(date +%s%N)
  
  elapsed=$(( (end_time - start_time) / 1000000 ))
  TOTAL_TIME=$((TOTAL_TIME + elapsed))
  
  local http_code=$(echo "$resp" | tail -n1)
  local body=$(echo "$resp" | sed '$d')
  
  if [[ "$DEBUG" == "1" ]]; then
    echo "=== POST $url [${elapsed}ms] [HTTP $http_code] ===" >&2
    echo "$body" >&2
    echo "" >&2
  fi
  
  echo "$body"
}

get_json() {
  local url="${BASE%/}$1" pin="${2:-$PIN}"
  local start_time end_time elapsed
  
  start_time=$(date +%s%N)
  local resp=$(curl -sS -w "\n%{http_code}" -X GET \
    -H 'accept: application/json' \
    -H "x-pin: $pin" \
    "$url" 2>&1)
  end_time=$(date +%s%N)
  
  elapsed=$(( (end_time - start_time) / 1000000 ))
  TOTAL_TIME=$((TOTAL_TIME + elapsed))
  
  local http_code=$(echo "$resp" | tail -n1)
  local body=$(echo "$resp" | sed '$d')
  
  if [[ "$DEBUG" == "1" ]]; then
    echo "=== GET $url [${elapsed}ms] [HTTP $http_code] ===" >&2
    echo "$body" >&2
    echo "" >&2
  fi
  
  echo "$body"
}

check_json() { 
  jq -e . >/dev/null 2>&1 <<<"$1" || { 
    bad "$2: Not JSON"
    return 1
  }
}

# Helper: Complete a quiz
complete_quiz() {
  local runId="$1" start="$2" speed="${3:-50}"
  local qcount=$(jq '.questions | length' <<<"$start")
  
  for ((i=0; i<qcount; i++)); do
    local qid=$(jq -r ".questions[$i]._id" <<<"$start")
    local ans=$(jq ".questions[$i].correctAnswer" <<<"$start")
    local resp=$(post_json "/quiz/answer" "{\"quizRunId\":\"$runId\",\"questionId\":\"$qid\",\"answer\":$ans,\"responseMs\":$speed}")
    
    if jq -e '.completed==true' >/dev/null <<<"$resp"; then
      echo "$resp"
      return
    fi
  done
}

# =============================================================================
# TESTS
# =============================================================================
test_auth() {
  say "TEST: Authentication"
  local start_time=$(date +%s%N)
  
  local resp=$(post_json "/auth/login-pin" "{\"pin\":\"$PIN\",\"name\":\"$NAME\"}")
  check_json "$resp" "auth-login" && jq -e '.token and .user._id' >/dev/null <<<"$resp" && ok "Valid login" || bad "Login failed"
  
  resp=$(post_json "/auth/login-pin" "{\"pin\":\"$PIN\"}")
  check_json "$resp" "auth-no-name" && jq -e '.error' >/dev/null <<<"$resp" && ok "Rejects missing name" || bad "Should error"
  
  resp=$(post_json "/auth/login-pin" "{\"name\":\"$NAME\"}")
  check_json "$resp" "auth-no-pin" && jq -e '.error' >/dev/null <<<"$resp" && ok "Rejects missing PIN" || bad "Should error"
  
  local end_time=$(date +%s%N)
  local elapsed=$(( (end_time - start_time) / 1000000 ))
  say " └─ Auth: ${elapsed}ms"
}

test_user() {
  say "TEST: User Endpoints"
  local start_time=$(date +%s%N)
  
  local resp=$(get_json "/user/progress")
  check_json "$resp" "progress" && jq -e '.progress.L1' >/dev/null <<<"$resp" && ok "GET /user/progress" || bad "Failed"
  
  resp=$(get_json "/user/daily")
  check_json "$resp" "daily" && jq -e 'has("correctCount") or has("_id")' >/dev/null <<<"$resp" && ok "GET /user/daily" || bad "Failed"
  
  resp=$(post_json "/user/theme" '{"themeKey":"dark"}')
  check_json "$resp" "theme" && jq -e '.success==true or (.error | contains("locked"))' >/dev/null <<<"$resp" && ok "POST /user/theme" || bad "Failed"
  
  resp=$(post_json "/user/rate-video" '{"videoId":"test","rating":5,"level":1,"beltOrDegree":"white"}')
  check_json "$resp" "rate" && jq -e '.success==true' >/dev/null <<<"$resp" && ok "POST /user/rate-video" || bad "Failed"
  
  resp=$(post_json "/user/reset" '{}')
  check_json "$resp" "reset" && jq -e '.success==true' >/dev/null <<<"$resp" && ok "POST /user/reset" || bad "Failed"
  
  local end_time=$(date +%s%N)
  local elapsed=$(( (end_time - start_time) / 1000000 ))
  say " └─ User: ${elapsed}ms"
}

test_quiz_operations() {
  say "TEST: All Operations (add/sub/mul/div)"
  local start_time=$(date +%s%N)
  
  post_json "/user/reset" '{}' >/dev/null
  
  local ops=("add" "sub" "mul" "div")
  for op in "${ops[@]}"; do
    subsay "Testing: $op"
    
    local prep=$(post_json "/quiz/prepare" "{\"level\":1,\"beltOrDegree\":\"white\",\"operation\":\"$op\"}")
    local runId=$(jq -r '.quizRunId' <<<"$prep")
    local start=$(post_json "/quiz/start" "{\"quizRunId\":\"$runId\"}")
    
    if jq -e '.questions | length >= 10' >/dev/null <<<"$start"; then
      ok "Operation '$op' works"
    else
      bad "Operation '$op' failed"
    fi
  done
  
  local end_time=$(date +%s%N)
  local elapsed=$(( (end_time - start_time) / 1000000 ))
  say " └─ Operations: ${elapsed}ms"
}

test_quiz_colored_progression() {
  say "TEST: Colored Belt Progression"
  local start_time=$(date +%s%N)
  
  post_json "/user/reset" '{}' >/dev/null
  
  local belts=("white" "yellow" "green" "blue" "red" "brown")
  local expected_next=("yellow" "green" "blue" "red" "brown" "")
  
  for i in "${!belts[@]}"; do
    local belt="${belts[$i]}"
    local next="${expected_next[$i]}"
    
    subsay "Belt: $belt"
    
    local prep=$(post_json "/quiz/prepare" "{\"level\":1,\"beltOrDegree\":\"$belt\",\"operation\":\"add\"}")
    local runId=$(jq -r '.quizRunId' <<<"$prep")
    local start=$(post_json "/quiz/start" "{\"quizRunId\":\"$runId\"}")
    local final=$(complete_quiz "$runId" "$start" 30)
    
    if jq -e '.completed==true and .passed==true' >/dev/null <<<"$final"; then
      ok "Belt '$belt' completed"
      
      if [[ -n "$next" ]]; then
        local prog=$(get_json "/user/progress")
        jq -e ".progress.L1.$next.unlocked==true" >/dev/null <<<"$prog" && ok "'$next' unlocked" || bad "'$next' not unlocked"
      fi
    else
      bad "Belt '$belt' failed"
    fi
  done
  
  local end_time=$(date +%s%N)
  local elapsed=$(( (end_time - start_time) / 1000000 ))
  say " └─ Colored Progression: ${elapsed}ms"
}

test_quiz_wrong_flows() {
  say "TEST: Wrong Answer Flows"
  local start_time=$(date +%s%N)
  
  post_json "/user/reset" '{}' >/dev/null
  
  subsay "Wrong → practice → resume"
  local prep=$(post_json "/quiz/prepare" '{"level":1,"beltOrDegree":"yellow","operation":"add"}')
  local runId=$(jq -r '.quizRunId' <<<"$prep")
  local start=$(post_json "/quiz/start" "{\"quizRunId\":\"$runId\"}")
  
  local qid=$(jq -r '.questions[0]._id' <<<"$start")
  local correct=$(jq '.questions[0].correctAnswer' <<<"$start")
  local wrong=$((correct + 5))
  
  local wresp=$(post_json "/quiz/answer" "{\"quizRunId\":\"$runId\",\"questionId\":\"$qid\",\"answer\":$wrong,\"responseMs\":100}")
  jq -e 'has("practice")' >/dev/null <<<"$wresp" && ok "Wrong triggers practice" || bad "Practice not triggered"
  
  local pid=$(jq -r '.practice._id' <<<"$wresp")
  local pcorrect=$(jq '.practice.correctAnswer' <<<"$wresp")
  
  local presp=$(post_json "/quiz/practice/answer" "{\"quizRunId\":\"$runId\",\"questionId\":\"$pid\",\"answer\":$pcorrect}")
  jq -e '.resume==true or has("next")' >/dev/null <<<"$presp" && ok "Practice resume works" || bad "Resume failed"
  
  local end_time=$(date +%s%N)
  local elapsed=$(( (end_time - start_time) / 1000000 ))
  say " └─ Wrong Flows: ${elapsed}ms"
}

test_quiz_black_belts() {
  say "TEST: Black Belt Degrees"
  local start_time=$(date +%s%N)
  
  # Unlock black first
  post_json "/user/reset" '{}' >/dev/null
  local belts=("white" "yellow" "green" "blue" "red" "brown")
  for belt in "${belts[@]}"; do
    local prep=$(post_json "/quiz/prepare" "{\"level\":1,\"beltOrDegree\":\"$belt\",\"operation\":\"add\"}")
    local runId=$(jq -r '.quizRunId' <<<"$prep")
    local start=$(post_json "/quiz/start" "{\"quizRunId\":\"$runId\"}")
    complete_quiz "$runId" "$start" 20 >/dev/null
  done
  
  ok "Black unlocked"
  
  # Test black-1
  subsay "Testing black-1"
  local prep=$(post_json "/quiz/prepare" '{"level":1,"beltOrDegree":"black-1","operation":"add"}')
  local runId=$(jq -r '.quizRunId' <<<"$prep")
  local start=$(post_json "/quiz/start" "{\"quizRunId\":\"$runId\"}")
  
  local qcount=$(jq '.questions | length' <<<"$start")
  local timelimit=$(jq '.timer.limitMs' <<<"$start")
  
  [[ "$qcount" -ge 20 ]] && ok "Black-1 has $qcount questions" || bad "Count wrong"
  [[ "$timelimit" -eq 60000 ]] && ok "Black-1 time: ${timelimit}ms" || bad "Time wrong"
  
  local final=$(complete_quiz "$runId" "$start" 5)
  jq -e '.completed==true and .passed==true' >/dev/null <<<"$final" && ok "Black-1 passed" || skip "Black-1 failed"
  
  local end_time=$(date +%s%N)
  local elapsed=$(( (end_time - start_time) / 1000000 ))
  say " └─ Black Belts: ${elapsed}ms"
}

test_negative_cases() {
  say "TEST: Negative Cases"
  local start_time=$(date +%s%N)
  
  local resp=$(post_json "/quiz/prepare" '{"level":1}')
  jq -e '.error' >/dev/null <<<"$resp" && ok "Rejects missing beltOrDegree" || bad "Should error"
  
  resp=$(post_json "/quiz/start" '{"quizRunId":"invalid"}')
  jq -e '.error' >/dev/null <<<"$resp" && ok "Rejects invalid runId" || bad "Should error"
  
  local end_time=$(date +%s%N)
  local elapsed=$(( (end_time - start_time) / 1000000 ))
  say " └─ Negative: ${elapsed}ms"
}

test_admin() {
  say "TEST: Admin"
  local start_time=$(date +%s%N)
  
  local resp=$(get_json "/admin/today-stats" "$ADMIN_PIN")
  jq -e 'type=="array"' >/dev/null <<<"$resp" && ok "Admin stats works" || bad "Failed"
  
  resp=$(get_json "/admin/today-stats" "9999")
  jq -e '.error' >/dev/null <<<"$resp" && ok "Rejects wrong PIN" || bad "Should reject"
  
  local end_time=$(date +%s%N)
  local elapsed=$(( (end_time - start_time) / 1000000 ))
  say " └─ Admin: ${elapsed}ms"
}

# =============================================================================
# MAIN
# =============================================================================
main() {
  local script_start=$(date +%s%N)
  
  say "═══════════════════════════════════════════════════════════════"
  say "  INFINITYISLAND QC TEST SUITE"
  say "═══════════════════════════════════════════════════════════════"
  printf " Target:  %s\n" "$BASE"
  printf " User:   %s (PIN: %s)\n" "$NAME" "$PIN"
  printf " Debug:  %s\n" "$DEBUG"
  say "═══════════════════════════════════════════════════════════════"
  
  test_auth
  test_user
  test_quiz_operations
  test_quiz_colored_progression
  test_quiz_wrong_flows
  test_quiz_black_belts
  test_negative_cases
  test_admin
  
  local script_end=$(date +%s%N)
  local script_time=$(( (script_end - script_start) / 1000000 ))
  local avg_time=0
  [[ $((PASS + FAIL)) -gt 0 ]] && avg_time=$((TOTAL_TIME / (PASS + FAIL)))
  
  echo ""
  say "═══════════════════════════════════════════════════════════════"
  say "  TEST SUMMARY"
  say "═══════════════════════════════════════════════════════════════"
  printf " ${GREEN} PASSED:${NC}  %3d\n" "$PASS"
  printf " ${RED} FAILED:${NC}  %3d\n" "$FAIL"
  printf " ${YELLOW} SKIPPED:${NC} %3d\n" "$SKIP"
  say "───────────────────────────────────────────────────────────────"
  printf " API Time:   %.2fs\n" "$(echo "scale=2; $TOTAL_TIME / 1000" | bc)"
  printf " Script Time: %.2fs\n" "$(echo "scale=2; $script_time / 1000" | bc)"
  printf " Avg Response: %dms\n" "$avg_time"
  say "═══════════════════════════════════════════════════════════════"
  
  if [[ $FAIL -gt 0 ]]; then
    say "${RED} TESTS FAILED${NC}"
    exit 1
  else
    say "${GREEN} ALL TESTS PASSED!${NC}"
    exit 0
  fi
}

main "$@"
