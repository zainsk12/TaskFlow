import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import TaskFilters from './TaskFilters'

const defaultValues = {
  status: '',
  priority: '',
  categoryId: '',
  search: '',
  sort: 'createdAt,desc',
}

function renderTaskFilters(overrides = {}) {
  const props = {
    values: defaultValues,
    categories: [],
    presets: [],
    onChange: vi.fn(),
    onClear: vi.fn(),
    onApplyPreset: vi.fn(),
    onSavePreset: vi.fn(),
    onDeletePreset: vi.fn(),
    ...overrides,
  }

  render(<TaskFilters {...props} />)

  return props
}

describe('TaskFilters', () => {
  it('renders the search input and filter controls', () => {
    renderTaskFilters()

    expect(
      screen.getByPlaceholderText('Search tasks by title or description...'),
    ).toBeInTheDocument()

    expect(screen.getByText('Save preset')).toBeInTheDocument()
    expect(screen.getByText('All statuses')).toBeInTheDocument()
    expect(screen.getByText('All priorities')).toBeInTheDocument()
    expect(screen.getByText('All categories')).toBeInTheDocument()
  })

  it('calls onChange when searching', () => {
    const props = renderTaskFilters()

    const searchInput = screen.getByPlaceholderText(
      'Search tasks by title or description...',
    )

    fireEvent.change(searchInput, {
      target: { value: 'important task' },
    })

    expect(props.onChange).toHaveBeenCalledWith({
      search: 'important task',
    })
  })

  it('shows clear filters when a filter is active', () => {
    const props = renderTaskFilters({
      values: {
        ...defaultValues,
        status: 'TODO',
      },
    })

    const clearButton = screen.getByRole('button', {
      name: /clear filters/i,
    })

    expect(clearButton).toBeInTheDocument()

    fireEvent.click(clearButton)

    expect(props.onClear).toHaveBeenCalledTimes(1)
  })

  it('calls onSavePreset when Save preset is clicked', () => {
    const props = renderTaskFilters()

    fireEvent.click(
      screen.getByRole('button', {
        name: /save preset/i,
      }),
    )

    expect(props.onSavePreset).toHaveBeenCalledTimes(1)
  })

  it('renders saved presets and applies a selected preset', () => {
    const props = renderTaskFilters({
      presets: [
        {
          id: 'preset-1',
          name: 'High Priority',
          status: 'TODO',
          priority: 'HIGH',
          categoryId: '',
          search: 'urgent',
          sort: 'priority,desc',
        },
      ],
    })

    const presetButton = screen.getByRole('button', {
      name: 'High Priority',
    })

    expect(presetButton).toBeInTheDocument()

    fireEvent.click(presetButton)

    expect(props.onApplyPreset).toHaveBeenCalledWith({
      id: 'preset-1',
      name: 'High Priority',
      status: 'TODO',
      priority: 'HIGH',
      categoryId: '',
      search: 'urgent',
      sort: 'priority,desc',
    })
  })

  it('deletes a saved preset', () => {
    const props = renderTaskFilters({
      presets: [
        {
          id: 'preset-1',
          name: 'High Priority',
        },
      ],
    })

    const deleteButton = screen.getByRole('button', {
      name: 'Delete High Priority',
    })

    fireEvent.click(deleteButton)

    expect(props.onDeletePreset).toHaveBeenCalledWith('preset-1')
  })
})