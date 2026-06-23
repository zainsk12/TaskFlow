import {
  useMutation,
  useQuery,
  useQueryClient,
  keepPreviousData,
} from '@tanstack/react-query'
import { listTasks, createTask, updateTask, deleteTask } from '../../api/tasks'

/**
 * Lists tasks for the given query params (filters + pagination + sort). The
 * params are part of the query key, so changing a filter refetches; the previous
 * page is kept on screen during pagination to avoid flicker.
 */
export function useTasks(params) {
  return useQuery({
    queryKey: ['tasks', params],
    queryFn: () => listTasks(params),
    placeholderData: keepPreviousData,
  })
}

function useInvalidate() {
  const queryClient = useQueryClient()
  return () => {
    queryClient.invalidateQueries({ queryKey: ['tasks'] })
    // Task changes shift dashboard counts and category task counts.
    queryClient.invalidateQueries({ queryKey: ['dashboard'] })
    queryClient.invalidateQueries({ queryKey: ['categories'] })
  }
}

export function useCreateTask() {
  const invalidate = useInvalidate()
  return useMutation({ mutationFn: createTask, onSuccess: invalidate })
}

export function useUpdateTask() {
  const invalidate = useInvalidate()
  return useMutation({
    mutationFn: ({ id, ...payload }) => updateTask(id, payload),
    onSuccess: invalidate,
  })
}

export function useDeleteTask() {
  const invalidate = useInvalidate()
  return useMutation({ mutationFn: deleteTask, onSuccess: invalidate })
}

/**
 * Builds a PUT payload from a task response, optionally overriding fields. PUT is
 * a full replace (omitted description/dueDate/categoryId clear), so we always
 * send the current values.
 */
export function toUpdatePayload(task, overrides = {}) {
  return {
    title: task.title,
    description: task.description ?? null,
    status: task.status,
    priority: task.priority,
    dueDate: task.dueDate ?? null,
    categoryId: task.categoryId ?? null,
    tags: task.tags ?? null,
    ...overrides,
  }
}
