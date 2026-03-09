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
  Button,
  Snackbar,
  Alert,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import PageHeader from '../components/PageHeader';
import JsonViewer from '../components/JsonViewer';
import JsonEditor from '../components/JsonEditor';
import ResourceTable, { type Column } from '../components/ResourceTable';
import type { GuestDTO, DeploymentDTO, ServiceDTO, BuildDTO, SimpleExtensionDTO } from '../api/types';
import { guestApi } from '../api/services';

export default function GuestDetail() {
  const { namespace, name } = useParams<{ namespace: string; name: string }>();
  const navigate = useNavigate();
  const [guest, setGuest] = useState<GuestDTO | null>(null);
  const [tab, setTab] = useState(0);
  const [deployments, setDeployments] = useState<DeploymentDTO[]>([]);
  const [services, setServices] = useState<ServiceDTO[]>([]);
  const [builds, setBuilds] = useState<BuildDTO[]>([]);
  const [extensions, setExtensions] = useState<SimpleExtensionDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false, message: '', severity: 'success',
  });

  const load = () => {
    if (!namespace || !name) return;
    Promise.all([
      guestApi.get(namespace, name),
      guestApi.deployments(namespace, name).catch(() => []),
      guestApi.services(namespace, name).catch(() => []),
      guestApi.builds(namespace, name).catch(() => []),
      guestApi.extensions(namespace, name).catch(() => []),
    ]).then(([g, d, s, b, e]) => {
      setGuest(g);
      setDeployments(d);
      setServices(s);
      setBuilds(b);
      setExtensions(e);
    }).catch(console.error).finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, [namespace, name]);

  const handleSaveSpec = async (data: unknown) => {
    if (!namespace || !name) return;
    const updated = await guestApi.update(namespace, name, data as Record<string, unknown>);
    setGuest(updated);
    setSnackbar({ open: true, message: 'Spec saved successfully', severity: 'success' });
  };

  const handleDelete = async () => {
    if (!namespace || !name) return;
    if (!confirm(`Delete guest "${name}" in namespace "${namespace}"?`)) return;
    try {
      await guestApi.delete(namespace, name);
      navigate('/guests');
    } catch {
      setSnackbar({ open: true, message: 'Failed to delete guest', severity: 'error' });
    }
  };

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}><CircularProgress /></Box>;
  if (!guest) return <Typography color="error">Guest not found</Typography>;

  const depColumns: Column<DeploymentDTO>[] = [
    { id: 'name', label: 'Name', render: (r) => r.meta.name },
    { id: 'namespace', label: 'Namespace', render: (r) => <Chip label={r.meta.namespace} size="small" variant="outlined" /> },
  ];
  const svcColumns: Column<ServiceDTO>[] = [
    { id: 'name', label: 'Name', render: (r) => r.meta.name },
    { id: 'namespace', label: 'Namespace', render: (r) => <Chip label={r.meta.namespace} size="small" variant="outlined" /> },
  ];
  const buildColumns: Column<BuildDTO>[] = [
    { id: 'name', label: 'Name', render: (r) => r.meta.name },
    { id: 'namespace', label: 'Namespace', render: (r) => <Chip label={r.meta.namespace} size="small" variant="outlined" /> },
  ];
  const extColumns: Column<SimpleExtensionDTO>[] = [
    { id: 'name', label: 'Name', render: (r) => r.meta.name },
    { id: 'namespace', label: 'Namespace', render: (r) => <Chip label={r.meta.namespace} size="small" variant="outlined" /> },
  ];

  return (
    <>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <PageHeader
          title={guest.meta.name}
          subtitle={`Namespace: ${guest.meta.namespace}`}
          breadcrumbs={[{ label: 'Guests', to: '/guests' }, { label: guest.meta.name }]}
        />
        <Button
          variant="outlined"
          color="error"
          startIcon={<DeleteIcon />}
          onClick={handleDelete}
          sx={{ mt: 1 }}
        >
          Delete
        </Button>
      </Box>

      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, md: 4 }}>
          <Card><CardContent>
            <Typography variant="body2" color="text.secondary">Name</Typography>
            <Typography fontWeight={600}>{guest.meta.name}</Typography>
          </CardContent></Card>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <Card><CardContent>
            <Typography variant="body2" color="text.secondary">Namespace</Typography>
            <Typography fontWeight={600}>{guest.meta.namespace}</Typography>
          </CardContent></Card>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <Card><CardContent>
            <Typography variant="body2" color="text.secondary">UUID</Typography>
            <Typography fontWeight={600} sx={{ fontFamily: 'monospace', fontSize: 13 }}>{guest.meta.uuid}</Typography>
          </CardContent></Card>
        </Grid>
      </Grid>

      <Card>
        <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ borderBottom: 1, borderColor: 'divider', px: 2 }}>
          <Tab label="Spec (Edit)" />
          <Tab label="Status" />
          <Tab label={`Deployments (${deployments.length})`} />
          <Tab label={`Services (${services.length})`} />
          <Tab label={`Builds (${builds.length})`} />
          <Tab label={`Extensions (${extensions.length})`} />
        </Tabs>
        <Box sx={{ p: 2 }}>
          {tab === 0 && <JsonEditor data={guest.spec} onSave={handleSaveSpec} />}
          {tab === 1 && <JsonViewer data={guest.status} />}
          {tab === 2 && (
            <ResourceTable
              columns={depColumns}
              rows={deployments}
              getRowKey={(r) => `${r.meta.namespace}/${r.meta.name}`}
              onRowClick={(r) => navigate(`/deployments/${r.meta.namespace}/${r.meta.name}`)}
              emptyMessage="No deployments"
            />
          )}
          {tab === 3 && (
            <ResourceTable
              columns={svcColumns}
              rows={services}
              getRowKey={(r) => `${r.meta.namespace}/${r.meta.name}`}
              onRowClick={(r) => navigate(`/services/${r.meta.namespace}/${r.meta.name}`)}
              emptyMessage="No services"
            />
          )}
          {tab === 4 && <ResourceTable columns={buildColumns} rows={builds} getRowKey={(r) => `${r.meta.namespace}/${r.meta.name}`} emptyMessage="No builds" />}
          {tab === 5 && (
            <ResourceTable
              columns={extColumns}
              rows={extensions}
              getRowKey={(r) => `${r.meta.namespace}/${r.meta.name}`}
              onRowClick={(r) => navigate(`/extensions/${r.meta.namespace}/${r.meta.name}`)}
              emptyMessage="No extensions"
            />
          )}
        </Box>
      </Card>

      <Snackbar open={snackbar.open} autoHideDuration={3000} onClose={() => setSnackbar((s) => ({ ...s, open: false }))}>
        <Alert severity={snackbar.severity} variant="filled" onClose={() => setSnackbar((s) => ({ ...s, open: false }))}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </>
  );
}
