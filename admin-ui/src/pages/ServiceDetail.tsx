import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  Box,
  Card,
  CardContent,
  Grid,
  Tab,
  Tabs,
  Typography,
  CircularProgress,
} from '@mui/material';
import PageHeader from '../components/PageHeader';
import JsonViewer from '../components/JsonViewer';
import type { ServiceDTO, K8sServiceDTO } from '../api/types';
import { serviceApi } from '../api/services';

export default function ServiceDetail() {
  const { namespace, name } = useParams<{ namespace: string; name: string }>();
  const [service, setService] = useState<ServiceDTO | null>(null);
  const [k8sService, setK8sService] = useState<K8sServiceDTO | null>(null);
  const [tab, setTab] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!namespace || !name) return;
    Promise.all([
      serviceApi.get(namespace, name),
      serviceApi.k8sObject(namespace, name).catch(() => null),
    ]).then(([s, k]) => {
      setService(s);
      setK8sService(k);
    }).catch(console.error).finally(() => setLoading(false));
  }, [namespace, name]);

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}><CircularProgress /></Box>;
  if (!service) return <Typography color="error">Service not found</Typography>;

  return (
    <>
      <PageHeader
        title={service.meta.name}
        subtitle={`Namespace: ${service.meta.namespace}`}
        breadcrumbs={[{ label: 'Services', to: '/services' }, { label: service.meta.name }]}
      />

      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, md: 4 }}>
          <Card><CardContent>
            <Typography variant="body2" color="text.secondary">Name</Typography>
            <Typography fontWeight={600}>{service.meta.name}</Typography>
          </CardContent></Card>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <Card><CardContent>
            <Typography variant="body2" color="text.secondary">Namespace</Typography>
            <Typography fontWeight={600}>{service.meta.namespace}</Typography>
          </CardContent></Card>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <Card><CardContent>
            <Typography variant="body2" color="text.secondary">UUID</Typography>
            <Typography fontWeight={600} sx={{ fontFamily: 'monospace', fontSize: 13 }}>{service.meta.uuid}</Typography>
          </CardContent></Card>
        </Grid>
      </Grid>

      <Card>
        <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ borderBottom: 1, borderColor: 'divider', px: 2 }}>
          <Tab label="Spec" />
          <Tab label="Status" />
          {k8sService && <Tab label="K8s Object" />}
        </Tabs>
        <Box sx={{ p: 2 }}>
          {tab === 0 && <JsonViewer data={service.spec} />}
          {tab === 1 && <JsonViewer data={service.status} />}
          {tab === 2 && k8sService && <JsonViewer data={k8sService} />}
        </Box>
      </Card>
    </>
  );
}
