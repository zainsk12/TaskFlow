import { useState } from 'react'
import Modal from '../../components/ui/Modal'
import Input from '../../components/ui/Input'
import Button from '../../components/ui/Button'
import ColorPicker from './ColorPicker'
import { useCreateCategory, useUpdateCategory } from './useCategories'
import { extractErrorMessage } from '../../api/client'

const DEFAULT_COLOR = '#6366F1'
const HEX_RE = /^#([0-9A-Fa-f]{6})$/

/**
 * The actual form. It mounts fresh each time the modal opens (the parent keys
 * it), so initial state comes straight from {@code category} via useState — no
 * reset effect needed.
 */
function CategoryForm({ category, onClose }) {
  const isEdit = Boolean(category)
  const createMut = useCreateCategory()
  const updateMut = useUpdateCategory()

  const [name, setName] = useState(category?.name ?? '')
  const [color, setColor] = useState(category?.color ?? DEFAULT_COLOR)
  const [errors, setErrors] = useState({})
  const [submitError, setSubmitError] = useState('')

  const saving = createMut.isPending || updateMut.isPending

  function validate() {
    const next = {}
    const trimmed = name.trim()
    if (!trimmed) next.name = 'Name is required'
    else if (trimmed.length > 40) next.name = 'Name must be at most 40 characters'
    if (!HEX_RE.test(color)) next.color = 'Pick a valid color'
    setErrors(next)
    return Object.keys(next).length === 0
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setSubmitError('')
    if (!validate()) return
    const payload = { name: name.trim(), color }
    try {
      if (isEdit) await updateMut.mutateAsync({ id: category.id, ...payload })
      else await createMut.mutateAsync(payload)
      onClose()
    } catch (err) {
      setSubmitError(extractErrorMessage(err, 'Could not save the category.'))
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
        label="Name"
        value={name}
        onChange={(e) => setName(e.target.value)}
        placeholder="e.g. Work"
        maxLength={40}
        error={errors.name}
        autoFocus
      />
      <ColorPicker value={color} onChange={setColor} />
      {errors.color && <p className="text-xs font-medium text-rose-600">{errors.color}</p>}

      <div className="-mx-5 -mb-4 mt-2 flex justify-end gap-3 border-t border-slate-100 bg-slate-50 px-5 py-3">
        <Button variant="secondary" onClick={onClose} disabled={saving}>
          Cancel
        </Button>
        <Button type="submit" loading={saving}>
          {isEdit ? 'Save changes' : 'Create'}
        </Button>
      </div>
    </form>
  )
}

/** Create/edit modal wrapper for a category. */
export default function CategoryFormModal({ open, onClose, category }) {
  return (
    <Modal open={open} onClose={onClose} title={category ? 'Edit category' : 'New category'}>
      <CategoryForm key={category?.id ?? 'new'} category={category} onClose={onClose} />
    </Modal>
  )
}
