import { useEffect, useState } from 'react';
import { Box, Card, CardContent, Grid, Typography, CircularProgress } from '@mui/material';
import PeopleIcon from '@mui/icons-material/People';
import CloudIcon from '@mui/icons-material/Cloud';
import DnsIcon from '@mui/icons-material/Dns';
import StorageIcon from '@mui/icons-material/Storage';
import BuildIcon from '@mui/icons-material/Build';
import ExtensionIcon from '@mui/icons-material/Extension';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import { useNavigate } from 'react-router-dom';
import PageHeader from '../components/PageHeader';
import { statsApi } from '../api/services';
import type { StatsDTO } from '../api/types';

interface StatCard {
  label: string;
  key: keyof StatsDTO;
  icon: React.ReactNode;
  color: string;
  path: string;
}

export default function Dashboard() {
  const navigate = useNavigate();
  const [stats, setStats] = useState<StatsDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    statsApi.get()
      .then(setStats)
      .catch(() => setError(true))
      .finally(() => setLoading(false));
  }, []);

  const cards: StatCard[] = [
    { label: 'Guests', key: 'guests', icon: <PeopleIcon sx={{ fontSize: 32 }} />, color: '#6C9BF2', path: '/guests' },
    { label: 'Deployments', key: 'deployments', icon: <CloudIcon sx={{ fontSize: 32 }} />, color: '#4DD0E1', path: '/deployments' },
    { label: 'Services', key: 'services', icon: <DnsIcon sx={{ fontSize: 32 }} />, color: '#66BB6A', path: '/services' },
    { label: 'Pods', key: 'pods', icon: <StorageIcon sx={{ fontSize: 32 }} />, color: '#FFA726', path: '/pods' },
    { label: 'Builds', key: 'builds', icon: <BuildIcon sx={{ fontSize: 32 }} />, color: '#AB47BC', path: '/builds' },
    { label: 'Extensions', key: 'extensions', icon: <ExtensionIcon sx={{ fontSize: 32 }} />, color: '#EF5350', path: '/extensions' },
    { label: 'Accounts', key: 'accounts', icon: <AccountCircleIcon sx={{ fontSize: 32 }} />, color: '#42A5F5', path: '/accounts' },
  ];

  return (
    <Box>
      <PageHeader title="Dashboard" subtitle="Overview of your Kubernetes operator resources" />

      <Grid container spacing={3}>
        {cards.map((card) => (
          <Grid size={{ xs: 12, sm: 6, md: 4, lg: 3 }} key={card.label}>
            <Card
              sx={{
                cursor: 'pointer',
                transition: 'all 0.2s',
                '&:hover': {
                  transform: 'translateY(-2px)',
                  boxShadow: `0 4px 20px ${card.color}22`,
                  borderColor: `${card.color}44`,
                },
              }}
              onClick={() => navigate(card.path)}
            >
              <CardContent sx={{ p: 3, '&:last-child': { pb: 3 } }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <Box>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                      {card.label}
                    </Typography>
                    {loading ? (
                      <CircularProgress size={28} />
                    ) : (
                      <Typography variant="h4" sx={{ color: card.color }}>
                        {error ? '--' : (stats?.[card.key] ?? '--')}
                      </Typography>
                    )}
                  </Box>
                  <Box
                    sx={{
                      p: 1,
                      borderRadius: 2,
                      bgcolor: `${card.color}18`,
                      color: card.color,
                    }}
                  >
                    {card.icon}
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
}
