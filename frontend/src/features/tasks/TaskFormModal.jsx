import { useState } from 'react'
import Modal from '../../components/ui/Modal'
import Input from '../../components/ui/Input'
import Textarea from '../../components/ui/Textarea'
import Select from '../../components/ui/Select'
import Button from '../../components/ui/Button'
import { useCreateTask, useUpdateTask } from './useTasks'
import { useCategories } from '../categories/useCategories'
import { extractErrorMessage } from '../../api/client'
import { isoToLocalInput, localInputToIso } from '../../lib/format'
import {
  TASK_STATUSES,
  TASK_PRIORITIES,
  STATUS_LABELS,
  PRIORITY_LABELS,
} from '../../lib/constants'

const STATUS_OPTIONS = TASK_STATUSES.map((s) => ({ value: s, label: STATUS_LABELS[s] }))
const PRIORITY_OPTIONS = TASK_PRIORITIES.map((p) => ({ value: p, label: PRIORITY_LABELS[p] }))

function initialForm(task) {
  if (!task) {
    return {
      title: '',
      description: '',
      status: 'TODO',
      priority: 'MEDIUM',
      dueDate: '',
      categoryId: '',
      tags: '',
    }
  }
  return {
    title: task.title ?? '',
    description: task.description ?? '',
    status: task.status ?? 'TODO',
    priority: task.priority ?? 'MEDIUM',
    dueDate: isoToLocalInput(task.dueDate),
    categoryId: task.categoryId ?? '',
    tags: (task.tags ?? []).join(', '),
  }
}

/**
 * The actual task form. Mounts fresh each time the modal opens (parent keys it),
 * so initial state comes from {@code task} via useState — no reset effect.
 */
function TaskForm({ task, onClose }) {
  const isEdit = Boolean(task)
  const createMut = useCreateTask()
  const updateMut = useUpdateTask()
  const { data: categories } = useCategories()

  const [form, setForm] = useState(() => initialForm(task))
  const [errors, setErrors] = useState({})
  const [submitError, setSubmitError] = useState('')

  const saving = createMut.isPending || updateMut.isPending
  const update = (field) => (e) => setForm((f) => ({ ...f, [field]: e.target.value }))
  const categoryOptions = (categories ?? []).map((c) => ({ value: c.id, label: c.name }))

  function validate() {
    const next = {}
    const title = form.title.trim()
    if (!title) next.title = 'Title is required'
    else if (title.length > 120) next.title = 'Title must be at most 120 characters'
    if (form.description.length > 2000) next.description = 'Description is too long'
    setErrors(next)
    return Object.keys(next).length === 0
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setSubmitError('')
    if (!validate()) return

    const tags = form.tags
      .split(',')
      .map((t) => t.trim())
      .filter(Boolean)

    const payload = {
      title: form.title.trim(),
      description: form.description.trim() || null,
      status: form.status,
      priority: form.priority,
      dueDate: localInputToIso(form.dueDate),
      categoryId: form.categoryId || null,
      tags: tags.length ? tags : null,
    }

    try {
      if (isEdit) await updateMut.mutateAsync({ id: task.id, ...payload })
      else await createMut.mutateAsync(payload)
      onClose()
    } catch (err) {
      setSubmitError(extractErrorMessage(err, 'Could not save the task.'))
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4" noValidate>
      {submitError && (
        <div className="rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
          {submitError}
        </div>
      )}

      <Input
        label="Title"
        value={form.title}
        onChange={update('title')}
        placeholder="What needs doing?"
        maxLength={120}
        error={errors.title}
        autoFocus
      />

      <Textarea
        label="Description"
        value={form.description}
        onChange={update('description')}
        placeholder="Add details (optional)"
        rows={3}
        error={errors.description}
      />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Select label="Status" value={form.status} onChange={update('status')} options={STATUS_OPTIONS} />
        <Select
          label="Priority"
          value={form.priority}
          onChange={update('priority')}
          options={PRIORITY_OPTIONS}
        />
        <Input
          label="Due date"
          type="datetime-local"
          value={form.dueDate}
          onChange={update('dueDate')}
        />
        <Select
          label="Category"
          value={form.categoryId}
          onChange={update('categoryId')}
          options={categoryOptions}
          placeholder="No category"
        />
      </div>

      <Input
        label="Tags"
        value={form.tags}
        onChange={update('tags')}
        placeholder="Comma-separated, e.g. urgent, q3"
      />

      <div className="-mx-5 -mb-4 mt-2 flex justify-end gap-3 border-t border-slate-100 bg-slate-50 px-5 py-3">
        <Button variant="secondary" onClick={onClose} disabled={saving}>
          Cancel
        </Button>
        <Button type="submit" loading={saving}>
          {isEdit ? 'Save changes' : 'Create task'}
        </Button>
      </div>
    </form>
  )
}

/** Create/edit modal wrapper for a task. */
export default function TaskFormModal({ open, onClose, task }) {
  return (
    <Modal open={open} onClose={onClose} title={task ? 'Edit task' : 'New task'} size="lg">
      <TaskForm key={task?.id ?? 'new'} task={task} onClose={onClose} />
    </Modal>
  )
}
