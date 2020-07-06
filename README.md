# cqf-tooling

Tooling for CQL and IG Authors

This tooling provides various useful tools for building CQFramework related content and implementation guides, including QUICK page generation, ModelInfo generation from StructureDefinitions, and ValueSet creation.

Documentation is provided in the [Main](https://github.com/cqframework/cqf-tooling/blob/master/src/main/java/org/opencds/cqf/Main.java) class.

Builds of the main and develop branches are uploaded to the Maven repository for org.opencds.cqf

## Commit Policy

All new development takes place on &lt;feature&gt; branches off develop. Changes to new features branches may be committed directly if desired, but PRs are preferred. Once feature development on the branch is complete, the feature branch is submitted to develop as a PR. The PR is reviewed by maintainers and regression testing by the CI build occurs. Once the PR is merged to develop, the CI build publishes a SNAPSHOT.

Changes to master and maintenance branches must be done through an approved PR. Feature branches are merged to the develop branch for regression testing. Only the develop branch may be merged to master. Delete branches after merging to keep the repository clean.

Commits to develop and master trigger a deployment to Maven Central. Master releases SHALL NOT have any SNAPSHOT dependencies.
