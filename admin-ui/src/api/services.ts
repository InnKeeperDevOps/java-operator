import api, { PREFIX } from './client';
import type {
  AccountDTO,
  BuildDTO,
  DeploymentDTO,
  GuestDTO,
  K8sPodDTO,
  K8sServiceDTO,
  ServiceDTO,
  SimpleExtensionDTO,
  Container,
  K8sDeploymentDTO,
  EventLog,
  StatsDTO,
} from './types';

// ── Stats ────────────────────────────────────────────────
export const statsApi = {
  get: () => api.get<StatsDTO>(`${PREFIX}/stats/`).then((r) => r.data),
};

// ── Accounts ──────────────────────────────────────────────
export const accountApi = {
  list: () => api.get<AccountDTO[]>(`${PREFIX}/account/`).then((r) => r.data),
  get: (name: string) =>
    api.get<AccountDTO>(`${PREFIX}/account/${name}/`).then((r) => r.data),
  create: (name: string, spec: Record<string, unknown>) =>
    api.post<AccountDTO>(`${PREFIX}/account/${name}/`, spec).then((r) => r.data),
  update: (name: string, spec: Record<string, unknown>) =>
    api.put<AccountDTO>(`${PREFIX}/account/${name}/`, spec).then((r) => r.data),
  delete: (name: string) =>
    api.delete(`${PREFIX}/account/${name}/`).then((r) => r.data),
  grant: (name: string, permission: string) =>
    api.put<AccountDTO>(`${PREFIX}/account/${name}/grant/${permission}`).then((r) => r.data),
  revoke: (name: string, permission: string) =>
    api.put<AccountDTO>(`${PREFIX}/account/${name}/revoke/${permission}`).then((r) => r.data),
};

// ── Guests ────────────────────────────────────────────────
export const guestApi = {
  list: () => api.get<GuestDTO[]>(`${PREFIX}/guest/`).then((r) => r.data),
  get: (ns: string, name: string) =>
    api.get<GuestDTO>(`${PREFIX}/guest/${ns}/${name}/`).then((r) => r.data),
  create: (ns: string, name: string, spec: Record<string, unknown>) =>
    api.post<GuestDTO>(`${PREFIX}/guest/${ns}/${name}/`, spec).then((r) => r.data),
  update: (ns: string, name: string, spec: Record<string, unknown>) =>
    api.put<GuestDTO>(`${PREFIX}/guest/${ns}/${name}/`, spec).then((r) => r.data),
  delete: (ns: string, name: string) =>
    api.delete(`${PREFIX}/guest/${ns}/${name}/`).then((r) => r.data),
  deployments: (ns: string, name: string) =>
    api.get<DeploymentDTO[]>(`${PREFIX}/guest/${ns}/${name}/deployments`).then((r) => r.data),
  saveDeployments: (ns: string, name: string, settings: Record<string, unknown>[]) =>
    api.put<DeploymentDTO[]>(`${PREFIX}/guest/${ns}/${name}/deployments`, settings).then((r) => r.data),
  services: (ns: string, name: string) =>
    api.get<ServiceDTO[]>(`${PREFIX}/guest/${ns}/${name}/services`).then((r) => r.data),
  saveServices: (ns: string, name: string, settings: Record<string, unknown>[]) =>
    api.put<ServiceDTO[]>(`${PREFIX}/guest/${ns}/${name}/services`, settings).then((r) => r.data),
  builds: (ns: string, name: string) =>
    api.get<BuildDTO[]>(`${PREFIX}/guest/${ns}/${name}/builds`).then((r) => r.data),
  saveBuilds: (ns: string, name: string, settings: Record<string, unknown>[]) =>
    api.put<BuildDTO[]>(`${PREFIX}/guest/${ns}/${name}/builds`, settings).then((r) => r.data),
  extensions: (ns: string, name: string) =>
    api.get<SimpleExtensionDTO[]>(`${PREFIX}/guest/${ns}/${name}/extensions`).then((r) => r.data),
  saveExtensions: (ns: string, name: string, specs: Record<string, unknown>[]) =>
    api.put<SimpleExtensionDTO[]>(`${PREFIX}/guest/${ns}/${name}/extensions`, specs).then((r) => r.data),
  k8sDeployments: (ns: string, name: string) =>
    api.get<K8sDeploymentDTO[]>(`${PREFIX}/guest/${ns}/${name}/k8s/deployments`).then((r) => r.data),
};

// ── Deployments ───────────────────────────────────────────
export const deploymentApi = {
  list: () => api.get<DeploymentDTO[]>(`${PREFIX}/deployment/`).then((r) => r.data),
  get: (ns: string, name: string) =>
    api.get<DeploymentDTO>(`${PREFIX}/deployment/${ns}/${name}/`).then((r) => r.data),
  pods: (ns: string, name: string) =>
    api.get<K8sPodDTO[]>(`${PREFIX}/deployment/${ns}/${name}/pods`).then((r) => r.data),
};

// ── Services ──────────────────────────────────────────────
export const serviceApi = {
  list: () => api.get<ServiceDTO[]>(`${PREFIX}/service/`).then((r) => r.data),
  get: (ns: string, name: string) =>
    api.get<ServiceDTO>(`${PREFIX}/service/${ns}/${name}/`).then((r) => r.data),
  k8sObject: (ns: string, name: string) =>
    api.get<K8sServiceDTO>(`${PREFIX}/service/${ns}/${name}/object`).then((r) => r.data),
};

// ── Builds ────────────────────────────────────────────────
export const buildApi = {
  list: () => api.get<BuildDTO[]>(`${PREFIX}/build/`).then((r) => r.data),
};

// ── Pods ──────────────────────────────────────────────────
export const podApi = {
  list: () => api.get<K8sPodDTO[]>(`${PREFIX}/pod/`).then((r) => r.data),
  containers: (ns: string, name: string) =>
    api.get<Container[]>(`${PREFIX}/pod/${ns}/${name}/`).then((r) => r.data),
  logs: (ns: string, name: string, container: string) =>
    api.get<string>(`${PREFIX}/pod/${ns}/${name}/${container}/logs`).then((r) => r.data),
};

// ── Extensions ────────────────────────────────────────────
export const extensionApi = {
  list: () => api.get<SimpleExtensionDTO[]>(`${PREFIX}/extension/`).then((r) => r.data),
  get: (ns: string, name: string) =>
    api.get<SimpleExtensionDTO>(`${PREFIX}/extension/${ns}/${name}/`).then((r) => r.data),
};

// ── Errors ────────────────────────────────────────────────
export const errorApi = {
  permissions: () => api.get<EventLog[]>('/error/permission').then((r) => r.data),
  io: () => api.get<EventLog[]>('/error/io').then((r) => r.data),
};
