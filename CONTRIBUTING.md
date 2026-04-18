# Contributing to Minecraft Command Maker

Thank you for your interest in contributing to the Minecraft Command Maker project! We welcome contributions from the community and appreciate your effort to improve the mod.

## Getting Started

### Prerequisites

- Java 17, 21, or 25
- Gradle (included via `gradlew`)
- Git
- A GitHub account

### Setting Up Your Development Environment

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/Diamondstar-Mods/Minecraft-Command-Maker.git
   cd Minecraft-Command-Maker
   ```
3. **Add upstream remote** to stay in sync:
   ```bash
   git remote add upstream https://github.com/Diamondstar-Mods/Minecraft-Command-Maker.git
   ```
4. **Build the project**:
   ```bash
   ./gradlew build
   ```

## Development Workflow

### Creating a Branch

Create a new branch for your changes:
```bash
git checkout -b feature/your-feature-name
```

Use descriptive branch names:
- `feature/new-command` for new features
- `fix/bug-description` for bug fixes
- `docs/update-guide` for documentation updates
- `refactor/module-name` for code refactoring

### Making Changes

- Follow the existing code style and conventions
- Keep commits focused and logical
- Write clear, descriptive commit messages
- Include comments for complex logic
- Test your changes thoroughly

### Testing

Before submitting a pull request, ensure:

1. **Code builds successfully**:
   ```bash
   ./gradlew build
   ```

2. **No compilation errors**:
   ```bash
   ./gradlew check
   ```

3. **Manual testing** in-game for mod features
   ```bash
   ./gradlew :server:runClient
   ./gradlew :client:runClient

## Submitting Changes

### Committing Your Work

Write clear commit messages:
```bash
git commit -m "Add feature: brief description of changes"
```

Good commit messages:
- Start with a verb (Add, Fix, Update, Improve, etc.)
- Be concise but descriptive
- Reference issues if applicable (e.g., "Fixes #123")

### Pushing and Creating a Pull Request

1. **Push your branch**:
   ```bash
   git push origin feature/your-feature-name
   ```

2. **Create a pull request** on GitHub with:
   - Clear title describing the changes
   - Description of what was changed and why
   - Reference to any related issues
   - Screenshots/videos for UI changes if applicable

3. **Wait for review** and be responsive to feedback

### Pull Request Guidelines

- Keep PRs focused on a single feature or fix
- Include relevant issue references
- Provide context and motivation for changes
- Ensure all CI checks pass
- Be respectful and open to suggestions

## Code Style

- Follow Java naming conventions (camelCase for variables/methods, PascalCase for classes)
- Use proper indentation (4 spaces or 1 tab)
- Keep lines reasonably short (max 120 characters)
- Add Javadoc comments for public methods and classes
- Use meaningful variable and function names

## Documentation

Help us keep documentation up-to-date:

- Update the wiki if your changes affect user-facing features
- Add/update code comments for complex logic
- Document any new configuration options
- Update README.md if your changes affect setup or usage

## Reporting Issues

Found a bug or have a feature request?

1. **Check existing issues** to avoid duplicates
2. **Create a new issue** with:
   - Clear title
   - Detailed description
   - Steps to reproduce (for bugs)
   - Expected vs. actual behavior
   - Minecraft/Mod version
   - Relevant logs or screenshots

## Review Process

- Maintainers will review your PR (very quickly, they are bored)
- Changes may be requested
- Be open to constructive feedback
- Reviews aim to maintain code quality and project consistency

## Types of Contributions

We welcome various contributions:

- **Bug fixes**: Fix identified issues
- **Features**: Add new functionality
- **Documentation**: Improve guides and wikis
- **Code quality**: Refactor and improve existing code
- **Testing**: Add tests and improve test coverage

## License

By contributing, you agree that your contributions will be licensed under the same license as the project (check LICENSE file).

## Questions?

- Open an issue for questions
- Check existing documentation in `/docs`
- Visit wiki at `commandmakerwiki.lucasgeitgey.com` or `minecraft-command-maker.vercel.app`
- Ask in the project (forum)[https://commandmakerwiki.lucasgeitgey.com/forum.html] or chat

## Recognition

Contributors will be recognized in:
- Pull request acknowledgments
- Release notes (for significant contributions)
- Project contributor list

Thank you for making Minecraft Command Maker better! 🎮
