#!/bin/bash
# Publish local artifacts and run the consumer examples against them.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
GRADLEW="${REPO_ROOT}/gradlew"
DEFAULT_VERSION="$(awk -F= '$1 == "developmentVersion" { print substr($0, index($0, "=") + 1) }' "${REPO_ROOT}/gradle.properties")"
PUBLISHED_MODULES="$(awk -F= '$1 == "publishedModules" { print substr($0, index($0, "=") + 1) }' "${REPO_ROOT}/gradle.properties")"
JAVA_MINIMUM_VERSION="$(awk -F= '$1 == "javaMinimumVersion" { print substr($0, index($0, "=") + 1) }' "${REPO_ROOT}/gradle.properties")"
VERSION="${KRANDOM_VERSION:-${DEFAULT_VERSION}}"
REQUIRE_JAVA17="${KRANDOM_REQUIRE_JAVA17:-false}"

if [[ -z "${PUBLISHED_MODULES}" ]]; then
    echo "gradle.properties must define publishedModules." >&2
    exit 1
fi
if [[ -z "${JAVA_MINIMUM_VERSION}" ]]; then
    echo "gradle.properties must define javaMinimumVersion." >&2
    exit 1
fi
PUBLISH_TASKS=()
IFS=',' read -r -a published_modules <<< "${PUBLISHED_MODULES}"
for module in "${published_modules[@]}"; do
    PUBLISH_TASKS+=(":${module}:publishToMavenLocal")
done

step() { echo; echo "==> $*"; }

# Whether Gradle's toolchain detection, which the example builds use, finds a JDK of the given version, whose
# directory it then sets in FOUND_JDK_HOME. Without one the run fails when KRANDOM_REQUIRE_JAVA17=true, as CI
# sets, and skips the examples on that Java otherwise.
FOUND_JDK_HOME=""
require_jdk() {
    local version="$1" toolchains
    if ! toolchains="$("${GRADLEW}" -p "${REPO_ROOT}/examples/java-gradle" -q javaToolchains --no-daemon --console=plain)"; then
        echo "Listing the JDKs that Gradle detects failed."
        exit 1
    fi
    # Each detected Java is an entry that starts with " + "; the report also lists JREs, which cannot compile.
    # Prints the location of the first JDK of that version.
    FOUND_JDK_HOME="$(awk -v version="${version}" '
        function check() { if (found == "" && language == version && jdk == "true") found = home }
        /^ \+ / { check(); language = ""; jdk = ""; home = "" }
        /\| Location:/ { home = $0; sub(/^.*\| Location:[[:space:]]*/, "", home) }
        /\| Language Version:/ { language = $NF }
        /\| Is JDK:/ { jdk = $NF }
        END { check(); if (found != "") print found }' <<< "${toolchains}")"
    if [[ -n "${FOUND_JDK_HOME}" ]]; then
        return 0
    fi
    if [[ "${REQUIRE_JAVA17}" == "true" ]]; then
        echo "Gradle detects no JDK ${version} (./gradlew javaToolchains), which KRANDOM_REQUIRE_JAVA17=true requires."
        exit 1
    fi
    return 1
}

cd "${REPO_ROOT}"

"${REPO_ROOT}/scripts/require_java21.sh"

step "Publish krandom modules ${VERSION} to Maven local"
"${GRADLEW}" \
    "${PUBLISH_TASKS[@]}" \
    -PreleaseVersion="${VERSION}" \
    --no-daemon \
    --console=plain

step "Verify Java + Gradle example"
(
    cd "${REPO_ROOT}/examples/java-gradle"
    ./gradlew -PkrandomVersion="${VERSION}" test --no-daemon --console=plain
)

# The oldest Java that kRandom runs on: a consumer on it resolves the Gradle metadata and runs the modules.
if require_jdk "${JAVA_MINIMUM_VERSION}"; then
    step "Verify Java + Gradle example on Java ${JAVA_MINIMUM_VERSION}"
    (
        cd "${REPO_ROOT}/examples/java-gradle"
        ./gradlew -PkrandomVersion="${VERSION}" -PjavaVersion="${JAVA_MINIMUM_VERSION}" test --no-daemon --console=plain
    )
else
    step "Skip Java + Gradle example on Java ${JAVA_MINIMUM_VERSION}"
    echo "Gradle detects no JDK ${JAVA_MINIMUM_VERSION} (./gradlew javaToolchains); install one, or set KRANDOM_REQUIRE_JAVA17=true to fail instead."
fi

step "Verify Java + Maven example"
(
    cd "${REPO_ROOT}/examples/java-maven"
    mvn -q -Dkrandom.version="${VERSION}" test
)
