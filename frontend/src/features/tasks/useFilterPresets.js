import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'
import {
  listFilterPresets,
  createFilterPreset,
  deleteFilterPreset,
} from '../../api/filterPresets'

const KEY = ['filter-presets']

export function useFilterPresets() {
  return useQuery({
    queryKey: KEY,
    queryFn: listFilterPresets,
  })
}

export function useCreateFilterPreset() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: createFilterPreset,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: KEY })
    },
  })
}

export function useDeleteFilterPreset() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: deleteFilterPreset,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: KEY })
    },
  })
}