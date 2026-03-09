import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Card,
  Typography,
  CircularProgress,
  Box,
  TableSortLabel,
} from '@mui/material';
import { useState, useMemo } from 'react';

export interface Column<T> {
  id: string;
  label: string;
  render: (row: T) => React.ReactNode;
  sortable?: boolean;
  getValue?: (row: T) => string | number;
}

interface ResourceTableProps<T> {
  columns: Column<T>[];
  rows: T[];
  loading?: boolean;
  emptyMessage?: string;
  onRowClick?: (row: T) => void;
  getRowKey: (row: T) => string;
}

export default function ResourceTable<T>({
  columns,
  rows,
  loading,
  emptyMessage = 'No data found',
  onRowClick,
  getRowKey,
}: ResourceTableProps<T>) {
  const [orderBy, setOrderBy] = useState<string | null>(null);
  const [order, setOrder] = useState<'asc' | 'desc'>('asc');

  const handleSort = (colId: string) => {
    if (orderBy === colId) {
      setOrder(order === 'asc' ? 'desc' : 'asc');
    } else {
      setOrderBy(colId);
      setOrder('asc');
    }
  };

  const safeRows = Array.isArray(rows) ? rows : [];

  const sorted = useMemo(() => {
    if (!orderBy) return safeRows;
    const col = columns.find((c) => c.id === orderBy);
    if (!col?.getValue) return safeRows;
    return [...safeRows].sort((a, b) => {
      const va = col.getValue!(a);
      const vb = col.getValue!(b);
      const cmp = va < vb ? -1 : va > vb ? 1 : 0;
      return order === 'asc' ? cmp : -cmp;
    });
  }, [safeRows, orderBy, order, columns]);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Card>
      <TableContainer>
        <Table>
          <TableHead>
            <TableRow>
              {columns.map((col) => (
                <TableCell key={col.id} sx={{ fontWeight: 600, color: 'text.secondary', fontSize: 12, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                  {col.sortable ? (
                    <TableSortLabel
                      active={orderBy === col.id}
                      direction={orderBy === col.id ? order : 'asc'}
                      onClick={() => handleSort(col.id)}
                    >
                      {col.label}
                    </TableSortLabel>
                  ) : (
                    col.label
                  )}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {sorted.length === 0 ? (
              <TableRow>
                <TableCell colSpan={columns.length}>
                  <Typography color="text.secondary" align="center" sx={{ py: 4 }}>
                    {emptyMessage}
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              sorted.map((row) => (
                <TableRow
                  key={getRowKey(row)}
                  hover
                  onClick={() => onRowClick?.(row)}
                  sx={{ cursor: onRowClick ? 'pointer' : 'default' }}
                >
                  {columns.map((col) => (
                    <TableCell key={col.id}>{col.render(row)}</TableCell>
                  ))}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Card>
  );
}
