import client from './client'

// Category API calls (see docs/API_SPEC.md §5).

export async function listCategories() {
  const { data } = await client.get('/categories')
  return data // CategoryResponse[]
}

export async function createCategory({ name, color }) {
  const { data } = await client.post('/categories', { name, color })
  return data
}

export async function updateCategory(id, { name, color }) {
  const { data } = await client.put(`/categories/${id}`, { name, color })
  return data
}

export async function deleteCategory(id) {
  await client.delete(`/categories/${id}`)
}
