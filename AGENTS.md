# AI Agent Guidelines for gotemplate4j

## Project Context
- This is a Go template engine implementation for Java
- Before making changes, read ./README.md for project overview and basic usage
- Check ./CHANGELOG for recent updates and development direction

## Development Environment
- Java Version: >= 1.8
- Build Tool: Maven (use `mvn` commands, not Gradle)
- No additional dependencies except Vanilla Java

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

## Build & Deployment Commands
- Compile: `mvn compile`
- Run tests: `mvn test` 
- Package: `mvn package`
- Full build with signing: `mvn verify`
- Deploy to Nexus: `mvn deploy`

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