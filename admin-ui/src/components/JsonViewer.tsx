import { Box } from '@mui/material';

interface JsonViewerProps {
  data: unknown;
}

export default function JsonViewer({ data }: JsonViewerProps) {
  return (
    <Box
      component="pre"
      sx={{
        bgcolor: 'rgba(0,0,0,0.3)',
        borderRadius: 2,
        p: 2,
        overflow: 'auto',
        fontSize: 13,
        lineHeight: 1.6,
        fontFamily: '"JetBrains Mono", "Fira Code", monospace',
        color: 'text.primary',
        border: '1px solid',
        borderColor: 'divider',
        maxHeight: 500,
      }}
    >
      {JSON.stringify(data, null, 2)}
    </Box>
  );
}
