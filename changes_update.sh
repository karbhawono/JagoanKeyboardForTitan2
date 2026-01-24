#!/bin/bash
#
# changes_update.sh - Automated Changelog Generator for JagoanKeyboard
# 
# This script automatically detects code changes from git commits and updates
# CHANGES.md with properly formatted changelog entries.
#
# Usage:
#   ./changes_update.sh              # Auto-detect changes since last tag
#   ./changes_update.sh --since v0.4.0  # Changes since specific tag
#   ./changes_update.sh --dry-run    # Preview without modifying
#   ./changes_update.sh --help       # Show help
#
# Requirements:
#   - Git repository
#   - Conventional commit messages (feat:, fix:, docs:, etc.)
#   - app/build.gradle.kts with versionName
#
# Author: Aryo Karbhawono
# Date: 2025-01-22
#

set -e

# ============================================================================
# Configuration
# ============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CHANGELOG="$SCRIPT_DIR/CHANGES.md"
GRADLE_FILE="$SCRIPT_DIR/app/build.gradle.kts"
BACKUP_DIR="$SCRIPT_DIR/.changelog_backups"
TEMP_ENTRY="/tmp/changelog_entry_$$.md"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Options
DRY_RUN=false
SINCE_REF=""

# ============================================================================
# Helper Functions
# ============================================================================

print_info() {
    echo -e "${CYAN}ℹ${NC} $1"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_section() {
    echo ""
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
}

show_help() {
    cat << EOF
Automated Changelog Generator for JagoanKeyboard

Usage: $0 [OPTIONS]

Options:
    --since <ref>    Changes since tag/commit (default: last tag)
    --dry-run        Preview without modifying files
    --help           Show this help message

Examples:
    $0                          # Auto-detect changes since last tag
    $0 --since v0.4.0           # Changes since v0.4.0
    $0 --dry-run                # Preview mode

Requirements:
    - Conventional commit messages (feat:, fix:, docs:, etc.)
    - Git repository
    - app/build.gradle.kts with versionName

Conventional Commit Prefixes:
    feat:     New features
    fix:      Bug fixes
    docs:     Documentation changes
    test:     Testing additions/changes
    refactor: Code refactoring
    perf:     Performance improvements
    improve:  General improvements
    build:    Build system changes
    deps:     Dependency updates
    ci:       CI/CD changes
    chore:    Maintenance tasks

EOF
    exit 0
}

# ============================================================================
# Validation Functions
# ============================================================================

check_requirements() {
    print_info "Checking requirements..."
    
    # Check if git repository
    if ! git rev-parse --git-dir > /dev/null 2>&1; then
        print_error "Not a git repository"
        exit 1
    fi
    
    # Check if CHANGES.md exists
    if [[ ! -f "$CHANGELOG" ]]; then
        print_error "CHANGES.md not found at: $CHANGELOG"
        exit 1
    fi
    
    # Check if build.gradle.kts exists
    if [[ ! -f "$GRADLE_FILE" ]]; then
        print_error "app/build.gradle.kts not found at: $GRADLE_FILE"
        exit 1
    fi
    
    print_success "All requirements met"
}

# ============================================================================
# Version Management
# ============================================================================

get_version_from_gradle() {
    local version=$(grep 'versionName = ' "$GRADLE_FILE" | sed 's/.*versionName = "\(.*\)".*/\1/')
    
    if [[ -z "$version" ]]; then
        print_error "Could not extract version from build.gradle.kts"
        exit 1
    fi
    
    echo "$version"
}

get_last_git_tag() {
    local tag=$(git describe --tags --abbrev=0 2>/dev/null || echo "")
    
    if [[ -z "$tag" ]]; then
        # No tags found, use initial commit
        echo $(git rev-list --max-parents=0 HEAD)
    else
        echo "$tag"
    fi
}

# ============================================================================
# Backup Management
# ============================================================================

backup_changelog() {
    mkdir -p "$BACKUP_DIR"
    
    local timestamp=$(date +%Y%m%d_%H%M%S)
    local backup_file="$BACKUP_DIR/CHANGES.md.$timestamp"
    
    cp "$CHANGELOG" "$backup_file"
    print_success "Backup created: $backup_file"
    
    echo "$backup_file"
}

# ============================================================================
# Git Analysis
# ============================================================================

detect_commits() {
    local since_ref="$1"
    
    # Get commits in format: "hash|subject"
    git log ${since_ref}..HEAD --pretty=format:"%h|%s" 2>/dev/null || echo ""
}

categorize_commit() {
    local msg="$1"
    
    case "$msg" in
        feat:*|feature:*)
            echo "FEATURES"
            ;;
        fix:*|bugfix:*)
            echo "FIXES"
            ;;
        docs:*|doc:*)
            echo "DOCS"
            ;;
        test:*|tests:*)
            echo "TESTS"
            ;;
        refactor:*)
            echo "REFACTOR"
            ;;
        perf:*|improve:*|improvement:*)
            echo "IMPROVEMENTS"
            ;;
        build:*|deps:*|dep:*)
            echo "DEPENDENCIES"
            ;;
        ci:*)
            echo "CI"
            ;;
        chore:*)
            echo "CHORE"
            ;;
        *)
            echo "MISC"
            ;;
    esac
}

