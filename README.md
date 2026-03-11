# cqf-tooling

[![Maven Central](https://maven-badges.sml.io/sonatype-central/org.opencds.cqf/tooling/badge.svg)](https://maven-badges.sml.io/sonatype-central/org.opencds.cqf/tooling) [![Build Status](https://www.travis-ci.com/cqframework/cqf-tooling.svg?branch=master)](https://www.travis-ci.com/cqframework/cqf-tooling) [![project chat](https://img.shields.io/badge/zulip-join_chat-brightgreen.svg)](https://chat.fhir.org/#narrow/stream/179220-cql)

Tooling for CQL and IG Authors

This tooling provides various useful tools for building CQFramework related content and implementation guides, including QUICK page generation, ModelInfo generation from StructureDefinitions, and ValueSet creation.

## Usage

Building this project requires Java 11+ and Maven 3.8+. The resulting jar is compatible with Java 11+.

Build the project with:

```bash
mvn package
```

Run a specific operation using (example running VSAC Spreadsheet conversion):

```bash
mvn exec:java \
-Dexec.mainClass="org.opencds.cqf.tooling.cli.Main" \
-Dexec.args="-VsacXlsxToValueSetBatch \
-ptsd=src/test/resources/org/opencds/cqf/tooling/terminology \
-op=target/test/resources/org/opencds/cqf/tooling/terminology/output \
-setname=true \
-vssrc=cms"
```

Executable jars are produced by the CI system on Maven Central: [Download executable jar](https://oss.sonatype.org/service/local/artifact/maven/redirect?r=releases&g=org.opencds.cqf&a=tooling-cli&v=LATEST)

This can be run with `java -jar tooling-cli-3.10.0.jar -VsacXlsxToValueSetBatch`

### Documentation
Various operation documentation can be found in the individual operation `README` files.
- [Bundling](tooling/src/main/java/org/opencds/cqf/tooling/operations/bundle/README.md)
- [Date Roller](tooling/src/main/java/org/opencds/cqf/tooling/operations/dateroller/README.md)
- [Library generation and updating](tooling/src/main/java/org/opencds/cqf/tooling/operations/library/README.md)
- [MAT resource extraction](tooling/src/main/java/org/opencds/cqf/tooling/operations/mat/README.md)
- [Process Accelerator Kit](tooling/src/main/java/org/opencds/cqf/tooling/operations/acceleratorkit/README.md)
- [Profile Conformance](tooling/src/main/java/org/opencds/cqf/tooling/operations/validation/README.md)
- [Valueset generator](tooling/src/main/java/org/opencds/cqf/tooling/operations/valueset/generate/config/README.md)
- [Valueset $expand](tooling/src/main/java/org/opencds/cqf/tooling/operations/valueset/expansion/README.md)


## Commit Policy

All new development takes place on `<feature>` branches off `master`. Once feature development on the branch is complete, the feature branch is submitted to `master` as a PR. The PR is reviewed by maintainers and regression testing by the CI build occurs.

Changes to the `master` branch must be done through an approved PR. Delete branches after merging to keep the repository clean.

Merges to `master` trigger a deployment to the Maven Snapshots repositories. Once ready for a release, the `master` branch is updated with the correct version number and is tagged. Tags trigger a full release to Maven Central and a corresponding release to Github. Releases SHALL NOT have a SNAPSHOT version, nor any SNAPSHOT dependencies.

## Release Process

To release a new version of the tooling:
1. Update master to be a release version (and all the reviews, bug fixes, etc. that that requires)
   1. Regression test against IGs known to use CQF Tooling
2. Passed CI Build = ready for release
3. Use the following command to release from your local build (releases are not automatic)
   1. `mvn deploy -DskipTests=true -Dmaven.test.skip=true -T 4 -B -P release`
4. Create a Github Release (specify a tag of vX.X.X (e.g. v3.9.1) pointing at master to be created on release)
   1. Choose the "Auto-generate release notes" option
5. Update master version to vNext-SNAPSHOT (e.g. v3.10.0-SNAPSHOT)
6. Close all issues included in the release

## Getting Help

Bugs and feature requests can be filed with [Github Issues](https://github.com/cqframework/cqf-tooling/issues).

The implementers are active on the official FHIR [Zulip chat for CQL](https://chat.fhir.org/#narrow/stream/179220-cql).

Inquires for commercial support can be directed to [info@alphora.com](info@alphora.com).

## Related Projects

[Clinical Quality Language](https://github.com/cqframework/clinical_quality_language) - Tooling in support of the CQL specification, including the CQL verifier/translator used in this project.

[CQL Support for Atom](https://atom.io/packages/language-cql) - Open source CQL IDE with syntax highlighting, linting, and local CQL evaluation.

[CQF Ruler](https://github.com/DBCG/cqf-ruler) - Integrates this project into the HAPI FHIR server, exposing some functionality as services.

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
