import { useMemo, useState } from 'react'
import { Plus, CheckSquare, AlertCircle, Save } from 'lucide-react'
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
import {
  useFilterPresets,
  useCreateFilterPreset,
  useDeleteFilterPreset,
} from './useFilterPresets'
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

  const [savePresetOpen, setSavePresetOpen] = useState(false)
  const [presetName, setPresetName] = useState('')
  const [presetError, setPresetError] = useState('')

  const deleteMut = useDeleteTask()
  const createPresetMut = useCreateFilterPreset()
  const deletePresetMut = useDeleteFilterPreset()

  const { data: categories } = useCategories()
  const { data: presets = [], isLoading: presetsLoading } = useFilterPresets()

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
    [
      filters.status,
      filters.priority,
      filters.categoryId,
      filters.sort,
      debouncedSearch,
      page,
    ],
  )

  const { data, isLoading, isError, isFetching, refetch } = useTasks(params)

  const categoriesById = useMemo(() => {
    const map = {}
    for (const c of categories ?? []) {
      map[c.id] = c
    }
    return map
  }, [categories])

  function patchFilters(patch) {
    setFilters((f) => ({ ...f, ...patch }))
    setPage(0)
  }

  function clearFilters() {
    setFilters(DEFAULT_FILTERS)
    setPage(0)
  }

  function applyPreset(preset) {
    setFilters({
      status: preset.status ?? '',
      priority: preset.priority ?? '',
      categoryId: preset.categoryId ?? '',
      search: preset.search ?? '',
      sort: preset.sort || DEFAULT_FILTERS.sort,
    })
    setPage(0)
  }

  function openSavePreset() {
    setPresetName('')
    setPresetError('')
    setSavePresetOpen(true)
  }

  async function savePreset() {
    const name = presetName.trim()

    if (!name) {
      setPresetError('Preset name is required.')
      return
    }

    setPresetError('')

    try {
      await createPresetMut.mutateAsync({
        name,
        status: filters.status,
        priority: filters.priority,
        categoryId: filters.categoryId,
        search: filters.search,
        sort: filters.sort,
      })

      setSavePresetOpen(false)
      setPresetName('')
    } catch (err) {
      setPresetError(
        extractErrorMessage(err, 'Could not save the filter preset.'),
      )
    }
  }

  async function deletePreset(id) {
    try {
      await deletePresetMut.mutateAsync(id)
    } catch (err) {
      setPresetError(
        extractErrorMessage(err, 'Could not delete the filter preset.'),
      )
    }
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
      setDeleteError(
        extractErrorMessage(err, 'Could not delete the task.'),
      )
    }
  }

  const tasks = data?.content ?? []

  const hasAnyFilter =
    filters.status ||
    filters.priority ||
    filters.categoryId ||
    debouncedSearch

  return (
    <div className="mx-auto max-w-5xl space-y-6">
      <div className="flex items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Tasks</h1>
          <p className="mt-1 text-sm text-slate-500">
            Create, organize and track your work.
          </p>
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
          presets={presetsLoading ? [] : presets}
          onChange={patchFilters}
          onClear={clearFilters}
          onApplyPreset={applyPreset}
          onSavePreset={openSavePreset}
          onDeletePreset={deletePreset}
          savingPreset={createPresetMut.isPending}
          deletingPresetId={
            deletePresetMut.isPending ? deletePresetMut.variables : null
          }
        />
      </Card>

      {savePresetOpen && (
        <Card className="p-4">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-end">
            <div className="flex-1">
              <label
                htmlFor="preset-name"
                className="mb-1 block text-sm font-medium text-slate-700"
              >
                Preset name
              </label>

              <input
                id="preset-name"
                type="text"
                value={presetName}
                maxLength={40}
                onChange={(e) => setPresetName(e.target.value)}
                placeholder="e.g. High priority"
                className="block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 focus:border-brand-400 focus:outline-none focus:ring-2 focus:ring-brand-200"
              />

              {presetError && (
                <p className="mt-1 text-sm text-rose-600">{presetError}</p>
              )}
            </div>

            <div className="flex gap-2">
              <Button
                type="button"
                onClick={savePreset}
                loading={createPresetMut.isPending}
              >
                <Save className="h-4 w-4" />
                Save
              </Button>

              <Button
                type="button"
                variant="secondary"
                onClick={() => setSavePresetOpen(false)}
              >
                Cancel
              </Button>
            </div>
          </div>
        </Card>
      )}

      {isLoading && (
        <div className="flex justify-center py-16">
          <Spinner size="lg" />
        </div>
      )}

      {isError && (
        <Card className="p-8 text-center">
          <AlertCircle className="mx-auto mb-3 h-10 w-10 text-rose-500" />

          <h2 className="text-lg font-semibold text-slate-900">
            Couldn&apos;t load tasks
          </h2>

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
        <div
          className={`space-y-3 transition-opacity ${
            isFetching ? 'opacity-60' : ''
          }`}
        >
          {tasks.map((task) => (
            <TaskRow
              key={task.id}
              task={task}
              category={
                task.categoryId
                  ? categoriesById[task.categoryId]
                  : undefined
              }
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

      <TaskFormModal
        open={formOpen}
        onClose={() => setFormOpen(false)}
        task={editing}
      />

      <ConfirmDialog
        open={Boolean(deleting)}
        onClose={() => setDeleting(null)}
        onConfirm={confirmDelete}
        loading={deleteMut.isPending}
        title="Delete task"
        message={
          <>
            Delete{' '}
            <span className="font-semibold">{deleting?.title}</span>?
            This can&apos;t be undone.

            {deleteError && (
              <span className="mt-2 block text-rose-600">
                {deleteError}
              </span>
            )}
          </>
        }
      />
    </div>
  )
}