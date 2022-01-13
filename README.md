# RIPE Whois Database

License
-------
This software is distributed under the BSD License.
See: https://raw.github.com/RIPE-NCC/whois/master/LICENSE.md

[![License](https://img.shields.io/badge/License-BSD%203--Clause-blue.svg)](https://opensource.org/licenses/BSD-3-Clause)

Running Integration Tests for Development
-----------------------------------------

It is preferable to write integration tests during development, and commit them along with any code changes. All dependencies apart from the database are mocked. 

Running Whois Locally For Development
-------------------------------------

You can run a standalone Whois server locally. Dependencies must be configured in a local properties file.

### Running within an IDE

See [Running whois from within Intellij](https://github.com/RIPE-NCC/whois/wiki/Development#running-whois-from-within-intellij) for instructions.

### Running outside an IDE
- See [Installation instructions](https://github.com/RIPE-NCC/whois/wiki/Installation-instructions).

Git
---

### Configure pre-commit hooks

Pre-commit hook can be found in `tools/precommitcheck`. This can be optionally configured by running: `ln -fsv tools/precommitcheck .git/hooks/pre-commit` 

Tests
-----

### Run a single integration test

mvn clean install -DfailIfNoTests=false -Pintegration -Dit.test=NrtmClientInvalidHostTestIntegration

### Run all integration tests in a single module

mvn clean install -Pintegration -pl whois-query

