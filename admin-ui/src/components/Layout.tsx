import { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  AppBar,
  Box,
  CssBaseline,
  Drawer,
  IconButton,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
  Divider,
  Avatar,
  Tooltip,
  useMediaQuery,
  useTheme,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import DashboardIcon from '@mui/icons-material/Dashboard';
import PeopleIcon from '@mui/icons-material/People';
import CloudIcon from '@mui/icons-material/Cloud';
import StorageIcon from '@mui/icons-material/Storage';
import BuildIcon from '@mui/icons-material/Build';
import ViewInArIcon from '@mui/icons-material/ViewInAr';
import ExtensionIcon from '@mui/icons-material/Extension';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import SettingsIcon from '@mui/icons-material/Settings';
import DnsIcon from '@mui/icons-material/Dns';

const DRAWER_WIDTH = 260;

interface NavItem {
  label: string;
  path: string;
  icon: React.ReactNode;
}

const navItems: NavItem[] = [
  { label: 'Dashboard', path: '/', icon: <DashboardIcon /> },
  { label: 'Guests', path: '/guests', icon: <PeopleIcon /> },
  { label: 'Deployments', path: '/deployments', icon: <CloudIcon /> },
  { label: 'Services', path: '/services', icon: <DnsIcon /> },
  { label: 'Pods', path: '/pods', icon: <StorageIcon /> },
  { label: 'Builds', path: '/builds', icon: <BuildIcon /> },
  { label: 'Extensions', path: '/extensions', icon: <ExtensionIcon /> },
  { label: 'Accounts', path: '/accounts', icon: <AccountCircleIcon /> },
];

export default function Layout() {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [mobileOpen, setMobileOpen] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const handleNav = (path: string) => {
    navigate(path);
    if (isMobile) setMobileOpen(false);
  };

  const drawer = (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <Box sx={{ p: 2.5, display: 'flex', alignItems: 'center', gap: 1.5 }}>
        <Avatar
          sx={{
            bgcolor: 'primary.main',
            width: 36,
            height: 36,
            fontSize: 16,
            fontWeight: 700,
          }}
        >
          <ViewInArIcon sx={{ fontSize: 20 }} />
        </Avatar>
        <Box>
          <Typography variant="subtitle1" fontWeight={700} lineHeight={1.2}>
            InnKeeper
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Operator Admin
          </Typography>
        </Box>
      </Box>

      <Divider />

      <List sx={{ flex: 1, px: 1.5, py: 1 }}>
        {navItems.map((item) => {
          const active = item.path === '/'
            ? location.pathname === '/'
            : location.pathname.startsWith(item.path);
          return (
            <ListItemButton
              key={item.path}
              onClick={() => handleNav(item.path)}
              selected={active}
              sx={{
                borderRadius: 2,
                mb: 0.5,
                '&.Mui-selected': {
                  bgcolor: 'rgba(108,155,242,0.12)',
                  '&:hover': { bgcolor: 'rgba(108,155,242,0.18)' },
                  '& .MuiListItemIcon-root': { color: 'primary.main' },
                  '& .MuiListItemText-primary': { color: 'primary.main', fontWeight: 600 },
                },
              }}
            >
              <ListItemIcon sx={{ minWidth: 40, color: 'text.secondary' }}>
                {item.icon}
              </ListItemIcon>
              <ListItemText
                primary={item.label}
                primaryTypographyProps={{ fontSize: 14 }}
              />
            </ListItemButton>
          );
        })}
      </List>

      <Divider />
      <Box sx={{ p: 2 }}>
        <Tooltip title="Settings">
          <IconButton size="small" sx={{ color: 'text.secondary' }}>
            <SettingsIcon fontSize="small" />
          </IconButton>
        </Tooltip>
      </Box>
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      <CssBaseline />

      {/* App Bar – mobile only */}
      <AppBar
        position="fixed"
        elevation={0}
        sx={{
          display: { md: 'none' },
          bgcolor: 'background.paper',
          borderBottom: '1px solid',
          borderColor: 'divider',
        }}
      >
        <Toolbar>
          <IconButton edge="start" onClick={() => setMobileOpen(!mobileOpen)} sx={{ color: 'text.primary' }}>
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" noWrap sx={{ ml: 1 }}>
            InnKeeper
          </Typography>
        </Toolbar>
      </AppBar>

      {/* Sidebar */}
      <Box component="nav" sx={{ width: { md: DRAWER_WIDTH }, flexShrink: 0 }}>
        {/* Mobile drawer */}
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{
            display: { xs: 'block', md: 'none' },
            '& .MuiDrawer-paper': { width: DRAWER_WIDTH, bgcolor: 'background.paper' },
          }}
        >
          {drawer}
        </Drawer>

        {/* Desktop drawer */}
        <Drawer
          variant="permanent"
          sx={{
            display: { xs: 'none', md: 'block' },
            '& .MuiDrawer-paper': { width: DRAWER_WIDTH, bgcolor: 'background.paper' },
          }}
          open
        >
          {drawer}
        </Drawer>
      </Box>

      {/* Main content */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          mt: { xs: 8, md: 0 },
          bgcolor: 'background.default',
          minHeight: '100vh',
          overflow: 'auto',
        }}
      >
        <Outlet />
      </Box>
    </Box>
  );
}
