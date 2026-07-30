#!/bin/bash
set -e

SOURCE_FILE="/usr/local/google/home/pwujczyk/github/Home.Configuration/fitness.api.terraform.tfvars"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
TARGET_FILE="$PROJECT_ROOT/terraform/terraform.tfvars"

echo "Copying Terraform variables..."
echo "From: $SOURCE_FILE"
echo "To:   $TARGET_FILE"

if [ ! -f "$SOURCE_FILE" ]; then
    echo "Error: Source file $SOURCE_FILE does not exist." >&2
    exit 1
fi

mkdir -p "$(dirname "$TARGET_FILE")"
cp "$SOURCE_FILE" "$TARGET_FILE"

echo "Successfully copied and renamed to terraform.tfvars!"
