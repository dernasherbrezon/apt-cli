[![Build Status](https://app.travis-ci.com/dernasherbrezon/apt-cli.svg?branch=main)](https://app.travis-ci.com/github/dernasherbrezon/apt-cli)

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
