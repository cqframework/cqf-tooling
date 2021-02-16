# cqf-tooling

Tooling for CQL and IG Authors

This tooling provides various useful tools for building CQFramework related content and implementation guides, including QUICK page generation, ModelInfo generation from StructureDefinitions, and ValueSet creation.

Documentation is provided in the [Main](src/main/java/org/opencds/cqf/tooling/Main.java) class.

Builds of the master branch are uploaded to the Maven repository for org.opencds.cqf

## Commit Policy

All new development takes place on ```<feature>``` branches off ```master```. Once feature development on the branch is complete, the feature branch is submitted to ```master``` as a PR. The PR is reviewed by maintainers and regression testing by the CI build occurs.

Changes to the ```master``` branch must be done through an approved PR. Delete branches after merging to keep the repository clean.

Merges to ```master``` trigger a deployment to the Maven Snapshots repositories. Once ready for a release, the ```master``` branch is updated with the correct version number and is tagged. Tags trigger a full release to Maven Central and a corresponding release to Github. Releases SHALL NOT have a SNAPSHOT version, nor any SNAPSHOT dependencies.