extract_commit_message() {
    local msg="$1"
    
    # Remove prefix (feat:, fix:, etc.) and clean up
    echo "$msg" | sed -E 's/^[a-z]+: *//' | sed 's/^/- /'
}

detect_file_changes() {
    local since_ref="$1"
    
    git diff --name-only ${since_ref}..HEAD 2>/dev/null || echo ""
}

categorize_file() {
    local filepath="$1"
    
    case "$filepath" in
        */test/*Test.kt|*/test/*.kt)
            echo "Testing"
            ;;
        */ui/*.kt)
            echo "UI/UX"
            ;;
        */engine/*.kt)
            echo "Core Engine"
            ;;
        */ime/*.kt)
            echo "Input Method"
            ;;
        */util/*.kt)
            echo "Utilities"
            ;;
        */data/repository/*.kt)
            echo "Data Layer"
            ;;
        */domain/*.kt)
            echo "Domain Layer"
            ;;
        *build.gradle*|gradle.properties)
            echo "Build System"
            ;;
        *.md)
            echo "Documentation"
            ;;
        *)
            echo "Other"
            ;;
    esac
}

# ============================================================================
# Changelog Generation
# ============================================================================

generate_changelog_entry() {
    local version="$1"
    local since_ref="$2"
    local date=$(date +%Y-%m-%d)
    
    # Initialize category variables
    local FEATURES=""
    local FIXES=""
    local IMPROVEMENTS=""
    local TESTS=""
    local DOCS=""
    local DEPENDENCIES=""
    local REFACTOR=""
    local CI=""
    local CHORE=""
    local MISC=""
    
    # Detect commits
    local commits=$(detect_commits "$since_ref")
    local commit_count=0
    
    if [[ -n "$commits" ]]; then
        while IFS='|' read -r hash subject; do
            if [[ -n "$subject" ]]; then
                local category=$(categorize_commit "$subject")
                local message=$(extract_commit_message "$subject")
                
                case "$category" in
                    FEATURES)     FEATURES+="$message"$'\n' ;;
                    FIXES)        FIXES+="$message"$'\n' ;;
                    IMPROVEMENTS) IMPROVEMENTS+="$message"$'\n' ;;
                    TESTS)        TESTS+="$message"$'\n' ;;
                    DOCS)         DOCS+="$message"$'\n' ;;
                    DEPENDENCIES) DEPENDENCIES+="$message"$'\n' ;;
                    REFACTOR)     REFACTOR+="$message"$'\n' ;;
                    CI)           CI+="$message"$'\n' ;;
                    CHORE)        CHORE+="$message"$'\n' ;;
                    MISC)         MISC+="$message"$'\n' ;;
                esac
                
                ((commit_count++))
            fi
        done <<< "$commits"
    fi
    
    print_success "Found $commit_count commits"
    
    # Detect file changes
    local files=$(detect_file_changes "$since_ref")
    local file_count=0
    
    if [[ -n "$files" ]]; then
        file_count=$(echo "$files" | wc -l | tr -d ' ')
        print_success "Detected $file_count file changes"
    fi
    
    # Build changelog entry
    cat > "$TEMP_ENTRY" << EOF
## [$version] - $date

EOF
    
    # Add Features section
    if [[ -n "$FEATURES" ]]; then
        cat >> "$TEMP_ENTRY" << EOF
### 🎉 New Features

${FEATURES}
EOF
    fi
    
    # Add Bug Fixes section
    if [[ -n "$FIXES" ]]; then
        cat >> "$TEMP_ENTRY" << EOF
### 🐛 Bug Fixes

${FIXES}
EOF
    fi
    
    # Add Improvements section
    if [[ -n "$IMPROVEMENTS" ]]; then
        cat >> "$TEMP_ENTRY" << EOF
### 🔧 Improvements

${IMPROVEMENTS}
EOF
    fi
    
    # Add Testing section
    if [[ -n "$TESTS" ]]; then
        cat >> "$TEMP_ENTRY" << EOF
### 🧪 Testing

${TESTS}
EOF
    fi
    
    # Add Dependencies section
    if [[ -n "$DEPENDENCIES" ]]; then
        cat >> "$TEMP_ENTRY" << EOF
### 📦 Dependencies

${DEPENDENCIES}
EOF
    fi
    
    # Add Documentation section
    if [[ -n "$DOCS" ]]; then
        cat >> "$TEMP_ENTRY" << EOF
### 📝 Documentation

${DOCS}
EOF
    fi
    
    # Add Refactoring section
    if [[ -n "$REFACTOR" ]]; then
        cat >> "$TEMP_ENTRY" << EOF
### 🏗️ Refactoring

${REFACTOR}
EOF
    fi
    
    # Add CI section
    if [[ -n "$CI" ]]; then
        cat >> "$TEMP_ENTRY" << EOF
### 🔄 CI/CD

${CI}
EOF
    fi
    
    # Add Chore section
    if [[ -n "$CHORE" ]]; then
        cat >> "$TEMP_ENTRY" << EOF
### 🧹 Chores

${CHORE}
EOF
    fi
    
    # Add Miscellaneous section
    if [[ -n "$MISC" ]]; then
        cat >> "$TEMP_ENTRY" << EOF
### 📋 Miscellaneous

${MISC}
EOF
    fi
    
    # Add separator
    cat >> "$TEMP_ENTRY" << EOF

---

EOF
    
    # Check if entry is essentially empty (only header and separator)
    local entry_lines=$(cat "$TEMP_ENTRY" | grep -v '^$' | grep -v '^---$' | wc -l | tr -d ' ')
    if [[ $entry_lines -le 1 ]]; then
        print_warning "No changes detected - generated empty entry"
        cat >> "$TEMP_ENTRY" << EOF
### 📋 Notes

No significant changes detected. Manual entry may be required.

EOF
    fi
}

# ============================================================================
# User Interface
# ============================================================================

show_preview() {
    print_section "Generated Changelog Entry"
    cat "$TEMP_ENTRY"
    print_section "End of Entry"
}

prompt_review() {
    while true; do
        echo ""
        read -p "Review this changelog entry? [Y/e(dit)/n(o)]: " response
        
        case "$response" in
            [Yy]|"")
                return 0
                ;;
            [Ee])
                ${EDITOR:-nano} "$TEMP_ENTRY"
                print_section "Updated Entry (after editing)"
                cat "$TEMP_ENTRY"
                print_section "End of Entry"
                ;;
            [Nn])
                print_info "Changelog update aborted"
                cleanup
                exit 0
                ;;
            *)
                print_warning "Invalid input. Please enter Y, e, or n"
                ;;
        esac
    done
}

# ============================================================================
# File Operations
# ============================================================================

insert_entry() {
    local entry_file="$1"
    local temp_output="$CHANGELOG.tmp"
    
    # Insert after first "---" line, before first version entry
    awk -v entry_file="$entry_file" '
        /^---$/ && !inserted {
            print
            print ""
            while ((getline line < entry_file) > 0) {
                print line
            }
            close(entry_file)
            inserted=1
            next
        }
        {print}
    ' "$CHANGELOG" > "$temp_output"
    
    mv "$temp_output" "$CHANGELOG"
    print_success "Changelog updated successfully"
}

# ============================================================================
# Cleanup
# ============================================================================

cleanup() {
    if [[ -f "$TEMP_ENTRY" ]]; then
        rm -f "$TEMP_ENTRY"
    fi
}

trap cleanup EXIT

# ============================================================================
# Main Script
# ============================================================================

main() {
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --since)
                SINCE_REF="$2"
                shift 2
                ;;
            --dry-run)
                DRY_RUN=true
                shift
                ;;
            --help)
                show_help
                ;;
            *)
                print_error "Unknown option: $1"
                echo "Use --help for usage information"
                exit 1
                ;;
        esac
    done
    
    # Print header
    echo ""
    echo -e "${BLUE}╔════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║  JagoanKeyboard Changelog Generator           ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════════════╝${NC}"
    echo ""
    
    # Check requirements
    check_requirements
    
    # Get version from gradle
    VERSION=$(get_version_from_gradle)
    print_info "Current version: $VERSION (from app/build.gradle.kts)"
    
    # Determine since reference
    if [[ -z "$SINCE_REF" ]]; then
        SINCE_REF=$(get_last_git_tag)
        print_info "Detecting changes since: $SINCE_REF (last tag)"
    else
        print_info "Detecting changes since: $SINCE_REF (user specified)"
    fi
    
    # Verify reference exists
    if ! git rev-parse "$SINCE_REF" >/dev/null 2>&1; then
        print_error "Reference '$SINCE_REF' not found in git"
        exit 1
    fi
    
    # Create backup
    if [[ "$DRY_RUN" = false ]]; then
        BACKUP_FILE=$(backup_changelog)
    else
        print_info "DRY RUN mode - no backup created"
    fi
    
    # Generate changelog entry
    print_info "Analyzing commits and changes..."
    generate_changelog_entry "$VERSION" "$SINCE_REF"
    
    # Show preview
    show_preview
    
    # Dry run - exit here
    if [[ "$DRY_RUN" = true ]]; then
        print_info "DRY RUN complete - no changes made"
        echo ""
        print_info "Entry saved to: $TEMP_ENTRY"
        # Don't cleanup in dry-run so user can review temp file
        trap - EXIT
        exit 0
    fi
    
    # Prompt for review
    prompt_review
    
    # Insert entry
    insert_entry "$TEMP_ENTRY"
    
    # Success message
    echo ""
    print_section "✅ Success!"
    print_success "Changelog updated: $CHANGELOG"
    print_success "Backup saved: $BACKUP_FILE"
    echo ""
    print_info "Next steps:"
    echo "  1. Review the updated CHANGES.md"
    echo "  2. Make any manual adjustments if needed"
    echo "  3. Commit the changes:"
    echo "     git add CHANGES.md"
    echo "     git commit -m \"docs: Update changelog for v$VERSION\""
    echo "     git tag -a \"v$VERSION\" -m \"Release v$VERSION\""
    echo ""
}

# Run main script
main "$@"