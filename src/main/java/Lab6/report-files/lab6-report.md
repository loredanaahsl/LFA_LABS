# Laboratory Work #6: Parser AST Build

### Course: Formal Languages & Finite Automata
### Author: Condrea Loredana, FAF-231
### Professors: Cretu Dumitru, Irina Cojuhari

----
## Theory
### Parsing
Parsing is the process of analyzing a sequence of tokens to determine its grammatical structure with respect to a given formal grammar. In the context of programming languages and compilers, parsing is a crucial step that converts the linear sequence of tokens into a hierarchical structure that represents the syntactic relationships between the tokens.

### Abstract Syntax Tree (AST)
An Abstract Syntax Tree is a tree representation of the abstract syntactic structure of source code. Each node of the tree denotes a construct occurring in the source code. The syntax is 'abstract' in the sense that it does not represent every detail appearing in the real syntax, but rather just the structural or content-related details.

## Objectives:
1. Get familiar with parsing, what it is and how it can be programmed [1].
2. Get familiar with the concept of AST [2].
3. In addition to what has been done in the 3rd lab work do the following:
  1. In case you didn't have a type that denotes the possible types of tokens you need to:
    1. Have a type __*TokenType*__ (like an enum) that can be used in the lexical analysis to categorize the tokens.
    2. Please use regular expressions to identify the type of the token.
  2. Implement the necessary data structures for an AST that could be used for the text you have processed in the 3rd lab work.
  3. Implement a simple parser program that could extract the syntactic information from the input text.

---
## Implementation

### Overview
This 6th lab showcases a comprehensive music notation parser that builds upon my lexer from lab 3. It uses regular expressions for token identification and implements a recursive descent parser to construct an Abstract Syntax Tree. The system supports various musical elements including notes with octaves and modifiers, rests, duration markers, bar lines, repeat sections, dynamics, and tempo commands. </br>The user interface is enhanced with templates, and a help system to guide users in writing correct music notation.

### Core Components

#### 1. Token Type Definition
I used an enum to define all possible token types, making it easy to categorize and process different musical elements. This enumeration serves as the foundation for our lexical analysis, providing clear categorization for each type of musical element that can appear in the input. The token types are organized into logical groups, which helps establish a clear mapping between input tokens and their semantic meaning. This organization enables efficient pattern matching during parsing while maintaining a clean separation between different types of musical elements. The structure makes the code more maintainable and easier to extend with new token types as needed.

```python
class TokenType(Enum):
    # Music structure nodes
    SCORE = auto()         # Root node for the entire score
    BAR = auto()           # Bar line
    REPEAT = auto()        # Repeat section
    
    # Note-related nodes
    NOTE = auto()          # Musical note
    REST = auto()          # Rest
    CHORD = auto()         # Multiple notes played together
    
    # Expression nodes
    DYNAMIC = auto()       # Dynamic marking (p, f, mf, etc.)
    TEMPO = auto()         # Tempo marking
    
    # Command nodes
    COMMAND = auto()       # Special command
```

#### 2. AST Node Structure
The base `ASTNode` class and its subclasses form the hierarchical structure of our AST. This implementation follows the Composite design pattern, where each node can contain child nodes, creating a tree structure that represents the musical composition. The base class provides common functionality that all node types inherit, including maintaining a reference to its parent for bidirectional traversal. The `add_child` method ensures proper parent-child relationships, while the `is_last_child` method plays a crucial role in tree visualization. Each node can store additional data through its optional value field, and type hints ensure type safety throughout the implementation.

```python
class ASTNode:
    def __init__(self, node_type: NodeType, value: Optional[str] = None):
        self.type = node_type
        self.value = value
        self.children: List[ASTNode] = []
        self._parent = None
    
    def add_child(self, child: 'ASTNode') -> None:
        self.children.append(child)
        child._parent = self
    
    def is_last_child(self) -> bool:
        if not self._parent:
            return True
        return self._parent.children[-1] == self
```

