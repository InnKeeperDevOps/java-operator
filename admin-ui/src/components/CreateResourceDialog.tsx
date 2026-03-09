import { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Alert,
  Box,
} from '@mui/material';

interface CreateResourceDialogProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (data: { name: string; namespace: string; spec: Record<string, unknown> }) => Promise<void>;
  title: string;
  showNamespace?: boolean;
  defaultNamespace?: string;
  specTemplate?: Record<string, unknown>;
}

export default function CreateResourceDialog({
  open,
  onClose,
  onSubmit,
  title,
  showNamespace = true,
  defaultNamespace = 'default',
  specTemplate = {},
}: CreateResourceDialogProps) {
  const [name, setName] = useState('');
  const [namespace, setNamespace] = useState(defaultNamespace);
  const [specJson, setSpecJson] = useState(JSON.stringify(specTemplate, null, 2));
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async () => {
    setError('');
    if (!name.trim()) {
      setError('Name is required');
      return;
    }
    if (showNamespace && !namespace.trim()) {
      setError('Namespace is required');
      return;
    }
    let spec: Record<string, unknown>;
    try {
      spec = JSON.parse(specJson);
    } catch {
      setError('Invalid JSON in spec');
      return;
    }
    setSubmitting(true);
    try {
      await onSubmit({ name: name.trim(), namespace: namespace.trim(), spec });
      setName('');
      setNamespace(defaultNamespace);
      setSpecJson(JSON.stringify(specTemplate, null, 2));
      onClose();
    } catch (e) {
      setError('Failed to create: ' + (e instanceof Error ? e.message : 'Unknown error'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
          <TextField
            label="Name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            size="small"
            fullWidth
            required
          />
          {showNamespace && (
            <TextField
              label="Namespace"
              value={namespace}
              onChange={(e) => setNamespace(e.target.value)}
              size="small"
              fullWidth
              required
            />
          )}
          <Box
            component="textarea"
            value={specJson}
            onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setSpecJson(e.target.value)}
            spellCheck={false}
            placeholder="Spec JSON"
            sx={{
              width: '100%',
              minHeight: 200,
              bgcolor: 'rgba(0,0,0,0.3)',
              borderRadius: 1,
              p: 2,
              fontSize: 13,
              lineHeight: 1.6,
              fontFamily: '"JetBrains Mono", "Fira Code", monospace',
              color: 'text.primary',
              border: '1px solid',
              borderColor: 'divider',
              resize: 'vertical',
              outline: 'none',
              '&:focus': {
                borderColor: 'primary.main',
              },
            }}
          />
          {error && <Alert severity="error">{error}</Alert>}
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button variant="contained" onClick={handleSubmit} disabled={submitting}>
          {submitting ? 'Creating...' : 'Create'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
