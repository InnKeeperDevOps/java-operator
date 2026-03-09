import { useEffect, useState } from 'react';
import { Chip } from '@mui/material';
import PageHeader from '../components/PageHeader';
import ResourceTable, { type Column } from '../components/ResourceTable';
import type { BuildDTO } from '../api/types';
import { buildApi } from '../api/services';

const columns: Column<BuildDTO>[] = [
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

export default function BuildList() {
  const [items, setItems] = useState<BuildDTO[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    buildApi.list().then(setItems).catch(console.error).finally(() => setLoading(false));
  }, []);

  return (
    <>
      <PageHeader title="Builds" subtitle={`${items.length} builds`} />
      <ResourceTable
        columns={columns}
        rows={items}
        loading={loading}
        getRowKey={(r) => `${r.meta.namespace}/${r.meta.name}`}
        emptyMessage="No builds found"
      />
    </>
  );
}
