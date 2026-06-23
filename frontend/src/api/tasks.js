import client from './client'

// Task API calls (see docs/API_SPEC.md §4).

/** Drops null/undefined/'' values so empty filters aren't sent as query params. */
function clean(params = {}) {
  return Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== null && v !== undefined && v !== ''),
  )
}

/**
 * Lists tasks. {@code params} may include status, priority, categoryId, search,
 * overdue, dueAfter, dueBefore, page, size and sort ("field,dir").
 * Returns the PageResponse wrapper.
 */
export async function listTasks(params = {}) {
  const { data } = await client.get('/tasks', { params: clean(params) })
  return data
}

export async function createTask(payload) {
  const { data } = await client.post('/tasks', payload)
  return data
}

export async function updateTask(id, payload) {
  const { data } = await client.put(`/tasks/${id}`, payload)
  return data
}

export async function deleteTask(id) {
  await client.delete(`/tasks/${id}`)
}
