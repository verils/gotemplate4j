# AI Agent Guidelines for gotemplate4j

## Project Context
- This is a Go template engine implementation for Java
- Before making changes, read ./README.md for project overview and basic usage
- Check ./CHANGELOG for recent updates and development direction

## Development Environment
- Java Version: >= 1.8
- Build Tool: Maven (ALWAYS use `./mvnw` wrapper, NOT `mvn` directly)
- No additional dependencies except Vanilla Java
- IMPORTANT: Use `./mvnw` to avoid JAVA_HOME configuration issues

## Code Style Guidelines
- Follow standard Java naming conventions (camelCase for variables/methods, PascalCase for classes)
- Use UTF-8 encoding for all files
- Keep methods concise and focused on single responsibility
- Add Javadoc comments for public APIs
- Use meaningful variable names that reflect purpose

## Testing Requirements
- All new features must include unit tests
- Run tests with: `mvn test`
- Test files located in: `src/test/java/io/github/verils/gotemplate/`
- Use JUnit 5 (Jupiter) for testing
- Maintain existing test patterns and structure
- **All existing tests and new tests must succeed** - no test failures are acceptable
- **Code coverage must not be lower than 80%** - ensure comprehensive test coverage for all new code

## Build & Deployment Commands
- Compile: `./mvnw compile`
- Run tests: `./mvnw test` 
- Package: `./mvnw package`
- Full build with signing: `./mvnw verify`
- Deploy to Nexus: `./mvnw deploy`
- Clean build: `./mvnw clean`

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