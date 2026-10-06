#!/bin/bash
echo "========================================================"
echo " Starting AURA Financial Platform (Java Backend)"
echo "========================================================"
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$DIR"
mvn spring-boot:run
