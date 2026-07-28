variable "project_id" {
  type        = string
  description = "Google Cloud Project ID"
  default     = "pwujczyk-pt"
}

variable "region" {
  type        = string
  description = "GCP Region for the subnetwork"
  default     = "europe-central2"
}

variable "network_name" {
  type        = string
  description = "Name of the VPC network"
  default     = "fitness2"
}

variable "subnet_cidr" {
  type        = string
  description = "CIDR IP range for the Warsaw subnetwork"
  default     = "10.10.0.0/24"
}

variable "zone" {
  type        = string
  description = "GCP Zone for the virtual machine"
  default     = "europe-central2-a"
}

variable "instance_name" {
  type        = string
  description = "Name of the virtual machine instance"
  default     = "fitness-vm"
}

variable "machine_type" {
  type        = string
  description = "Machine type for the virtual machine"
  default     = "e2-medium"
}
