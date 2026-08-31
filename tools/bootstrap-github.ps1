param(
    [string]$Repo = "StanleyLl0yd/impulse"
)

$ErrorActionPreference = "Stop"
if ($PSVersionTable.PSVersion.Major -ge 7) {
    $PSNativeCommandUseErrorActionPreference = $true
}
$RulesetName = "Main branch protection"

gh auth status | Out-Null

Write-Host "Configuring repository settings for $Repo..."
gh api --method PATCH "repos/$Repo" `
    -F "allow_squash_merge=true" `
    -F "allow_merge_commit=false" `
    -F "allow_rebase_merge=false" `
    -F "delete_branch_on_merge=true" `
    -F "has_issues=true" `
    -F "has_projects=false" `
    -F "has_wiki=false" | Out-Null

foreach ($endpoint in @("vulnerability-alerts", "automated-security-fixes", "private-vulnerability-reporting")) {
    try {
        gh api --method PUT "repos/$Repo/$endpoint" | Out-Null
        Write-Host "Enabled $endpoint."
    }
    catch {
        Write-Warning "Could not enable $endpoint through the API. Check GitHub Security settings manually if needed."
    }
}

try {
    gh api --method PATCH "repos/$Repo" `
        -F "security_and_analysis[secret_scanning][status]=enabled" `
        -F "security_and_analysis[secret_scanning_push_protection][status]=enabled" | Out-Null
    Write-Host "Enabled secret scanning and push protection."
}
catch {
    Write-Warning "Could not change secret-scanning settings through the API; public-repository defaults may already apply."
}

$existingId = gh api "repos/$Repo/rulesets" --jq ".[] | select(.name == `"$RulesetName`") | .id" | Select-Object -First 1
if ($existingId) {
    Write-Host "Ruleset '$RulesetName' already exists (id $existingId); leaving it unchanged."
}
else {
    $ruleset = @{
        name = $RulesetName
        target = "branch"
        enforcement = "active"
        conditions = @{
            ref_name = @{
                include = @("~DEFAULT_BRANCH")
                exclude = @()
            }
        }
        rules = @(
            @{ type = "deletion" },
            @{ type = "non_fast_forward" },
            @{
                type = "required_status_checks"
                parameters = @{
                    strict_required_status_checks_policy = $true
                    do_not_enforce_on_create = $false
                    required_status_checks = @(
                        @{ context = "verify" }
                    )
                }
            },
            @{
                type = "pull_request"
                parameters = @{
                    required_approving_review_count = 0
                    dismiss_stale_reviews_on_push = $false
                    required_reviewers = @()
                    require_code_owner_review = $false
                    require_last_push_approval = $false
                    required_review_thread_resolution = $true
                    require_extra_approval_for_unattributed_changes = $false
                    allowed_merge_methods = @("squash")
                }
            }
        )
        bypass_actors = @()
    } | ConvertTo-Json -Depth 10

    $ruleset | gh api --method POST "repos/$Repo/rulesets" --input - | Out-Null
    Write-Host "Created '$RulesetName'."
}

Write-Host "Repository hardening complete."
