# Reznor- Emulation Application

Reznor- is planned to be "An APP for your emulation needs" as described in README.md. This repository is currently in its initial state with minimal content.

**Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.**

## Current Repository State

**IMPORTANT**: This repository is currently minimal and contains only:
- README.md with basic project description
- This copilot-instructions.md file

**No build system, dependencies, or application code exists yet.** Any development work will need to start from scratch.

## Working Effectively

### Repository Structure
- Repository root: `/`
- Documentation: `README.md`
- No source code directories exist yet
- No build configuration files exist yet
- No dependency management files exist yet

### Initial Setup for Development
When beginning development on this repository:

1. **Choose your technology stack** - Since this is an emulation application, consider:
   - **Native languages**: C++, Rust, or C for performance-critical emulation cores
   - **Cross-platform frameworks**: Electron, Tauri, or Qt for desktop applications  
   - **Web technologies**: TypeScript/JavaScript with WebAssembly for browser-based emulation
   - **Game development engines**: Unity, Godot, or custom engine approaches

2. **Set up build system** - Depending on chosen stack:
   - For C++: CMake, Meson, or Makefile
   - For Rust: Cargo (built-in)
   - For Node.js/Electron: npm/yarn with package.json
   - For Python: pip with requirements.txt or pyproject.toml

3. **Create project structure** - Typical emulation project layout:
   ```
   src/
   ├── core/          # Emulation core(s)
   ├── ui/            # User interface
   ├── audio/         # Audio processing
   ├── input/         # Input handling  
   ├── debugger/      # Debugging tools
   └── tests/         # Unit tests
   ```

### Development Guidelines

**Performance Considerations**:
- Emulation requires high performance - profile early and often
- Consider timing-sensitive code paths for accurate emulation
- Memory management is critical for handling ROM/RAM state

**Testing Strategy**:
- Unit tests for individual components
- Integration tests with known ROM test suites
- Performance benchmarks for emulation accuracy
- Cross-platform compatibility testing

**Validation Scenarios**:
When code exists, always test:
1. **ROM Loading**: Verify various ROM formats load correctly
2. **Emulation Accuracy**: Test with known good ROMs and compare output
3. **Performance**: Ensure consistent frame rates and timing
4. **Input Handling**: Test all supported input methods
5. **Save/Load States**: Verify state persistence works correctly
6. **Cross-platform**: Test on multiple operating systems if supported

## Common Emulation Development Considerations

### ROM and Legal Considerations
- Never include copyrighted ROM files in the repository
- Document supported ROM formats clearly
- Include clear legal disclaimers about ROM usage
- Consider supporting homebrew/open-source ROMs for testing

### Emulation Core Design
- Separate emulation logic from UI/platform code
- Design for modularity to support multiple systems
- Consider accuracy vs. performance trade-offs
- Implement proper timing and synchronization

### User Interface Requirements
- ROM file browser/loader
- Emulation controls (pause, reset, save/load states)
- Input configuration
- Display options (scaling, filters, etc.)
- Audio settings
- Debug/development tools

## Build and Test Commands

**Currently**: No build system exists. The following commands should be implemented when development begins:

### Expected Build Commands (to be implemented)
```bash
# Installation
make install          # or npm install, cargo build, etc.
./configure          # if using autotools
```

### Expected Test Commands (to be implemented)  
```bash
make test            # or npm test, cargo test, etc.
make bench           # for performance testing
```

### Expected Run Commands (to be implemented)
```bash
./reznor [rom_file]  # or similar executable name
make run             # or npm start, cargo run, etc.
```

## Validation Requirements

**NEVER CANCEL builds or long-running operations** - Set timeouts of 60+ minutes for initial builds.

When implemented, builds may take significant time due to:
- Emulation core compilation and optimization
- Cross-platform binary generation  
- Asset processing and bundling
- **NEVER CANCEL**: Initial builds may take 30-45 minutes
- **NEVER CANCEL**: Test suites may take 15-30 minutes with ROM validation

### Manual Testing Scenarios (when implemented)
Always validate changes with:
1. Load a test ROM and verify it starts correctly
2. Test basic emulation functionality (if ROM runs)
3. Verify input controls work as expected
4. Test save/load state functionality
5. Check performance metrics meet targets
6. Verify cross-platform compatibility

## Future Development Notes

### Recommended Libraries/Dependencies
Consider these battle-tested emulation libraries:
- **Audio**: SDL2 Audio, OpenAL, or platform-specific APIs
- **Graphics**: SDL2, SFML, or native platform APIs
- **Input**: SDL2 Input or platform-specific APIs
- **Cross-platform**: Qt, GTK, or web technologies
- **Performance profiling**: Built-in profilers, Valgrind, or perf

### Architecture Patterns
- Model-View-Controller for UI separation
- Observer pattern for event handling
- State pattern for emulation modes
- Factory pattern for ROM format support
- Thread-safe design for multi-threaded emulation

## Repository Information

**Repository URL**: https://github.com/n00bno0b/Reznor-
**Current State**: Minimal repository with only README.md
**Development Status**: Pre-development phase
**Target Platform**: To be determined based on implementation choices

Last updated: Initial creation - no builds or tests validated yet due to minimal repository state.