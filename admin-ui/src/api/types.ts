export interface ObjectMetaDTO {
  name: string;
  namespace: string;
  uuid: string;
}

export interface AccountSpec {
  permissions?: string[];
  [key: string]: unknown;
}

export interface AccountStatus {
  [key: string]: unknown;
}

export interface AccountDTO {
  spec: AccountSpec;
  status: AccountStatus;
  meta: ObjectMetaDTO;
}

export interface GuestSpec {
  [key: string]: unknown;
}

export interface GuestStatus {
  [key: string]: unknown;
}

export interface GuestDTO {
  spec: GuestSpec;
  status: GuestStatus;
  meta: ObjectMetaDTO;
}

export interface DeploymentSpec {
  [key: string]: unknown;
}

export interface DeploymentStatus {
  [key: string]: unknown;
}

export interface DeploymentDTO {
  spec: DeploymentSpec;
  status: DeploymentStatus;
  meta: ObjectMetaDTO;
}

export interface ServiceSpec {
  [key: string]: unknown;
}

export interface ServiceStatus {
  [key: string]: unknown;
}

export interface ServiceDTO {
  spec: ServiceSpec;
  status: ServiceStatus;
  meta: ObjectMetaDTO;
}

export interface K8sServiceDTO {
  spec: ServiceSpec;
  status: ServiceStatus;
  meta: ObjectMetaDTO;
}

export interface BuildSpec {
  [key: string]: unknown;
}

export interface BuildStatus {
  [key: string]: unknown;
}

export interface BuildDTO {
  spec: BuildSpec;
  status: BuildStatus;
  meta: ObjectMetaDTO;
}

export interface PodSpec {
  [key: string]: unknown;
}

export interface PodStatus {
  phase?: string;
  [key: string]: unknown;
}

export interface K8sPodDTO {
  spec: PodSpec;
  status: PodStatus;
  meta: ObjectMetaDTO;
}

export interface Container {
  name: string;
  image?: string;
  [key: string]: unknown;
}

export interface SimpleExtensionSpec {
  [key: string]: unknown;
}

export interface SimpleExtensionStatus {
  [key: string]: unknown;
}

export interface SimpleExtensionDTO {
  spec: SimpleExtensionSpec;
  status: SimpleExtensionStatus;
  meta: ObjectMetaDTO;
}

export interface K8sDeploymentDTO {
  spec: DeploymentSpec;
  status: DeploymentStatus;
  meta: ObjectMetaDTO;
}

export interface EventLog {
  message?: string;
  timestamp?: string;
  [key: string]: unknown;
}

export interface StatsDTO {
  guests: number;
  deployments: number;
  services: number;
  pods: number;
  builds: number;
  extensions: number;
  accounts: number;
}
