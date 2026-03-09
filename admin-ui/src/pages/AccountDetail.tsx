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
  TextField,
  IconButton,
  Alert,
  Snackbar,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import PageHeader from '../components/PageHeader';
import JsonViewer from '../components/JsonViewer';
import JsonEditor from '../components/JsonEditor';
import type { AccountDTO } from '../api/types';
import { accountApi } from '../api/services';

export default function AccountDetail() {
  const { name } = useParams<{ name: string }>();
  const navigate = useNavigate();
  const [account, setAccount] = useState<AccountDTO | null>(null);
  const [tab, setTab] = useState(0);
  const [loading, setLoading] = useState(true);
  const [newPerm, setNewPerm] = useState('');
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false, message: '', severity: 'success',
  });

  const load = () => {
    if (!name) return;
    accountApi.get(name).then(setAccount).catch(console.error).finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, [name]);

  const handleGrant = async () => {
    if (!name || !newPerm.trim()) return;
    try {
      const updated = await accountApi.grant(name, newPerm.trim());
      setAccount(updated);
      setNewPerm('');
      setSnackbar({ open: true, message: `Permission "${newPerm.trim()}" granted`, severity: 'success' });
    } catch {
      setSnackbar({ open: true, message: 'Failed to grant permission', severity: 'error' });
    }
  };

  const handleRevoke = async (perm: string) => {
    if (!name) return;
    try {
      const updated = await accountApi.revoke(name, perm);
      setAccount(updated);
      setSnackbar({ open: true, message: `Permission "${perm}" revoked`, severity: 'success' });
    } catch {
      setSnackbar({ open: true, message: 'Failed to revoke permission', severity: 'error' });
    }
  };

  const handleSaveSpec = async (data: unknown) => {
    if (!name) return;
    const updated = await accountApi.update(name, data as Record<string, unknown>);
    setAccount(updated);
    setSnackbar({ open: true, message: 'Spec saved successfully', severity: 'success' });
  };

  const handleDelete = async () => {
    if (!name) return;
    if (!confirm(`Delete account "${name}"?`)) return;
    try {
      await accountApi.delete(name);
      navigate('/accounts');
    } catch {
      setSnackbar({ open: true, message: 'Failed to delete account', severity: 'error' });
    }
  };

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}><CircularProgress /></Box>;
  if (!account) return <Typography color="error">Account not found</Typography>;

  const permissions = account.spec?.permissions || [];

  return (
    <>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <PageHeader
          title={account.meta.name}
          breadcrumbs={[{ label: 'Accounts', to: '/accounts' }, { label: account.meta.name }]}
        />
        <Button variant="outlined" color="error" startIcon={<DeleteIcon />} onClick={handleDelete} sx={{ mt: 1 }}>
          Delete
        </Button>
      </Box>

      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, md: 6 }}>
          <Card><CardContent>
            <Typography variant="body2" color="text.secondary">Name</Typography>
            <Typography fontWeight={600}>{account.meta.name}</Typography>
          </CardContent></Card>
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <Card><CardContent>
            <Typography variant="body2" color="text.secondary">Permissions</Typography>
            <Typography fontWeight={600}>{permissions.length}</Typography>
          </CardContent></Card>
        </Grid>
      </Grid>

      <Card>
        <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ borderBottom: 1, borderColor: 'divider', px: 2 }}>
          <Tab label="Permissions" />
          <Tab label="Spec (Edit)" />
          <Tab label="Status" />
        </Tabs>
        <Box sx={{ p: 2 }}>
          {tab === 0 && (
            <Box>
              <Box sx={{ display: 'flex', gap: 1, mb: 3 }}>
                <TextField
                  size="small"
                  placeholder="e.g. guest.list"
                  value={newPerm}
                  onChange={(e) => setNewPerm(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleGrant()}
                  sx={{ flex: 1, maxWidth: 400 }}
                />
                <Button variant="contained" startIcon={<AddIcon />} onClick={handleGrant} disabled={!newPerm.trim()}>
                  Grant
                </Button>
              </Box>
              <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                {permissions.length === 0 ? (
                  <Typography color="text.secondary">No permissions assigned</Typography>
                ) : (
                  permissions.map((p) => (
                    <Chip
                      key={p}
                      label={p}
                      color="primary"
                      variant="outlined"
                      onDelete={() => handleRevoke(p)}
                      deleteIcon={<IconButton size="small"><DeleteIcon fontSize="small" /></IconButton>}
                    />
                  ))
                )}
              </Box>
            </Box>
          )}
          {tab === 1 && <JsonEditor data={account.spec} onSave={handleSaveSpec} />}
          {tab === 2 && <JsonViewer data={account.status} />}
        </Box>
      </Card>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={() => setSnackbar((s) => ({ ...s, open: false }))}
      >
        <Alert severity={snackbar.severity} variant="filled" onClose={() => setSnackbar((s) => ({ ...s, open: false }))}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </>
  );
}
