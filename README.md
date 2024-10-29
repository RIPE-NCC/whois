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

More information [here](https://docs.db.ripe.net/)

### Running within an IDE

See [Running whois from within Intellij](https://docs.db.ripe.net/Installation-and-Development/Building-whois/#running-whois-from-within-intellij) for instructions.

### Running outside an IDE
- See [Installation instructions](https://docs.db.ripe.net/Installation-and-Development/Installation-instructions/).

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


## Configure Ajc (AspectJ) Compiler

Whois uses AspectJ to perform compile-time aspect weaving of the code. This is needed for some functionality, e.g. the @RetryFor annotation.

Compile-time weaving works during a command-line Maven build, as the pom.xml uses aspectj-maven-plugin.

Any code that depends on AspectJ will fail if modified in IntelliJ without using the Ajc (AspectJ) compiler.

You can configure Ajc in the Preferences as follows:

* First install the AspectJ plugin provided by JetBrains from the IDE plugin repository
* Go to Build, Execution, Deployment -> Compiler -> Java Compiler
  * Choose "Use Compiler: Ajc"
  * Configure Path to aspectjtools.jar, e.g. ~/.m2/repository/org/aspectj/aspectjtools/1.9.8/aspectjtools-1.9.7.jar
  * Press "Test" to confirm it's working.
* Go to Build, Execution, Deployment -> Build Tools -> Maven -> Importing
  * Uncheck "Detect compiler automatically" (otherwise IntelliJ will revert from Ajc to JavaC)
