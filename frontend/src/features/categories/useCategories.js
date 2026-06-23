import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  listCategories,
  createCategory,
  updateCategory,
  deleteCategory,
} from '../../api/categories'

const KEY = ['categories']

/** Loads the user's categories (with task counts). */
export function useCategories() {
  return useQuery({ queryKey: KEY, queryFn: listCategories })
}

/** Invalidates the queries affected by a category change. */
function useInvalidate() {
  const queryClient = useQueryClient()
  return () => {
    queryClient.invalidateQueries({ queryKey: KEY })
    // Category counts feed the dashboard and tasks reference categories.
    queryClient.invalidateQueries({ queryKey: ['dashboard'] })
    queryClient.invalidateQueries({ queryKey: ['tasks'] })
  }
}

export function useCreateCategory() {
  const invalidate = useInvalidate()
  return useMutation({ mutationFn: createCategory, onSuccess: invalidate })
}

export function useUpdateCategory() {
  const invalidate = useInvalidate()
  return useMutation({
    mutationFn: ({ id, ...payload }) => updateCategory(id, payload),
    onSuccess: invalidate,
  })
}

export function useDeleteCategory() {
  const invalidate = useInvalidate()
  return useMutation({ mutationFn: deleteCategory, onSuccess: invalidate })
}