#### 3. Tree Visualization
The AST includes a sophisticated tree visualization system that creates a human-readable representation of the musical structure. This visualization is crucial for debugging and understanding the parsed structure. The system uses ASCII characters to create a tree-like structure, with proper indentation to show hierarchy. It employs different characters for intermediate and final nodes, maintaining vertical lines to show parent-child relationships. The visualization includes node values when present and recursively processes all children to build the complete tree representation.

```python
def __str__(self, level: int = 0) -> str:
    # For root node, don't add any prefix
    if level == 0:
        ret = f"{self.type.name}"
        if self.value is not None:
            ret += f": {self.value}"
        ret += "\n"
    else:
        # For all other nodes, add proper tree structure
        prefix = "│   " * (level - 1)
        if level > 0:
            prefix += "└── " if self.is_last_child() else "├── "
        ret = prefix + f"{self.type.name}"
        if self.value is not None:
            ret += f": {self.value}"
        ret += "\n"
    
    # Add children with proper indentation
    for child in self.children:
        ret += child.__str__(level + 1)
    return ret
```

#### 4. Parser Implementation
The parser uses a recursive descent approach to build the AST. This implementation follows the classic recursive descent parsing pattern, where each grammar rule is implemented as a method in the parser class. The parser maintains a current position in the token stream and tracks the current musical section and bar. It uses type checking to determine how to integrate new nodes and automatically creates bars when needed. The implementation handles the hierarchical structure of musical notation, ensuring that each element is placed in its proper context within the tree.

```python
class Parser:
    def __init__(self, tokens: List[Token]):
        self.tokens = tokens
        self.current = 0
        self.root = ASTNode(NodeType.SCORE)
        self.current_section = self.root
        self.current_bar = None
```

The parser maintains state for the current section and bar:

```python
def parse(self) -> ASTNode:
    while not self.is_at_end():
        node = self.parse_statement()
        if node:
            if isinstance(node, BarNode):
                if self.current_bar:
                    self.current_section.add_child(self.current_bar)
                self.current_bar = node
            elif isinstance(node, (NoteNode, RestNode)):
                if self.current_bar:
                    self.current_bar.add_child(node)
                else:
                    self.current_bar = BarNode()
                    self.current_bar.add_child(node)
            else:
                self.root.add_child(node)
        else:
            self.advance()
    return self.root
```

Key aspects of the parser:
- Maintains a current position in the token stream
- Tracks the current musical section and bar
- Uses type checking to determine how to integrate new nodes
- Automatically creates bars when needed
- Handles the hierarchical structure of musical notation

The parser handles different types of statements through a pattern-matching system that identifies various musical elements. It delegates to specialized parsing methods for each type, maintaining a clean separation of concerns for different musical elements. When encountering unrecognized tokens, it returns None, allowing the parser to continue processing the rest of the input.

```python
def parse_statement(self) -> Optional[ASTNode]:
    if self.match(TokenType.NOTE):
        return self.parse_note()
    elif self.match(TokenType.REST):
        return self.parse_rest()
    elif self.match(TokenType.BAR):
        return BarNode()
    elif self.match(TokenType.REPEAT_START):
        return self.parse_repeat()
    elif self.match(TokenType.DYNAMIC):
        return DynamicNode(self.previous().value)
    elif self.match(TokenType.COMMAND):
        return self.parse_command()
    return None
```

#### 5. Note Parsing
The parser handles notes with their properties, implementing a detailed parsing strategy for musical notes. The system extracts the basic pitch from the token and sets sensible defaults for octave and duration. It handles optional octave specifications and processes duration values, including support for dotted notes. The implementation is designed to support future extensions with modifiers while creating complete note nodes with all necessary properties.

```python
def parse_note(self) -> NoteNode:
    token = self.previous()
    pitch = token.value
    octave = 4  # default octave
    duration = 1.0  # default duration
    modifiers = []
    
    # Parse octave
    if self.match(TokenType.OCTAVE):
        octave = int(self.previous().value)
    
    # Parse duration
    if self.match(TokenType.DURATION):
        duration = 1.0 / int(self.previous().value)
        if self.match(TokenType.DOT):
            duration *= 1.5
    
    return NoteNode(pitch, octave, duration, modifiers)
```

