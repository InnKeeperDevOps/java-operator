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
  Button,
  List,
  ListItemButton,
  ListItemText,
} from '@mui/material';
import PageHeader from '../components/PageHeader';
import StatusChip from '../components/StatusChip';
import type { Container } from '../api/types';
import { podApi } from '../api/services';

export default function PodDetail() {
  const { namespace, name } = useParams<{ namespace: string; name: string }>();
  const [containers, setContainers] = useState<Container[]>([]);
  const [tab, setTab] = useState(0);
  const [selectedContainer, setSelectedContainer] = useState<string | null>(null);
  const [logs, setLogs] = useState<string>('');
  const [logsLoading, setLogsLoading] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!namespace || !name) return;
    podApi.containers(namespace, name)
      .then((c) => {
        setContainers(c);
        if (c.length > 0) setSelectedContainer(c[0].name);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [namespace, name]);

  const fetchLogs = () => {
    if (!namespace || !name || !selectedContainer) return;
    setLogsLoading(true);
    podApi.logs(namespace, name, selectedContainer)
      .then(setLogs)
      .catch((e) => setLogs(`Error fetching logs: ${e.message}`))
      .finally(() => setLogsLoading(false));
  };

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}><CircularProgress /></Box>;

  return (
    <>
      <PageHeader
        title={name || ''}
        subtitle={`Namespace: ${namespace}`}
        breadcrumbs={[{ label: 'Pods', to: '/pods' }, { label: name || '' }]}
      />

      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, md: 4 }}>
          <Card><CardContent>
            <Typography variant="body2" color="text.secondary">Pod Name</Typography>
            <Typography fontWeight={600}>{name}</Typography>
          </CardContent></Card>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <Card><CardContent>
            <Typography variant="body2" color="text.secondary">Namespace</Typography>
            <Typography fontWeight={600}>{namespace}</Typography>
          </CardContent></Card>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <Card><CardContent>
            <Typography variant="body2" color="text.secondary">Containers</Typography>
            <Typography fontWeight={600}>{containers.length}</Typography>
          </CardContent></Card>
        </Grid>
      </Grid>

      <Card>
        <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ borderBottom: 1, borderColor: 'divider', px: 2 }}>
          <Tab label="Containers" />
          <Tab label="Logs" />
        </Tabs>
        <Box sx={{ p: 2 }}>
          {tab === 0 && (
            containers.length === 0 ? (
              <Typography color="text.secondary">No containers found</Typography>
            ) : (
              <Grid container spacing={2}>
                {containers.map((c) => (
                  <Grid size={{ xs: 12, md: 6 }} key={c.name}>
                    <Card variant="outlined">
                      <CardContent>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                          <Typography fontWeight={600}>{c.name}</Typography>
                          <StatusChip label="Running" />
                        </Box>
                        {c.image && (
                          <Typography variant="body2" color="text.secondary" sx={{ fontFamily: 'monospace', fontSize: 12 }}>
                            {c.image}
                          </Typography>
                        )}
                      </CardContent>
                    </Card>
                  </Grid>
                ))}
              </Grid>
            )
          )}
          {tab === 1 && (
            <Box>
              <Box sx={{ display: 'flex', gap: 2, mb: 2, alignItems: 'center' }}>
                <List dense sx={{ display: 'flex', gap: 0.5, p: 0 }}>
                  {containers.map((c) => (
                    <ListItemButton
                      key={c.name}
                      selected={selectedContainer === c.name}
                      onClick={() => setSelectedContainer(c.name)}
                      sx={{ borderRadius: 1, py: 0.5, px: 1.5 }}
                    >
                      <ListItemText primary={c.name} primaryTypographyProps={{ fontSize: 13 }} />
                    </ListItemButton>
                  ))}
                </List>
                <Button variant="contained" size="small" onClick={fetchLogs} disabled={!selectedContainer || logsLoading}>
                  {logsLoading ? 'Loading...' : 'Fetch Logs'}
                </Button>
              </Box>
              <Box
                component="pre"
                sx={{
                  bgcolor: '#0a0e17',
                  borderRadius: 2,
                  p: 2,
                  overflow: 'auto',
                  fontSize: 12,
                  lineHeight: 1.8,
                  fontFamily: '"JetBrains Mono", "Fira Code", monospace',
                  color: '#d4d4d4',
                  maxHeight: 500,
                  minHeight: 200,
                  border: '1px solid',
                  borderColor: 'divider',
                  whiteSpace: 'pre-wrap',
                  wordBreak: 'break-all',
                }}
              >
                {logs || 'Click "Fetch Logs" to view container logs'}
              </Box>
            </Box>
          )}
        </Box>
      </Card>
    </>
  );
}
