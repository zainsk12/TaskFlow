import { useMemo, useState } from 'react'
import { Plus, CheckSquare, AlertCircle } from 'lucide-react'
import Card from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import Spinner from '../../components/ui/Spinner'
import EmptyState from '../../components/ui/EmptyState'
import Pagination from '../../components/ui/Pagination'
import ConfirmDialog from '../../components/ui/ConfirmDialog'
import TaskFilters from './TaskFilters'
import TaskRow from './TaskRow'
import TaskFormModal from './TaskFormModal'
import { useTasks, useDeleteTask } from './useTasks'
import { useCategories } from '../categories/useCategories'
import { extractErrorMessage } from '../../api/client'
import { useDebouncedValue } from '../../lib/useDebouncedValue'

const PAGE_SIZE = 10
const DEFAULT_FILTERS = {
  status: '',
  priority: '',
  categoryId: '',
  search: '',
  sort: 'createdAt,desc',
}

export default function TasksPage() {
  const [filters, setFilters] = useState(DEFAULT_FILTERS)
  const [page, setPage] = useState(0)

  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [deleting, setDeleting] = useState(null)
  const [deleteError, setDeleteError] = useState('')

  const deleteMut = useDeleteTask()
  const { data: categories } = useCategories()

  // Debounce only the search text; other filters apply immediately.
  const debouncedSearch = useDebouncedValue(filters.search, 350)

  const params = useMemo(
    () => ({
      status: filters.status,
      priority: filters.priority,
      categoryId: filters.categoryId,
      search: debouncedSearch,
      sort: filters.sort,
      page,
      size: PAGE_SIZE,
    }),
    [filters.status, filters.priority, filters.categoryId, filters.sort, debouncedSearch, page],
  )

  const { data, isLoading, isError, isFetching, refetch } = useTasks(params)

  const categoriesById = useMemo(() => {
    const map = {}
    for (const c of categories ?? []) map[c.id] = c
    return map
  }, [categories])

  // Any filter change resets to the first page.
  function patchFilters(patch) {
    setFilters((f) => ({ ...f, ...patch }))
    setPage(0)
  }

  function clearFilters() {
    setFilters(DEFAULT_FILTERS)
    setPage(0)
  }

  function openCreate() {
    setEditing(null)
    setFormOpen(true)
  }

  function openEdit(task) {
    setEditing(task)
    setFormOpen(true)
  }

  async function confirmDelete() {
    setDeleteError('')
    try {
      await deleteMut.mutateAsync(deleting.id)
      setDeleting(null)
    } catch (err) {
      setDeleteError(extractErrorMessage(err, 'Could not delete the task.'))
    }
  }

  const tasks = data?.content ?? []
  const hasAnyFilter =
    filters.status || filters.priority || filters.categoryId || debouncedSearch

  return (
    <div className="mx-auto max-w-5xl space-y-6">
      <div className="flex items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Tasks</h1>
          <p className="mt-1 text-sm text-slate-500">Create, organize and track your work.</p>
        </div>
        <Button onClick={openCreate}>
          <Plus className="h-4 w-4" />
          New task
        </Button>
      </div>

      <Card className="p-4">
        <TaskFilters
          values={filters}
          categories={categories ?? []}
          onChange={patchFilters}
          onClear={clearFilters}
        />
      </Card>

      {isLoading && (
        <div className="flex justify-center py-16">
          <Spinner size="lg" />
        </div>
      )}

      {isError && (
        <Card className="p-8 text-center">
          <AlertCircle className="mx-auto mb-3 h-10 w-10 text-rose-500" />
          <h2 className="text-lg font-semibold text-slate-900">Couldn’t load tasks</h2>
          <Button className="mt-4" onClick={refetch}>
            Retry
          </Button>
        </Card>
      )}

      {!isLoading && !isError && tasks.length === 0 && (
        <Card>
          <EmptyState
            icon={CheckSquare}
            title={hasAnyFilter ? 'No matching tasks' : 'No tasks yet'}
            message={
              hasAnyFilter
                ? 'Try adjusting or clearing your filters.'
                : 'Create your first task to get started.'
            }
            action={
              hasAnyFilter ? (
                <Button variant="secondary" onClick={clearFilters}>
                  Clear filters
                </Button>
              ) : (
                <Button onClick={openCreate}>
                  <Plus className="h-4 w-4" />
                  New task
                </Button>
              )
            }
          />
        </Card>
      )}

      {!isLoading && !isError && tasks.length > 0 && (
        <div className={`space-y-3 transition-opacity ${isFetching ? 'opacity-60' : ''}`}>
          {tasks.map((task) => (
            <TaskRow
              key={task.id}
              task={task}
              category={task.categoryId ? categoriesById[task.categoryId] : undefined}
              onEdit={openEdit}
              onDelete={(t) => {
                setDeleteError('')
                setDeleting(t)
              }}
            />
          ))}

          <div className="pt-2">
            <Pagination
              page={data.page}
              size={data.size}
              totalPages={data.totalPages}
              totalElements={data.totalElements}
              onChange={setPage}
            />
          </div>
        </div>
      )}

      <TaskFormModal open={formOpen} onClose={() => setFormOpen(false)} task={editing} />

      <ConfirmDialog
        open={Boolean(deleting)}
        onClose={() => setDeleting(null)}
        onConfirm={confirmDelete}
        loading={deleteMut.isPending}
        title="Delete task"
        message={
          <>
            Delete <span className="font-semibold">{deleting?.title}</span>? This can’t be undone.
            {deleteError && <span className="mt-2 block text-rose-600">{deleteError}</span>}
          </>
        }
      />
    </div>
  )
}
