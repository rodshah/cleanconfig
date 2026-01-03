#!/bin/bash
set -e

# Security: Script only does git operations (commit, tag, push).
# Publishing requires GitHub Secrets (GitHub Actions) or ~/.gradle/gradle.properties (local).
# Only users with repo push access + Maven Central credentials can release.

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_info() { echo -e "${GREEN}✓${NC} $1"; }
print_warn() { echo -e "${YELLOW}⚠${NC} $1"; }
print_error() { echo -e "${RED}✗${NC} $1"; }
print_step() { echo -e "${BLUE}→${NC} $1"; }

echo ""
echo "========================================="
echo "   CleanConfig Release Script"
echo "========================================="
echo ""

# Check if on main branch
CURRENT_BRANCH=$(git branch --show-current)
if [ "$CURRENT_BRANCH" != "main" ]; then
    print_error "You must be on the main branch to release."
    echo "  Current branch: $CURRENT_BRANCH"
    exit 1
fi

# Check for uncommitted changes
if ! git diff-index --quiet HEAD --; then
    print_error "You have uncommitted changes. Please commit or stash them first."
    echo ""
    git status --short
    exit 1
fi

# Pull latest changes
print_step "Pulling latest changes from origin..."
git pull origin main

# Get current version
CURRENT_VERSION=$(grep "^version=" gradle.properties | cut -d'=' -f2)
SUGGESTED_VERSION=${CURRENT_VERSION%-SNAPSHOT}

echo ""
print_info "Current version: $CURRENT_VERSION"
echo ""

# Ask user for release version
read -p "Enter release version [$SUGGESTED_VERSION]: " RELEASE_VERSION
RELEASE_VERSION=${RELEASE_VERSION:-$SUGGESTED_VERSION}

# Validate version format
if ! [[ $RELEASE_VERSION =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    print_error "Invalid version format: $RELEASE_VERSION"
    echo "  Expected format: X.Y.Z (e.g., 0.1.0)"
    exit 1
fi

# Calculate next snapshot version (increment patch)
IFS='.' read -r MAJOR MINOR PATCH <<< "$RELEASE_VERSION"
NEXT_PATCH=$((PATCH + 1))
NEXT_VERSION="$MAJOR.$MINOR.$NEXT_PATCH-SNAPSHOT"

# Ask user for next snapshot version (allow override)
echo ""
read -p "Enter next development version [$NEXT_VERSION]: " USER_NEXT_VERSION
NEXT_VERSION=${USER_NEXT_VERSION:-$NEXT_VERSION}

# Validate next version has -SNAPSHOT
if [[ ! $NEXT_VERSION =~ -SNAPSHOT$ ]]; then
    print_error "Next version must end with -SNAPSHOT: $NEXT_VERSION"
    exit 1
fi

# Show release plan
echo ""
print_warn "Release Plan:"
echo "  Current version:  $CURRENT_VERSION"
echo "  Release version:  $RELEASE_VERSION"
echo "  Next version:     $NEXT_VERSION"
echo ""
read -p "Proceed with release? (y/N): " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    print_warn "Release cancelled."
    exit 0
fi

echo ""
echo "========================================="
echo "   Starting Release Process"
echo "========================================="
echo ""

# Step 0: Run full build and tests
print_step "Step 0: Running full Gradle build and tests..."
echo "  This may take a few minutes..."
if ! ./gradlew clean build test --no-daemon; then
    print_error "Build failed! Cannot proceed with release."
    echo ""
    echo "Please fix the failing tests/build issues and try again."
    exit 1
fi
print_info "Build and tests passed"
echo ""

# Step 1: Update to release version
print_step "Step 1: Updating version to $RELEASE_VERSION..."
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    sed -i '' "s/version=.*/version=$RELEASE_VERSION/" gradle.properties
else
    # Linux
    sed -i "s/version=.*/version=$RELEASE_VERSION/" gradle.properties
fi
print_info "Version updated in gradle.properties"

# Step 2: Commit release version
print_step "Step 2: Committing release version..."
git add gradle.properties
git commit -m "chore: release $RELEASE_VERSION"
print_info "Release commit created"

# Step 3: Create tag
print_step "Step 3: Creating tag v$RELEASE_VERSION..."
git tag -a "v$RELEASE_VERSION" -m "Release version $RELEASE_VERSION"
print_info "Tag created"

# Step 4: Push commit and tag
print_step "Step 4: Pushing to remote..."
git push origin main
git push origin "v$RELEASE_VERSION"
print_info "Release pushed to GitHub"

echo ""
print_info "Release v$RELEASE_VERSION triggered!"
echo "  Monitor at: https://github.com/rodshah/cleanconfig/actions"
echo ""

# Wait a moment
sleep 2

# Step 5: Update to next snapshot version
print_step "Step 5: Updating version to $NEXT_VERSION..."
if [[ "$OSTYPE" == "darwin"* ]]; then
    sed -i '' "s/version=.*/version=$NEXT_VERSION/" gradle.properties
else
    sed -i "s/version=.*/version=$NEXT_VERSION/" gradle.properties
fi
print_info "Version updated to next snapshot"

# Step 6: Commit next snapshot version
print_step "Step 6: Committing next development version..."
git add gradle.properties
git commit -m "chore: prepare for next development iteration ($NEXT_VERSION)"
print_info "Snapshot commit created"

# Step 7: Push snapshot version
print_step "Step 7: Pushing snapshot version..."
git push origin main
print_info "Snapshot version pushed"

echo ""
echo "========================================="
echo "   Release Complete! 🎉"
echo "========================================="
echo ""
echo "What's next:"
echo "  1. Monitor GitHub Actions:"
echo "     https://github.com/rodshah/cleanconfig/actions"
echo ""
echo "  2. Check deployment status (wait 2-5 minutes):"
echo "     https://central.sonatype.com/publishing/deployments"
echo ""
echo "  3. Verify on Maven Central (after ~15-30 minutes):"
echo "     https://central.sonatype.com/artifact/com.cleanconfig/cleanconfig-core/$RELEASE_VERSION"
echo ""
echo "  4. Update release notes on GitHub:"
echo "     https://github.com/rodshah/cleanconfig/releases/tag/v$RELEASE_VERSION"
echo ""
