# Changelog Generator Usage Guide

## Quick Start

```bash
# Preview changes without modifying
./changes_update.sh --dry-run

# Generate and update changelog
./changes_update.sh

# Specify starting point
./changes_update.sh --since v0.4.0
```

## How It Works

1. **Detects changes** from git commits since last tag
2. **Categorizes** based on conventional commit prefixes
3. **Generates** formatted markdown entry
4. **Shows preview** for review
5. **Prompts** to accept/edit/cancel
6. **Inserts** into change.md with automatic backup

## Conventional Commit Format

For best results, use conventional commit messages:

```bash
# Features
git commit -m "feat: Add number formatting"
git commit -m "feature: Smart autocorrect system"

# Bug Fixes
git commit -m "fix: ALT+Space triggering symbol picker"
git commit -m "bugfix: Currency symbol spacing"

# Improvements
git commit -m "improve: Better phone detection"
git commit -m "perf: Optimize dictionary loading"

# Testing
git commit -m "test: Add NumberFormatter tests"

# Documentation
git commit -m "docs: Update README"

# Dependencies
git commit -m "deps: Update Kotlin to 1.9.22"
git commit -m "build: Upgrade Gradle"

# Refactoring
git commit -m "refactor: Simplify shouldFormatInField"

# CI/CD
git commit -m "ci: Add GitHub Actions workflow"

# Maintenance
git commit -m "chore: Clean up unused imports"
```

## Interactive Review

When prompted:
- **Y** (or Enter): Accept and insert into changelog
- **e**: Edit in your editor ($EDITOR or nano)
- **n**: Cancel without changes

## Backup System

Every run creates a backup:
- Location: `.changelog_backups/change.md.YYYYMMDD_HHMMSS`
- Automatically excluded from git
- Useful for recovery if needed

## Tips

1. **Use conventional commits** for automatic categorization
2. **Review before accepting** - you can always edit
3. **Run dry-run first** to preview
4. **Commit manually** after reviewing
5. **Create git tags** for version milestones

## Example Workflow

```bash
# 1. Make some changes and commit with conventional format
git commit -m "feat: Add smart number formatting"
git commit -m "fix: ALT+Space symbol picker bug"
git commit -m "test: Add comprehensive unit tests"

# 2. Preview the changelog
./changes_update.sh --dry-run

# 3. Generate and review
./changes_update.sh
# -> Review, edit if needed, accept

# 4. Commit the changelog
git add change.md
git commit -m "docs: Update changelog for v0.4.1"

# 5. Tag the release
git tag -a "v0.4.1" -m "Release v0.4.1"
git push origin v0.4.1
```

## Troubleshooting

### "No changes detected"
- No commits since last tag
- Try: `./changes_update.sh --since HEAD~5`

### "Not a git repository"
- Must run from project root
- Ensure `.git/` directory exists

### Commits in "Miscellaneous"
- Commits don't follow conventional format
- Edit them manually in review step

### Version mismatch
- Version read from `app/build.gradle.kts`
- Update versionName before running

## Files Modified

- `change.md` - Updated with new entry
- `.changelog_backups/` - Backup created (auto-gitignored)

## Created By

Script: `changes_update.sh`
Author: Aryo Karbhawono
Date: 2025-01-22
