import { Chip, type ChipProps } from '@mui/material';

interface StatusChipProps {
  label: string;
  size?: ChipProps['size'];
}

const colorMap: Record<string, ChipProps['color']> = {
  running: 'success',
  active: 'success',
  ready: 'success',
  succeeded: 'success',
  healthy: 'success',
  pending: 'warning',
  building: 'warning',
  waiting: 'warning',
  progressing: 'info',
  unknown: 'default',
  failed: 'error',
  error: 'error',
  crashloopbackoff: 'error',
  terminated: 'error',
};

export default function StatusChip({ label, size = 'small' }: StatusChipProps) {
  const color = colorMap[label.toLowerCase()] || 'default';
  return (
    <Chip
      label={label}
      color={color}
      size={size}
      variant="outlined"
      sx={{ fontWeight: 600, textTransform: 'capitalize' }}
    />
  );
}
