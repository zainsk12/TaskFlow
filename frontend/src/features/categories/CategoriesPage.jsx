import { useState } from 'react'
import { Plus, Pencil, Trash2, FolderKanban, AlertCircle } from 'lucide-react'
import Card from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import Spinner from '../../components/ui/Spinner'
import EmptyState from '../../components/ui/EmptyState'
import ConfirmDialog from '../../components/ui/ConfirmDialog'
import CategoryFormModal from './CategoryFormModal'
import { useCategories, useDeleteCategory } from './useCategories'
import { extractErrorMessage } from '../../api/client'

export default function CategoriesPage() {
  const { data: categories, isLoading, isError, refetch } = useCategories()
  const deleteMut = useDeleteCategory()

  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [deleting, setDeleting] = useState(null)
  const [deleteError, setDeleteError] = useState('')

  function openCreate() {
    setEditing(null)
    setFormOpen(true)
  }

  function openEdit(category) {
    setEditing(category)
    setFormOpen(true)
  }

  async function confirmDelete() {
    setDeleteError('')
    try {
      await deleteMut.mutateAsync(deleting.id)
      setDeleting(null)
    } catch (err) {
      setDeleteError(extractErrorMessage(err, 'Could not delete the category.'))
    }
  }

  return (
    <div className="mx-auto max-w-5xl space-y-6">
      <div className="flex items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Categories</h1>
          <p className="mt-1 text-sm text-slate-500">Organize your tasks into groups.</p>
        </div>
        <Button onClick={openCreate}>
          <Plus className="h-4 w-4" />
          New category
        </Button>
      </div>

      {isLoading && (
        <div className="flex justify-center py-16">
          <Spinner size="lg" />
        </div>
      )}

      {isError && (
        <Card className="p-8 text-center">
          <AlertCircle className="mx-auto mb-3 h-10 w-10 text-rose-500" />
          <h2 className="text-lg font-semibold text-slate-900">Couldn’t load categories</h2>
          <Button className="mt-4" onClick={refetch}>
            Retry
          </Button>
        </Card>
      )}

      {!isLoading && !isError && categories?.length === 0 && (
        <Card>
          <EmptyState
            icon={FolderKanban}
            title="No categories yet"
            message="Create your first category to start grouping tasks."
            action={
              <Button onClick={openCreate}>
                <Plus className="h-4 w-4" />
                New category
              </Button>
            }
          />
        </Card>
      )}

      {!isLoading && !isError && categories?.length > 0 && (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {categories.map((c) => (
            <Card key={c.id} className="flex items-center gap-3 p-4">
              <span
                className="h-10 w-10 shrink-0 rounded-lg"
                style={{ backgroundColor: c.color }}
                aria-hidden="true"
              />
              <div className="min-w-0 flex-1">
                <p className="truncate font-semibold text-slate-900">{c.name}</p>
                <p className="text-xs text-slate-500">
                  {c.taskCount} {c.taskCount === 1 ? 'task' : 'tasks'}
                </p>
              </div>
              <div className="flex items-center gap-1">
                <button
                  type="button"
                  onClick={() => openEdit(c)}
                  className="rounded-lg p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
                  aria-label={`Edit ${c.name}`}
                >
                  <Pencil className="h-4 w-4" />
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setDeleteError('')
                    setDeleting(c)
                  }}
                  className="rounded-lg p-2 text-slate-400 hover:bg-rose-50 hover:text-rose-600"
                  aria-label={`Delete ${c.name}`}
                >
                  <Trash2 className="h-4 w-4" />
                </button>
              </div>
            </Card>
          ))}
        </div>
      )}

      <CategoryFormModal
        open={formOpen}
        onClose={() => setFormOpen(false)}
        category={editing}
      />

      <ConfirmDialog
        open={Boolean(deleting)}
        onClose={() => setDeleting(null)}
        onConfirm={confirmDelete}
        loading={deleteMut.isPending}
        title="Delete category"
        message={
          <>
            Delete <span className="font-semibold">{deleting?.name}</span>? Tasks in this category
            won’t be deleted, but they’ll no longer be grouped here.
            {deleteError && <span className="mt-2 block text-rose-600">{deleteError}</span>}
          </>
        }
      />
    </div>
  )
}
