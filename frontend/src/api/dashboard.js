import client from './client'

// Dashboard analytics API calls (see docs/API_SPEC.md §6).

export async function getSummary() {
  const { data } = await client.get('/dashboard/summary')
  return data
}

export async function getStatusDistribution() {
  const { data } = await client.get('/dashboard/status-distribution')
  return data // { TODO, IN_PROGRESS, DONE }
}

export async function getPriorityDistribution() {
  const { data } = await client.get('/dashboard/priority-distribution')
  return data // { LOW, MEDIUM, HIGH }
}

export async function getProductivity() {
  const { data } = await client.get('/dashboard/productivity')
  return data // { completionPercentage, overduePercentage, activeWorkload }
}

export async function getRecentTasks(limit = 5) {
  const { data } = await client.get('/dashboard/recent-tasks', { params: { limit } })
  return data // TaskResponse[]
}
