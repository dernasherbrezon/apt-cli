[![Main Workflow](https://github.com/dernasherbrezon/apt-cli/actions/workflows/build.yml/badge.svg)](https://github.com/dernasherbrezon/apt-cli/actions/workflows/build.yml) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=dernasherbrezon_apt-cli&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=dernasherbrezon_apt-cli)

# About

Cross-platform command line interface for managing APT repositories.

# Usage

Download the latest version from the [Releases](https://github.com/dernasherbrezon/apt-cli/releases).

```
java -jar apt-cli.jar --help
```

# Features

  * Save .deb files. Simply upload multiple .deb files into APT repository.
  * Cleanup repository. Delete unused files from APT repository.
  * Delete packages.
  * Support [by-hash](https://wiki.ubuntu.com/AptByHash) APT repositories.

# Development

The application is based on [apt-man](https://github.com/dernasherbrezon/apt-man) library.
