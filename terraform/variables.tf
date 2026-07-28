variable "project_id" {
  type        = string
  description = "Google cloud id"
  default     = "pwujczyk-pt"
}

variable "region" {
  type        = string
  description = "Region"
  default     = "europe-central2"
}

variable "network_name" {
  type        = string
  description = "Network name"
  default     = "fitness2"
}

variable "subnet_cidr" {
  type        = string
  description = "Pula adresowa CIDR dla podsieci w Warszawie"
  default     = "10.10.0.0/24"
}
