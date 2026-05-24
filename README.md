# Collaborative To-Do List — Cross-Language Project

Two functionally identical implementations of a collaborative to-do list app:
one in **Java** (demonstrating OOP + concurrency) and one in **JavaScript / Node.js**
(demonstrating async/await + JSON-native storage).

---

## Repository Layout

```
todo-project/
├── java-todo/
│   └── src/main/java/todo/
│       ├── Task.java            # Data model with enums
│       ├── User.java            # User model
│       ├── TaskManager.java     # Thread-safe task store (ReadWriteLock)
│       ├── ConcurrencyDemo.java # ExecutorService multi-thread demo
│       └── Main.java            # Interactive CLI entry point
│
├── javascript-todo/
│   ├── taskStore.js             # Async task store (Promise queue + JSON file)
│   ├── demo.js                  # Promise.all concurrency demo
│   ├── index.js                 # Interactive CLI entry point
│   └── package.json
│
├── README.md                    # This file
└── report/
    └── report.md                # Written comparison report
```

---

## Prerequisites

| Language   | Required                          |
|------------|-----------------------------------|
| Java       | JDK 17 or later                   |
| JavaScript | Node.js 18 or later (uses ES modules) |

---

## Running the Java Implementation

### 1. Compile

```bash
cd java-todo
mkdir -p out
javac -d out $(find src -name "*.java")
```

### 2. Run the concurrency demo + interactive CLI

```bash
java -cp out todo.Main
```

You will see the concurrency demo run automatically, then be prompted to log in
as `alice` (admin), `bob` (member), or `carol` (member).

### Menu options
| Option | Action |
|--------|--------|
| 1 | List all tasks |
| 2 | List your own tasks |
| 3 | Add a new task |
| 4 | Update a task's status |
| 5 | Delete a task |
| 6 | Filter by category |
| 7 | Filter by status |
| 8 | Summary / statistics |
| 9 | Exit |

---

## Running the JavaScript Implementation

### 1. Install dependencies

```bash
cd javascript-todo
npm install
```

### 2. Run the concurrency demo only

```bash
node demo.js
```

This seeds 5 tasks, then runs Alice, Bob, and Carol's actions concurrently via
`Promise.all`, printing interleaved output and a final summary.

### 3. Run the interactive CLI

```bash
node index.js
```

Same menu structure as the Java version. Tasks are persisted to `tasks.json`
between sessions.

> **Note:** Delete `tasks.json` before each fresh demo run:
> `rm -f tasks.json`

---

## Key Language-Specific Features Demonstrated

| Feature | Java | JavaScript |
|---------|------|------------|
| Concurrency | `ExecutorService`, `ReadWriteLock`, `CountDownLatch` | `Promise.all`, async write queue |
| Data model | `enum Status`, `enum Category`, typed classes | Plain JS objects + JSON file |
| Filtering | Java Streams (`filter`, `collect`, `groupingBy`) | Array `.filter()`, `.reduce()` |
| Error handling | Checked exceptions + permission guards | `try/catch` with async-aware `.catch()` |
| Storage | In-memory `ConcurrentHashMap` | JSON flat file via `fs.readFileSync/writeFileSync` |

---

## Users and Roles

| Username | Role   | Permissions |
|----------|--------|-------------|
| alice    | admin  | Add, update, delete, reassign any task |
| bob      | member | Add tasks; update/delete only own tasks |
| carol    | member | Add tasks; update/delete only own tasks |
