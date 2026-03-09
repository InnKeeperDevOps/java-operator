import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#6C9BF2',
      light: '#9DBCF8',
      dark: '#4A7AD4',
    },
    secondary: {
      main: '#4DD0E1',
      light: '#80DEEA',
      dark: '#00ACC1',
    },
    background: {
      default: '#0F1724',
      paper: '#182136',
    },
    text: {
      primary: '#E8ECF4',
      secondary: '#8B95A8',
    },
    divider: 'rgba(255,255,255,0.08)',
    success: { main: '#66BB6A' },
    warning: { main: '#FFA726' },
    error: { main: '#EF5350' },
    info: { main: '#42A5F5' },
  },
  typography: {
    fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    h4: { fontWeight: 700, letterSpacing: '-0.02em' },
    h5: { fontWeight: 600, letterSpacing: '-0.01em' },
    h6: { fontWeight: 600 },
  },
  shape: { borderRadius: 10 },
  components: {
    MuiCard: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
          border: '1px solid rgba(255,255,255,0.06)',
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          backgroundImage: 'none',
          borderRight: '1px solid rgba(255,255,255,0.06)',
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          fontWeight: 500,
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: {
          borderBottom: '1px solid rgba(255,255,255,0.06)',
        },
      },
    },
  },
});

export default theme;
