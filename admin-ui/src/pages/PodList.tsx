import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Chip } from '@mui/material';
import PageHeader from '../components/PageHeader';
import ResourceTable, { type Column } from '../components/ResourceTable';
import StatusChip from '../components/StatusChip';
import type { K8sPodDTO } from '../api/types';
import { podApi } from '../api/services';

const columns: Column<K8sPodDTO>[] = [
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
    id: 'status',
    label: 'Status',
    sortable: true,
    getValue: (r) => r.status?.phase || 'Unknown',
    render: (r) => <StatusChip label={r.status?.phase || 'Unknown'} />,
  },
];

export default function PodList() {
  const [items, setItems] = useState<K8sPodDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    podApi.list().then(setItems).catch(console.error).finally(() => setLoading(false));
  }, []);

  return (
    <>
      <PageHeader title="Pods" subtitle={`${items.length} pods`} />
      <ResourceTable
        columns={columns}
        rows={items}
        loading={loading}
        getRowKey={(r) => `${r.meta.namespace}/${r.meta.name}`}
        onRowClick={(r) => navigate(`/pods/${r.meta.namespace}/${r.meta.name}`)}
        emptyMessage="No pods found"
      />
    </>
  );
}
