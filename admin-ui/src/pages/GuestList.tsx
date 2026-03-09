import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Chip, Button, Box } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import PageHeader from '../components/PageHeader';
import ResourceTable, { type Column } from '../components/ResourceTable';
import CreateResourceDialog from '../components/CreateResourceDialog';
import type { GuestDTO } from '../api/types';
import { guestApi } from '../api/services';

const columns: Column<GuestDTO>[] = [
  {
    id: 'name',
    label: 'Name',
    sortable: true,
    getValue: (r) => r.meta.name,
    render: (r) => <strong>{r.meta.name}</strong>,
  },
  {
    id: 'namespace',
    label: 'Namespace',
    sortable: true,
    getValue: (r) => r.meta.namespace,
    render: (r) => <Chip label={r.meta.namespace} size="small" variant="outlined" />,
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

const defaultGuestSpec = {
  builds: [],
  deployments: [],
  services: [],
  ext: [],
};

export default function GuestList() {
  const [guests, setGuests] = useState<GuestDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [createOpen, setCreateOpen] = useState(false);
  const navigate = useNavigate();

  const load = () => {
    setLoading(true);
    guestApi.list().then(setGuests).catch(console.error).finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const handleCreate = async (data: { name: string; namespace: string; spec: Record<string, unknown> }) => {
    await guestApi.create(data.namespace, data.name, data.spec);
    load();
  };

  return (
    <>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <PageHeader title="Guests" subtitle={`${guests.length} guests`} />
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => setCreateOpen(true)} sx={{ mt: 1 }}>
          Create Guest
        </Button>
      </Box>
      <ResourceTable
        columns={columns}
        rows={guests}
        loading={loading}
        getRowKey={(r) => `${r.meta.namespace}/${r.meta.name}`}
        onRowClick={(r) => navigate(`/guests/${r.meta.namespace}/${r.meta.name}`)}
        emptyMessage="No guests found"
      />
      <CreateResourceDialog
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onSubmit={handleCreate}
        title="Create Guest"
        specTemplate={defaultGuestSpec}
      />
    </>
  );
}
