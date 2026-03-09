import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Chip, Box } from '@mui/material';
import PageHeader from '../components/PageHeader';
import ResourceTable, { type Column } from '../components/ResourceTable';
import type { AccountDTO } from '../api/types';
import { accountApi } from '../api/services';

const columns: Column<AccountDTO>[] = [
  {
    id: 'name',
    label: 'Name',
    sortable: true,
    getValue: (r) => r.meta.name,
    render: (r) => <strong>{r.meta.name}</strong>,
  },
  {
    id: 'permissions',
    label: 'Permissions',
    render: (r) => {
      const perms = r.spec?.permissions || [];
      if (perms.length === 0) return <span style={{ opacity: 0.5 }}>None</span>;
      return (
        <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
          {perms.slice(0, 3).map((p) => (
            <Chip key={p} label={p} size="small" variant="outlined" color="primary" />
          ))}
          {perms.length > 3 && (
            <Chip label={`+${perms.length - 3}`} size="small" variant="outlined" />
          )}
        </Box>
      );
    },
  },
  {
    id: 'uuid',
    label: 'UUID',
    render: (r) => (
      <span style={{ fontFamily: 'monospace', fontSize: 12, opacity: 0.7 }}>
        {r.meta.uuid?.substring(0, 8)}...
      </span>
    ),
  },
];

export default function AccountList() {
  const [items, setItems] = useState<AccountDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    accountApi.list().then(setItems).catch(console.error).finally(() => setLoading(false));
  }, []);

  return (
    <>
      <PageHeader title="Accounts" subtitle={`${items.length} accounts`} />
      <ResourceTable
        columns={columns}
        rows={items}
        loading={loading}
        getRowKey={(r) => r.meta.name}
        onRowClick={(r) => navigate(`/accounts/${r.meta.name}`)}
        emptyMessage="No accounts found"
      />
    </>
  );
}
