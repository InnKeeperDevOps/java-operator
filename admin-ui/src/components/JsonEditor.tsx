import { useState } from 'react';
import { Box, Button, Alert } from '@mui/material';
import SaveIcon from '@mui/icons-material/Save';
import UndoIcon from '@mui/icons-material/Undo';

interface JsonEditorProps {
  data: unknown;
  onSave: (data: unknown) => Promise<void>;
  readonly?: boolean;
}

export default function JsonEditor({ data, onSave, readonly = false }: JsonEditorProps) {
  const formatted = JSON.stringify(data, null, 2);
  const [value, setValue] = useState(formatted);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState(false);
  const dirty = value !== formatted;

  const handleSave = async () => {
    setError('');
    setSuccess(false);
    try {
      const parsed = JSON.parse(value);
      setSaving(true);
      await onSave(parsed);
      setSuccess(true);
      setTimeout(() => setSuccess(false), 3000);
    } catch (e) {
      if (e instanceof SyntaxError) {
        setError('Invalid JSON: ' + e.message);
      } else {
        setError('Save failed: ' + (e instanceof Error ? e.message : 'Unknown error'));
      }
    } finally {
      setSaving(false);
    }
  };

  const handleReset = () => {
    setValue(formatted);
    setError('');
  };

  return (
    <Box>
      {!readonly && (
        <Box sx={{ display: 'flex', gap: 1, mb: 2, alignItems: 'center' }}>
          <Button
            variant="contained"
            size="small"
            startIcon={<SaveIcon />}
            onClick={handleSave}
            disabled={!dirty || saving}
          >
            {saving ? 'Saving...' : 'Save'}
          </Button>
          <Button
            variant="outlined"
            size="small"
            startIcon={<UndoIcon />}
            onClick={handleReset}
            disabled={!dirty}
          >
            Reset
          </Button>
          {error && <Alert severity="error" sx={{ py: 0, flex: 1 }}>{error}</Alert>}
          {success && <Alert severity="success" sx={{ py: 0, flex: 1 }}>Saved successfully</Alert>}
        </Box>
      )}
      <Box
        component="textarea"
        value={value}
        onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => {
          if (!readonly) setValue(e.target.value);
        }}
        readOnly={readonly}
        spellCheck={false}
        sx={{
          width: '100%',
          minHeight: 300,
          maxHeight: 600,
          bgcolor: 'rgba(0,0,0,0.3)',
          borderRadius: 2,
          p: 2,
          overflow: 'auto',
          fontSize: 13,
          lineHeight: 1.6,
          fontFamily: '"JetBrains Mono", "Fira Code", monospace',
          color: 'text.primary',
          border: dirty ? '2px solid' : '1px solid',
          borderColor: dirty ? 'primary.main' : 'divider',
          resize: 'vertical',
          outline: 'none',
          '&:focus': {
            borderColor: 'primary.main',
          },
        }}
      />
    </Box>
  );
}