#### 6. Error Handling
The parser includes robust error handling to gracefully handle malformed input. The system maintains the parser's position for potential recovery and handles missing repeat endings gracefully. It preserves the parse state for error recovery and returns None for invalid structures, allowing the parser to continue processing after encountering errors. This approach ensures that the parser can handle various edge cases while maintaining its ability to process valid input.

```python
def parse_repeat(self) -> RepeatNode:
    repeat_node = RepeatNode()
    start_pos = self.current
    
    while not self.match(TokenType.REPEAT_END):
        if self.is_at_end():
            # If we reach the end without finding a repeat end, 
            # rewind to the start of the repeat and parse as normal
            self.current = start_pos
            return None
        
        node = self.parse_statement()
        if node:
            repeat_node.add_child(node)
    
    return repeat_node
```

### Code Structure
#### AST Implementation
The AST is implemented using a class hierarchy:
- Base `ASTNode` class with support for children
- Specialized node classes for different musical elements:
  - `NoteNode`: Represents musical notes with pitch, octave, duration, and modifiers
  - `RestNode`: Represents rests with duration
  - `BarNode`: Represents bar lines
  - `RepeatNode`: Represents repeat sections
  - `DynamicNode`: Represents dynamic markings
  - `TempoNode`: Represents tempo commands

#### Parser Implementation
The parser uses a recursive descent approach:
- `Parser` class that takes a list of tokens as input
- Methods for parsing different musical elements:
  - `parse_note()`: Parses notes with their modifiers and duration
  - `parse_rest()`: Parses rests with duration
  - `parse_repeat()`: Parses repeat sections
  - `parse_tempo()`: Parses tempo commands
  - `parse_command()`: Parses special commands

#### User Interface
The interface provides:
- File-based input
- Direct input with templates
- Help system
- Color-coded output
- Error handling

## Results
The implementation successfully:
- Parses music notation into tokens
- Builds an AST representation
- Handles various musical elements
- Provides a user-friendly interface

### Example
```
\bpm 120
mf
c4 4 | d4 4 | e4 4 | f4 4 |
|: g4 4 | a4 4 | b4 4 | c5 4 :|
p
c5 4 | b4 4 | a4 4 | g4 4 |
```

This produces an AST like:
```
SCORE
├── TEMPO: 120 BPM
├── DYNAMIC: mf
├── BAR
│   ├── NOTE: c4 (1.0)
│   ├── NOTE: d4 (1.0)
│   ├── NOTE: e4 (1.0)
│   └── NOTE: f4 (1.0)
├── REPEAT: 2x
│   ├── BAR
│   │   ├── NOTE: g4 (1.0)
│   │   ├── NOTE: a4 (1.0)
│   │   ├── NOTE: b4 (1.0)
│   │   └── NOTE: c5 (1.0)
│   └── BAR
│       ├── NOTE: c5 (1.0)
│       ├── NOTE: b4 (1.0)
│       ├── NOTE: a4 (1.0)
│       └── NOTE: g4 (1.0)
└── DYNAMIC: p
```

## Conclusions
The laboratory work successfully implemented a parser and AST builder for music notation. The implementation demonstrates:
- Understanding of parsing concepts
- Ability to design and implement AST structures
- Extension of previous lexer implementation
- Creation of a user-friendly interface

## References
1. [Cretu Dumitru and Vasile Drumea, Irina Cojuhari. DSL_laboratory_works Repository](https://github.com/filpatterson/DSL_laboratory_works)
2. [Python Documentation](https://docs.python.org/3/)
3. [Regular Expressions in Python](https://docs.python.org/3/library/re.html)
4. [Abstract Syntax Trees](https://en.wikipedia.org/wiki/Abstract_syntax_tree) 