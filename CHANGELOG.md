# MariaDB4j Release Notes

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## v3.3.0 - 2025-10-03 (Issue #[1250](https://github.com/MariaDB4j/MariaDB4j/issues/1250))

### TL;DR

* Moved datadir resuse behind existing newEmbeddedDB() call by @TheKnowles in https://github.com/MariaDB4j/MariaDB4j/pull/1218
* Open existing database by @xtianus in https://github.com/MariaDB4j/MariaDB4j/pull/1166
* feat: Add newBuilder(DBConfigurationBuilder cloneFrom) by @vorburger in https://github.com/MariaDB4j/MariaDB4j/pull/1252

### Details

* fix: Sonatype's PITA by @vorburger in https://github.com/MariaDB4j/MariaDB4j/pull/1263
* fix: Make copy of executables Map in DBConfiguration by @vorburger in https://github.com/MariaDB4j/MariaDB4j/pull/1256
* fix: Add missing Maven POM <name> which block deploy (see #1250) by @vorburger in https://github.com/MariaDB4j/MariaDB4j/pull/1265
* fix: Include cause in re-throw by @vorburger in https://github.com/MariaDB4j/MariaDB4j/pull/1257
* docs: Add jvm-repo-rebuild/reproducible-central link to README by @vorburger in https://github.com/MariaDB4j/MariaDB4j/pull/1164
* docs: Update CHANGELOG by @vorburger in https://github.com/MariaDB4j/MariaDB4j/pull/1163
* chore: Add additional pre-commit-hooks by @vorburger in https://github.com/MariaDB4j/MariaDB4j/pull/1170
* chore: Bump root POM's pom-lite version from 2.2.2 to 2.3.0 by @vorburger in https://github.com/MariaDB4j/MariaDB4j/pull/1264
* chore: chmod -x README.md by @vorburger in https://github.com/MariaDB4j/MariaDB4j/pull/1171
* chore: Increment project version from 3.2.0-SNAPSHOT to 3.2.1-SNAPSHOT by @vorburger in https://github.com/MariaDB4j/MariaDB4j/pull/1249
* chore: Increment project version from 3.2.1-SNAPSHOT to 3.3.0-SNAPSHOT by @vorburger in https://github.com/MariaDB4j/MariaDB4j/pull/1259
* chore: Use windows-2025 instead of windows-latest by @vorburger in https://github.com/MariaDB4j/MariaDB4j/pull/1221
* clean: Remove last remnants of long un-used maven-checkstyle-plugin by @vorburger in https://github.com/MariaDB4j/MariaDB4j/pull/1255
* clean: Remove un-used Error Prone annotations by @vorburger in https://github.com/MariaDB4j/MariaDB4j/pull/1253

### Version Bumps

* Bump error-prone to 2.42.0 with manual fix by @vorburger in https://github.com/MariaDB4j/MariaDB4j/pull/1251
* chore: Fix problem with new maven-invoker-plugin version by @vorburger in https://github.com/MariaDB4j/MariaDB4j/pull/1254

with:

* build(deps-dev): Bump org.junit.jupiter:junit-jupiter-engine from 5.12.1 to 5.12.2 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1180
* build(deps-dev): Bump org.junit.jupiter:junit-jupiter-engine from 5.12.2 to 5.13.0 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1187
* build(deps-dev): Bump org.junit.jupiter:junit-jupiter-engine from 5.13.0 to 5.13.4 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1210
* build(deps-dev): Bump org.junit.jupiter:junit-jupiter-engine from 5.13.4 to 6.0.0 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1247
* build(deps): Bump actions/checkout from 4.2.2 to 5.0.0 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1229
* build(deps): Bump actions/dependency-review-action from 4.5.0 to 4.6.0 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1176
* build(deps): Bump actions/dependency-review-action from 4.6.0 to 4.7.1 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1190
* build(deps): Bump actions/dependency-review-action from 4.7.1 to 4.8.0 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1237
* build(deps): Bump actions/setup-java from 4.7.0 to 4.7.1 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1177
* build(deps): Bump actions/setup-java from 4.7.1 to 5.0.0 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1228
* build(deps): Bump advanced-security/maven-dependency-submission-action from 4.1.1 to 4.1.2 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1175
* build(deps): Bump advanced-security/maven-dependency-submission-action from 4.1.2 to 5.0.0 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1191
* build(deps): Bump awalsh128/cache-apt-pkgs-action from 1.5.0 to 1.5.1 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1214
* build(deps): Bump awalsh128/cache-apt-pkgs-action from 1.5.1 to 1.5.3 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1226
* build(deps): Bump ch.vorburger.exec:exec from 3.3.1 to 3.3.2 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1186
* build(deps): Bump com.spotify.fmt:fmt-maven-plugin from 2.25 to 2.27 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1189
* build(deps): Bump com.spotify.fmt:fmt-maven-plugin from 2.27 to 2.29 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1243
* build(deps): Bump commons-io:commons-io from 2.18.0 to 2.19.0 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1182
* build(deps): Bump commons-io:commons-io from 2.19.0 to 2.20.0 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1213
* build(deps): Bump errorprone.version from 2.37.0 to 2.38.0 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1181
* build(deps): Bump github/codeql-action from 3.28.13 to 3.28.16 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1173
* build(deps): Bump github/codeql-action from 3.28.16 to 3.28.18 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1193
* build(deps): Bump github/codeql-action from 3.28.18 to 3.29.5 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1215
* build(deps): Bump github/codeql-action from 3.29.7 to 3.30.5 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1233
* build(deps): Bump github/codeql-action from 3.30.5 to 3.30.6 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1258
* build(deps): Bump maven.version from 3.9.9 to 3.9.11 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1208
* build(deps): Bump org.apache.maven:maven-compat from 3.9.9 to 3.9.11 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1209
* build(deps): Bump org.apache.maven.plugins:maven-compiler-plugin from 3.14.0 to 3.14.1 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1242
* build(deps): Bump org.apache.maven.plugins:maven-gpg-plugin from 3.2.7 to 3.2.8 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1212
* build(deps): Bump org.apache.maven.plugins:maven-javadoc-plugin from 3.11.2 to 3.12.0 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1245
* build(deps): Bump org.apache.maven.plugins:maven-shade-plugin from 3.6.0 to 3.6.1 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1244
* build(deps): Bump org.apache.maven.plugins:maven-surefire-plugin from 3.5.3 to 3.5.4 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1240
* build(deps): Bump org.apache.maven.surefire:surefire-junit47 from 3.5.3 to 3.5.4 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1246
* build(deps): Bump org.assertj:assertj-core from 3.27.3 to 3.27.6 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1241
* build(deps): Bump org.codehaus.mojo:license-maven-plugin from 2.5.0 to 2.6.0 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1198
* build(deps): Bump org.codehaus.mojo:license-maven-plugin from 2.6.0 to 2.7.0 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1236
* build(deps): Bump org.mockito:mockito-core from 5.16.1 to 5.17.0 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1179
* build(deps): Bump org.mockito:mockito-core from 5.17.0 to 5.18.0 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1188
* build(deps): Bump org.mockito:mockito-core from 5.18.0 to 5.20.0 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1232
* build(deps): Bump ossf/scorecard-action from 2.4.1 to 2.4.2 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1192
* build(deps): Bump ossf/scorecard-action from 2.4.2 to 2.4.3 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1238
* build(deps): Bump springboot.version from 3.4.4 to 3.4.5 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1178
* build(deps): Bump springboot.version from 3.4.5 to 3.5.0 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1185
* build(deps): Bump springboot.version from 3.5.0 to 3.5.4 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1211
* build(deps): Bump springboot.version from 3.5.4 to 3.5.6 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1239
* build(deps): Bump step-security/harden-runner from 2.11.0 to 2.12.0 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1174
* build(deps): Bump step-security/harden-runner from 2.12.0 to 2.13.0 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1216
* build(deps): Bump step-security/harden-runner from 2.13.0 to 2.13.1 by @dependabot[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1235
* build(deps): Upgrade pre-commit from 4.2.0 to 4.3.0 by @vorburger in https://github.com/MariaDB4j/MariaDB4j/pull/1260

and:

* [pre-commit.ci] pre-commit autoupdate by @pre-commit-ci[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1183
* [pre-commit.ci] pre-commit autoupdate by @pre-commit-ci[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1184
* [pre-commit.ci] pre-commit autoupdate by @pre-commit-ci[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1194
* [pre-commit.ci] pre-commit autoupdate by @pre-commit-ci[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1196
* [pre-commit.ci] pre-commit autoupdate by @pre-commit-ci[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1206
* [pre-commit.ci] pre-commit autoupdate by @pre-commit-ci[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1165
* [pre-commit.ci] pre-commit autoupdate by @pre-commit-ci[bot] in https://github.com/MariaDB4j/MariaDB4j/pull/1219

## New Contributors
* @xtianus made their first contribution in https://github.com/MariaDB4j/MariaDB4j/pull/1166

**Full Changelog**: https://github.com/MariaDB4j/MariaDB4j/compare/mariaDB4j-3.2.0...mariaDB4j-3.3.0


## v3.2.0 - 2024-04-10 (Issue #[1137](https://github.com/MariaDB4j/MariaDB4j/issues/1137))

See https://github.com/MariaDB4j/MariaDB4j/releases/tag/mariaDB4j-3.2.0 for more details, but the TL;DR is:

* Bumped MariaDB binaries from 10.11.5 to 11.4.5 (LTS); incl. macOS!
* Bumped ch.vorburger.exec [from 3.2.0 to 3.3.1](https://github.com/vorburger/ch.vorburger.exec/releases) (also released today)
* Switched tests from mysql-connector-java to mariadb-java-client
* Bumped many other external Maven dependencies
* Replaced Checkstyle with Google Code Format
* Fixed root causes of tests which never ran
* README cleaned up
* CI related chores

See https://github.com/MariaDB4j/MariaDB4j/compare/mariaDB4j-3.1.0...3.2.0 for full diff.

## v3.1.0 - 2024-03-27

See https://github.com/MariaDB4j/MariaDB4j/releases/tag/mariaDB4j-3.1.0:

* feat: Upgrade MariaDB binaries for Linux & Mac from 10.2.11 to 10.11.5 by @TheKnowles in https://github.com/vorburger/MariaDB4j/pull/771
* fix: Upgrade ch.vorburger.exec [from 3.1.5 to 3.2.0](https://github.com/vorburger/ch.vorburger.exec/compare/exec-3.1.5...exec-3.2.0); fixes e.g. [exec#9](https://github.com/vorburger/ch.vorburger.exec/issues/9)
* fix: Resource Leak from DirectoryStream in DBShutdownHook
* docs: Add 💸 OpenCollective etc. to README

### Chore & Cleanup etc.

* build: Add ErrorProne Code Quality Tool (fixes #736)
* chore: Apply StepSecurity best practices (see #785 for #661 and#786)
* chore: Create scorecard.yml (see #661)
* chore: Better Workflow Action Permissions (fixes #791)
* chore: Limit Workflow Action Permissions (fixes #791)
* chore: Remove Checkstyle from Pre-Commit (see #786)
* chore: Remove old DBs/ binaries (for #661)
* chore: Remove shellcheck from Pre-Commit (see #786)
* chore: Switch 3.0.3-SNAPSHOT to 3.1.0-SNAPSHOT
* docs: Add OpenSSF Best Practices badge to README (fixes #661)
* docs: Add libcrypt.so.1 tip to README (fixes #916)
* docs: Add Pre-Commit Hooks badge to README (see #786)
* fix: Remove trailing whitespaces from many files (for #786)
* fix: Reproducible Builds (re. #661)
* fix: Use JEP 247 to fix broken build under Java 21 (fixes #903)
* test: Added Windows MariaDB install action & enabled local mariadb test for Windows (#781 fixes #713)

### Dependency Upgrades

* build: Bump Maven from 3.9.0 to 3.9.6 (and Wrapper from 3.1.1 to 3.2.0)
* build(deps): Bump actions/checkout from 3.1.0 to 4.1.2
* build(deps): Bump actions/dependency-review-action from 2.5.1 to 4.2.3
* build(deps): Bump actions/setup-java from 3.13.0 to 4.2.1
* build(deps): Bump actions/upload-artifact from 3.1.0 to 4.3.1
* build(deps): Bump advanced-security/maven-dependency-submission-action
* build(deps): Bump actions/checkout from 3.1.0 to 4.1.2
* build(deps): Bump actions/dependency-review-action from 2.5.1 to 4.2.3
* build(deps): Bump actions/setup-java from 3.13.0 to 4.2.1
* build(deps): Bump actions/upload-artifact from 3.1.0 to 4.3.1
* build(deps): Bump advanced-security/maven-dependency-submission-action
* build(deps): Bump awalsh128/cache-apt-pkgs-action from 1.3.0 to 1.8.1
* build(deps): Bump commons-io:commons-io from 2.13.0 to 2.15.1
* build(deps): Bump com.puppycrawl.tools:checkstyle
* build(deps): Bump errorprone.version from 2.21.1 to 2.26.1
* build(deps): Bump github/codeql-action from 2.22.0 to 3.24.9
* build(deps): Bump maven.version from 3.9.4 to 3.9.6
* build(deps): Bump org.apache.maven:maven-compat from 3.9.4 to 3.9.6
* build(deps): Bump advanced-security/maven-dependency-submission-action
* build(deps): Bump awalsh128/cache-apt-pkgs-action from 1.3.0 to 1.8.1
* build(deps): Bump commons-io:commons-io from 2.13.0 to 2.15.1
* build(deps): Bump com.puppycrawl.tools:checkstyle
* build(deps): Bump errorprone.version from 2.21.1 to 2.26.1
* build(deps): Bump github/codeql-action from 2.22.0 to 3.24.9
* build(deps): Bump maven.version from 3.9.4 to 3.9.6
* build(deps): Bump org.apache.maven:maven-compat from 3.9.4 to 3.9.6
* build(deps): Bump org.apache.maven.plugins:maven-checkstyle-plugin
* build(deps): Bump org.apache.maven.plugins:maven-compiler-plugin
* build(deps): Bump org.apache.maven.plugins:maven-gpg-plugin
* build(deps): Bump org.apache.maven.plugins:maven-javadoc-plugin
* build(deps): Bump org.apache.maven.plugins:maven-plugin-plugin
* build(deps): Bump org.apache.maven.plugins:maven-project-info-reports-plugin
* build(deps): Bump org.apache.maven.plugins:maven-shade-plugin
* build(deps): Bump org.apache.maven.plugin-tools:maven-plugin-annotations
* build(deps): Bump org.assertj:assertj-core from 3.24.2 to 3.25.3
* build(deps): Bump org.codehaus.mojo:license-maven-plugin
* build(deps): Bump org.mockito:mockito-core from 5.5.0 to 5.11.0
* build(deps): Bump org.slf4j:slf4j-simple from 2.0.9 to 2.0.12
* build(deps): Bump ossf/scorecard-action from 2.1.2 to 2.3.1
* build(deps): Bump springboot.version from 3.1.3 to 3.2.4
* build(deps): Bump step-security/harden-runner from 2.5.1 to 2.7.0

This Changelog was manually written by the maintainer, based on: `git log --no-merges --pretty=format:"%s" mariaDB4j-3.0.2..main | sort | grep -v "\[maven-release-plugin\]" | sed 's/.*/\* &/' > CHANGELOG-git.md` (TODO: Write a thing which "collapses" Dependency Upgrades, instead of doing it by hand).

**Full Changelog**: https://github.com/MariaDB4j/MariaDB4j/compare/mariaDB4j-3.0.2...mariaDB4j-3.1.0

## v3.0.1 - 2023-05-01 (Issue #[696](https://github.com/MariaDB4j/MariaDB4j/issues/696))

See https://github.com/MariaDB4j/MariaDB4j/releases/tag/mariaDB4j-3.0.1:

* feat: Switch from Java 11 to Java 17
* feat: Expose Configuration defaultCharacterSet on MariaDB4jSpringService (#674)
* feat: Replace javax.annotation with jakarta.annotation (fixes #648)
* feat: Removing tmpdir argument from mysql_install_db execution
* feat: Rename master branch to main
* docs: Add https://tidelift.com/security link to SECURITY.md
* docs: Change mvn to ./mvnw on README
* build: Add Maven Wrapper
* build: Add workflow for a maven verify on windows-runner
* build: Add GitHub Action (instead of TravisCI; fixes #680)
* build: Move Java Version from GH WF YAML into .java-version from jenv
* build: Remove dorny/test-reporter GitHub Action (see #715)
* build: Remove .travis.yml (see #680)
* build: Replace fixed java-version: '17' in Windows Action with .java-version (like Linux)
* test: Skip testLocalMariaDB() if isWindows()
* test: Improve testLocalMariaDB() failure (fixes #681)

### Dependency Upgrades

* Bump assertj-core from 3.23.1 to 3.24.1
* Bump assertj-core from 3.24.1 to 3.24.2
* Bump checkstyle from 10.3.3 to 10.3.4
* Bump checkstyle from 10.3.4 to 10.4
* Bump checkstyle from 10.4 to 10.5.0
* Bump checkstyle from 10.5.0 to 10.6.0
* Bump checkstyle from 10.6.0 to 10.8.0
* Bump checkstyle from 10.8.0 to 10.8.1
* Bump checkstyle from 10.8.1 to 10.9.3
* Bump checkstyle from 10.9.3 to 10.10.0
* Bump ch.vorburger.exec to 3.1.4 (fixes #233)
* Bump codeql-action (fixes #685)
* Bump maven-checkstyle-plugin from 3.2.0 to 3.2.1
* Bump maven-checkstyle-plugin from 3.2.1 to 3.2.2
* Bump maven-compat from 3.8.6 to 3.8.7
* Bump maven-compat from 3.8.7 to 3.9.0
* Bump maven-compat from 3.9.0 to 3.9.1
* Bump maven-compiler-plugin from 3.10.1 to 3.11.0
* Bump maven-invoker-plugin from 3.3.0 to 3.4.0
* Bump maven-invoker-plugin from 3.4.0 to 3.5.0
* Bump maven-invoker-plugin from 3.5.0 to 3.5.1
* Bump maven-javadoc-plugin from 3.4.1 to 3.5.0
* Bump maven-plugin-annotations from 3.6.4 to 3.7.0
* Bump maven-plugin-annotations from 3.7.0 to 3.8.1
* Bump maven-plugin-annotations from 3.8.1 to 3.8.2
* Bump maven-plugin-plugin from 3.6.4 to 3.7.0
* Bump maven-plugin-plugin from 3.7.0 to 3.8.1
* Bump maven-plugin-plugin from 3.8.1 to 3.8.2
* Bump maven-project-info-reports-plugin from 3.4.1 to 3.4.2
* Bump maven-project-info-reports-plugin from 3.4.2 to 3.4.3
* Bump maven-release-plugin from 3.0.0-M6 to 3.0.0-M7
* Bump maven-release-plugin from 3.0.0-M7 to 3.0.0
* Bump maven-shade-plugin from 3.4.0 to 3.4.1
* Bump maven.version from 3.8.6 to 3.8.7
* Bump maven.version from 3.8.7 to 3.9.0
* Bump maven.version from 3.9.0 to 3.9.1
* Bump mockito-core from 4.11.0 to 5.1.1
* Bump mockito-core from 4.8.0 to 4.8.1
* Bump mockito-core from 4.8.1 to 4.9.0
* Bump mockito-core from 4.9.0 to 4.11.0
* Bump mockito-core from 5.1.1 to 5.2.0
* Bump mockito-core from 5.2.0 to 5.3.0
* Bump mockito-core from 5.3.0 to 5.3.1
* Bump slf4j-simple from 2.0.2 to 2.0.3
* Bump slf4j-simple from 2.0.3 to 2.0.5
* Bump slf4j-simple from 2.0.5 to 2.0.6
* Bump slf4j-simple from 2.0.6 to 2.0.7
* Bump Spring Boot from 2.7.5 to 3.0.4 (fixes #669)
* Bump springboot.version from 2.7.4 to 2.7.5
* Bump springboot.version from 3.0.4 to 3.0.5
* Bump springboot.version from 3.0.5 to 3.0.6

This Changelog was manually written by the maintainer, based on: `git log --no-merges --pretty=format:"%s" mariaDB4j-2.6.0..mariaDB4j-3.0.1 | sort | grep -v "\[maven-release-plugin\]" | sed 's/.*/\* &/' > CHANGELOG-git.md` (TODO: Write a thing which "collapses" Dependency Upgrades).

**Full Changelog**: https://github.com/MariaDB4j/MariaDB4j/compare/mariaDB4j-2.6.0...mariaDB4j-3.0.1

## v2.6.0 - 2022-10-01 (Issue #[621](https://github.com/MariaDB4j/MariaDB4j/issues/621))

https://github.com/MariaDB4j/MariaDB4j/releases/tag/mariaDB4j-2.6.0:

* Remove wrong space from DYLD_FALLBACK_LIBRARY_PATH by @vorburger in https://github.com/vorburger/MariaDB4j/pull/561
* Support using locally installed MariaDB (#560) by @vorburger in https://github.com/vorburger/MariaDB4j/pull/565
* Add documentation for using native MariaDB binaries by @mrdziuban in https://github.com/vorburger/MariaDB4j/pull/622
* Add configurable tmpdir by @simonzkl in https://github.com/vorburger/MariaDB4j/pull/604
* Support default character set configuration. by @agostop in https://github.com/vorburger/MariaDB4j/pull/533
* Remove `setDefaultCharacterSet()` from DBConfiguration (but not DBConfigurationBuilder) by @vorburger in https://github.com/vorburger/MariaDB4j/pull/564
* Remove explicit Log4j dependency (no longer needed now) by @vorburger in https://github.com/vorburger/MariaDB4j/pull/553
* Set source API default charset set to UTF-8 by @dassio in https://github.com/vorburger/MariaDB4j/pull/573
* Switch Travis CI from Java 8 to 11 and Ubuntu Xenial 16.04 to Focal 20.04 by @vorburger in https://github.com/vorburger/MariaDB4j/pull/566
* Update pom.xml by @dev-fringe in https://github.com/vorburger/MariaDB4j/pull/515
* Mechanical reformat of core classes (with Eclipse) by @vorburger in https://github.com/vorburger/MariaDB4j/pull/562
* Encore mechanical reformat by @vorburger in https://github.com/vorburger/MariaDB4j/pull/563

### Dependency Upgrades

* Bump springboot version to 2.7.4 by @mrdziuban in https://github.com/vorburger/MariaDB4j/pull/623
* Bump assertj-core from 3.21.0 to 3.22.0 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/529
* Bump assertj-core from 3.22.0 to 3.23.1 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/587
* Bump checkstyle from 10.1 to 10.3 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/585
* Bump checkstyle from 10.3 to 10.3.1 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/595
* Bump checkstyle from 10.3.1 to 10.3.2 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/606
* Bump checkstyle from 10.3.2 to 10.3.3 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/613
* Bump checkstyle from 9.2 to 9.2.1 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/523
* Bump checkstyle from 9.2.1 to 9.3 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/541
* Bump checkstyle from 9.3 to 10.1 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/569
* Bump log4j-api from 2.16.0 to 2.17.0 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/516
* Bump log4j-api from 2.17.0 to 2.17.1 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/525
* Bump log4j-api from 2.17.1 to 2.17.2 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/552
* Bump log4j-core from 2.16.0 to 2.17.0 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/517
* Bump log4j-core from 2.17.0 to 2.17.1 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/524
* Bump log4j-to-slf4j from 2.16.0 to 2.17.0 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/518
* Bump log4j-to-slf4j from 2.17.0 to 2.17.1 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/526
* Bump log4j-to-slf4j from 2.17.1 to 2.17.2 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/550
* Bump maven-checkstyle-plugin from 3.1.2 to 3.2.0 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/612
* Bump maven-common-artifact-filters from 3.2.0 to 3.3.0 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/591
* Bump maven-common-artifact-filters from 3.3.0 to 3.3.1 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/600
* Bump maven-common-artifact-filters from 3.3.1 to 3.3.2 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/615
* Bump maven-compat from 3.8.4 to 3.8.5 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/557
* Bump maven-compat from 3.8.5 to 3.8.6 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/589
* Bump maven-compiler-plugin from 3.10.0 to 3.10.1 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/555
* Bump maven-compiler-plugin from 3.8.1 to 3.9.0 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/531
* Bump maven-compiler-plugin from 3.9.0 to 3.10.0 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/546
* Bump maven-invoker-plugin from 3.2.2 to 3.3.0 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/584
* Bump maven-jar-plugin from 3.2.0 to 3.2.1 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/528
* Bump maven-jar-plugin from 3.2.1 to 3.2.2 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/532
* Bump maven-jar-plugin from 3.2.2 to 3.3.0 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/616
* Bump maven-javadoc-plugin from 3.3.2 to 3.4.0 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/576
* Bump maven-javadoc-plugin from 3.4.0 to 3.4.1 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/609
* Bump maven-plugin-annotations from 3.6.2 to 3.6.4 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/537
* Bump maven-plugin-plugin from 3.6.2 to 3.6.4 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/536
* Bump maven-project-info-reports-plugin from 3.1.2 to 3.2.1 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/542
* Bump maven-project-info-reports-plugin from 3.2.1 to 3.2.2 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/547
* Bump maven-project-info-reports-plugin from 3.2.2 to 3.3.0 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/579
* Bump maven-project-info-reports-plugin from 3.3.0 to 3.4.0 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/601
* Bump maven-project-info-reports-plugin from 3.4.0 to 3.4.1 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/607
* Bump maven-release-plugin from 3.0.0-M4 to 3.0.0-M5 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/527
* Bump maven-release-plugin from 3.0.0-M5 to 3.0.0-M6 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/590
* Bump maven-shade-plugin from 3.2.4 to 3.3.0 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/568
* Bump maven-shade-plugin from 3.3.0 to 3.4.0 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/618
* Bump maven.version from 3.8.4 to 3.8.5 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/556
* Bump maven.version from 3.8.5 to 3.8.6 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/588
* Bump mockito-core from 4.1.0 to 4.2.0 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/519
* Bump mockito-core from 4.2.0 to 4.3.1 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/540
* Bump mockito-core from 4.3.1 to 4.4.0 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/554
* Bump mockito-core from 4.4.0 to 4.8.0 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/614
* Bump mysql-connector-java from 8.0.16 to 8.0.28 in /mariaDB4j-maven-plugin/src/it/mariadb4j-maven-plugin-test-basic by @dependabot in https://github.com/vorburger/MariaDB4j/pull/592
* Bump mysql-connector-java from 8.0.27 to 8.0.28 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/535
* Bump mysql-connector-java from 8.0.28 to 8.0.29 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/580
* Bump mysql-connector-java from 8.0.29 to 8.0.30 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/603
* Bump slf4j-simple from 1.7.32 to 1.7.35 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/539
* Bump slf4j-simple from 1.7.35 to 1.7.36 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/545
* Bump slf4j-simple from 1.7.36 to 2.0.0 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/611
* Bump slf4j-simple from 2.0.0 to 2.0.2 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/620
* Bump springboot.version from 2.6.1 to 2.6.2 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/521
* Bump springboot.version from 2.6.2 to 2.6.3 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/534
* Bump springboot.version from 2.6.3 to 2.6.4 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/548
* Bump springboot.version from 2.6.4 to 2.6.5 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/559
* Bump springboot.version from 2.6.5 to 2.6.6 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/570
* Bump version from 2.5.4 to 2.6.0 by @vorburger in https://github.com/vorburger/MariaDB4j/pull/567

### New Contributors

* @dev-fringe made their first contribution in https://github.com/vorburger/MariaDB4j/pull/515
* @agostop made their first contribution in https://github.com/vorburger/MariaDB4j/pull/533
* @dassio made their first contribution in https://github.com/vorburger/MariaDB4j/pull/573
* @simonzkl made their first contribution in https://github.com/vorburger/MariaDB4j/pull/604
* @mrdziuban made their first contribution in https://github.com/vorburger/MariaDB4j/pull/622

**Full Changelog**: https://github.com/vorburger/MariaDB4j/compare/mariaDB4j-2.5.3...mariaDB4j-2.6.0

## v2.5.0 - 2.5.3

https://github.com/MariaDB4j/MariaDB4j/releases/tag/mariaDB4j-2.5.3:

### New Features

* Ability to pass an `InputStream` for initial sourcing by @asbachb in https://github.com/vorburger/MariaDB4j/pull/274 for #273
* Rename installPreparation method to createMysqlInstallProcess and expose as protected by @Anthoknee in https://github.com/vorburger/MariaDB4j/pull/300
* DB.run - Continue on error (Issue #259) by @glittle1972 in https://github.com/vorburger/MariaDB4j/pull/260
* Add option to specify os user to the mysqld process by @m80592 in https://github.com/vorburger/MariaDB4j/pull/403
* Dynamic package with direct download of mariadb by @srbala in https://github.com/vorburger/MariaDB4j/pull/235 for #230
* Add option to run database scripts less verbose by @JD-CSTx in https://github.com/vorburger/MariaDB4j/pull/335

### Improvements

* Reduce file copying during classpath unpacking. by @tjni in https://github.com/vorburger/MariaDB4j/pull/285
* Update README to include a section about the JUnit rule by @yiftizur in https://github.com/vorburger/MariaDB4j/pull/239
* Disable ~/.m2 caching in .travis.yml (re. #262) by @vorburger in https://github.com/vorburger/MariaDB4j/pull/263
* Upgrade to GitHub-native Dependabot by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/433
* Remove spring version by @mdindoffer in https://github.com/vorburger/MariaDB4j/pull/248 for issue #247
* Fix build warnings - import order, resolves issue#236 by @srbala in https://github.com/vorburger/MariaDB4j/pull/240
* Clean up mariaDB4j-maven-plugin by @vorburger in https://github.com/vorburger/MariaDB4j/pull/218
* Remove un-used AssertJ by @vorburger in https://github.com/vorburger/MariaDB4j/pull/226
* Use mvn package instead of install in .travis.yml by @vorburger in https://github.com/vorburger/MariaDB4j/pull/290
* Enforce Checkstyle (fixes #264) by @vorburger in https://github.com/vorburger/MariaDB4j/pull/500
* Expose database URL as Maven Project property in `mariaDB4j-maven-plugin` by @robin-xyzt-ai in https://github.com/vorburger/MariaDB4j/pull/476
* Enable Checkstyle for src/test by @vorburger in https://github.com/vorburger/MariaDB4j/pull/503

### Bug Fixes

* Fix windows datadir with spaces + maven-plugin tests by @vorburger in https://github.com/vorburger/MariaDB4j/pull/234
* [MariaDB4jService] Do not recreate the DB if already running by @Tomlincoln in https://github.com/vorburger/MariaDB4j/pull/382
* Add name to USERS.MD by @ROMVoid95 in https://github.com/vorburger/MariaDB4j/pull/220
* Add jakarta.annotation-api to fix broken build on Java 11 (fixes #456) by @vorburger in https://github.com/vorburger/MariaDB4j/pull/457
* Add automatic module name to core by @jensli in https://github.com/vorburger/MariaDB4j/pull/387 for #372
* Clean up dependabot.yml by @vorburger in https://github.com/vorburger/MariaDB4j/pull/486
* Fix build warnings - javadoc, issue #237 by @srbala in https://github.com/vorburger/MariaDB4j/pull/242
* Fixed typo: `caase` -> `case` by @asbachb in https://github.com/vorburger/MariaDB4j/pull/275
* Added Visual Studio Code project files to .gitignore. by @glittle1972 in https://github.com/vorburger/MariaDB4j/pull/272
* Fixes #257 - maven-checkstyle-plugin:3.1.0 by @glittle1972 in https://github.com/vorburger/MariaDB4j/pull/271
* Fix indentation in pom.xml by @vorburger in https://github.com/vorburger/MariaDB4j/pull/291
* Fixes #265 - clean more build warnings inc SQL SuppressWarnings by @glittle1972 in https://github.com/vorburger/MariaDB4j/pull/270
* Fix dataDir is checked and not the baseDir by @shai125 in https://github.com/vorburger/MariaDB4j/pull/280
* Maven Plugins: Fixing typo error by @sherrif10 in https://github.com/vorburger/MariaDB4j/pull/361
* Add null safety check to mysqldProcess.  Fixes #103. by @vorburger in https://github.com/vorburger/MariaDB4j/pull/297
* Clean comment. by @AdelDima in https://github.com/vorburger/MariaDB4j/pull/327
* Run `mvn verify` instead of only `package` on Travis by @vorburger in https://github.com/vorburger/MariaDB4j/pull/490
* Remove fixed version of commons-io from Maven plugin dependencies by @vorburger in https://github.com/vorburger/MariaDB4j/pull/491
* Fix Maven plugin dependencies scope and remove commons-io dependency from Maven plugin (but it's still transitively inherited) by @vorburger in https://github.com/vorburger/MariaDB4j/pull/492
* Use magic <version>@project.version@ in Maven Plugin IT by @vorburger in https://github.com/vorburger/MariaDB4j/pull/494
* Fixes #488 by @robin-xyzt-ai in https://github.com/vorburger/MariaDB4j/pull/499
* When dumping the database, close the outputstream once the dump is done by @robin-xyzt-ai in https://github.com/vorburger/MariaDB4j/pull/502
* Spaces in windows paths are no longer replaced by %20 by @Blanco27 in https://github.com/vorburger/MariaDB4j/pull/504
* Replace DB's toWindowsPath() with inline File.getCanonicalFile() by @vorburger in https://github.com/vorburger/MariaDB4j/pull/505
* Fix failing release due to Checkstyle in generated-sources (fixes #511) by @vorburger in https://github.com/vorburger/MariaDB4j/pull/512

### Dependency Upgrades

* Bump MariaDB from 10.3.13 to 10.3.16 by @vorburger in https://github.com/vorburger/MariaDB4j/pull/294
* Bump ch.vorburger.exec to 3.1.3 (fixes #501) by @vorburger in https://github.com/vorburger/MariaDB4j/pull/506
* Bump log4j dependencyManagement to 2.15.0 instead of 2.14.1 (#509) by @vorburger in https://github.com/vorburger/MariaDB4j/pull/510
* Bump maven.version from 3.5.4 to 3.6.1 by @vorburger in https://github.com/vorburger/MariaDB4j/pull/295
* Bump maven-release-plugin from 2.5.3 to 3.0.0-M4 by @vorburger in https://github.com/vorburger/MariaDB4j/pull/496
* Bump mockito-core from 3.5.15 to 4.1.0 by @vorburger in https://github.com/vorburger/MariaDB4j/pull/487
* Bump maven-common-artifact-filters & maven-project-info-reports-plugin by @vorburger in https://github.com/vorburger/MariaDB4j/pull/489
* Maven plugin upgrade 3.5.4 by @duttonw in https://github.com/vorburger/MariaDB4j/pull/175
* Bump assertj-core from 3.11.1 to 3.12.0 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/216
* Bump assertj-core from 3.11.1 to 3.13.2 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/302
* Bump assertj-core from 3.13.2 to 3.14.0 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/312
* Bump assertj-core from 3.14.0 to 3.15.0 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/329
* Bump assertj-core from 3.15.0 to 3.16.1 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/349
* Bump assertj-core from 3.16.1 to 3.17.0 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/370
* Bump assertj-core from 3.17.0 to 3.17.2 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/376
* Bump assertj-core from 3.17.2 to 3.21.0 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/469
* Bump commons-io from 2.10.0 to 2.11.0 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/459
* Bump commons-io from 2.6 to 2.7 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/352
* Bump commons-io from 2.7 to 2.8.0 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/378
* Bump commons-io from 2.8.0 to 2.10.0 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/447
* Bump junit from 4.12 to 4.13 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/323
* Bump junit from 4.12 to 4.13.1 in /mariaDB4j-maven-plugin/src/it/mariadb4j-maven-plugin-test-basic by @dependabot in https://github.com/vorburger/MariaDB4j/pull/386
* Bump junit from 4.13 to 4.13.1 in /mariaDB4j-maven-plugin by @dependabot in https://github.com/vorburger/MariaDB4j/pull/385
* Bump junit from 4.13.1 to 4.13.2 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/431
* Bump license-maven-plugin from 1.17 to 1.19 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/224
* Bump license-maven-plugin from 1.19 to 1.20 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/252
* Bump license-maven-plugin from 1.20 to 2.0.0 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/268
* Bump maven-checkstyle-plugin from 3.0.0 to 3.1.0 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/257
* Bump maven-common-artifact-filters from 1.4 to 3.1.0 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/196
* Bump maven-compat from 3.1.0 to 3.6.1 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/250
* Bump maven-compat from 3.6.1 to 3.6.2 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/301
* Bump maven-compat from 3.6.2 to 3.6.3 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/316
* Bump maven-compat from 3.6.3 to 3.8.4 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/478
* Bump maven-compiler-plugin from 3.8.0 to 3.8.1 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/254
* Bump maven-invoker-plugin from 3.2.0 to 3.2.1 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/306
* Bump maven-invoker-plugin from 3.2.1 to 3.2.2 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/422
* Bump maven-jar-plugin from 3.1.1 to 3.1.2 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/255
* Bump maven-jar-plugin from 3.1.2 to 3.2.0 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/314
* Bump maven-plugin-annotations from 3.2 to 3.6.0 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/185
* Bump maven-plugin-annotations from 3.6.0 to 3.6.2 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/482
* Bump maven-plugin-plugin from 3.5.2 to 3.6.0 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/186
* Bump maven-plugin-plugin from 3.6.0 to 3.6.2 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/483
* Bump maven-project-info-reports-plugin from 3.0.0 to 3.1.0 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/353
* Bump maven-project-info-reports-plugin from 3.1.0 to 3.1.1 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/377
* Bump maven-shade-plugin from 3.2.1 to 3.2.2 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/331
* Bump maven-shade-plugin from 3.2.2 to 3.2.3 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/345
* Bump maven-shade-plugin from 3.2.3 to 3.2.4 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/354
* Bump maven.version from 3.6.1 to 3.6.2 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/303
* Bump maven.version from 3.6.2 to 3.6.3 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/315
* Bump maven.version from 3.6.3 to 3.8.4 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/477
* Bump mockito-core from 2.24.0 to 2.24.5 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/222
* Bump mockito-core from 2.24.5 to 2.25.0 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/232
* Bump mockito-core from 2.25.0 to 2.28.2 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/258
* Bump mockito-core from 2.28.2 to 3.0.0 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/276
* Bump mockito-core from 3.0.0 to 3.1.0 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/308
* Bump mockito-core from 3.1.0 to 3.2.0 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/317
* Bump mockito-core from 3.2.0 to 3.2.4 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/319
* Bump mockito-core from 3.2.4 to 3.3.0 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/337
* Bump mockito-core from 3.3.0 to 3.3.3 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/341
* Bump mockito-core from 3.3.3 to 3.4.4 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/360
* Bump mockito-core from 3.4.4 to 3.4.6 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/365
* Bump mockito-core from 3.4.6 to 3.5.0 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/368
* Bump mockito-core from 3.5.0 to 3.5.5 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/371
* Bump mockito-core from 3.5.10 to 3.5.11 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/379
* Bump mockito-core from 3.5.11 to 3.5.13 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/381
* Bump mockito-core from 3.5.13 to 3.5.15 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/399
* Bump mockito-core from 3.5.5 to 3.5.7 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/373
* Bump mockito-core from 3.5.7 to 3.5.10 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/375
* Bump mysql-connector-java from 8.0.12 to 8.0.16 in /mariaDB4j-maven-plugin/src/it/mariadb4j-maven-plugin-test-basic by @dependabot in https://github.com/vorburger/MariaDB4j/pull/356
* Bump mysql-connector-java from 8.0.13 to 8.0.17 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/305
* Bump mysql-connector-java from 8.0.15 to 8.0.16 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/253
* Bump mysql-connector-java from 8.0.16 to 8.0.17 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/277
* Bump mysql-connector-java from 8.0.17 to 8.0.18 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/309
* Bump mysql-connector-java from 8.0.18 to 8.0.19 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/326
* Bump mysql-connector-java from 8.0.19 to 8.0.20 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/347
* Bump mysql-connector-java from 8.0.20 to 8.0.21 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/359
* Bump mysql-connector-java from 8.0.21 to 8.0.22 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/388
* Bump mysql-connector-java from 8.0.22 to 8.0.27 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/474
* Bump slf4j-simple from 1.7.25 to 1.7.26 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/221
* Bump slf4j-simple from 1.7.26 to 1.7.28 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/282
* Bump slf4j-simple from 1.7.28 to 1.7.29 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/311
* Bump slf4j-simple from 1.7.29 to 1.7.30 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/320
* Bump slf4j-simple from 1.7.30 to 1.7.31 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/445
* Bump slf4j-simple from 1.7.31 to 1.7.32 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/462
* Bump springboot.version from 2.1.2.RELEASE to 2.1.3.RELEASE by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/215
* Bump springboot.version from 2.1.3.RELEASE to 2.1.5.RELEASE by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/256
* Bump springboot.version from 2.1.5.RELEASE to 2.1.6.RELEASE by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/266
* Bump springboot.version from 2.1.6.RELEASE to 2.1.7.RELEASE by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/283
* Bump springboot.version from 2.1.7.RELEASE to 2.1.9.RELEASE by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/307
* Bump springboot.version from 2.1.9.RELEASE to 2.2.1.RELEASE by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/313
* Bump springboot.version from 2.2.1.RELEASE to 2.2.2.RELEASE by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/318
* Bump springboot.version from 2.2.2.RELEASE to 2.2.4.RELEASE by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/328
* Bump springboot.version from 2.2.4.RELEASE to 2.2.5.RELEASE by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/338
* Bump springboot.version from 2.2.5.RELEASE to 2.3.0.RELEASE by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/351
* Bump springboot.version from 2.3.0.RELEASE to 2.3.1.RELEASE by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/355
* Bump springboot.version from 2.3.1.RELEASE to 2.3.2.RELEASE by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/362
* Bump springboot.version from 2.3.2.RELEASE to 2.3.3.RELEASE by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/369
* Bump springboot.version from 2.3.3.RELEASE to 2.3.4.RELEASE by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/380
* Bump springboot.version from 2.3.4.RELEASE to 2.4.4 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/426
* Bump springboot.version from 2.4.4 to 2.5.2 by @dependabot-preview in https://github.com/vorburger/MariaDB4j/pull/449
* Bump springboot.version from 2.5.2 to 2.6.0 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/479
* Bump springboot.version from 2.6.0 to 2.6.1 by @dependabot in https://github.com/vorburger/MariaDB4j/pull/507

### New Contributors

* @ROMVoid95 made their first contribution in https://github.com/vorburger/MariaDB4j/pull/220
* @mdindoffer made their first contribution in https://github.com/vorburger/MariaDB4j/pull/248
* @srbala made their first contribution in https://github.com/vorburger/MariaDB4j/pull/240
* @glittle1972 made their first contribution in https://github.com/vorburger/MariaDB4j/pull/260
* @asbachb made their first contribution in https://github.com/vorburger/MariaDB4j/pull/275
* @tjni made their first contribution in https://github.com/vorburger/MariaDB4j/pull/285
* @shai125 made their first contribution in https://github.com/vorburger/MariaDB4j/pull/280
* @Anthoknee made their first contribution in https://github.com/vorburger/MariaDB4j/pull/300
* @AdelDima made their first contribution in https://github.com/vorburger/MariaDB4j/pull/327
* @JD-CSTx made their first contribution in https://github.com/vorburger/MariaDB4j/pull/335
* @sherrif10 made their first contribution in https://github.com/vorburger/MariaDB4j/pull/361
* @Tomlincoln made their first contribution in https://github.com/vorburger/MariaDB4j/pull/382
* @m80592 made their first contribution in https://github.com/vorburger/MariaDB4j/pull/403
* @jensli made their first contribution in https://github.com/vorburger/MariaDB4j/pull/387
* @robin-xyzt-ai made their first contribution in https://github.com/vorburger/MariaDB4j/pull/499
* @Blanco27 made their first contribution in https://github.com/vorburger/MariaDB4j/pull/504

**Full Changelog**: https://github.com/vorburger/MariaDB4j/compare/mariaDB4j-2.4.0...mariaDB4j-2.5.3

## v2.4.0 - 2019-02-11

see also [the 2.4.0 release page on GitHub](https://github.com/vorburger/MariaDB4j/releases/tag/mariaDB4j-2.4.0)

### Added

* @duttonw contributed @mike10004's mariaDB4j-maven-plugin (@kevinconaway/@vorburger fixed ITs)
* @kbyyd24 added mariaDB4j-springboot module for auto-configuration with spring boot
* @neeleshs with @duttonw added new API for a callback if the DB process crashes
* @yiftizur contributed cool new MariaDB4jRule JUnit rule for easy tests

### Changed

* @thesquaregroot only set --max_allowed_packet if it is not in the configured arguments
* @EGJ Updated Builder Methods Returning Void To Return The Builder
* @dependabot bumped 3rd party libraries, thank you https://dependabot.com
* @vorburger made a number of release related changes and clean ups

### Fixed

* No bug fixes, because... it has no more bugs?! ;-)

## v2.3.0 - 2018-05-15

* @cortiz added dumpXML and dumpSQL
* @marcelvanderperk added setSecurityDisabled()
* @bjornblomqvist empty password string is treated the same as null
* @paulroemer added setDeletingTemporaryBaseAndDataDirsOnShutdown()
* @lde-avaleo and @jai-deep contributed MariaDB 10.2.11 (and @cortiz 10.1.23)
* @vorburger moved code to [ch.vorburger.exec](https://github.com/vorburger/ch.vorburger.exec) and added dependency
* @dependabot bumped various 3rd party libraries, courtesy of https://dependabot.com
* @vorburger now compiles MariaDB4j with Java 8 instead of 6
* @vorburger fixed bug #88 running MariaDb4j on Glassfish

## v2.2.3 - 2017-02-10

* @lpearson05 contributed upgrade of older commons-collections with CVE-2011-2092 vulnerability to commons-collections 4.1 (https://issues.apache.org/jira/browse/COLLECTIONS-580)
* @clfsoft contributed [issue #49](https://github.com/vorburger/MariaDB4j/issues/49) upgrade of MariaDB Win 32 version from 10.0.13 to 10.1.20
* @vorburger bumped mariadb-java-client from version 1.4.6 to 1.5.5, and Spring Boot from 1.4.0 to 1.5.1

## v2.2.2 - 2016-08-20

* @hanklank contributed [issue #37](https://github.com/vorburger/MariaDB4j/issues/37) upgrade of MariaDB Mac OS X version from 5.5.34 to 10.1.9 (tested by @brendonanderson)
* Fixed [issue #27](https://github.com/vorburger/MariaDB4j/issues/27) Do not log info messages as errors
* Upgrade version of Spring Boot from 1.3.6 to 1.4.0

## v2.2.1 - 2016-07-24

* Maven central release [issue #21](https://github.com/vorburger/MariaDB4j/issues/21): Finally, as requested for too long by too many... ;-) FYI @nicmon @metawave @krm1312 @alexpanov @jinahya @kedgecomb @lc-nyovchev @tbenedetti-lendico @fleger @chrisbloe @fleger @lc-nyovchev @ollemuhr @laurent-dol
* @anverus fixed [issue #39](https://github.com/vorburger/MariaDB4j/issues/39): If baseDir is set libedir has to be repointed too to make use of bundled native libs
* @ghiron for @honestica contributed upgrade of mariadb linux version from 10.1.8 to 10.1.13
* README updated with new section re. DB upgrade contributions
* Upgrade version of Spring Framework, Spring Boot, and some Maven plugins
* API extension: class DB has a handy getConfiguration() method to get its original DBConfiguration back
* JARs built include README, CHANGES, LEGAL, LICENSE; and (new!) CONTRIBUTORS, CONTRIBUTING, NOTICE
* NOTICE file https://github.com/vorburger/MariaDB4j/issues/14
* Fedora 24 related build test failure and README doc update

## v2.2.0 - 2016-05-05

* MAJOR Distribution and project org. split up formerly monolithic MariaDB4j into separate core, exec and binaries artifacts; separately versioned
* @CedricGatay: NEW addArg() method in DBConfigurationBuilder to pass additional flags when spawning a new MariaDB/Mysql process (e.g. like lower_case_table_names, in a mixed OS environment)
* @jahewson: Security related fixed Exception if there are spaces in the data directory path (https://github.com/vorburger/MariaDB4j/issues/30)
* @timorohwedder: API extended for setting OS dependent library path to optional binary libraries
* @timorohwedder: Bumped (upgraded) bundled MariaDB Linux version
* Kevin McLaughlin: Synchronize DB install to try to fix some intermittent test failures when running parallel tests in maven that depend on MariaDB4j
* Bumped (upgraded) versions of some 3rd-party Java libraries; thanks https://www.versioneye.com/java/ch.vorburger.mariadb4j:mariadb4j/
* Src: Tabs to Spaces, and enforced by Checkstyle running in Build

## v2.1.3 - 2014-12-27

* FIXED Windows package, now tested; it was completely broken in 2.1.1 (but worked in the original 2.1.0)

## v2.1.1 - 2014-12-03

* FIXED bad concurrency bug https://github.com/vorburger/MariaDB4j/issues/10
* Upgraded commons-exec v1.2 => v1.3 & Spring Boot v1.1.6 => v1.1.9
* Less annoyingly verbose logging now
* minor code clean-ups etc.

## v2.1.0 - 2014-09-21

* Original first public release
  (project existed before without Maven Bintray release; people just built from source)
