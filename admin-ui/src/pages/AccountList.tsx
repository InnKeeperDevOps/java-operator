import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Chip, Box, Button } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import PageHeader from '../components/PageHeader';
import ResourceTable, { type Column } from '../components/ResourceTable';
import CreateResourceDialog from '../components/CreateResourceDialog';
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

const defaultAccountSpec = {
  permissions: [],
  roles: [],
  email: '',
  name: '',
};

export default function AccountList() {
  const [items, setItems] = useState<AccountDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [createOpen, setCreateOpen] = useState(false);
  const navigate = useNavigate();

  const load = () => {
    setLoading(true);
    accountApi.list().then(setItems).catch(console.error).finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const handleCreate = async (data: { name: string; namespace: string; spec: Record<string, unknown> }) => {
    await accountApi.create(data.name, data.spec);
    load();
  };

  return (
    <>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <PageHeader title="Accounts" subtitle={`${items.length} accounts`} />
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => setCreateOpen(true)} sx={{ mt: 1 }}>
          Create Account
        </Button>
      </Box>
      <ResourceTable
        columns={columns}
        rows={items}
        loading={loading}
        getRowKey={(r) => r.meta.name}
        onRowClick={(r) => navigate(`/accounts/${r.meta.name}`)}
        emptyMessage="No accounts found"
      />
      <CreateResourceDialog
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onSubmit={handleCreate}
        title="Create Account"
        showNamespace={false}
        specTemplate={defaultAccountSpec}
      />
    </>
  );
}
