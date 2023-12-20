#!/bin/sh

echo "Starting deployment."

target_dir=$1

python3 -u python/main.py "${target_dir}"
exit_code=$?

echo "Finished deployment."
exit $exit_code