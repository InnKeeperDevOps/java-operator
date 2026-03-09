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
import type { SimpleExtensionDTO } from '../api/types';
import { extensionApi } from '../api/services';

export default function ExtensionDetail() {
  const { namespace, name } = useParams<{ namespace: string; name: string }>();
  const [extension, setExtension] = useState<SimpleExtensionDTO | null>(null);
  const [tab, setTab] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!namespace || !name) return;
    extensionApi.get(namespace, name)
      .then(setExtension)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [namespace, name]);

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}><CircularProgress /></Box>;
  if (!extension) return <Typography color="error">Extension not found</Typography>;

  return (
    <>
      <PageHeader
        title={extension.meta.name}
        subtitle={`Namespace: ${extension.meta.namespace}`}
        breadcrumbs={[{ label: 'Extensions', to: '/extensions' }, { label: extension.meta.name }]}
      />

      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, md: 4 }}>
          <Card><CardContent>
            <Typography variant="body2" color="text.secondary">Name</Typography>
            <Typography fontWeight={600}>{extension.meta.name}</Typography>
          </CardContent></Card>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <Card><CardContent>
            <Typography variant="body2" color="text.secondary">Namespace</Typography>
            <Typography fontWeight={600}>{extension.meta.namespace}</Typography>
          </CardContent></Card>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <Card><CardContent>
            <Typography variant="body2" color="text.secondary">UUID</Typography>
            <Typography fontWeight={600} sx={{ fontFamily: 'monospace', fontSize: 13 }}>{extension.meta.uuid}</Typography>
          </CardContent></Card>
        </Grid>
      </Grid>

      <Card>
        <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ borderBottom: 1, borderColor: 'divider', px: 2 }}>
          <Tab label="Spec" />
          <Tab label="Status" />
        </Tabs>
        <Box sx={{ p: 2 }}>
          {tab === 0 && <JsonViewer data={extension.spec} />}
          {tab === 1 && <JsonViewer data={extension.status} />}
        </Box>
      </Card>
    </>
  );
}
