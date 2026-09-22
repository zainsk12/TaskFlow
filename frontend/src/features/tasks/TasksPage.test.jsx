import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { describe, expect, it, vi, beforeEach } from 'vitest'

import TasksPage from './TasksPage'

const mockUseTasks = vi.fn()
const mockDeleteTask = vi.fn()
const mockUseCategories = vi.fn()
const mockUseFilterPresets = vi.fn()
const mockCreateFilterPreset = vi.fn()
const mockDeleteFilterPreset = vi.fn()

vi.mock('./useTasks', () => ({
  useTasks: (...args) => mockUseTasks(...args),
  useDeleteTask: () => ({
    mutateAsync: mockDeleteTask,
    isPending: false,
  }),
}))

vi.mock('../categories/useCategories', () => ({
  useCategories: () => mockUseCategories(),
}))

vi.mock('./useFilterPresets', () => ({
  useFilterPresets: () => mockUseFilterPresets(),
  useCreateFilterPreset: () => ({
    mutateAsync: mockCreateFilterPreset,
    isPending: false,
  }),
  useDeleteFilterPreset: () => ({
    mutateAsync: mockDeleteFilterPreset,
    isPending: false,
    variables: null,
  }),
}))

vi.mock('../../lib/useDebouncedValue', () => ({
  useDebouncedValue: (value) => value,
}))

vi.mock('./TaskRow', () => ({
  default: ({ task }) => <div>{task.title}</div>,
}))

vi.mock('./TaskFormModal', () => ({
  default: () => null,
}))

vi.mock('../../components/ui/Pagination', () => ({
  default: () => null,
}))

vi.mock('../../components/ui/ConfirmDialog', () => ({
  default: () => null,
}))

beforeEach(() => {
  vi.clearAllMocks()

  mockUseTasks.mockReturnValue({
    data: {
      content: [
        {
          id: 'task-1',
          title: 'Complete project',
          categoryId: '',
        },
      ],
      page: 0,
      size: 10,
      totalPages: 1,
      totalElements: 1,
    },
    isLoading: false,
    isError: false,
    isFetching: false,
    refetch: vi.fn(),
  })

  mockUseCategories.mockReturnValue({
    data: [],
  })

  mockUseFilterPresets.mockReturnValue({
    data: [],
    isLoading: false,
  })
})

describe('TasksPage', () => {
  it('renders the task page and existing task', () => {
    render(<TasksPage />)

    expect(screen.getByRole('heading', { name: 'Tasks' })).toBeInTheDocument()
    expect(screen.getByText('Complete project')).toBeInTheDocument()
    expect(screen.getByText('Save preset')).toBeInTheDocument()
  })

  it('opens the save preset form', () => {
    render(<TasksPage />)

    fireEvent.click(
      screen.getByRole('button', {
        name: /save preset/i,
      }),
    )

    expect(screen.getByLabelText('Preset name')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /^save$/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /cancel/i })).toBeInTheDocument()
  })

  it('requires a preset name before saving', async () => {
    render(<TasksPage />)

    fireEvent.click(
      screen.getByRole('button', {
        name: /save preset/i,
      }),
    )

    fireEvent.click(
      screen.getByRole('button', {
        name: /^save$/i,
      }),
    )

    expect(
      screen.getByText('Preset name is required.'),
    ).toBeInTheDocument()

    expect(mockCreateFilterPreset).not.toHaveBeenCalled()
  })

  it('creates a filter preset with the current filters', async () => {
    mockCreateFilterPreset.mockResolvedValue({
      id: 'preset-1',
      name: 'My Todo Tasks',
    })

    render(<TasksPage />)

    fireEvent.click(
      screen.getByRole('button', {
        name: /save preset/i,
      }),
    )

    fireEvent.change(screen.getByLabelText('Preset name'), {
      target: {
        value: 'My Todo Tasks',
      },
    })

    fireEvent.click(
      screen.getByRole('button', {
        name: /^save$/i,
      }),
    )

    await waitFor(() => {
      expect(mockCreateFilterPreset).toHaveBeenCalledWith({
        name: 'My Todo Tasks',
        status: '',
        priority: '',
        categoryId: '',
        search: '',
        sort: 'createdAt,desc',
      })
    })
  })

  it('renders a saved preset', () => {
    mockUseFilterPresets.mockReturnValue({
      data: [
        {
          id: 'preset-1',
          name: 'My Todo Tasks',
          status: 'TODO',
          priority: '',
          categoryId: '',
          search: '',
          sort: 'createdAt,desc',
        },
      ],
      isLoading: false,
    })

    render(<TasksPage />)

    expect(
      screen.getByRole('button', {
        name: 'My Todo Tasks',
      }),
    ).toBeInTheDocument()
  })
})