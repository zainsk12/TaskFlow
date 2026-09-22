import client from './client'

export async function listFilterPresets() {
  const { data } = await client.get('/filter-presets')
  return data
}

export async function createFilterPreset(payload) {
  const { data } = await client.post('/filter-presets', payload)
  return data
}

export async function deleteFilterPreset(id) {
  await client.delete(`/filter-presets/${id}`)
}