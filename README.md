# cqf-tooling

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.opencds.cqf/tooling/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.opencds.cqf/tooling) [![Build Status](https://www.travis-ci.com/cqframework/cqf-tooling.svg?branch=master)](https://www.travis-ci.com/cqframework/cqf-tooling)

Tooling for CQL and IG Authors

This tooling provides various useful tools for building CQFramework related content and implementation guides, including QUICK page generation, ModelInfo generation from StructureDefinitions, and ValueSet creation.

## Usage

Building this project requires Java 9+ and Maven 3.5+. The resulting jar is compatible with Java 8+.

Build the project with:

```bash
mvn package
```

Run a specific operation using (example running VSAC Spreadsheet conversion):

```bash
mvn exec:java \
-Dexec.mainClass="org.opencds.cqf.tooling.Main" \
-Dexec.args="-VsacXlsxToValueSetBatch \
-ptsd=src/test/resources/org/opencds/cqf/tooling/terminology \
-op=target/test/resources/org/opencds/cqf/tooling/terminology/output \
-setname=true \
-vssrc=cms"
```

Documentation of the various operations is provided in the [Main](src/main/java/org/opencds/cqf/tooling/Main.java) class.

Executable jars are produced by the CI system and uploaded to the Maven repository for org.opencds.cqf.

## Commit Policy

All new development takes place on `<feature>` branches off `master`. Once feature development on the branch is complete, the feature branch is submitted to `master` as a PR. The PR is reviewed by maintainers and regression testing by the CI build occurs.

Changes to the `master` branch must be done through an approved PR. Delete branches after merging to keep the repository clean.

Merges to `master` trigger a deployment to the Maven Snapshots repositories. Once ready for a release, the `master` branch is updated with the correct version number and is tagged. Tags trigger a full release to Maven Central and a corresponding release to Github. Releases SHALL NOT have a SNAPSHOT version, nor any SNAPSHOT dependencies.

## License

Copyright 2019+ Dynamic Content Group, LLC (dba Alphora)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
