import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Chip } from '@mui/material';
import PageHeader from '../components/PageHeader';
import ResourceTable, { type Column } from '../components/ResourceTable';
import type { SimpleExtensionDTO } from '../api/types';
import { extensionApi } from '../api/services';

const columns: Column<SimpleExtensionDTO>[] = [
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

export default function ExtensionList() {
  const [items, setItems] = useState<SimpleExtensionDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    extensionApi.list().then(setItems).catch(console.error).finally(() => setLoading(false));
  }, []);

  return (
    <>
      <PageHeader title="Extensions" subtitle={`${items.length} extensions`} />
      <ResourceTable
        columns={columns}
        rows={items}
        loading={loading}
        getRowKey={(r) => `${r.meta.namespace}/${r.meta.name}`}
        onRowClick={(r) => navigate(`/extensions/${r.meta.namespace}/${r.meta.name}`)}
        emptyMessage="No extensions found"
      />
    </>
  );
}
