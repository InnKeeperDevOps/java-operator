import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Card,
  CardContent,
  Grid,
  Tab,
  Tabs,
  Typography,
  CircularProgress,
  Chip,
} from '@mui/material';
import PageHeader from '../components/PageHeader';
import JsonViewer from '../components/JsonViewer';
import ResourceTable, { type Column } from '../components/ResourceTable';
import StatusChip from '../components/StatusChip';
import type { DeploymentDTO, K8sPodDTO } from '../api/types';
import { deploymentApi } from '../api/services';

export default function DeploymentDetail() {
  const { namespace, name } = useParams<{ namespace: string; name: string }>();
  const navigate = useNavigate();
  const [deployment, setDeployment] = useState<DeploymentDTO | null>(null);
  const [pods, setPods] = useState<K8sPodDTO[]>([]);
  const [tab, setTab] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!namespace || !name) return;
    Promise.all([
      deploymentApi.get(namespace, name),
      deploymentApi.pods(namespace, name).catch(() => []),
    ]).then(([d, p]) => {
      setDeployment(d);
      setPods(p);
    }).catch(console.error).finally(() => setLoading(false));
  }, [namespace, name]);

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}><CircularProgress /></Box>;
  if (!deployment) return <Typography color="error">Deployment not found</Typography>;

  const podColumns: Column<K8sPodDTO>[] = [
    { id: 'name', label: 'Name', render: (r) => <strong>{r.meta.name}</strong> },
    { id: 'namespace', label: 'Namespace', render: (r) => <Chip label={r.meta.namespace} size="small" variant="outlined" /> },
    { id: 'status', label: 'Status', render: (r) => <StatusChip label={r.status?.phase || 'Unknown'} /> },
  ];

  return (
    <>
      <PageHeader
        title={deployment.meta.name}
        subtitle={`Namespace: ${deployment.meta.namespace}`}
        breadcrumbs={[{ label: 'Deployments', to: '/deployments' }, { label: deployment.meta.name }]}
      />

      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, md: 4 }}>
          <Card><CardContent>
            <Typography variant="body2" color="text.secondary">Name</Typography>
            <Typography fontWeight={600}>{deployment.meta.name}</Typography>
          </CardContent></Card>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <Card><CardContent>
            <Typography variant="body2" color="text.secondary">Namespace</Typography>
            <Typography fontWeight={600}>{deployment.meta.namespace}</Typography>
          </CardContent></Card>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <Card><CardContent>
            <Typography variant="body2" color="text.secondary">Pods</Typography>
            <Typography fontWeight={600}>{pods.length}</Typography>
          </CardContent></Card>
        </Grid>
      </Grid>

      <Card>
        <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ borderBottom: 1, borderColor: 'divider', px: 2 }}>
          <Tab label="Spec" />
          <Tab label="Status" />
          <Tab label={`Pods (${pods.length})`} />
        </Tabs>
        <Box sx={{ p: 2 }}>
          {tab === 0 && <JsonViewer data={deployment.spec} />}
          {tab === 1 && <JsonViewer data={deployment.status} />}
          {tab === 2 && (
            <ResourceTable
              columns={podColumns}
              rows={pods}
              getRowKey={(r) => `${r.meta.namespace}/${r.meta.name}`}
              onRowClick={(r) => navigate(`/pods/${r.meta.namespace}/${r.meta.name}`)}
              emptyMessage="No pods"
            />
          )}
        </Box>
      </Card>
    </>
  );
}
