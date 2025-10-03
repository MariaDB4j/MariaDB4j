#!/usr/bin/env bash

set -euxo pipefail

if ! [ -e ".venv/bin/pre-commit" ]; then
  if ! [ -x "$(command -v python3)" ]; then
    echo "python3 is not installed, please run e.g. 'sudo apt-get install virtualenv python3-venv' (or an equivalent)"
    exit 255
  fi

  if ! [ -d .venv/ ]; then
     python3 -m venv .venv
  fi
  # shellcheck disable=SC1091
  source .venv/bin/activate

  # .venv/bin/pip install -r requirements.txt
  .venv/bin/pip install pre-commit==4.3.0

else
  # shellcheck disable=SC1091
  source .venv/bin/activate
fi

pre-commit run --all-files
