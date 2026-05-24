// taskStore.js
// Demonstrates:
//   - Async/await for simulated concurrent access
//   - JSON as the data format (native to JS)
//   - Closures and module pattern
//   - Promise-based locking to simulate concurrent safe writes

import { readFileSync, writeFileSync, existsSync } from 'fs';

const DB_FILE = './tasks.json';

let idCounter = 1;

// ---------------------------------------------------------------------------
// Simple async "lock" using a promise chain - simulates safe concurrent writes
// ---------------------------------------------------------------------------
let writeQueue = Promise.resolve();

function queueWrite(fn) {
  const next = writeQueue.then(fn);
  // Prevent a rejected write from poisoning the queue for future calls
  writeQueue = next.catch(() => {});
  return next;
}

// ---------------------------------------------------------------------------
// Persistence helpers
// ---------------------------------------------------------------------------
function loadTasks() {
  if (!existsSync(DB_FILE)) return { tasks: [], nextId: 1 };
  const raw = readFileSync(DB_FILE, 'utf-8');
  const data = JSON.parse(raw);
  idCounter = data.nextId || 1;
  return data;
}

function saveTasks(data) {
  writeFileSync(DB_FILE, JSON.stringify(data, null, 2), 'utf-8');
}

// ---------------------------------------------------------------------------
// Task CRUD - all functions return Promises to demonstrate async/await
// ---------------------------------------------------------------------------

/**
 * @param {string} title
 * @param {string} description
 * @param {'WORK'|'PERSONAL'|'SHOPPING'|'HEALTH'|'OTHER'} category
 * @param {string} assignedTo
 * @returns {Promise<object>} created task
 */
export async function addTask(title, description, category, assignedTo) {
  return queueWrite(() => {
    // Simulate async I/O delay (e.g. network/DB round-trip)
    return new Promise((resolve) => {
      setTimeout(() => {
        const data = loadTasks();
        const task = {
          id: idCounter++,
          title,
          description,
          category: category.toUpperCase(),
          assignedTo,
          status: 'PENDING',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
        };
        data.tasks.push(task);
        data.nextId = idCounter;
        saveTasks(data);
        resolve(task);
      }, 10); // 10 ms simulated latency
    });
  });
}

/**
 * Remove a task. Members can only delete their own tasks; admins can delete any.
 */
export async function removeTask(id, requestingUser) {
  return queueWrite(() => {
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        const data = loadTasks();
        const idx = data.tasks.findIndex(t => t.id === id);
        if (idx === -1) return reject(new Error(`Task #${id} not found`));

        const task = data.tasks[idx];
        if (!requestingUser.isAdmin && task.assignedTo !== requestingUser.username) {
          return reject(new Error('Permission denied: you can only delete your own tasks'));
        }
        data.tasks.splice(idx, 1);
        saveTasks(data);
        resolve(task);
      }, 10);
    });
  });
}

/**
 * Update task status.
 */
export async function updateStatus(id, newStatus, requestingUser) {
  return queueWrite(() => {
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        const data = loadTasks();
        const task = data.tasks.find(t => t.id === id);
        if (!task) return reject(new Error(`Task #${id} not found`));
        if (!requestingUser.isAdmin && task.assignedTo !== requestingUser.username) {
          return reject(new Error('Permission denied'));
        }
        task.status = newStatus.toUpperCase();
        task.updatedAt = new Date().toISOString();
        saveTasks(data);
        resolve(task);
      }, 10);
    });
  });
}

/**
 * Reassign a task (admin only).
 */
export async function reassignTask(id, newUser, requestingUser) {
  return queueWrite(() => {
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        if (!requestingUser.isAdmin) {
          return reject(new Error('Permission denied: only admins can reassign tasks'));
        }
        const data = loadTasks();
        const task = data.tasks.find(t => t.id === id);
        if (!task) return reject(new Error(`Task #${id} not found`));
        task.assignedTo = newUser;
        task.updatedAt = new Date().toISOString();
        saveTasks(data);
        resolve(task);
      }, 10);
    });
  });
}

// ---------------------------------------------------------------------------
// Read helpers - synchronous reads are fine (no mutation risk)
// ---------------------------------------------------------------------------

export function getAllTasks() {
  const { tasks } = loadTasks();
  return tasks;
}

export function getTasksByUser(username) {
  return getAllTasks().filter(t => t.assignedTo.toLowerCase() === username.toLowerCase());
}

export function getTasksByCategory(category) {
  return getAllTasks().filter(t => t.category === category.toUpperCase());
}

export function getTasksByStatus(status) {
  return getAllTasks().filter(t => t.status === status.toUpperCase());
}

export function getTaskById(id) {
  return getAllTasks().find(t => t.id === id) || null;
}

export function getSummary() {
  const tasks = getAllTasks();
  const byStatus = {};
  const byCategory = {};
  const byUser = {};

  for (const task of tasks) {
    byStatus[task.status]       = (byStatus[task.status] || 0) + 1;
    byCategory[task.category]   = (byCategory[task.category] || 0) + 1;
    byUser[task.assignedTo]     = (byUser[task.assignedTo] || 0) + 1;
  }

  return { total: tasks.length, byStatus, byCategory, byUser };
}
