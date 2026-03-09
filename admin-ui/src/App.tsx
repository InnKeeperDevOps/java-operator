import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import theme from './theme/theme';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import GuestList from './pages/GuestList';
import GuestDetail from './pages/GuestDetail';
import DeploymentList from './pages/DeploymentList';
import DeploymentDetail from './pages/DeploymentDetail';
import ServiceList from './pages/ServiceList';
import ServiceDetail from './pages/ServiceDetail';
import PodList from './pages/PodList';
import PodDetail from './pages/PodDetail';
import BuildList from './pages/BuildList';
import ExtensionList from './pages/ExtensionList';
import ExtensionDetail from './pages/ExtensionDetail';
import AccountList from './pages/AccountList';
import AccountDetail from './pages/AccountDetail';

export default function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <Routes>
          <Route element={<Layout />}>
            <Route path="/" element={<Dashboard />} />

            <Route path="/guests" element={<GuestList />} />
            <Route path="/guests/:namespace/:name" element={<GuestDetail />} />

            <Route path="/deployments" element={<DeploymentList />} />
            <Route path="/deployments/:namespace/:name" element={<DeploymentDetail />} />

            <Route path="/services" element={<ServiceList />} />
            <Route path="/services/:namespace/:name" element={<ServiceDetail />} />

            <Route path="/pods" element={<PodList />} />
            <Route path="/pods/:namespace/:name" element={<PodDetail />} />

            <Route path="/builds" element={<BuildList />} />

            <Route path="/extensions" element={<ExtensionList />} />
            <Route path="/extensions/:namespace/:name" element={<ExtensionDetail />} />

            <Route path="/accounts" element={<AccountList />} />
            <Route path="/accounts/:name" element={<AccountDetail />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  );
}
