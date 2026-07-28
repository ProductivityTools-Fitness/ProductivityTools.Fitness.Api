terraform {
  required_version = ">= 1.5.0"

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
  }
}

provider "google" {
  project = var.project_id
  region  = var.region
}

# 1. Main VPC (Custom Subnet Mode)
resource "google_compute_network" "vpc_network" {
  name                    = var.network_name
  auto_create_subnetworks = false
  description             = "VPC network for Fitness environment"
}

# 2. Subnetwork in Warsaw region (europe-central2)
resource "google_compute_subnetwork" "subnet" {
  name                     = "${var.network_name}-subnet"
  ip_cidr_range            = var.subnet_cidr
  region                   = var.region
  network                  = google_compute_network.vpc_network.id
  private_ip_google_access = true
  description              = "Subnetwork located in Warsaw region (europe-central2)"
}

# 3. Network Firewall Policy: fitness-basic-access
resource "google_compute_network_firewall_policy" "fitness_basic_access" {
  name        = "fitness-basic-access"
  description = "Network firewall policy for Fitness environment"
}

# Associate the Firewall Policy with the VPC Network
resource "google_compute_network_firewall_policy_association" "fitness_policy_assoc" {
  name              = "fitness-basic-access-assoc"
  attachment_target = google_compute_network.vpc_network.id
  firewall_policy   = google_compute_network_firewall_policy.fitness_basic_access.name
}

# Single rule within the Firewall Policy allowing port 80 (HTTP)
resource "google_compute_network_firewall_policy_rule" "allow_http" {
  firewall_policy = google_compute_network_firewall_policy.fitness_basic_access.name
  description     = "Allows incoming HTTP traffic on port 80"
  priority        = 1000
  direction       = "INGRESS"
  action          = "allow"
  rule_name       = "allow-http-80"

  match {
    src_ip_ranges = ["0.0.0.0/0"]
    layer4_configs {
      ip_protocol = "tcp"
      ports       = ["80"]
    }
  }
}

# 4. Virtual Machine in the Warsaw subnetwork
resource "google_compute_instance" "fitness_vm" {
  name         = var.instance_name
  machine_type = var.machine_type
  zone         = var.zone

  boot_disk {
    initialize_params {
      image = "debian-cloud/debian-12"
      size  = 20
      type  = "pd-balanced"
    }
  }

  network_interface {
    subnetwork = google_compute_subnetwork.subnet.id

    access_config {
      # Public external IP address
    }
  }

  metadata = {
    enable-oslogin = "TRUE"
  }
}
