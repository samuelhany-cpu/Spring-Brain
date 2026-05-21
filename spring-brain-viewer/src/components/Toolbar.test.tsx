import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { Toolbar } from './Toolbar'
import type { DiagnosticsReport } from '../types'

const mockDiagnostics: DiagnosticsReport = {
  schemaVersion: '1.0.0',
  diagnostics: [
    { severity: 'WARNING', code: 'TEST', message: 'test', file: 'A.java', line: 1 },
    { severity: 'WARNING', code: 'TEST2', message: 'test2', file: 'B.java', line: 2 },
  ],
}

describe('Toolbar', () => {
  it('shows stats badge with node and edge counts', () => {
    render(
      <Toolbar
        nodeCount={17}
        edgeCount={11}
        diagnostics={mockDiagnostics}
        visibleTypes={new Set(['route', 'controller', 'service', 'repository', 'entity', 'config_property'])}
        searchQuery=""
        onVisibleTypesChange={vi.fn()}
        onSearchChange={vi.fn()}
      />
    )
    expect(screen.getByText('17 nodes · 11 edges')).toBeInTheDocument()
  })

  it('shows warnings badge when diagnostics present', () => {
    render(
      <Toolbar
        nodeCount={5}
        edgeCount={3}
        diagnostics={mockDiagnostics}
        visibleTypes={new Set(['route', 'controller', 'service', 'repository', 'entity', 'config_property'])}
        searchQuery=""
        onVisibleTypesChange={vi.fn()}
        onSearchChange={vi.fn()}
      />
    )
    expect(screen.getByText('⚠ 2 warnings')).toBeInTheDocument()
  })

  it('hides diagnostics badge when no diagnostics', () => {
    const emptyDiag: DiagnosticsReport = { schemaVersion: '1.0.0', diagnostics: [] }
    render(
      <Toolbar
        nodeCount={5}
        edgeCount={3}
        diagnostics={emptyDiag}
        visibleTypes={new Set(['route', 'controller', 'service', 'repository', 'entity', 'config_property'])}
        searchQuery=""
        onVisibleTypesChange={vi.fn()}
        onSearchChange={vi.fn()}
      />
    )
    expect(screen.queryByText(/warnings/)).not.toBeInTheDocument()
  })

  it('calls onSearchChange when search input changes', () => {
    const onSearch = vi.fn()
    render(
      <Toolbar
        nodeCount={5}
        edgeCount={3}
        diagnostics={{ schemaVersion: '1.0.0', diagnostics: [] }}
        visibleTypes={new Set(['route', 'controller', 'service', 'repository', 'entity', 'config_property'])}
        searchQuery=""
        onVisibleTypesChange={vi.fn()}
        onSearchChange={onSearch}
      />
    )
    fireEvent.change(screen.getByPlaceholderText('Search class, route…'), { target: { value: 'User' } })
    expect(onSearch).toHaveBeenCalledWith('User')
  })
})
