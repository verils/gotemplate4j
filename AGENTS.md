# AI Agent Guidelines for gotemplate4j

## Project Context
- This is a Go template engine implementation for Java
- Before making changes, read ./README.md for project overview and basic usage
- Check ./CHANGELOG for recent updates and development direction

## Development Environment
- Java Version: 1.8 (Project targets Java 8 compatibility)
- Build Tool: Maven (ALWAYS use `./mvnw` wrapper, NOT `mvn` directly)
- No additional dependencies except Vanilla Java
- IMPORTANT: Use `./mvnw` to avoid JAVA_HOME configuration issues

### Java Version Management
**CRITICAL**: The project requires Java 8 for compilation and testing. If your default Java version is not 8:

1. **Check current Java version:**
   ```bash
   java -version
   ```

2. **Find available Java 8 installation:**
   - Windows: Check `C:\Program Files\Eclipse Adoptium\` or `C:\Program Files\Java\`
   - Look for directories like `jdk-8.0.xxx.x-hotspot` or `jdk1.8.0_xxx`

3. **Set JAVA_HOME temporarily for this project:**
   ```bash
   # Windows PowerShell
   $env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-8.0.482.8-hotspot"
   $env:PATH="$env:JAVA_HOME\bin;$env:PATH"
   
   # Windows CMD
   set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-8.0.482.8-hotspot
   set PATH=%JAVA_HOME%\bin;%PATH%
   
   # Linux/Mac
   export JAVA_HOME=/usr/lib/jvm/java-8-openjdk
   export PATH=$JAVA_HOME/bin:$PATH
   ```

4. **Verify Java 8 is active:**
   ```bash
   java -version
   # Should show: openjdk version "1.8.x" or "8.x.x"
   ```

5. **Alternative: Configure Maven toolchains** (recommended for multi-JDK environments)
   - Create `.mvn/toolchains.xml` to specify JDK 8 for this project
   - This allows Maven to use Java 8 even when your system default is different

## Code Style Guidelines
- Follow standard Java naming conventions (camelCase for variables/methods, PascalCase for classes)
- Use UTF-8 encoding for all files
- Keep methods concise and focused on single responsibility
- Add Javadoc comments for public APIs
- Use meaningful variable names that reflect purpose

## Testing Requirements
- All new features must include unit tests
- Run tests with: `./mvnw test`
- Test files located in: `src/test/java/io/github/verils/gotemplate/`
- Use JUnit 5 (Jupiter) for testing
- Maintain existing test patterns and structure
- **All existing tests and new tests must succeed** - no test failures are acceptable
- **Code coverage must not be lower than 90%** - template engines require exceptional test coverage due to complex parsing logic, security implications, and numerous edge cases

### Code Coverage Verification
The project uses JaCoCo for code coverage analysis with automatic enforcement:

1. **Run tests and generate coverage report:**
   ```bash
   ./mvnw clean test jacoco:report
   ```

2. **Run full build with coverage check (enforces 90% minimum):**
   ```bash
   # Skip GPG signing for local builds
   ./mvnw clean verify "-Dgpg.skip=true"
   ```

3. **View coverage reports:**
   - HTML Report: Open `target/site/jacoco/index.html` in browser
   - XML Report: `target/site/jacoco/jacoco.xml` (for CI/CD integration)
   - CSV Report: `target/site/jacoco/jacoco.csv` (for quick analysis)

4. **Coverage thresholds enforced:**
   - Instruction Coverage: ≥ 90%
   - Branch Coverage: ≥ 85%
   - Build will FAIL if thresholds are not met
   
   **Rationale**: Template engines require higher coverage than typical applications because:
   - Complex parsing logic with many edge cases must be thoroughly tested
   - Security-critical code (template injection prevention) needs comprehensive validation
   - 20+ AST node types each require extensive testing
   - Library backward compatibility demands bulletproof execution paths

5. **Improving coverage:**
   - Focus on classes with low branch coverage (check CSV report)
   - Add tests for edge cases and error handling paths
   - Use HTML report to identify uncovered lines (marked in red)

## Build & Deployment Commands
- Compile: `./mvnw compile`
- Run tests: `./mvnw test`
- Generate coverage report: `./mvnw test jacoco:report`
- Verify with coverage check: `./mvnw verify "-Dgpg.skip=true"`
- Package: `./mvnw package`
- Full build with signing: `./mvnw verify`
- Deploy to Nexus: `./mvnw deploy`
- Clean build: `./mvnw clean`

**Note**: When running locally without GPG keys, always add `"-Dgpg.skip=true"` to skip artifact signing.

## Task Management Rules
- For changes >200 lines: create detailed plan before implementation
- Break complex tasks into smaller, verifiable steps
- Verify each step before proceeding to next
- Use existing code patterns as reference

## File Structure Awareness
- Main source: `src/main/java/io/github/verils/gotemplate/`
- Test source: `src/test/java/io/github/verils/gotemplate/`
- Templates: `src/test/resources/*.tmpl`
- Core components: Lexer, Parser, Executor, AST nodes

## Quality Standards
- Maintain backward compatibility when possible
- Follow existing error handling patterns (TemplateException hierarchy)
- Preserve experimental nature warnings in documentation
- Do not modify LICENSE or core project metadata without explicit request

### Documentation Guidelines
- **README.md vs CHANGELOG separation**: 
  - README.md should contain stable, version-agnostic information (overview, installation, basic usage, documentation links)
  - CHANGELOG should contain all version-specific changes, new features, and release notes
  - NEVER add "New in vX.Y.Z" sections or version-specific feature lists to README.md
  - Keep README.md clean and focused on helping users get started quickly

### Documentation Synchronization
- **CRITICAL**: When updating README.md, ALWAYS update all translated versions (e.g., README_zh.md)
- Keep all documentation translations in sync with the English version
- Ensure code examples, version numbers, and feature descriptions match across all language versions
- If you cannot translate to a specific language, flag it for manual translation

## Handoff Protocol
When ending session:
1. Summarize changes made
2. List any remaining TODOs or incomplete tasks  
3. Note any deviations from standard patterns
4. Provide specific commands to verify changes work correctly

## Special Considerations
- This is experimental software - mark production warnings appropriately
- Complex number support is incomplete - be aware of limitations
- Built-in functions are partially implemented
- PipeNode functionality is weak - handle with care