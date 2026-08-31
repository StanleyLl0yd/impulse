#!/usr/bin/env bash
set -euo pipefail

REPO="${1:-StanleyLl0yd/impulse}"
RULESET_NAME="Main branch protection"

gh auth status >/dev/null

echo "Configuring repository settings for $REPO..."
gh api --method PATCH "repos/$REPO" \
  -F allow_squash_merge=true \
  -F allow_merge_commit=false \
  -F allow_rebase_merge=false \
  -F delete_branch_on_merge=true \
  -F has_issues=true \
  -F has_projects=false \
  -F has_wiki=false >/dev/null

for endpoint in vulnerability-alerts automated-security-fixes private-vulnerability-reporting; do
  if gh api --method PUT "repos/$REPO/$endpoint" >/dev/null 2>&1; then
    echo "Enabled $endpoint."
  else
    echo "Could not enable $endpoint through the API; check GitHub Security settings manually if needed."
  fi
done

if gh api --method PATCH "repos/$REPO" \
  -F 'security_and_analysis[secret_scanning][status]=enabled' \
  -F 'security_and_analysis[secret_scanning_push_protection][status]=enabled' >/dev/null 2>&1; then
  echo "Enabled secret scanning and push protection."
else
  echo "Could not change secret-scanning settings through the API; public-repository defaults may already apply."
fi

existing_id="$(gh api "repos/$REPO/rulesets" --jq ".[] | select(.name == \"$RULESET_NAME\") | .id" | head -n 1)"
if [[ -n "$existing_id" ]]; then
  echo "Ruleset '$RULESET_NAME' already exists (id $existing_id); leaving it unchanged."
else
  gh api --method POST "repos/$REPO/rulesets" --input - >/dev/null <<'JSON'
{
  "name": "Main branch protection",
  "target": "branch",
  "enforcement": "active",
  "conditions": {
    "ref_name": {
      "include": ["~DEFAULT_BRANCH"],
      "exclude": []
    }
  },
  "rules": [
    {"type": "deletion"},
    {"type": "non_fast_forward"},
    {
      "type": "required_status_checks",
      "parameters": {
        "strict_required_status_checks_policy": true,
        "do_not_enforce_on_create": false,
        "required_status_checks": [
          {"context": "verify"}
        ]
      }
    },
    {
      "type": "pull_request",
      "parameters": {
        "required_approving_review_count": 0,
        "dismiss_stale_reviews_on_push": false,
        "required_reviewers": [],
        "require_code_owner_review": false,
        "require_last_push_approval": false,
        "required_review_thread_resolution": true,
        "require_extra_approval_for_unattributed_changes": false,
        "allowed_merge_methods": ["squash"]
      }
    }
  ],
  "bypass_actors": []
}
JSON
  echo "Created '$RULESET_NAME'."
fi

echo "Repository hardening complete."
