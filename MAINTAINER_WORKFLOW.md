# Maintainer Workflow

This guide describes the normal maintenance and release loop for Outlook Message Parser.

## Ground Rules

- Work on the repository's single `master` branch and keep it fast-forward aligned with `origin/master`.
- Use Java 8 for local verification; Java 8 is the minimum supported runtime.
- Reuse the repository's existing GitHub labels. `enhancement` and `major feature` are mutually exclusive; add orthogonal labels such as `bug`, `security`, `dependencies`, or a priority when useful.
- Do not release unless the request authorizes it. Once a semantic-version release is authorized, that authorization covers the matching CircleCI approval, Maven Central verification, GitHub release, and milestone closure.
- Preserve unrelated local changes and never rewrite shared history.

## Start Clean

```powershell
git status --short --branch
git fetch --prune --tags origin
git pull --ff-only origin master
gh auth status
java -version  # should report 1.8.x
```

Stop and inspect any branch divergence or unrecognized worktree change before editing.

## Triage GitHub Work

Inspect issues, pull requests, labels, milestones, and releases before deciding what needs to change:

```powershell
gh issue list --repo bbottema/outlook-message-parser --state all --limit 100
gh pr list --repo bbottema/outlook-message-parser --state all --limit 100
gh api 'repos/bbottema/outlook-message-parser/milestones?state=all&per_page=100' --paginate
gh release list --repo bbottema/outlook-message-parser --limit 100
```

Create an issue for a coherent bug, feature, security fix, or user-visible enhancement. Do not create synthetic issues for tiny documentation, style, build-version, or routine dependency chores unless they materially affect users. Link the implementing commit or pull request and avoid duplicates.

## Milestones

- Release milestone titles are the exact numeric versions without a `v` prefix.
- Create or reuse the milestone when the version is known. Keep its description empty.
- Assign every issue and pull request represented by the release, including material dependency work. Exclude rejected, duplicate, unrelated, and superseded proposals.
- Set `due_on` to the planned release date while open. After publication, replace it with the actual release date at UTC midnight.
- Close the milestone only after every included item is closed and membership has been checked against the release notes.
- Historical milestones use the original tag or documented release date, not the bookkeeping date.

```powershell
$version = "1.16.2"
$milestones = gh api 'repos/bbottema/outlook-message-parser/milestones?state=all&per_page=100' --paginate | ConvertFrom-Json
$milestone = $milestones | Where-Object title -eq $version
gh api "repos/bbottema/outlook-message-parser/issues?milestone=$($milestone.number)&state=all&per_page=100" --paginate
```

GitHub can normalize dates unexpectedly. After creating or updating a milestone, read it back and verify the raw `due_on` value equals the intended `YYYY-MM-DDT00:00:00Z`.

## Implement and Verify

Keep changes focused and add regression coverage for behavior fixes. Run the full Java 8 verification before merging or releasing:

```powershell
mvn verify -Dmaven.javadoc.skip=true -Djacoco.skip=true -Dlicense.skip=true
git diff --check
```

The project inherits most Maven and Java configuration from `standard-project-parent`; inspect the effective POM when the Java baseline or plugin behavior is relevant.

## Release Notes

- Keep `RELEASE.txt` and the release section in `README.md` aligned with user-facing changes.
- Write concise entries that identify the exact version and link the relevant local issue or pull request.
- Explain compatibility, Java-baseline, dependency, or migration consequences when users may encounter them.
- Group routine dependency maintenance; omit internal build and test evidence.
- A revoked release remains documented with a direct upgrade recommendation.

Every published tag has its own GitHub release, including patches. Its title is `v{version}` and its body is a permanent, self-contained, tag-specific record. Links support the summary but never replace it. Do not attach assets that belong to another tag.

## Release

The CircleCI workflow builds `master` and exposes patch, minor, major, and as-is approval gates. Approve only the authorized gate. The deploy job owns version changes, Maven Central publication, the release commit, and the tag; do not pre-edit the POM version or create a tag manually.

After deployment:

1. Fetch the generated branch update and tags.
2. Verify the version exists in Maven Central under `org.simplejavamail:outlook-message-parser`.
3. Create or update the GitHub release for the existing numeric tag, titled `v{version}`.
4. Recheck milestone membership, close all represented items, set the actual release date, and close the milestone.
5. Confirm local `master` is clean and aligned with `origin/master`.

```powershell
gh release create $version --repo bbottema/outlook-message-parser --title "v$version" --notes-file RELEASE_NOTES.md
```

If Maven Central published but repository automation failed, diagnose that boundary before making any manual release commit or tag. Published Central versions are immutable; correct an artifact problem with a new patch release.

## Definition of Done

- The implementation and regression coverage are complete.
- Java 8 verification passes, or any external failure is explicitly recorded.
- GitHub labels, issue/PR state, and exact-version milestone are correct.
- `README.md`, `RELEASE.txt`, and the self-contained GitHub release agree on user-visible changes.
- The numeric tag and Maven Central artifact exist for a release.
- The milestone is closed with the actual release date and all represented items closed.
- `master` is clean and aligned with `origin/master`.
