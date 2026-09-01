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

# Enable Cloud Identity-Aware Proxy (IAP) API for browser-based SSH access
resource "google_project_service" "iap" {
  service            = "iap.googleapis.com"
  disable_on_destroy = false
}

# Enable Cloud SQL Admin API
resource "google_project_service" "sqladmin" {
  service            = "sqladmin.googleapis.com"
  disable_on_destroy = false
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
  description     = "Allows incoming HTTP traffic on port 8084"
  priority        = 1000
  direction       = "INGRESS"
  action          = "allow"
  rule_name       = "allow-http-8084"

  match {
    src_ip_ranges = ["0.0.0.0/0"]
    layer4_configs {
      ip_protocol = "tcp"
      ports       = ["8084"]
    }
  }
}

# Rule within the Firewall Policy allowing SSH (TCP port 22) from IAP and any source
resource "google_compute_network_firewall_policy_rule" "allow_ssh" {
  firewall_policy = google_compute_network_firewall_policy.fitness_basic_access.name
  description     = "Allows SSH traffic including browser-based SSH via Google Cloud IAP"
  priority        = 1001
  direction       = "INGRESS"
  action          = "allow"
  rule_name       = "allow-ssh"

  match {
    src_ip_ranges = [
      "35.235.240.0/20", # Google Cloud Identity-Aware Proxy (IAP) CIDR used by Cloud Console Browser SSH
      "0.0.0.0/0"
    ]
    layer4_configs {
      ip_protocol = "tcp"
      ports       = ["22"]
    }
  }
}

# 4. Virtual Machine in the Warsaw subnetwork with Nginx serving a web page
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
    enable-oslogin = "FALSE"
  }

  # Startup script: installs JDK 21, dependencies, and automatically configures GitHub Self-Hosted Runner
  metadata_startup_script = <<-EOF
    #!/bin/bash
    set -e

    # 1. Install prerequisites & Java 21 (Adoptium Temurin 21)
    apt-get update
    apt-get install -y curl jq git wget tar sudo ca-certificates gnupg

    mkdir -p /etc/apt/keyrings
    wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor --yes -o /etc/apt/keyrings/adoptium.gpg
    echo "deb [signed-by=/etc/apt/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb bookworm main" > /etc/apt/sources.list.d/adoptium.list
    apt-get update
    apt-get install -y temurin-21-jdk || apt-get install -y -t bookworm-backports openjdk-21-jdk

    # 2. Prepare application directory
    mkdir -p /opt/PT.Fitness-Api
    chmod 777 /opt/PT.Fitness-Api

    # 3. Setup GitHub Actions Self-Hosted Runner if credentials are provided
    REPO="${var.github_repo}"
    PAT="${var.github_pat}"

    if [ -n "$REPO" ] && [ -n "$PAT" ]; then
      echo "Setting up GitHub Actions Runner for $REPO..."
      mkdir -p /opt/actions-runner
      cd /opt/actions-runner

      # Download runner package
      RUNNER_VERSION=$(curl -s https://api.github.com/repos/actions/runner/releases/latest | jq -r .tag_name | sed 's/v//')
      RUNNER_VERSION="$${RUNNER_VERSION:-2.317.0}"

      curl -o actions-runner-linux-x64.tar.gz -L "https://github.com/actions/runner/releases/download/v$${RUNNER_VERSION}/actions-runner-linux-x64-$${RUNNER_VERSION}.tar.gz"
      tar xzf ./actions-runner-linux-x64.tar.gz

      # Obtain registration token from GitHub API
      REG_TOKEN=$(curl -sX POST \
        -H "Accept: application/vnd.github+json" \
        -H "Authorization: Bearer $${PAT}" \
        "https://api.github.com/repos/$${REPO}/actions/runners/registration-token" | jq -r .token)

      if [ -n "$REG_TOKEN" ] && [ "$REG_TOKEN" != "null" ]; then
        export RUNNER_ALLOW_RUNASROOT="1"
        ./config.sh --url "https://github.com/$${REPO}" --token "$${REG_TOKEN}" --name "${var.instance_name}" --unattended --replace
        ./svc.sh install root
        ./svc.sh start
        echo "GitHub Actions Runner registered and running as a systemd service!"
      else
        echo "Error: Could not retrieve registration token from GitHub API."
      fi
    fi
  EOF
}

# 5. Cloud SQL PostgreSQL Instance with Private Service Connect (PSC) enabled
resource "google_sql_database_instance" "postgres" {
  name                = var.db_instance_name
  database_version    = var.db_version
  region              = var.region
  deletion_protection = false

  settings {
    tier = var.db_tier

    ip_configuration {
      ipv4_enabled = false

      psc_config {
        psc_enabled               = true
        allowed_consumer_projects = [var.project_id]
      }
    }

    backup_configuration {
      enabled = false
    }
  }

  depends_on = [
    google_project_service.sqladmin
  ]
}

# Default Database inside Cloud SQL
resource "google_sql_database" "database" {
  name     = var.db_name
  instance = google_sql_database_instance.postgres.name
}

# PostgreSQL Database User
resource "google_sql_user" "db_user" {
  name     = var.db_user
  instance = google_sql_database_instance.postgres.name
  password = var.db_password
}

# 6. Private Service Connect (PSC) Endpoint in consumer Subnetwork
# Internal IP reserved for the PSC endpoint within the Warsaw subnet
resource "google_compute_address" "db_psc_ip" {
  name         = "fitness-db-psc-ip"
  subnetwork   = google_compute_subnetwork.subnet.id
  address_type = "INTERNAL"
  region       = var.region
}

# Forwarding rule pointing to the Cloud SQL PSC Service Attachment
resource "google_compute_forwarding_rule" "db_psc_endpoint" {
  name                  = "fitness-db-psc-endpoint"
  region                = var.region
  network               = google_compute_network.vpc_network.id
  subnetwork            = google_compute_subnetwork.subnet.id
  ip_address            = google_compute_address.db_psc_ip.self_link
  target                = google_sql_database_instance.postgres.psc_service_attachment_link
  load_balancing_scheme = ""
}

# 7. Development/Test Cloud SQL PostgreSQL Instance (with Public IP for Cloud SQL Auth Proxy)
resource "google_sql_database_instance" "postgres_dev" {
  name                = var.dev_db_instance_name
  database_version    = var.db_version
  region              = var.region
  deletion_protection = false

  settings {
    tier = var.db_tier

    ip_configuration {
      ipv4_enabled = true # Enables Public IP for Cloud SQL Auth Proxy access
    }

    backup_configuration {
      enabled = false
    }
  }

  depends_on = [
    google_project_service.sqladmin
  ]
}

# Default Database inside Dev Cloud SQL
resource "google_sql_database" "database_dev" {
  name     = var.db_name
  instance = google_sql_database_instance.postgres_dev.name
}

# PostgreSQL Database User inside Dev Cloud SQL
resource "google_sql_user" "db_user_dev" {
  name     = var.db_user
  instance = google_sql_database_instance.postgres_dev.name
  password = var.db_password
}
